# What is the Nazgul Framework: Core Model Parent?

The Model parent extends the Nazgul Framework: Core API Parent POM by including
dependencies used to simplify automated testing model entity classes.

Therefore, any Model projects should use the following parent definition,
adjusting the relativePath according to their position within the reactor:

<pre class="brush: xml"><![CDATA[
    <!-- +=============================================== -->
    <!-- | Section 1:  Project information                -->
    <!-- +=============================================== -->
    <parent>
        <groupId>se.jguru.nazgul.core.poms.core-model-parent</groupId>
        <artifactId>nazgul-core-model-parent</artifactId>
        <version>1.6.1-SNAPSHOT</version>
        <relativePath>../../poms/core-model-parent</relativePath>
    </parent>
]]></pre>