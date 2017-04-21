/*
 * #%L
 * Nazgul Project: nazgul-core-configuration-model
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.configuration.model;

import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"valueClassName"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractProperty<K extends Serializable & Comparable<K>,
        V extends Serializable> extends NazgulEntity implements Property<K, V> {

    // Internal state
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute(required = true)
    protected String valueClassName;

    private transient Class<V> valueTypeClass;

    /**
     * <p>JAXB / JPA-friendly constructor.</p>
     * <p><strong>Note!</strong> For framework use only.</p>
     */
    public AbstractProperty() {
        super();
    }

    /**
     * Creates a new AbstractProperty which can hold values of the supplied valueTypeClass.
     *
     * @param valueTypeClass The class (V) of values held by this AbstractProperty.
     *                       The valueTypeClass argument cannot be {@code null}.
     */
    public AbstractProperty(@NotNull final Class<V> valueTypeClass) {

        // Check sanity
        Validate.notNull(valueTypeClass, "valueTypeClass");

        // Assign internal state
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
            valueTypeClass = getValue() != null
                    ? (Class<V>) getValue().getClass()
                    : loadValueTypeClass(valueClassName);
        }

        // All done
        return valueTypeClass;
    }

    /**
     * Specification for how to re-load the ValueType class if its state is not found
     * within the standard transient member of this Property object.
     *
     * @param valueTypeClassName The fully qualified name of the ValueType class.
     * @return The valueTypeClass.
     */
    protected Class<V> loadValueTypeClass(@NotNull @Size(min = 1) final String valueTypeClassName) {

        // Check sanity
        Validate.notEmpty(valueTypeClassName, "valueTypeClassName ");

        final V value = getValue();
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
        if(valueTypeClass == null) {
            throw new NullPointerException("ValueType class could not be loaded for valueTypeClassName ["
                    + valueTypeClassName + "]. Tried both ThreadContext and class-level ClassLoaders.");
        }

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
                .notNullOrEmpty(valueClassName, "valueClassName")
                .endExpressionAndValidate();
    }
}
