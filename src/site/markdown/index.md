# What is the Nazgul Framework?

<img src="images/nazgul.jpg" style="float:right" width="203" height="236"/> The Nazgul Framework project holds a
collection of best-pracises and sensible configurations enabling you to start projects quickly and scale them to huge
sizes without having to change the development or deployment model. Moreover, the Nazgul Framework strives to increase
usability for the developers and architects working on a project, as well as reduce complexity/tanglement and increase
productivity.

The Nazgul Framework has two main components:

1. **Nazgul Framework: Tools**. The Nazgul Tools project (another reactor; parent of this one) aims to
    use best-of-breed tools to achieve a usable, well-composed and simple mode of development and deployment. It
    defines codestyle, IDE integration and global aspects usable in any project development.

2. **Nazgul Framework: Core**. The Nazgul Core project (this reactor) provides a set of commonly useable library
    tools, built on top of the Nazgul Tools codestyle, IDE integration and global aspects. These tools are mainly
    self-sustaining and are ready to be integrated and used in any project.

## Getting started

Include an API (or SPI) project as a dependency in your own project. (Yes, it is really that simple).
A typical way to get started could be:

````xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <!-- +=============================================== -->
    <!-- | Section 1:  Project information                -->
    <!-- +=============================================== -->
    <groupId>com.fooproject.services.shared.spi.jaxb</groupId>
    <artifactId>fooproject-services-shared-spi-jaxb</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>${project.artifactId}</name>
    <description>FooProject Service: Shared Transport (SPI, version ${project.version})</description>
    
    <properties>
        <!-- 
            Version definitions 
         -->
        <nazgul-core.version>[the latest released version]</nazgul-core.version>
    </properties>

    <!-- +=============================================== -->
    <!-- | Section 2:  Dependency (management) settings   -->
    <!-- +=============================================== -->
    <dependencies>
        <dependency>
            <groupId>se.jguru.nazgul.core.xmlbinding.spi.jaxb</groupId>
            <artifactId>nazgul-core-xmlbinding-spi-jaxb</artifactId>
            <version>${nazgul-core.version}</version>
        </dependency>
    </dependencies>
    
    <!--
        ... and other things likely follows ... 
    -->
</project>
````

This snippet in your POM enables use of the Java8 Time API types as first-class citizens within 
your JAXB-annotated classes. In the code example below, the Java8 time class ```LocalDateTime``` 
is used as the internal state of a class which can convert itself in a standard way to any transport form 
(typically XML or JSON) using JAXB. The same holds true for Joda-Time ```DateTime``` classes, if you 
prefer them over the JavaSE 8 time classes.   

````java
@XmlType(namespace = "http://foobar.corp.com/somenamespace", propOrder = {"admissionTimestamp", "lastModifiedAt"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Admission implements Serializable, Validatable {

    /**
     * The timestamp when this Admission was created.
     */
    @XmlAttribute(required = true)
    private LocalDateTime admissionTimestamp;

    /**
     * The timestamp when this Admission was last modified.
     */
    @XmlAttribute
    private LocalDateTime lastModifiedAt;
    
    // ... other stuff omitted...
}
````

This is only a tiny portion of the functionality found in the Nazgûl Framework: Core libraries.
However, you need not import all available dependencies. 
Nazgûl Core is a set of non-tangled library JARs - use the ones you need, and ignore the others.

## What can the Nazgul Framework do for my project?

In short: Increase your productivity, raise your server uptime and provide scalable projects.
Using the Nazgul Framework lets you focus on development while deployment, scalability and 
reduced tanglement are inherited from the choices within the Nazgul Framework.

## How should I proceed?

Just import an API or SPI and use the types defined within them.
If you want a more coordinated 

1. Read [a brief overview of the Nazgul Core reactor](nazgul_core.html).

2. Read through the thoughs and structure of [Nazgul Software Components](software_components.html).

3. Import some components from Nazgul Framework: Core for direct use - or use the `nazgul-tools-external-parent` as a
   parent within your development project.