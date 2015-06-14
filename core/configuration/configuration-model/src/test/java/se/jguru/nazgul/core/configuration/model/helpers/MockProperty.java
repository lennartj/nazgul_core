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
package se.jguru.nazgul.core.configuration.model.helpers;

import se.jguru.nazgul.core.configuration.model.AbstractProperty;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockProperty extends AbstractProperty<String, String> {

    // Internal state
    private String key;
    private String value;

    /**
     * JAXB / JPA-friendly constructor.<br/>
     * <strong>Note!</strong> For framework use only.
     */
    public MockProperty() {
    }

    /**
     * Creates a new AbstractProperty which can hold values of the supplied valueTypeClass.
     *
     * @param valueTypeClass The class (V) of values held by this AbstractProperty.
     *                       The valueTypeClass argument cannot be {@code null}.
     */
    public MockProperty(final Class<String> valueTypeClass) {
        super(valueTypeClass);
    }

    /**
     * MockProperty constructor.
     *
     * @param valueClassName     the class name of the value type.
     * @param loadValueTypeClass if {@code true}, load the valueTypeClass.
     */
    public MockProperty(final String valueClassName,
                        final boolean loadValueTypeClass) {

        // Delegate
        super();

        // Load the internal state.
        this.valueClassName = valueClassName;

        if (loadValueTypeClass) {
            loadValueTypeClass(valueClassName);
        }
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
    public String getValue() {
        return value;
    }
}
