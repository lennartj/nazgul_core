/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.core.reflection.api.conversion.registry.helpers;

import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CollectionsConverter {

    @Converter
    public <T> List<T> convertToList(final Collection<T> collection) {

        // Are we already a List?
        if(collection instanceof List) {
            return (List<T>) collection;
        }

        // Not an inbound List. Wrap and return.
        List<T> toReturn = new ArrayList<T>();
        toReturn.addAll(collection);
        return toReturn;
    }

    @Converter
    @SuppressWarnings("unchecked")
    public List convertSetToList(final Set set) {

        final List toReturn = new ArrayList();
        toReturn.add("convertSetToList");
        toReturn.addAll(set);
        return toReturn;
    }

    @Converter
    public <K, V> List<Tuple<K, V>> convertToList(final Map<K, V> map) {

        List<Tuple<K, V>> toReturn = new ArrayList<Tuple<K, V>>();
        for(K current : map.keySet()) {
            toReturn.add(new Tuple<K, V>(current, map.get(current)));
        }

        return toReturn;
    }
}
