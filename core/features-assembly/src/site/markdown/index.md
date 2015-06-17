# Feature Assembly

The nazgul-core-feature-assembly creates and deploys a Karaf feature file which facilitates installing
all Nazgul Framework: Core bundles into a Karaf runtime. Simply add the Nazgul Core feature configuration
to the file `etc/org.apache.karaf.features.repos.cfg`.

## Adding nazgul_core feature from the command line

Launch karaf normally:

    ./bin/karaf
    
Then add the nazgul_tools feature:

    karaf@root()> feature:repo-add mvn:se.jguru.nazgul.core.features/nazgul-core-features-assembly/LATEST/xml/features

If you list the available features, the nazgul-core feature is shown in the top of the listing. Mind that 
the version shown in your console is likely a stable version, unless you have built the Nazgul Framework: Core from 
source and on a SNAPSHOT version.

    karaf@root()> feature:list | grep nazgul-core
    nazgul-core-algorithms                   | 1.7.2-SNAPSHOT   |           | nazgul-core              | Nazgul Framework: Algorithms
    nazgul-core-reflection                   | 1.7.2-SNAPSHOT   |           | nazgul-core              | Nazgul Framework: Reflection API
    nazgul-core-resource-api                 | 1.7.2-SNAPSHOT   |           | nazgul-core              | Nazgul Framework: Resource API
    nazgul-core-resource-impl-resourcebundle | 1.7.2-SNAPSHOT   |           | nazgul-core              | Nazgul Framework: ResourceBundle Resource Implemen
    nazgul-core-xmlbinding-api               | 1.7.2-SNAPSHOT   |           | nazgul-core              | Nazgul Framework: XmlBinding API
    nazgul-core-xmlbinding-spi-jaxb          | 1.7.2-SNAPSHOT   |           | nazgul-core              | Nazgul Framework: XmlBinding JAXB SPI
    nazgul-core-clustering-api               | 1.7.2-SNAPSHOT   |           | nazgul-core              | Nazgul Framework: Clustering API
    nazgul-core-cache-api                    | 1.7.2-SNAPSHOT   |           | nazgul-core              | Nazgul Framework: Cache API
    nazgul-core-impl-ehcache                 | 1.7.2-SNAPSHOT   |           | nazgul-core              | Nazgul Framework: Cache Implementation - EhCache
    nazgul-core-configuration-model          | 1.7.2-SNAPSHOT   |           | nazgul-core              | Nazgul Framework: Configuration Model
    ...

The Nazgul Core features are now available for installation. However, all Nazgul Framework projects use 
[Simple Logging Facade for Java (SLF4J)](http://www.slf4j.org) for logging. The Nazgul Framework does not define an 
explicit feature dependency to slf4j, since the standard package of Karaf normally exports pax-logging - which, in turn,
exports org.slf4j. Simply install one of the nazgul-core bundles with the command

    karaf@root()> feature:install nazgul-core-algorithms
    
The feature will now be installed:
    
    karaf@root()> feature:list -i | grep nazgul
    nazgul-tools           | 4.1.1          | x         | nazgul-tools   | Nazgul Framework: Tools
    nazgul-core-algorithms | 1.7.2-SNAPSHOT | x         | nazgul-core    | Nazgul Framework: Algorithms
    ...
    
## Making Karaf auto-launch a `nazgul-core-something` feature

At boot time, Karaf reads the file `etc/org.apache.karaf.features.cfg` to find the feature repositories and features
which should be installed at boot time. If you want a particular feature to be automatically installed into Karaf
at boot time, add the Nazgul Core feature repository to the list of featureRepositories used by Karaf and the 
exact nazgul-core feature identifier to the end of the featuresBoot option:  
 
    #
    # Comma separated list of features repositories to register by default
    #
    featuresRepositories=mvn:org.apache.karaf.features/standard/3.0.3/xml/features,
        mvn:org.apache.karaf.features/enterprise/3.0.3/xml/features,
        mvn:org.ops4j.pax.web/pax-web-features/3.1.4/xml/features,
        mvn:org.apache.karaf.features/spring/3.0.3/xml/features,
        mvn:se.jguru.nazgul.tools.features/nazgul-tools-features-assembly/LATEST/xml/features,
        mvn:se.jguru.nazgul.core.features/nazgul-core-features-assembly/LATEST/xml/features
    
    #
    # Comma separated list of features to install at startup
    #
    featuresBoot=config,standard,region,package,kar,ssh,management,nazgul-tools,nazgul-core-resource-impl-resourcebundle
    
When launching Karaf, the nazgul-tools feature is now automatically installed:
    
    karaf@root()> feature:list -i | grep nazgul
    nazgul-tools                             | 4.1.1          | x         | nazgul-tools   | Nazgul Framework: Tools
    nazgul-core-algorithms                   | 1.7.2-SNAPSHOT | x         | nazgul-core    | Nazgul Framework: Algorithms
    nazgul-core-resource-api                 | 1.7.2-SNAPSHOT | x         | nazgul-core    | Nazgul Framework: Resource API
    nazgul-core-parser-api                   | 1.7.2-SNAPSHOT | x         | nazgul-core    | Nazgul Framework: Parser API
    nazgul-core-resource-impl-resourcebundle | 1.7.2-SNAPSHOT | x         | nazgul-core    | Nazgul Framework: ResourceBundle Resource Implementation
    ...

While the documentation was performed on a development (i.e. SNAPSHOT) version, your version of the
nazgul-core- features should be a fixed version.

## Bundles activated by the nazgul-tools feature

Several bundles are made active by the nazgul-core-resource-impl-resourcebundle feature. Again, please note that the 
versions of the nazgul-core- bundles will be fix, release versions unless you build a snapshot version yourself:

    karaf@root()> bundle:list
    START LEVEL 100 , List Threshold: 50
    ID | State  | Lvl | Version        | Name
    -----------------------------------------------------------------------------
    39 | Active |  60 | 1.7.2.SNAPSHOT | nazgul-core-algorithms-api
    63 | Active |  50 | 4.1.1          | nazgul-tools-validation-api
    64 | Active |  50 | 4.1.1          | nazgul-tools-validation-aspect
    65 | Active |  50 | 3.4.0          | Apache Commons Lang
    66 | Active |  50 | 1.8.6          | AspectJ_Runtime
    69 | Active |  60 | 1.7.2.SNAPSHOT | nazgul-core-resource-api
    70 | Active |  60 | 1.7.2.SNAPSHOT | nazgul-core-parser-api
    71 | Active |  60 | 1.7.2.SNAPSHOT | nazgul-core-resource-impl-resourcebundle
    
    