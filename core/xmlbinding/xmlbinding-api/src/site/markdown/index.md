Nazguz Core: XmlBinding API
===========================

The term "XML Binding" refers to the process of converting Java objects to [XML](http://www.w3.org/standards/xml/) 
formatted strings and vice versa. Using XML binding in an application implies that developers can use/access Java 
objects instead of directly manipulating XML data (for example using the low-level [DOM](http://www.w3.org/DOM/) or 
[SAX](http://sax.sourceforge.net/) approaches). Two concepts are important when discussing XML binding in general:

<table>
    <tr>
        <th width="25%">Term</th>
        <th width="75%">Description</th>
    </tr>
    <tr>
        <td>Marshalling</td>
        <td>Convert Java objects to an XML document</td>
    </tr>
    <tr>
        <td>Unmarshalling</td>
        <td>Convert an XML document to Java objects</td>
    </tr>
</table>

# XML Binding technologies  

Over the years, quite a few strategies/technologies/frameworks have been developed to perform this process. 
In itself, this is natural since a large part of configuration and message transport which needs to be done in a 
programming-language-neutral way must use some structured manner to manage data. 
XML has historically been a safe choice to perform this, although the 
[JSON](http://json.org/) (JavaScript Object Notation) format is also a commonly used method lately.

Some examples of competing XML binding technologies (far from a complete list!) are:

1. [JAXB](https://jaxb.java.net/), of which several implementations exist
2. [XStream](http://xstream.codehaus.org/)
3. [XmlBeans](http://xmlbeans.apache.org/)

## The Nazgul Core: XmlBinding API

The XmlBinding API specifies a minimal interaction pattern without any
ties to the underlying technology, such as JAXB or StAX. This minimal interaction pattern
defines how to convert Java object graphs to XML ("marshalling") and how to convert XML
to Java object graphs ("unmarshalling"). Moreover, well-known XML namespaces and a
technology-neutral way for how to map XML Namespace URIs to XML Namespace Prefixes.

The XML binding API is rarely used in itself; instead an SPI implementation adapting the
XML binding API to a certain technology is used. Please refer to an SPI project for
specific instructions regarding how to marshal and unmarshal Java to/from XML.