# About the Nazgul Core Parser API

The Nazgul Core Parser API revolves around three concepts:

<dl>
    <dt>TokenParser</dt>
    <dd>A <em>TokenParser</em> is an object which can substitute variables within a set of data,
        returning a String where all variables have been substituted for specific values. 
        In the Nazgul Core Parser API, a variable has the form <code>${token}</code> - hence the 
        name TokenParser.</dd>
    
    <dt>TokenDefinitions</dt>
    <dd>A <em>TokenDefinitions</em> class defines how to identify token delimiters and how to
        find the token name from a token string. For example, the DefaultTokenDefinitions class 
        defines <code>${</code> to be a string which identifies the start of a token, and 
        <code>}</code> a string which identifies its end - implying that a Pattern to identify
        tokens within a text would be <em>\\$\\{[^}]*\\}</em>. 

    <dt>ParserAgent</dt>
    <dd>A <em>ParserAgent</em> is an object that has information/algorithms about which tokens should be
        replaced with what data content. Several Parser agents can be used together,
        where each ParserAgent typically knows about a single type of token substitution.</dd>
</dl>


## Token Parser vs. Parser Agent

The *TokenParser* interface is the main type which should be used to parse data strings into resulting,
tokenized strings. Simply use the `substituteTokens(String data)` method to submit your data string as input and
receive the tokenized result as the return value.

The *ParserAgent* is what makes a TokenParser extensible, and encapsulates the algorithms to handle a (single)
type of token replacement. If using the `DefaultTokenParser` implementation, several ParserAgents can be added to
the TokenParser and used in concert.

Please refer to the `DefaultTokenParserTest` for examples of how to use the Parser API.
A snippet is pasted below:

<pre class="brush: java" title="Example validateNormalParsing() method."><![CDATA[
@Test
public void validateNormalParsing() {

    // Assemble
    final String data = "Your JDK version is $\{sysprop:java.version}, which is $\{good}.";
    final String expected = "Your JDK version is " + System.getProperty("java.version")
                            + ", which is bad.";

    final DefaultParserAgent parserAgent = new DefaultParserAgent();
    parserAgent.addStaticReplacement("good", "bad");

    final DefaultTokenParser unitUnderTest = new DefaultTokenParser();
    unitUnderTest.addAgent(parserAgent);

    // Act
    final String result = unitUnderTest.substituteTokens(data);

    // Assert
    Assert.assertEquals(expected, result);
}
]]></pre>