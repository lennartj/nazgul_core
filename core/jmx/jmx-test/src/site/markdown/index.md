# What is JMX?

<a href="http://en.wikipedia.org/wiki/Java_Management_Extensions">Java Management eXtensions</a> 
(or JMX for short) provides a consistent way to instrument and monitor Java applications.  
In this context, "instrumenting" an application means that we should implement the JMX API to enable 
administration tools to read and write runtime parameters during the application's normal execution,
optionally changing its behavior.

## Using the Nazgul Core: JMX Test component

Import the nazgul-core-jmx-test dependency into your POM in test scope to simplify writing
unit tests which requires the use of an MBeanServer in your tests. This project contains 
a jUnit Rule and a convenience abstract jUnit superclass which wraps a normal uses of the 
local MBean Server.

Therefore:

1. Import the Maven dependency to the nazgul-core-jmx-test in your project POM. 
   Use test scope to ensure that the dependencies do not leak into production code.
   
2. Implement your unit tests, by extending the `AbstractJmxTest` class (recommended)
   or using the `LocalMBeanServerRule` directly in your test class. 