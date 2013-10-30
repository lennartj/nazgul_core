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
package se.jguru.nazgul.core.configuration.model.common;

import se.jguru.nazgul.core.configuration.model.AbstractMutableProperty;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Property implementation using Strings for key and value.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class StringProperty extends AbstractMutableProperty<String, String> {

    /**
     * JAXB / JPA-friendly constructor.<br/>
     * <strong>Note!</strong> For framework use only.
     */
    public StringProperty() {
    }

    /**
     * Creates a new AbstractMutableProperty from the supplied key and non-null value data.
     *
     * @param key   The key of this AbstractMutableProperty. Cannot be null.
     * @param value The AbstractMutableProperty value. Cannot be null.
     */
    public StringProperty(final String key, final String value) {
        super(key, value);
    }

    /**
     * Creates a new AbstractMutableProperty from the supplied (non-null) key and valueTypeClass parameters.
     * The value of this AbstractMutableProperty is {@code null}.
     *
     * @param key            The key of this AbstractMutableProperty. Cannot be null.
     * @param valueTypeClass The class of the value for this AbstractMutableProperty. Cannot be null.
     */
    public StringProperty(final String key, final Class<String> valueTypeClass) {
        super(key, valueTypeClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // This implementation *replaces* that of its superclass.
        InternalStateValidationException.create()
                .notNullOrEmpty(getKey(), "key")
                .notNullOrEmpty(getValue(), "value")
                .notNullOrEmpty(valueClassName, "valueClassName")
                .endExpressionAndValidate();
    }
}
