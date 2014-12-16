# What is the Nazgul Framework: Core API Parent?

The API parent extends the Nazgul Framework: Core Parent POM by exposing all
packages within the project as public dependencies. This is the expected behaviour
for all API and SPI projects.

Therefore, any API or SPI project should use the following parent definition,
adjusting the relativePath according to their position within the reactor:

<pre class="brush: xml"><![CDATA[
    <!-- +=============================================== -->
    <!-- | Section 1:  Project information                -->
    <!-- +=============================================== -->
    <parent>
        <groupId>se.jguru.nazgul.core.poms.core-api-parent</groupId>
        <artifactId>nazgul-core-api-parent</artifactId>
        <version>1.6.1-SNAPSHOT</version>
        <relativePath>../../poms/core-api-parent</relativePath>
    </parent>
]]></pre>