/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.ClassInformationHolder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Entity holder used to convert and transport object instances over network connections.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = XmlBinder.CORE_NAMESPACE)
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"entityClasses", "items"})
@XmlAccessorType(XmlAccessType.FIELD)
public class EntityTransporter<T> implements ClassInformationHolder, Serializable {

    /**
     * The classes of the entities persisted.
     */
    @XmlElementWrapper(name = "entityClasses", nillable = false, required = true)
    @XmlElement(name = "entityClass")
    private SortedSet<String> entityClasses = new TreeSet<String>();

    /**
     * The list of entities persisted.
     */
    @XmlElementWrapper(name = "items", nillable = false, required = true)
    @XmlElement(name = "item")
    private List<Object> items = new ArrayList<Object>();

    @XmlTransient
    private static TransportTypeConverterRegistry typeConverterRegistry = new DefaultTransportTypeConverterRegistry();

    /**
     * JAXB-friendly constructor.
     */
    public EntityTransporter() {
        entityClasses.add(EntityTransporter.class.getName());
    }

    /**
     * Compound constructor, adding the provided object to this EntityWrapper.
     *
     * @param object An object that should be added to this EntityWrapper.
     */
    public EntityTransporter(final T object) {
        this();
        addItem(object);
    }

    /**
     * @return The fully qualified class names of all classes held within this ClassInformationHolder.
     */
    @Override
    public SortedSet<String> getClassInformation() {
        return entityClasses;
    }

    /**
     * @return The list of objects added to this EntityWrapper.
     */
    public List<T> getItems() {

        // Resurrect the entity objects from their transport object form.
        List<T> toReturn = new ArrayList<T>();

        for (Object current : items) {

            // Find a type converter to resurrect the object
            TransportTypeConverter resurrectionConverter = getRegistry().getRevivingTypeConverter(current);
            if (resurrectionConverter != null) {
                toReturn.add((T) resurrectionConverter.reviveAfterTransport(current));
            } else {
                toReturn.add((T) current);
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Adds the provided object to the list of items, and its corresponding class to the entityClasses list
     * (given that it is not already added to the list of entityClasses).
     *
     * @param toAdd The item to add
     * @throws IllegalArgumentException if argument is null
     */
    public void addItem(final T toAdd) {
        JaxbUtils.extractJaxbTransportData(toAdd, getRegistry(), items, entityClasses);
    }

    /**
     * @return The singleton TypeConverterRegistry used by all
     *         EntityWrapper instances.
     */
    public static TransportTypeConverterRegistry getRegistry() {
        return typeConverterRegistry;
    }

    /**
     * Assigns the singleton TypeConverterRegistry instance used
     * by all EntityWrapper instances.
     *
     * @param registry The TypeConverterRegistry instance to use.
     * @throws IllegalArgumentException if the registry parameter was null.
     */
    public static void setTransportTypeConverterRegistry(final TransportTypeConverterRegistry registry)
            throws IllegalArgumentException {

        Validate.notNull(registry, "Cannot handle null TransportTypeConverterRegistry");
        typeConverterRegistry = registry;
    }
}
