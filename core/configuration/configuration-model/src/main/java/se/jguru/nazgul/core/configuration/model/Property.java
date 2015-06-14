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

import se.jguru.nazgul.tools.validation.api.Validatable;

import java.io.Serializable;

/**
 * Configuration property relating a key to a value, in the spirit of Map.Entry.
 * All property implementations must sport a
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Property<K extends Serializable & Comparable<K>, V extends Serializable>
        extends Serializable, Validatable {

    /**
     * Retrieves the key of this Property.
     *
     * @return the key of this Property. Keys are never null or empty.
     */
    K getKey();

    /**
     * Retrieves the value of this Property. Note that values may be {@code null}.
     *
     * @return the value of this Property. Note that values may be {@code null}.
     */
    V getValue();

    /**
     * Retrieves the type of the Value of this Property.
     *
     * @return the type of the Value of this Property. This method may not return {@code null}.
     */
    Class<V> getValueType();
}
