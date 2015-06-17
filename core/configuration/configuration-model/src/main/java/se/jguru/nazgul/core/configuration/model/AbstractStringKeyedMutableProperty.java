/*
 * #%L
 * Nazgul Project: nazgul-core-configuration-model
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
package se.jguru.nazgul.core.configuration.model;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Abstract implementation of a MutableProperty with String keys.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"key"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractStringKeyedMutableProperty<V extends Serializable>
        extends AbstractMutableProperty<String, V> {

    // Internal state
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String key;

    /**
     * JAXB / JPA-friendly constructor.<br/>
     * <strong>Note!</strong> For framework use only.
     */
    public AbstractStringKeyedMutableProperty() {
    }

    /**
     * Creates a new AbstractMutableProperty which can hold values of the supplied valueTypeClass.
     *
     * @param valueTypeClass The class (V) of values held by this AbstractMutableProperty.
     *                       The valueTypeClass argument cannot be {@code null}.
     */
    public AbstractStringKeyedMutableProperty(final String key, final Class<V> valueTypeClass) {

        // Delegate
        super(valueTypeClass);

        // Assign internal state
        this.key = key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateEntityState() throws InternalStateValidationException {

        // Delegate
        super.validateEntityState();

        // Check sanity
        InternalStateValidationException.create()
                .notNullOrEmpty(key, "key")
                .endExpressionAndValidate();
    }

    /**
     * Equality comparison definition that compares key and value.
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object that) {

        // Check sanity
        if (this == that) {
            return true;
        }
        if (null == that || that.getClass() != getClass()) {
            return false;
        }

        // Delegate and compare
        final AbstractStringKeyedMutableProperty thatProperty = (AbstractStringKeyedMutableProperty) that;
        boolean valuesAreEqual = getValue() == null
                ? thatProperty.getValue() == null
                : getValue().equals(thatProperty.getValue());
        return valuesAreEqual && getKey().equals(thatProperty.getKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int valueHashcode = getValue() == null ? 0 : getValue().hashCode();
        return getKey().hashCode() + valueHashcode;
    }
}
