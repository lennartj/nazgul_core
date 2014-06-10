# About the Nazgul Core: Resource API

Use the Nazgul Core: Resource API to load and manage resources from resource files in runtime:

1. A specification for how to access locally (packaged) resources which may be Localized.
   This is typically the case when a Java SE application executes on a client machine, and you
   do not want to pollute your application code with implementation details of how the resources
   should be managed. (An improved ResourceBundle-based implementation is found in the
   `nazgul-core-resource-impl-resourcebundle` project, for instance).
2. Retrieve version information from packaged maven dependency files, making build-time version information
   available in runtime. Use the `MavenVersionExtractor` class to read build-time version information in runtime.
3. A simple way to load packaged Images (typically placed within a JAR found in the classpath of
   a runnable JAR). Use the `BundledImageLoader` for this.
4. Extract files (resources) from a JAR file within the classpath. Typically used when you want an
   application to save packaged files on the local computer - such as for user configuration, documents or
   other resources packaged within the application JARs.

## Example snippets: MavenVersionExtractor

Use the MavenVersionExtractor to find build-time versions in runtime components:

<pre class="brush: java" title="Example validateNormalParsing() method."><![CDATA[

    // Define the Maven groupId and artifactID coordinates
    final String groupId = "org.slf4j";
    final String artifactId = "slf4j-api";

    // Find the version of the slf4j-api artifact.
    final String versionAsString = MavenVersionExtractor.getDependencyVersion(
            groupId,
            artifactId,
            MavenVersionExtractor.getDependenciesReader());
]]></pre>

## Example snippets: BundleImageLoader

When creating a JavaSE application shipped to client within a JAR or other archive,
the required resources (such as images) are generally also bundled within the same JAR.
Loading them is simple using the BundleImageLoader:

<pre class="brush: java" title="Example validateNormalParsing() method."><![CDATA[
    // Define the packaged image to load
    // The path should be relative to the JAR in which it is packaged.
    final String imageToLoad = "images/orange_ball.png";

    // Load the image
    final Image orangeBall = BundledImageLoader.getBundledImage(imageToLoad);
]]></pre>

## Other examples

Refer to the implementation projects for other code snippet examples of usage.