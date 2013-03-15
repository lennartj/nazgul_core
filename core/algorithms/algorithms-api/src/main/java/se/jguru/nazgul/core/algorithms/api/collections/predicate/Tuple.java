/*
 * #%L
 * Nazgul Project: se.jguru.nazgul.core.algorithms.api.nazgul-core-algorithms-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.algorithms.api.collections.predicate;

/**
 * Trivial holder type for a Tuple with a Key and a Value.
 * This is basically a public version of Map.Entry.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class Tuple<K, V> {

    // Internal state
    private K key;
    private V value;

    /**
     * Compound constructor wrapping the key and value within this Tuple.
     *
     * @param key   The key of this Tuple.
     * @param value The value of this Tuple.
     */
    public Tuple(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * @return The tuple key.
     */
    public K getKey() {
        return key;
    }

    /**
     * @return The tuple value.
     */
    public V getValue() {
        return value;
    }
}
