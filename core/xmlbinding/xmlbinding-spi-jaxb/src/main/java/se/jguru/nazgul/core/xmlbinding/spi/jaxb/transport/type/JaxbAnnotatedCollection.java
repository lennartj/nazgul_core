/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.TransportTypeConverter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Transport type representing a {@code Collection} (subclass) value.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"originalCollectionType", "items"})
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbAnnotatedCollection<T extends Collection> extends AbstractJaxbAnnotatedTransportType<T> {

    // Internal state
    @XmlElement(required = true, nillable = false)
    private String originalCollectionType;

    /**
     * The list of entities persisted.
     */
    @XmlElementWrapper(name = "items", nillable = false, required = true)
    @XmlElement(name = "item")
    private List items = new ArrayList();

    /**
     * JAXB-friendly constructor.
     */
    public JaxbAnnotatedCollection() {
        super();
    }

    /**
     * Compound constructor, converting the supplied Collection (including its contained types)
     * to JAXB-compliant transport types.
     *
     * @param value The Collection value to convert to a JaxbAnnotatedCollection instance.
     */
    public JaxbAnnotatedCollection(final T value) {

        // Check sanity
        Validate.notNull(value, "Cannot handle null value argument.");

        // Assign the type name of the outermost Collection
        originalCollectionType = value.getClass().getName();

        // Create holder for class information
        final SortedSet<String> classInformation = new TreeSet<String>();

        // Add/convert all elements.
        for (Object current : value) {
            JaxbUtils.extractJaxbTransportData(current, EntityTransporter.getRegistry(), items, classInformation);
        }

        // Assign internal state
        super.value = null;
        setClassInformation(classInformation);
    }

    /**
     * @return The contained value.
     */
    @Override
    public T getValue() {

        if (value == null) {

            // Re-create the original Collection from the supplied originalCollectionType.
            try {
                final Class<T> aClass = (Class<T>) getClass().getClassLoader().loadClass(originalCollectionType);
                value = aClass.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Could not resurrect instance of type ["
                        + originalCollectionType + "]", e);
            }

            for (Object current : items) {

                // Find a type converter to resurrect the object
                TransportTypeConverter resurrectionConverter =
                        EntityTransporter.getRegistry().getRevivingTypeConverter(current);
                if (resurrectionConverter != null) {
                    value.add(resurrectionConverter.reviveAfterTransport(current));
                } else {
                    value.add(current);
                }
            }
        }

        // All done.
        return super.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Object that) {

        int result;

        if (that instanceof JaxbAnnotatedCollection) {

            if(value instanceof Comparable) {
                result = ((Comparable) value).compareTo(((JaxbAnnotatedCollection) that).getValue());
            } else {
                result = this.hashCode() < that.hashCode() ? -1 : (this.hashCode() == that.hashCode() ? 0 : 1);
            }

            return result;
        }

        if (that instanceof Collection) {
            if(value instanceof Comparable) {
                result = ((Comparable) value).compareTo(that);
            } else {
                result = this.hashCode() < that.hashCode() ? -1 : (this.hashCode() == that.hashCode() ? 0 : 1);
            }

            return result;
        }

        throw new ClassCastException("Cannot compare JaxbAnnotatedCollections to [" + that.getClass().getName() + "]");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return obj != null
                && (obj instanceof JaxbAnnotatedCollection || obj instanceof Collection)
                && this.compareTo(obj) == 0;
    }
}
