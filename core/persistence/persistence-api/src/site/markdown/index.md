# Nazgul Core: Persistence API

The Nazgul Core: Persistence API defines a simplified API containing common operations used in
working with persistent entities. As indicated within the Nazgul Core: Persistence Model
project, Entity classes combine JPA and JAXB specifications to create
entities that can be persisted (typically to a database using JPA or SQL)
and also marshalled (typically to a web service using JAXB and XML/JSON).

The Nazgul Core: Persistence API provides a set of specifications which simplify
the common persistence operations used with Entity classes (i.e. create, read,
update and delete or "CRUD"). Typically, integration service implementations should
use pre-defined NamedQuery bindings to fire frequently occurring
[JPQL](http://en.wikipedia.org/wiki/Java_Persistence_Query_Language) queries against
the database.

JPQL queries can use either a *very* verbose compile-checked API to ensure that the
named parameters or queries are not misspelled, or the non-typechecked and string-based
("don't misspell or you will notice only in integration testing") approach to defining
queries and parameters for the named queries. The Nazgul Core: Persistence API provides
a middle ground - providing type checking to some degree while not being extremely verbose.

#### Example: Using JpaPersistenceOperations and ParameterMapBuilder

<img src="images/nazgul_core_reactor.png"
style="float:right; margin:10px; border: 1px solid black; width:50%; height:50%;"/>
The PersistenceOperations specification provides a simplified interface to a combination of common JPA and
persistence operations. Typically, its standard implementation `JpaPersistenceOperations` is sufficient for use in
all types of application. Since JPA NamedQueries can either use named parameters or posititional parameters, the
JPA-related operations come in two flavours - one for the positional parameters where arguments are represented
as a List, and one for the named parameters where arguments are represented as a Map.

While one can certainly create a Map from scratch by any means possible, the ParameterMapBuilder helper class can
simplify creating the parameter Map using a builder-type pattern:

<pre class="brush: java"><![CDATA[

    // Create the JpaPersistenceOperations, working on a supplied EntityManager
    final EntityManager theEntityManager = ...
    final JpaPersistenceOperations jpa = new JpaPersistenceOperations(theEntityManager);

    // Create the parameter map
    final Map<String, Object> params = ParameterMapBuilder
        .with("firstName", "Lenn%")
        .with("lastName", "J%")
        .build();

    // Fire the query; retrieve the results
    final int maxNumResults = 64;
    final String namedQuery = "getPeopleByFirstNameAndLastName";
    final List<NamedParametersPerson> result = jpa.fireNamedQueryWithResultLimit(namedQuery, maxNumResults, params);
]]></pre>

The example above is really verbose in the interest of clarity - but it can also be shrunk considerably, should the
developer want to be more consise:

<pre class="brush: java"><![CDATA[

    // Create the JpaPersistenceOperations, working on a supplied EntityManager
    final EntityManager theEntityManager = ...

    // Fire the query, return the results
    final List<NamedParametersPerson> result = new JpaPersistenceOperations(theEntityManager)
        .fireNamedQueryWithResultLimit(
            "getPeopleByFirstNameAndLastName",
            64,
            ParameterMapBuilder.with("firstName", "Lenn%").with("lastName", "J%").build());

]]></pre>
