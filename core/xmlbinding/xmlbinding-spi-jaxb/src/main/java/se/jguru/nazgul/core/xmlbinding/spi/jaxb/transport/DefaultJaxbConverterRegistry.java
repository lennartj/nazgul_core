/*-
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.core.reflection.api.conversion.ConverterRegistry;
import se.jguru.nazgul.core.reflection.api.conversion.registry.DefaultConverterRegistry;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.StandardConverters;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Default JaxbConverterRegistry implementation, using reflection Converter
 * instances to convert back and forth between JAXB-annotated transport types.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultJaxbConverterRegistry implements JaxbConverterRegistry {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(DefaultJaxbConverterRegistry.class);

    // Internal state
    private static final List<? extends Class<? extends Serializable>> SELF_CONVERTIBLE =
            Arrays.asList(Boolean.class, Byte.class, Short.class, Integer.class, Float.class,
                    Double.class, Long.class, String.class);
    private ConverterRegistry registry;

    /**
     * Default constructor, creating a default ConverterRegistry to
     * which most calls are delegated.
     */
    public DefaultJaxbConverterRegistry() {

        // Create internal state
        registry = new DefaultConverterRegistry();

        // Add the standard converters
        addConverters(new StandardConverters());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void addConverters(final Object... converters) throws IllegalArgumentException {
        registry.add(converters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TransportType, OriginalType> Class<TransportType> getTransportType(
            @NotNull final Class<OriginalType> originalType) {

        // Check sanity
        Validate.notNull(originalType, "originalType");

        // If the OriginalType is annotated with @XmlType, simply return it.
        if (originalType.isAnnotationPresent(XmlType.class)) {
            return (Class<TransportType>) originalType;
        }

        // Find something in our registry?
        final Set<Class<?>> possibleConversions = registry.getPossibleConversions(originalType);
        for (Class<?> current : possibleConversions) {

            // Is this a transport type?
            // Also, disregard the JaxbAnnotatedNull in fuzzy logic searches.
            if (!(current.equals(JaxbAnnotatedNull.class))
                    && current.isAnnotationPresent(XmlType.class)) {
                return (Class<TransportType>) current;
            }
        }

        // No converter found.
        log.debug("No converter found for [" + originalType.getSimpleName() + "]. PossibleConversions: ["
                + possibleConversions + "], registry: " + registry);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <OriginalType, TransportType> Class<OriginalType> getOriginalType(
            @NotNull final Class<TransportType> transportType) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(transportType, "transportType");
        Validate.isTrue(transportType != JaxbAnnotatedNull.class,
                "Cannot acquire OriginalType for JaxbAnnotatedNull argument.");

        // If the transportType is neither a Primitive nor annotated with XmlType ... complain.
        boolean incorrectType = !transportType.isPrimitive()
                && !SELF_CONVERTIBLE.contains(transportType)
                && !transportType.isAnnotationPresent(XmlType.class);
        if (incorrectType) {
            throw new IllegalArgumentException("Supplied transportType [" + transportType.getName()
                    + "] is not annotated with XmlType.");
        }

        // Find something in our registry?
        for (Class<?> current : registry.getPossibleConversions(transportType)) {

            // Is this a non-transport type?
            if (!current.isAnnotationPresent(XmlType.class)) {
                return (Class<OriginalType>) current;
            }
        }

        // None found.
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <OriginalType, TransportType> TransportType packageForTransport(final OriginalType source)
            throws IllegalArgumentException {

        // Null values --> JaxbAnnotatedNull
        if (source == null) {
            return (TransportType) JaxbAnnotatedNull.getInstance();
        }

        // Find the transport type of the source.
        final Class<TransportType> transportTypeClass = getTransportType(source.getClass());

        TransportType toReturn = (TransportType) source;
        if (transportTypeClass != null) {

            // Convert the source instance to the supplied transportClass (or a subclass).
            toReturn = registry.convert(source, transportTypeClass);
        }

        // All done
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <OriginalType, TransportType> OriginalType resurrectAfterTransport(final TransportType transport)
            throws IllegalArgumentException {

        // No null values are accepted here (since they must have been converted
        // into JaxbAnnotatedNull instances).
        Validate.notNull(transport, "Cannot resurrect null transportType instance.");

        // Handle actual null cases
        if (transport instanceof JaxbAnnotatedNull) {
            return null;
        }

        // Find the original type of the transport
        final Class<OriginalType> originalType = getOriginalType(transport.getClass());

        // All done
        return originalType == null ? (OriginalType) transport : registry.convert(transport, originalType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "DefaultJaxbConverterRegistry with internal state: " + this.registry;
    }
}
