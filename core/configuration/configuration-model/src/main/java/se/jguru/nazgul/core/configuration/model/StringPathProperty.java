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

import se.jguru.nazgul.core.algorithms.tree.model.common.StringPath;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Property implementation using StringPaths for keys.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"key"})
public class StringPathProperty<ValueType extends Serializable> extends Property<String, StringPath, ValueType> {

    // Internal state
    @Transient
    private StringPath key;

    /**
     * JAXB / JPA-friendly constructor.<br/>
     * <strong>Note!</strong> For framework use only.
     */
    public StringPathProperty() {
        super();
    }

    /**
     * Compound constructor creating a StringPathProperty instance wrapping the
     * supplied values.
     *
     * @param key   The key of this StringPathProperty.
     * @param value The value held by this StringPathProperty.
     */
    public StringPathProperty(final StringPath key, final ValueType value) {
        super(key, value);
    }

    /**
     * Creates a new StringPathProperty from the supplied (non-null) key.
     * The value of this Property is {@code null}.
     *
     * @param key            The key of this Property. Cannot be null.
     * @param valueTypeClass The class of the value for this Property. Cannot be null.
     */
    public StringPathProperty(final StringPath key, final Class<ValueType> valueTypeClass) {
        super(key, valueTypeClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // Delegate
        super.validateEntityState();

        // Ensure that the name is neither empty nor null.
        InternalStateValidationException.create()
                .notNullOrEmpty(getName(), "name")
                .endExpressionAndValidate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void synchronizeInternalState(final StringPath key, final ValueType value) {

        // Assign the internal state
        this.key = key;
    }
}
