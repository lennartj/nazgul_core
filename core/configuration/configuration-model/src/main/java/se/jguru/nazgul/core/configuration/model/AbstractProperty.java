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
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract implementation of the Property specification, holding data and facilities required to convert
 * this AbstractProperty to transport and storage format.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"valueClassName", "key", "value"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractProperty<K extends Serializable & Comparable<K>, 
        V extends Serializable> extends NazgulEntity implements Property<K, V> {

    // Internal state
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlTransient
    private K key;

    @Basic(optional = true)
    @Column(nullable = true)
    @XmlTransient
    protected V value;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute(required = true)
    protected String valueClassName;

    private transient Class<V> valueTypeClass;

    /**
     * JAXB / JPA-friendly constructor.<br/>
     * <strong>Note!</strong> For framework use only.
     */
    public AbstractProperty() {
        super();
    }

    /**
     * Creates a new AbstractProperty from the supplied key and non-null value data.
     *
     * @param key   The key of this AbstractProperty. Cannot be null.
     * @param value The AbstractProperty value. Cannot be null.
     */
    public AbstractProperty(final K key, final V value) {

        // Check sanity
        Validate.notNull(key, "Cannot handle null key argument.");
        Validate.notNull(value, "Cannot handle null value argument.");

        // Assign internal state
        this.key = key;
        this.value = value;
        this.valueTypeClass = (Class<V>) value.getClass();
        this.valueClassName = valueTypeClass.getName();
    }

    /**
     * Creates a new AbstractProperty from the supplied (non-null) key and valueTypeClass parameters.
     * The value of this AbstractProperty is {@code null}.
     *
     * @param key            The key of this AbstractProperty. Cannot be null.
     * @param valueTypeClass The class of the value for this Property. Cannot be null.
     */
    public AbstractProperty(final K key, final Class<V> valueTypeClass) {

        // Check sanity
        Validate.notNull(key, "Cannot handle null key argument.");
        Validate.notNull(valueTypeClass, "Cannot handle null valueTypeClass argument.");

        // Assign internal state
        this.key = key;
        this.valueTypeClass = valueTypeClass;
        this.valueClassName = valueTypeClass.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<V> getValueType() {

        // Do we need to re-create the valueTypeClass?
        if (valueTypeClass == null) {
            valueTypeClass = value != null
                    ? (Class<V>) value.getClass()
                    : loadValueTypeClass(valueClassName);
        }

        // All done
        return valueTypeClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K getKey() {
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getValue() {
        return value;
    }

    /**
     * Specification for how to re-load the ValueType class if its state is not found
     * within the standard transient member of this Property object.
     *
     * @param valueTypeClassName The fully qualified name of the ValueType class.
     * @return The valueTypeClass.
     */
    protected Class<V> loadValueTypeClass(final String valueTypeClassName) {

        // Check sanity
        Validate.notEmpty(valueTypeClassName, "Cannot handle null or empty valueTypeClassName argument.");

        if (value != null) {
            valueTypeClass = (Class<V>) value.getClass();
        } else {

            final List<ClassLoader> classLoaders = Arrays.asList(
                    Thread.currentThread().getContextClassLoader(),
                    getClass().getClassLoader());

            for (ClassLoader current : classLoaders) {

                try {
                    valueTypeClass = (Class<V>) current.loadClass(valueTypeClassName);
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

        // Check sanity
        InternalStateValidationException.create()
                .notNull(key, "key")
                .notNullOrEmpty(valueClassName, "valueClassName")
                .endExpressionAndValidate();
    }
}
