## Example: An automated persistence test

Let's start with a typical test method that uses the mechanics of the 
persistence test component. In this case, we use a JPA-annotated class
to create a new Entity:

<pre class="brush: java" title="Example persistence test method: validateCreateEntity()"><![CDATA[
    @Test
    public void validateCreateEntity() throws Exception {

        // Assemble
        final Bird eagle = new Bird("Eagle", "Predator");
        final IDataSet expected = performStandardTestDbSetup();

        // Act
        jpa.create(eagle);
        commitAndStartNewTransaction();

        // Assert
        final List<Bird> birds = jpa.fireNamedQuery(Bird.GET_ALL_BIRDS);
        final IDataSet dbDataSet = iDatabaseConnection.createDataSet(new String[]{"BIRD"});
        
        Assert.assertEquals(2, birds.size());
        Assertion.assertEquals(expected, dbDataSet);
    }
]]></pre>
   
Creating the entity in the database is done by the `jpa.create(eagle);` statement. This method - in turn - 
is a thin wrapper invokes the standard EntityManager method `entityManager.persist(entity);` which persists 
the Bird object in the database. Essentially, the create statement performs all which should be validated
within the test. The rest of the statements in the test method either sets up the data within the database
or validates what is stored in the database.

### 1) The JPA-annotated Entity class: Bird

The Bird class must be JPA-annotated to be useable by an EntityManager; the annotated parts are found in the 
snippet below. Note that the Bird class extends `NazgulEntity`, which holds the identity and version fields 
required by the JPA specification. The NazgulEntity class also supplies other services and simplifications, 
implying that Entity classes extending it can focus on realizing their business model instead of the infrastructure
required by JPA.
 
<pre class="brush: java" title="JPA-annotated entity class snippet."><![CDATA[
    @Entity
    public class Bird extends NazgulEntity {
    
        @Basic(optional = false)
        @Column(nullable = false, length = 64)
        private String name;
    
        @Basic(optional = false)
        @Column(nullable = false, length = 128)
        private String category;
        
        ...
    }
]]></pre> 

### 2) Creating required database tables

One of the nice things about JPA is that providers can use annotated Entity classes to create the
database tables in an empty database schema. After connecting to the database (but before populating
the database with initial state data) the JPA provider uses all annotated JPA classes known to the 
PersistenceUnit to create database tables as required for the automated test.

The StandardPersistenceTest uses a custom ClassLoader to ensure that we can use a persistence.xml file
placed within the classpath - but not necessarily in `META-INF/persistence.xml`. Instead, the 
persistence.xml file is shared for all test methods within a unit test class - simply create a new
unit test class to use another persistence.xml. The StandardPersistenceTest assumes that the persistence.xml
file is placed in `"testdata/" + getClass().getSimpleName() + "/persistence.xml"`. For example, the 
unit test class JpaRelationsTest uses the persistence.xml file shown in the image below:

<img src="images/dbPopulationConfiguration.png" style="margin:10px; border:1px solid black;" width="50%" height="50%" />

The snippets in the 

### 2) Populating the database before test: `performStandardTestDbSetup()`
 
Databases must normally be pre-populate with a known state before firing JPA operations, in order to 
ascertain that the annotated entity classes, validators and other tidbits are correctly implemented. 
The operation which populates a database with initial state data is `performStandardTestDbSetup()`.  
The value returned from the  

This method
connects to the (in-memory) database used by the test-scope JPA EntityManager, and uses a configuration file to 
insert a well-known state into it.
 
The value returned from the `performStandardTestDbSetup()` method 
 
