# Feature Assembly

The nazgul-core-feature-assembly creates and deploys a Karaf feature file which facilitates installing
all Nazgul Framework: Core bundles into a Karaf runtime. Simply add the Nazgul Core feature configuration
to the file `etc/org.apache.karaf.features.repos.cfg`.

The feature itself is created as follows:

    nazgul_core = se.jguru.nazgul.core.core.features:nazgul-core-features-assembly:xml:features:(0,]

The entire file would end in something like the following

    ...
    wicket       = org.ops4j.pax.wicket:features:xml:features:(0,]
    hawtio       = io.hawt:hawtio-karaf:xml:features:(0,]
    nazgul_tools = se.jguru.nazgul.tools.features:nazgul-tools-features-assembly:xml:features:(0,]
    nazgul_core  = se.jguru.nazgul.core.features:nazgul-core-features-assembly:xml:features:(0,]

## Using the Karaf installer to install the Nazgul Core feature

Having edited the feature repos configuration file, restart Karaf. The nazgul core feature should be
seen in the normal feature listing in Karaf:

    karaf@root()> feature:list | grep nazgul
    nazgul-core      | 3.0.2-SNAPSHOT   |       | nazgul-tools       | Nazgul Framework: Tools

Install the Nazgul Tools feature, and inspect its content as follows:

    karaf@root()> feature:install nazgul-tools
    karaf@root()> list
    START LEVEL 100 , List Threshold: 50
     ID | State  | Lvl | Version        | Name
    --------------------------------------------------------------------
    106 | Active |  80 | 3.0.2.SNAPSHOT | nazgul-tools-validation-api
    107 | Active |  80 | 3.0.2.SNAPSHOT | nazgul-tools-validation-aspect
    108 | Active |  80 | 3.3.2          | Apache Commons Lang
    109 | Active |  80 | 1.8.0          | AspectJ_Runtime
    110 | Active |  80 | 1.7.7          | slf4j-api
    111 | Active |  80 | 1.1.2          | Logback Core Module
    112 | Active |  80 | 1.1.2          | Logback Classic Module

While the documentation was performed on a development (i.e. SNAPSHOT) version, your version of the
nazgul-tools-validation-aspect and its api should be a fixed version.