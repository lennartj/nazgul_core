# Nazgul Core: Core reactor

The core reactor contains projects producing artifacts intended for use as
general project library dependencies.
All projects follow the Nazgul Software Component structure, and are fully OSGi compliant.
This implies that you should import and code against the API (or SPI) project and import implementation projects
only into JEE or JSE deployment units (such as a WAR/EAR or executable JAR).