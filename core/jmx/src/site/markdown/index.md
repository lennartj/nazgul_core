# What is JMX?

<a href="http://en.wikipedia.org/wiki/Java_Management_Extensions">Java Management eXtensions</a> 
(or JMX for short) provides a consistent way to instrument and monitor Java applications.  
In this context, "instrumenting" an application means that we should implement the JMX API to enable 
administration tools to read and write runtime parameters during the application's normal execution,
optionally changing its behavior.

## Using the Nazgul Core: JMX component

1. Import the `nazgul-core-jmx-api` dependency in compile scope in your project.

2. Import the `nazgul-core-jmx-test` dependency in test scope in your project.