<pre class="brush: java" title="Setup database state method"><![CDATA[
    @Test
    public void validateCreateEntity() throws Exception {

        // Assemble
        ...
        final IDataSet expected = performStandardTestDbSetup();
]]></pre>

The path to the configuration file for the database setup is calculated from the name of the test class and 
the test method, on the standard form 
`"testdata/" + getClass().getSimpleName() + "/setup_" + testMethodName + ".xml"`. 
Illustrated in the image below, this leads to a structure under the testdata resource map where all setup 
and teardown data files are created in pairs, which yields a rather clean and obvious structure.
As illustrated in the image below, the 3 test methods in class `JpaRelationsTest` for which we want to 
setup and validate database state are "validateCreateJpaRelationEntities", "validateDeleteEntity" and 
"validateUpdateEntity":

<img src="images/dbPopulationConfiguration.png" style="margin:10px; border:1px solid black;" width="50%" height="50%" />

The "setup_" and "expected_" XML files contains standard [dbUnit](http://dbunit.sourceforge.net/) XML data 
instructions, which are used as templates to insert data into the database or verify data within the database
respectively. Since the test aims at creating an entity, we need no previous content in the database.   
However, just to illustrate that we need not start a unit test with an empty database, the content of the 
setup_validateCreateEntity.xml file contains the insertion of a row within the `bird` table:

<pre class="brush: xml"><![CDATA[
    <?xml version="1.0" encoding="UTF-8"?>
    <dataset>
        <BIRD id="1" version="1" name="Hawk" category="Predator" />
    </dataset>
        
]]></pre> 

This implies that we populate the database with some data before the automated test is started. For further
reference to the dataset structure, please refer to the [dbUnit](http://dbunit.sourceforge.net/) website. 

### 3) JPA transaction control
 
Frequently in JEE setups, the application server controls the transaction manager (known as Container-Managed 
Persistence, or CMP) implying that the developer can avoid mixing business logic and transaction management.
This can both be a blessing and a curse depending on the scenario, the developer's experience and the required 
level of control. However, in a JPA persistence test-scope scenario we should be able to manually control when to 
commit or rollback the transaction.

The Nazgul Core: Persistence Test approaches the transaction management in a simple way; the developer typically
wants to do one of four things:

<table>
    <tr>
        <th width="25%">Method</th>
        <th width="75%">Description</th>
    </tr>
    <tr>
        <td>commitAndStartNewTransaction(); or commit(true);</td>
        <td>Commit the active transaction, flush the JPA state to the database and create a new EntityTransaction 
            for further JPA operations. (A "Write the changes to the database" operation).</td>
    </tr>
    <tr>
        <td>commit(false);</td>
        <td>Commit the active transaction, flush the JPA state to the database and creates a new EntityTransaction 
            for further JPA operations. However, the newly created EntityTransaction is not started 
            (using `begin()`).</td>
    </tr>
    <tr>
         <td>rollbackAndStartNewTransaction(); or rollback(true);</td>
         <td>Rolls the active transaction back, flush the JPA state to the database and create a new, started 
             EntityTransaction for further JPA operations. (An "Abandon the changes to the database" operation).</td>
    </tr>
    <tr>
        <td>rollback(false);</td>
        <td>Commit the active transaction, flush the JPA state to the database and create a new EntityTransaction 
            for further JPA operations. However, the newly created EntityTransaction is not started 
            (using `begin()`) (An "Abandon the changes to the database" operation).</td>
    </tr>
</table>

By exposing these four methods, we can provide a simple entry to the EntityTransaction 
so developers can control and access database states which can be rather difficult to 
induce within a running JEE application server.
   
### 4) Validating database state

When your automatic JPA tests have used Entities to perform some kind of change in the database, you would
want to validate the changes done. In this case - just like in the setup phase - we use the 
[dbUnit](http://dbunit.sourceforge.net/) standard framework. [dbUnit](http://dbunit.sourceforge.net/) has 
a simple and database-independent way to represent table structures and data within them, and works well 
with the [jUnit test framework](http://junit.org/).

The standard way to validate database state has 3 parts: 
 
1) Reading the expected state (in the database) from a dbUnit configuration file. 
   This is done by `performStandardTestDbSetup()` method and delivered as its return value.
2) Reading the actual state from the database, which is done by a standard dbUnit method called 
   `createDataSet()`. The StandardPersistenceTest class ensures that the iDatabaseConnection variable 
   always points to the active in-memory database used by the JPA engine.
3) Comparing the expected and actual states is done with the standard dbUnit method 
`Assertion.assertEquals(expected, dbDataSet2);`.
    
This is all shown in the code snippet below:

<pre class="brush: java" title="Validate database state method"><![CDATA[
    @Test
    public void validateCreateEntity() throws Exception {

        // Assemble
        ...
        final IDataSet expected = performStandardTestDbSetup();
        
        ...
        
        // Assert
        final IDataSet dbDataSet = iDatabaseConnection.createDataSet(new String[]{"BIRD"});
        Assertion.assertEquals(expected, dbDataSet);
]]></pre>
 
To further simplify validating database state, the Nazgul Core: Persistence Test framework employ a dbUnit
configuration file with the path `"testdata/" + getClass().getSimpleName() + "/expected_" + testMethodName + ".xml"`,
as illustrated in the image below:
 
<img src="images/dbPopulationConfiguration.png" style="margin:10px; border:1px solid black;" width="50%" height="50%" />

The content of the expected dbUnit XML file is:
 
<pre class="brush: xml"><![CDATA[
    <?xml version="1.0" encoding="UTF-8"?>
    <dataset>
    </dataset>
        
]]></pre>

## On JPA Entities and automatic tests

We all want to develop code which can be verified using automated test suites.
JPA entity classes are no exception to that rule... but unit testing 
JPA-annotated entities tend to be somewhat problematic, since there are 
basically two approaches:

1. **Mocked approach**. Run plain jUnit tests, using the JPA-annotated 
   classes as plain POJOs. Mock all interactions with the database 
   within the test scaffolding (i.e. objects such as EntityManager, 
   EntityManagerFactory etc.) which implies that we can mainly test 
   business logic within the POJO. However, the JPA specification provides
   classes which expose rich behaviour implying that mocking it all is
   complex and potentially error-prone. ("How can I be sure that my mocked
   behaviour is equal to an actual provider's behaviour in some respect"?)
   Moreover, while mock frameworks are certainly manageable they do introduce
   some means of complexity into the tests - both in setup and validation.
   
2. **Integration approach**. Create an in-memory database before each test, and 
   hook the JPA-annotated entities up to that database. This enables testing
   all kinds of hooks and JPA behaviours within the entities as we are actually 
   talking to a proper database. However, the in-memory database must also be
   populated before each test and state-validated after each test - which means
   that we need some extra mechanics to perform both population and validation 
   in simple ways.
   
The phases 