On JPA Entities and automatic tests
=============================

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
   
The Nazgul Core: Persistence Test framework uses the integration approach; over 
the years it has become apparent that this approach yields less problems in the
long run.