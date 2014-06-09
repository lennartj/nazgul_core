# About the Nazgul Core: Resource Impl ResourceBundle

Use the Nazgul Core: Resource Impl ResourceBundle project as a replacement for
standard java ResourceBundles, with the following improvements:

1. The Nazgul Core resourcebundles support `UTF-8` encoding (whereas the plain Java SE ResourceBundles only
   support `ISO-8859-1` encoding). This implies that you can write non-ascii characters directly into your
   resourcebundle property files instead of having to convert them to unicode identifiers such as `u00E5`.
2. The Nazgul Core resourcebundles support token replacement of tokens defined within the resourcebundle
   property files through the use of the Nazgul Core parser API. This means you can easily substitute
   `${env:FOOBAR}` with the value of the environment variable named `FOOBAR` or create your own parser
   to include some dynamic values into resource bundle texts. This simplifies using a single resource text,
   instead of chopping it up into several smaller bits to enclose dates, environment values and the like.

The unit tests contain examples of token replacement and UTF-8 encoding.


