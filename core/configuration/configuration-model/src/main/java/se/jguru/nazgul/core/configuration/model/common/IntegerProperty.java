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
package se.jguru.nazgul.core.configuration.model.common;

import org.apache.commons.lang.Validate;
import se.jguru.nazgul.core.configuration.model.AbstractStringKeyedMutableProperty;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * AbstractStringKeyedMutableProperty implementation using Integers as values.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"value"})
@XmlAccessorType(XmlAccessType.FIELD)
public class IntegerProperty extends AbstractStringKeyedMutableProperty<Integer> {

    // Internal state
    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(required = false, nillable = true)
    private int value;

    /**
     * <p>JAXB / JPA-friendly constructor.</p>
     * <p><strong>Note!</strong> For framework use only.</p>
     */
    public IntegerProperty() {
    }

    /**
     * Creates a new IntegerProperty from the supplied key and value data.
     *
     * @param key   The key of this IntegerProperty. Cannot be null.
     * @param value The IntegerProperty value. Can be null.
     */
    public IntegerProperty(final String key, final int value) {

        // Delegate
        super(key, Integer.class);

        // Assign internal state
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final Integer value) {

        // Check sanity
        Validate.notNull("Cannot handle null value argument.");

        // Assign
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getValue() {
        return value;
    }
}
