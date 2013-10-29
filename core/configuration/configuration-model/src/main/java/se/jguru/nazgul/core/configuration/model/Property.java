/*
 * #%L
 * Nazgul Project: nazgul-core-configuration-model
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
package se.jguru.nazgul.core.configuration.model;

import org.apache.commons.lang.Validate;
import se.jguru.nazgul.core.algorithms.tree.model.Path;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration property relating a key to a value.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"valueTypeClassName"})
public abstract class Property<K extends Serializable & Comparable<K>, KeyType extends Path<K>,
        ValueType extends Serializable> extends NazgulEntity {

    // Internal state
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlTransient
    private KeyType key;

    @Basic(optional = true)
    @Column(nullable = true)
    @XmlTransient
    private ValueType value;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(nillable = false, required = true)
    protected String valueTypeClassName;

    // @XmlTransient
    protected transient Class<ValueType> valueTypeClass;

    /**
     * JAXB / JPA-friendly constructor.<br/>
     * <strong>Note!</strong> For framework use only.
     */
    public Property() {
        super();
    }

    /**
     * Creates a new Property from the supplied key and non-null value data.
     *
     * @param key   The key of this Property. Cannot be null.
     * @param value The Property value. Cannot be null.
     */
    @SuppressWarnings("all")
    public Property(final KeyType key, final ValueType value) {

        // Check sanity
        Validate.notNull(key, "Cannot handle null key argument.");
        Validate.notNull(value, "Cannot handle null value argument.");

        // Assign internal state
        this.key = key;
        this.value = value;
        valueTypeClass = (Class<ValueType>) value.getClass();
        valueTypeClassName = valueTypeClass.getName();

        // Synchronize
        synchronizeInternalState(key, value);
    }

    /**
     * Creates a new Property from the supplied (non-null) key and valueTypeClass parameters.
     * The value of this Property is {@code null}.
     *
     * @param key            The key of this Property. Cannot be null.
     * @param valueTypeClass The class of the value for this Property. Cannot be null.
     */
    public Property(final KeyType key, final Class<ValueType> valueTypeClass) {

        // Check sanity
        Validate.notNull(key, "Cannot handle null key argument.");
        Validate.notNull(valueTypeClass, "Cannot handle null valueTypeClass argument.");

        // Assign internal state
        this.key = key;
        this.valueTypeClass = valueTypeClass;
        valueTypeClassName = valueTypeClass.getName();

        // Synchronize internal state
        synchronizeInternalState(key, value);
    }

    /**
     * Retrieves the full key of this Property. A Property key is a Path (or Path subtype).
     *
     * @return The full key of this Property. Each Property key is a Path (or Path subtype).
     */
    public KeyType getKey() {
        return key;
    }

    /**
     * @return The property value.
     */
    public ValueType getValue() {
        return value;
    }

    /**
     * Assigns the supplied value to this Property.
     *
     * @param value The value of this property. {@code null} values are acceptable.
     */
    public void setValue(final ValueType value) {
        this.value = value;
    }

    /**
     * Retrieves the name of this Property.
     * The name is considered to be the last segment of the key Path.
     *
     * @return the last segment of the key Path for this Property.
     */
    public K getName() {

        // Get the last leaf in the Path name.
        final int nameSegmentIndex = key.size() - 1;
        if (nameSegmentIndex < 0) {
            throw new IllegalStateException("No property name found for key [" + key + "].");
        }

        // Retrieves the name of this Property.
        return key.get(nameSegmentIndex);
    }

    /**
     * @return The ValueType used by this Property.
     */
    public final Class<ValueType> getValueType() {

        // Do we need to re-create the valueTypeClass?
        if (valueTypeClass == null) {
            valueTypeClass = value != null
                    ? (Class<ValueType>) value.getClass()
                    : loadValueTypeClass(valueTypeClassName);
        }

        // All done.
        return valueTypeClass;
    }

    /**
     * Specification for how to re-load the ValueType class if its state is not found
     * within the standard transient member of this Property object.
     *
     * @param valueTypeClassName The fully qualified name of the ValueType class.
     * @return The valueTypeClass.
     */
    protected Class<ValueType> loadValueTypeClass(final String valueTypeClassName) {

        // Check sanity
        Validate.notEmpty(valueTypeClassName, "Cannot handle null or empty valueTypeClassName argument.");

        if (value != null) {
            valueTypeClass = (Class<ValueType>) value.getClass();
        } else {

            final List<ClassLoader> classLoaders = Arrays.asList(
                    Thread.currentThread().getContextClassLoader(),
                    getClass().getClassLoader());

            for (ClassLoader current : classLoaders) {

                try {
                    valueTypeClass = (Class<ValueType>) current.loadClass(valueTypeClassName);
                    if (valueTypeClass != null) {
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    // Ignore this
                }
            }
        }

        // Check more sanity
        Validate.notNull(valueTypeClass, "ValueType class could not be loaded for valueTypeClassName ["
                + valueTypeClassName + "]. Tried both ThreadContext and class-level ClassLoaders.");

        // All done.
        return valueTypeClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(key, "key")
                .notNull(getName(), "name")
                .endExpressionAndValidate();
    }

    /**
     * Synchronization method, invoked when the Property superclass needs to push values
     * to its subclass implementations.
     *
     * @param key The key value.
     * @param value The value
     */
    protected abstract void synchronizeInternalState(final KeyType key, final ValueType value);
}
