# Overview: Reflection API

The reflection API contains utilities simplifying tasks concerned with copying, converting
or introspecting data structures. Three main sections exist:

1. The conversion package contains classes specifying and implementing TypeConverters.
   A registry with full prioritization between converters of the same type is implemented
   in the PrioritizedTypeConverterRegistry.
2. A simple and quick Bin to Hex converter is implemented in the Serializer class.
   This utility can be of great use when binary hashes should be stored or transported
   to be properly resurrected at a later stage, such as is the case when handling encrypted
   password hashes.
3. The DependencyData utility can be used to extract build-time dependency information,
   written by the <a href="http://maven.apache.org/plugins/maven-dependency-plugin/">maven-dependency-plugin</a>.
   This is important to find out build versions of artifacts in runtime.