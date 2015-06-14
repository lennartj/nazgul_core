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

import java.io.Serializable;

/**
 * Specification for a mutable Property whose value can be updated.
 * This specification is typically only used by administrative tooling,
 * as opposed to applications (or other configuration clients) which
 * should regard Properties are read-only.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface MutableProperty<K extends Serializable & Comparable<K>,
        V extends Serializable> extends Property<K, V> {

    /**
     * Assigns the supplied value to this MutableProperty.
     *
     * @param value The value of this property. {@code null} values are acceptable.
     */
    void setValue(final V value);
}
