# What is JMX?

<a href="http://en.wikipedia.org/wiki/Java_Management_Extensions">Java Management eXtensions</a> 
(or JMX for short) provides a consistent way to instrument and monitor Java applications.  
In this context, "instrumenting" an application means that we should implement the JMX API to enable 
administration tools to read and write runtime parameters during the application's normal execution,
optionally changing its behavior.

## Using the Nazgul Core: JMX API component

Import the nazgul-core-jmx-api dependency into your POM in compile scope to simplify writing
components which publish to or consume information from the JMX server. The Nazgul Core JMX API provides
for 2 related types of JMX bean implementation, as shown in the image below:
 
<img src="images/classStructure.png" style="margin:10px; border: 1px solid black;"/>

1. **AbstractMBean**: An MBean which implements the LifecycleStateful interface, and hence 
   exposes its lifecycle state as a JMX property. Each known Lifecycle state is found in the Lifecycle 
   enumeration type. 

2. **AbstractSuspendableMBean**: An AbstractMBean which can be suspended (and re-started) using standard methods.
   These methods are `suspend()` and `resume()` respectively and are defined within the
   Suspendable interface.