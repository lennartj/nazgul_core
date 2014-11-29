/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb;

import org.apache.commons.lang3.Validate;
import org.w3c.dom.ls.LSResourceResolver;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.xmlbinding.api.NamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * JAXB implementation of the XmlBinder specification.
 * Must extend the JAXB NamespacePrefixMapper class due to poor JAXB design.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbXmlBinder implements XmlBinder<Object> {

    private static final int STREAM_BUFFER_INITIAL_CAPACITY = 5000;

    // Internal state
    private JaxbNamespacePrefixResolver namespacePrefixResolver;
    private JAXBContext initialTransportContext;

    public JaxbXmlBinder() {
        this(new JaxbNamespacePrefixResolver());
    }

    /**
     * Compound constructor, creating a JAXB-flavoured XmlBinder instance which uses the provided
     * TypeConverterRegistry to convert inbound types to and from JAXB-annotated transport types
     * during transport, and the provided JaxbNamespacePrefixResolver to resolve JAXB namespaces
     * and prefixes.
     *
     * @param namespacePrefixResolver The JAXB-implemented NamespacePrefixResolver which is also a JAXB
     *                                NamespacePrefixMapper. Use the NamespacePrefixResolver API to affect its
     *                                operation.
     */
    public JaxbXmlBinder(final JaxbNamespacePrefixResolver namespacePrefixResolver) {

        Validate.notNull(namespacePrefixResolver, "Cannot handle null namespacePrefixResolver argument.");

        this.namespacePrefixResolver = namespacePrefixResolver;

        // Create the initialTransportContext
        try {
            initialTransportContext = JAXBContext.newInstance(EntityTransporter.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Could not create initialTransportContext.", e);
        }
    }

    /**
     * Retrieves the NamespacePrefixResolver instance in use by this XmlBinder.
     *
     * @return the NamespacePrefixResolver instance in use by this XmlBinder,
     *         or {@code null} should no NamespacePrefixResolver be used by this
     *         XmlBinder.
     */
    @Override
    public final NamespacePrefixResolver getNamespacePrefixResolver() {
        return namespacePrefixResolver;
    }

    /**
     * Converts the provided source java objects to an XML formatted String.
     * Order between the provided objects is preserved in the resulting XML.
     *
     * @param toConvert The java objects to convert to an XML formatted String.
     * @return An XML representation of the provided javaObjects.
     * @throws IllegalArgumentException If the conversion could not be completed successfully.
     * @throws InternalStateValidationException
     *                                  if the Java Object Graph contained Validatable objects
     *                                  that did not pass validation.
     */
    @Override
    public String marshal(final Object... toConvert)
            throws IllegalArgumentException, InternalStateValidationException {

        // Create an EntityTransporter holding all given objects.
        final EntityTransporter<Object> transporter = new EntityTransporter<Object>();
        for (Object current : toConvert) {
            transporter.addItem(current);
        }

        // Validate without requiring resurrecting the items.
        transporter.validateItemState();

        // Acquire a Marshaller for the provided EntityTransporter
        final JAXBContext ctx = JaxbUtils.getJaxbContext(transporter, true);
        final Marshaller marshaller = JaxbUtils.getHumanReadableStandardMarshaller(ctx, namespacePrefixResolver, true);

        try {
            StringWriter resultWriter = new StringWriter();
            marshaller.marshal(transporter, resultWriter);
            return resultWriter.toString();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Could not convert to XML", e);
        }
    }

    /**
     * Reads the XML formatted string from the provided transportReader, and resurrects the object graph
     * found within the transportReader.
     *
     * @param transportReader The Reader holding a single XML-formatted String being converted by the marshal method
     *                        in an XmlBinder of the same internal implementation as this one.
     * @return A fully unmarshalled List holding clones of the original objects written to the stream.
     * @throws IllegalArgumentException If the object graph could not be properly resurrected.
     * @throws se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException
     *                                  if any object resurrected was a Validatable which did not pass validation.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public List<Object> unmarshal(final Reader transportReader)
            throws IllegalArgumentException, InternalStateValidationException {

        Validate.notNull(transportReader, "Cannot handle null transportReader argument.");

        // Read the stream content.
        final String content = readFully(transportReader);

        try {

            // Find all classes within the provided transporter
            Unmarshaller unmarshaller = initialTransportContext.createUnmarshaller();
            final EntityTransporter transporter = (EntityTransporter) unmarshaller.unmarshal(new StringReader(content));

            // Now we know all classes inside the EntityTransporter.
            final JAXBContext ctx = JaxbUtils.getJaxbContext(transporter, false);
            unmarshaller = ctx.createUnmarshaller();

            // Generate and assign the Schema to enable XSD validation.
            final Tuple<Schema, LSResourceResolver> transientTuple = JaxbUtils.generateTransientXSD(ctx);
            final Schema schema = transientTuple.getKey();
            unmarshaller.setSchema(schema);
            final Validator validator = schema.newValidator();
            validator.setResourceResolver(transientTuple.getValue());

            @SuppressWarnings("unchecked")
            final EntityTransporter<Object> toReturn = (EntityTransporter<Object>)
                    unmarshaller.unmarshal(new StringReader(content));

            // If the instances are validatable, perform validation.
            validate(toReturn);

            // All done.
            return toReturn.getItems();

        } catch (final JAXBException e) {
            throw new IllegalArgumentException("Could not convert XML data to java objects.", e);
        }
    }

    /**
     * Convenience {@code unmarshal} method which acquires a single object instance. Reads the XML formatted string
     * from the provided transportReader, and resurrects the object found within the transportReader.
     *
     * @param transportReader The Reader holding a single XML-formatted String being converted by the
     *                        marshal method in an XmlBinder of the same internal implementation type as this one.
     * @param <S>             The instance type to return.
     * @return A fully unmarshalled instance clone of the original object written to the stream.
     * @throws IllegalArgumentException If the object graph could not be properly resurrected.
     * @throws se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException
     *                                  if any object resurrected was a Validatable which did not pass validation.
     */
    @Override
    public <S> S unmarshalInstance(final Reader transportReader)
            throws IllegalArgumentException, InternalStateValidationException {

        // Convert
        final List<Object> objects = unmarshal(transportReader);

        // Sane result?
        S toReturn = null;
        if (objects.size() > 0) {
            toReturn = (S) objects.get(0);
        }
        if (objects.size() > 1) {
            throw new IllegalArgumentException("Expected to return one object, but resurrected ["
                    + objects.size() + "] objects.");
        }

        // All done.
        return toReturn;
    }

    //
    // Private helpers
    //

    /**
     * Reads all data from the provided Reader.
     *
     * @param reader The reader from which to read all data.
     * @return The data read from the reader, cast to a String.
     */
    private String readFully(final Reader reader) {

        final BufferedReader tmp = new BufferedReader(reader);
        final StringBuilder toReturn = new StringBuilder(STREAM_BUFFER_INITIAL_CAPACITY);

        try {
            for (String line = tmp.readLine(); line != null; line = tmp.readLine()) {
                toReturn.append(line).append('\n');
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Problem reading data from Reader", e);
        }

        // All done.
        return toReturn.toString();
    }

    /**
     * Validates the supplied argument if appropriate (i.e. if the toValidate instance implements Validatable).
     *
     * @param toValidate The transporter container to validate.
     * @throws InternalStateValidationException
     *          if the internal state Validation failed.
     */
    private void validate(final EntityTransporter toValidate) throws InternalStateValidationException {

        for (Object current : toValidate.getItems()) {
            if (current instanceof Validatable) {
                ((Validatable) current).validateInternalState();
            }
        }
    }
}
