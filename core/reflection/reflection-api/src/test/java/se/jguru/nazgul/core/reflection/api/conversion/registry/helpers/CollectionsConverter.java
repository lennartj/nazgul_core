package se.jguru.nazgul.core.reflection.api.conversion.registry.helpers;

import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    public <K, V> List<Tuple<K, V>> convertToList(final Map<K, V> map) {

        List<Tuple<K, V>> toReturn = new ArrayList<Tuple<K, V>>();
        for(K current : map.keySet()) {
            toReturn.add(new Tuple<K, V>(current, map.get(current)));
        }

        return toReturn;
    }
}
