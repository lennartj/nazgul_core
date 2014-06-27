# Overview: XmlBinding API

The task of converting Java objects to XML formatted strings is generally referred to
as XML "binding". The XmlBinding API specifies a minimal interaction pattern without any
ties to the underlying technology, such as JAXB or StAX. This minimal interaction pattern
defines how to convert Java object graphs to XML ("marshalling") and how to convert XML
to Java object graphs ("unmarshalling"). Moreover, well-known XML namespaces and a
technology-neutral way for how to map XML Namespace URIs to XML Namespace Prefixes.

The XML binding API is rarely used in itself; instead an SPI implementation adapting the
XML binding API to a certain technology is used. Please refer to an SPI project for
specific instructions regarding how to marshal and unmarshal Java to/from XML.