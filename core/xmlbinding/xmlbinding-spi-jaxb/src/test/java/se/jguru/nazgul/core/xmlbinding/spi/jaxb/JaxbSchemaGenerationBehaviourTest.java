/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXParseException;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.xmlbinding.api.DefaultNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.ThreePartCereal;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.ThreePartCerealWithNillableAnnotationElement;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbSchemaGenerationBehaviourTest {

    public static final String PREFIXMAPPER = "com.sun.xml.bind.namespacePrefixMapper";

    // Shared state
    private JAXBContext fullContext;
    private DefaultNamespacePrefixResolver defaultResolver;
    private JaxbNamespacePrefixResolver resolver;

    @Before
    public void setupSharedState() throws JAXBException {

        fullContext = JAXBContext.newInstance(ThreePartCereal.class,
                ThreePartCerealWithNillableAnnotationElement.class);

        defaultResolver = new DefaultNamespacePrefixResolver();
        defaultResolver.put("http://cereal", "cereal");
        defaultResolver.put("http://cereal/ingredients/flavour", "flavour");
        defaultResolver.put("http://cereal/ingredients/colour", "colour");
        defaultResolver.put("http://cereal/ingredients/base", "base");

        resolver = new JaxbNamespacePrefixResolver(defaultResolver);
    }

    @Test
    public void validateCorrectMarshallingWithNoNillableAnnotationElements() throws Exception {

        // Assemble
        final ThreePartCereal cereal = new ThreePartCereal("wholegrain", "raisins", "strawberries", 2, 1);
        final StringWriter resultWriter = new StringWriter();
        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<threePartCereal flavourPercentage=\"2\" colourPercentage=\"1\" xmlns:colour=\"http://cereal/ingredients/colour\" xmlns:base=\"http://cereal/ingredients/base\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:core=\"http://www.jguru.se/nazgul/core\" xmlns:flavour=\"http://cereal/ingredients/flavour\" xmlns:cereal=\"http://cereal\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <base:baseIngredient>wholegrain</base:baseIngredient>\n" +
                "    <flavour:flavourIngredient>raisins</flavour:flavourIngredient>\n" +
                "    <colour:colourIngredient>strawberries</colour:colourIngredient>\n" +
                "</threePartCereal>";

        final Marshaller marshaller = fullContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(PREFIXMAPPER, resolver);

        // Act
        final Schema schema = JaxbUtils.generateTransientXSD(fullContext, resolver);
        marshaller.setSchema(schema);
        marshaller.marshal(cereal, resultWriter);

        final String result = resultWriter.toString();

        // Assert
        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, result);
        Assert.assertTrue(diff.identical());
    }

    @Test
    public void validateIncorrectlyGeneratedSchemaOnNillableAnnotationElement() throws JAXBException {

        // Assemble
        final ThreePartCerealWithNillableAnnotationElement cereal =
                new ThreePartCerealWithNillableAnnotationElement("wholegrain", "raisins", "strawberries", 2, 1);

        final Marshaller marshaller = fullContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(PREFIXMAPPER, resolver);

        // Act & Assert
        final Schema schema = JaxbUtils.generateTransientXSD(fullContext, resolver);
        marshaller.setSchema(schema);

        try {
            marshaller.marshal(cereal, new StringWriter());
            Assert.fail();
        } catch (MarshalException e) {

            Assert.assertEquals(SAXParseException.class, e.getCause().getClass());
        }
    }


    @Test
    public void validateMarshallingWithSeparateNamespacesInRelations() throws Exception {

        // Assemble
        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<threePartCereal flavourPercentage=\"2\" colourPercentage=\"1\" xmlns:colour=\"http://cereal/ingredients/colour\" xmlns:base=\"http://cereal/ingredients/base\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:core=\"http://www.jguru.se/nazgul/core\" xmlns:flavour=\"http://cereal/ingredients/flavour\" xmlns:cereal=\"http://cereal\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <base:baseIngredient>wholegrain</base:baseIngredient>\n" +
                "    <flavour:flavourIngredient>raisins</flavour:flavourIngredient>\n" +
                "    <colour:colourIngredient>strawberries</colour:colourIngredient>\n" +
                "</threePartCereal>";
        final ThreePartCereal cereal = new ThreePartCereal("wholegrain", "raisins", "strawberries", 2, 1);

        final JAXBContext ctx = JAXBContext.newInstance(ThreePartCereal.class, EntityTransporter.class);
        final Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", resolver);

        final StringWriter resultWriter = new StringWriter();
        marshaller.marshal(cereal, resultWriter);

        final Schema transientSchema = JaxbUtils.generateTransientXSD(ctx, resolver);
        final Validator validator = transientSchema.newValidator();
        marshaller.setSchema(transientSchema);

        final StringWriter validationResultWriter = new StringWriter();
        final StreamResult streamResult = new StreamResult(validationResultWriter);
        validator.validate(new StreamSource(new StringReader(resultWriter.getBuffer().toString())), streamResult);

        // Act
        final String result = resultWriter.toString(); // unitUnderTest.marshal(cereal);

        // Assert
        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, result);
        Assert.assertTrue(diff.identical());
    }

    @Test
    public void validateJaxbSchemaGeneration() throws Exception {

        // Assemble
        final JAXBContext ctx1 = JAXBContext.newInstance(ThreePartCereal.class);
        final JAXBContext ctx2 = JAXBContext.newInstance(ThreePartCerealWithNillableAnnotationElement.class);
        final String requiredTargetNamespace = "targetNamespace=\"http://cereal\"";
        final String xPathToDiffElement = "/schema[1]/complexType[1]/sequence[1]/element[3]";

        // Act
        final List<String> schema1 = generateSchemas(ctx1, resolver);
        final List<String> schema2 = generateSchemas(ctx2, resolver);

        final List<String> relevantSchema1 = CollectionAlgorithms.filter(schema1, new Filter<String>() {
            @Override
            public boolean accept(String candidate) {
                return candidate.contains(requiredTargetNamespace);
            }
        });

        final List<String> relevantSchema2 = CollectionAlgorithms.filter(schema2, new Filter<String>() {
            @Override
            public boolean accept(String candidate) {
                return candidate.contains(requiredTargetNamespace);
            }
        });

        // Assert
        Assert.assertEquals(1, relevantSchema1.size());
        Assert.assertEquals(1, relevantSchema2.size());

        final String stringForm1Schema = relevantSchema1.get(0);
        final String stringForm2Schema = relevantSchema2.get(0);

        final List<String> linesNotInSchema2 = new ArrayList<String>();
        final BufferedReader schema1Reader = new BufferedReader(new StringReader(stringForm1Schema));
        String aLine = null;
        while ((aLine = schema1Reader.readLine()) != null) {
            if (!stringForm2Schema.contains(aLine.trim())) {
                linesNotInSchema2.add(aLine.trim());
            }
        }

        // 0: <xs:complexType name="threePartCereal">
        // 1: <xs:element ref="ns3:colourIngredient" minOccurs="0"/>
        Assert.assertTrue(linesNotInSchema2.get(1).contains(
                "<xs:element ref=\"ns3:colourIngredient\" minOccurs=\"0\"/>"));

        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(relevantSchema1.get(0), relevantSchema2.get(0));
        Assert.assertFalse(diff.similar());

        final SortedMap<String, List<Difference>> xpath2DiffMap = XmlTestUtils.getXPathLocationToDifferenceMap(diff);
        final List<Difference> elementDifference = xpath2DiffMap.get(xPathToDiffElement);

        Assert.assertEquals(6, elementDifference.size());
        Assert.assertEquals("number of element attributes", elementDifference.get(0).getDescription());
        for (final Difference currentDifference : elementDifference) {
            switch (currentDifference.getId()) {
                case 11:
                    // Different number of element attributes generated:
                    //
                    // 0: <xs:element ref="ns3:colourIngredient" minOccurs="0"/>
                    // 1: <xs:element name="colourIngredient" type="xs:string" form="qualified" nillable="true" minOccurs="0"/>
                    //
                    Assert.assertEquals(2, Integer.parseInt(currentDifference.getControlNodeDetail().getValue()));
                    Assert.assertEquals(5, Integer.parseInt(currentDifference.getTestNodeDetail().getValue()));
                    break;

                default:
                    // Anything else, typically attribute name changes.
                    break;
            }
        }
    }

    //
    // Private helpers
    //

    private List<String> generateSchemas(final JAXBContext ctx, final LSResourceResolver resourceResolver) {

        final List<String> toReturn = new ArrayList<String>();
        final List<ByteArrayOutputStream> schemaSnippets = new ArrayList<ByteArrayOutputStream>();

        try {
            ctx.generateSchema(new SchemaOutputResolver() {
                @Override
                public Result createOutput(final String namespaceUri, final String suggestedFileName)
                        throws IOException {

                    // Create the result ByteArrayOutputStream
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    schemaSnippets.add(out);

                    // Target the result to the generated ByteArrayOutputStream.
                    StreamResult streamResult = new StreamResult(out);
                    streamResult.setSystemId("");


                    // All done.
                    return streamResult;
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not acquire Schema snippets.", e);
        }

        // Convert and return
        return CollectionAlgorithms.transform(schemaSnippets, new Transformer<ByteArrayOutputStream, String>() {
            @Override
            public String transform(final ByteArrayOutputStream input) {
                return input.toString().trim();
            }
        });
    }

    @Test
    public void validateJaxbQnameAndNamespacePrefixMapperOperation() throws Exception {

        // Assemble
        final JAXBContext ctx = JAXBContext.newInstance(ThreePartCerealWithNillableAnnotationElement.class);
        final ThreePartCerealWithNillableAnnotationElement cereal =
                new ThreePartCerealWithNillableAnnotationElement("wholegrain", "raisins", null, 2, 1);
        final QName qName = new QName("http://www.jguru.se/foobar", "rootElementName", "xmlPrefix");
        final JAXBElement<ThreePartCerealWithNillableAnnotationElement> element =
                new JAXBElement<ThreePartCerealWithNillableAnnotationElement>(qName,
                        ThreePartCerealWithNillableAnnotationElement.class, cereal);
        final SortedMap<String, Tuple<String, Boolean>> namespaceToPrefixTupleMap =
                new TreeMap<String, Tuple<String, Boolean>>();

        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<xmlPrefix:rootElementName flavourPercentage=\"2\" colourPercentage=\"1\" xmlns:colour=\"http://cereal/ingredients/colour\" xmlns:base=\"http://cereal/ingredients/base\" xmlns:xmlPrefix=\"http://www.jguru.se/foobar\" xmlns:flavour=\"http://cereal/ingredients/flavour\">\n" +
                "    <base:baseIngredient>wholegrain</base:baseIngredient>\n" +
                "    <flavour:flavourIngredient>raisins</flavour:flavourIngredient>\n" +
                "    <colour:colourIngredient xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>\n" +
                "</xmlPrefix:rootElementName>";

        final Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
            @Override
            public String getPreferredPrefix(final String namespaceUri,
                                             final String suggestion,
                                             final boolean requirePrefix) {

                // Log the usage.
                namespaceToPrefixTupleMap.put(namespaceUri, new Tuple<String, Boolean>(suggestion, requirePrefix));

                if (suggestion == null && namespaceUri != null) {
                    return namespaceUri.equals("")
                            ? "fooBarz"
                            : namespaceUri.substring(namespaceUri.lastIndexOf("/") + 1);
                }

                // We have a suggested namespace prefix. Simply return it.
                return suggestion;
            }
        });

        // Act
        final StringWriter out = new StringWriter();
        marshaller.marshal(element, new StreamResult(out));

        // Assert
        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, out.toString());
        Assert.assertTrue(diff.identical());

        for(String current : namespaceToPrefixTupleMap.keySet()) {

            if(current.equals("http://www.w3.org/2001/XMLSchema-instance")) {
                Assert.assertEquals("xsi", namespaceToPrefixTupleMap.get(current).getKey());
                Assert.assertEquals(true, namespaceToPrefixTupleMap.get(current).getValue());
            } else {

                // Only the http://www.jguru.se/foobar namespace has a defined prefix via the QName.
                final String suppliedPrefix = "http://www.jguru.se/foobar".equals(current)  ? "xmlPrefix" : null;

                Assert.assertEquals(suppliedPrefix, namespaceToPrefixTupleMap.get(current).getKey());
                Assert.assertEquals(false, namespaceToPrefixTupleMap.get(current).getValue());
            }
        }
    }
}
