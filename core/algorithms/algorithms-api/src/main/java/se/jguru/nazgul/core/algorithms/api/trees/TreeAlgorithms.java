/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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

package se.jguru.nazgul.core.algorithms.api.trees;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.EnumMap;

/**
 * A suite of frequently used functional algorithms.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class TreeAlgorithms {

    /**
     * Retrieves a semi-populated EnumMap with all keys in ordinal order, and all values {@code null}.
     *
     * @param keyType The type of key used within the returned EnumMap.
     * @param <K>     The key class.
     * @param <V>     The value class.
     * @return a semi-populated EnumMap with all keys in ordinal order, and all values {@code null}.
     */
    public static <K extends Enum<K>, V extends Serializable & Comparable<V>> EnumMap<K, V>
    getEmptyEnumMap(final Class<K> keyType) {

        Validate.notNull(keyType, "Cannot handle null keyType argument.");

        EnumMap<K, V> toReturn = new EnumMap<K, V>(keyType);

        for (K current : keyType.getEnumConstants()) {
            toReturn.put(current, null);
        }

        return toReturn;
    }
}
