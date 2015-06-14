/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.ClassInformationHolder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;
import se.jguru.nazgul.tools.validation.api.Validatable;

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
 * @see ConditionalTransportTypeConverter
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
    private static JaxbConverterRegistry typeConverterRegistry = new DefaultJaxbConverterRegistry();

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

            // Resurrect the current item, if required.
            // The current item should not be null.
            toReturn.add((T) getRegistry().resurrectAfterTransport(current));
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
    public final void addItem(final T toAdd) {
        JaxbUtils.extractJaxbTransportData(toAdd, getRegistry(), items, entityClasses);
    }

    /**
     * Validates the state of all validatable objects within the items List.
     */
    public void validateItemState() {
        if (items != null) {
            for (Object current : items) {
                if (current instanceof Validatable) {
                    ((Validatable) current).validateInternalState();
                }
            }
        }
    }

    /**
     * @return The singleton JaxbConverterRegistry used by all EntityWrapper instances.
     */
    public static JaxbConverterRegistry getRegistry() {
        return typeConverterRegistry;
    }

    /**
     * Assigns the singleton TypeConverterRegistry instance used
     * by all EntityWrapper instances.
     *
     * @param registry The TypeConverterRegistry instance to use.
     * @throws IllegalArgumentException if the registry parameter was null.
     */
    public static void setTransportTypeConverterRegistry(final JaxbConverterRegistry registry)
            throws IllegalArgumentException {

        Validate.notNull(registry, "Cannot handle null registry argument.");
        typeConverterRegistry = registry;
    }
}
