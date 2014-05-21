# Overview: XmlBinding API

The task of converting Java objects to XML formatted strings is generally referred to
as XML "binding". The XmlBinding API specifies a minimal interaction pattern without any
ties to the underlying technology, such as JAXB or StAX.

The XML binding API is rarely used in itself; instead an SPI implementation adapting the
XML binding API to a certain technology is used. Please refer to an SPI project for
specific instructions regarding how to marshal and unmarshal Java to/from XML.