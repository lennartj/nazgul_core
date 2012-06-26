/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.api;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.predicate.Aggregator;
import se.jguru.nazgul.core.algorithms.api.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.predicate.Operation;
import se.jguru.nazgul.core.algorithms.api.predicate.Transformer;
import se.jguru.nazgul.core.algorithms.api.predicate.Tuple;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A suite of frequently used functional algorithms.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class CollectionAlgorithms {

    // Constants
    private static final String CANNOT_HANDLE_NULL_SOURCE_ARGUMENT = "Cannot handle null source argument.";
    private static final String CANNOT_HANDLE_NULL_TRANSFORMER_ARGUMENT = "Cannot handle null transformer argument.";

    /**
     * Copies the elements from the provided Enumeration into a List.
     *
     * @param source The enumeration from which to copy all elements.
     * @param <T>    The type of element found within the provided enumeration source.
     * @return A List holding all elements of the provided source.
     */
    public static <T> List<T> create(final Enumeration<T> source) {

        // Check sanity
        Validate.notNull(source, CANNOT_HANDLE_NULL_SOURCE_ARGUMENT);

        List<T> toReturn = new ArrayList<T>();
        while (source.hasMoreElements()) {
            toReturn.add(source.nextElement());
        }

        return toReturn;
    }

    /**
     * Aggregates (reduces) the provided source to a single result, by applying
     * the aggregator's aggregate method to all elements within the source Collection.
     *
     * @param source     The non-null source Collection holding T instances.
     * @param aggregator The aggregator used to aggregate a source element with the current result.
     * @param <T>        The type to operate on.
     * @param <R>        The result type.
     * @return An aggregate over the entire collection.
     */
    public static <T, R> R aggregate(final Collection<T> source, final Aggregator<T, R> aggregator) {

        // Check sanity
        Validate.notNull(source, CANNOT_HANDLE_NULL_SOURCE_ARGUMENT);
        Validate.notNull(aggregator, "Cannot handle null aggregator argument.");

        R toReturn = null;
        for (T current : source) {
            toReturn = aggregator.aggregate(toReturn, current);
        }

        return toReturn;
    }

    /**
     * Performs a no-return operation for all elements within the source collection.
     *
     * @param source    The non-null source Collection holding T instances.
     * @param operation The void-returning operation which should be performed for all elements within the collection.
     * @param <T>       The type to operate on.
     */
    public static <T> void forAll(final Collection<T> source, final Operation<T> operation) {

        // Check sanity
        Validate.notNull(source, CANNOT_HANDLE_NULL_SOURCE_ARGUMENT);
        Validate.notNull(operation, "Cannot handle null operation argument.");

        for (T current : source) {
            operation.operate(current);
        }
    }

    /**
     * Filters the provided collection using the given selector.
     *
     * @param source   The non-null source Collection holding T instances.
     * @param selector The T-type filter
     * @param <T>      The type to filter.
     * @param <C>      The Collection type which should be retrieved.
     * @return A new Collection holding the unique instances from the
     *         source list accepted by the GenericFilter (shallow
     *         copy, not deep-clone'd elements).
     */
    public static <T, C extends Collection<T>> C filter(final C source, final Filter<T> selector) {

        // Check sanity
        Validate.notNull(source, CANNOT_HANDLE_NULL_SOURCE_ARGUMENT);
        Validate.notNull(selector, "Cannot handle null selector argument.");

        // Create a return instance
        C toReturn = cloneEmpty(source);

        for (T current : source) {
            if (selector.accept(current) && !toReturn.contains(current)) {
                toReturn.add(current);
            }
        }

        return toReturn;
    }

    /**
     * Filters the provided Map using the given selector.
     *
     * @param source   The non-null source Map holding Entries of [K, V] instances.
     * @param selector The Filter selector for the provided Map
     * @param <K>      The key type of the Map.
     * @param <V>      The value type of the Map.
     * @return A new Map holding the instances from the
     *         source map accepted by the GeneriFilter (shallow
     *         copy, not deep-clone'd elements).
     */
    public static <K, V> Map<K, V> filter(final Map<K, V> source, final Filter<Tuple<K, V>> selector) {

        // Check sanity
        Validate.notNull(source, CANNOT_HANDLE_NULL_SOURCE_ARGUMENT);
        Validate.notNull(selector, "Cannot handle null selector argument.");

        // Create a return instance
        final Map<K, V> toReturn = new HashMap<K, V>();

        for (K current : source.keySet()) {

            final V value = source.get(current);
            if (selector.accept(new Tuple<K, V>(current, value))) {
                toReturn.put(current, value);
            }
        }

        return toReturn;
    }

    /**
     * Transforms (maps) each element within the source collection using the
     * provided transformer, returning the result in form of a Collection of
     * transformed instances.
     *
     * @param source      The non-null source Collection holding T instances.
     * @param transformer The transformer used to produce each R element from a T element.
     * @param <T>         The source collection type.
     * @param <R>         The response (transformed) collection type.
     * @param <C>         The Collection type.
     * @return A new Collection holding the transformed instances from the source.
     */
    public static <T, R, C extends Collection<R>> C transform(final Collection<T> source,
                                                              final Transformer<T, R> transformer) {

        // Check sanity
        Validate.notNull(source, CANNOT_HANDLE_NULL_SOURCE_ARGUMENT);
        Validate.notNull(transformer, CANNOT_HANDLE_NULL_TRANSFORMER_ARGUMENT);

        // Create a return instance
        Collection<R> toReturn = cloneEmptyFromType(source.getClass());

        for (T current : source) {
            toReturn.add(transformer.transform(current));
        }

        return (C) toReturn;
    }

    /**
     * Flattens (maps) each element within the source Map[K, V] using the
     * provided transformer, returning the result in form of a Collection of
     * transformed instances.
     *
     * @param source      The non-null source Map holding entries of [K, V] objects.
     * @param transformer The transformer used to produce a T instance from each [K, V]
     *                    entry within source.
     * @param <K>         The key type of the source map.
     * @param <V>         The value type of the source map.
     * @param <T>         The type of the returned Collection.
     * @return A List holding T instances.
     */
    public static <K, V, T> List<T> flatten(final Map<K, V> source, final Transformer<Tuple<K, V>, T> transformer) {

        // Check sanity
        Validate.notNull(source, CANNOT_HANDLE_NULL_SOURCE_ARGUMENT);
        Validate.notNull(transformer, CANNOT_HANDLE_NULL_TRANSFORMER_ARGUMENT);

        // Create a return instance
        List<T> toReturn = new ArrayList<T>();

        for (K current : source.keySet()) {
            toReturn.add(transformer.transform(new Tuple<K, V>(current, source.get(current))));
        }

        return toReturn;
    }

    /**
     * Transforms the sourceMap to a resulting Map using the provided transformer.
     *
     * @param source      The non-null source Map holding entries of [K1, V1] objects.
     * @param transformer The transformer used to produce entries of [K2, V2] from each entry of
     *                    [K1, V1] within source.
     * @param <K1>        The key type in the source Map.
     * @param <V1>        The value type in the source Map.
     * @param <K2>        The key type in the result Map.
     * @param <V2>        The value type in the result Map.
     * @return A new Map holding the transformed Entries from the source Map.
     */
    public static <K1, V1, K2, V2> Map<K2, V2> transform(final Map<K1, V1> source,
                                                         final Transformer<Tuple<K1, V1>, Tuple<K2, V2>> transformer) {
        // Check sanity
        Validate.notNull(source, CANNOT_HANDLE_NULL_SOURCE_ARGUMENT);
        Validate.notNull(transformer, CANNOT_HANDLE_NULL_TRANSFORMER_ARGUMENT);

        // Create a return instance
        Map<K2, V2> toReturn = new HashMap<K2, V2>();

        for (K1 current : source.keySet()) {

            final Tuple<K1, V1> sourceTuple = new Tuple<K1, V1>(current, source.get(current));
            final Tuple<K2, V2> resultTuple = transformer.transform(sourceTuple);

            if (resultTuple != null) {
                toReturn.put(resultTuple.getKey(), resultTuple.getValue());
            }
        }

        return toReturn;
    }

    /**
     * Transforms (maps) each element within the source collection using the
     * provided transformer, returning the result in form of a Map of
     * the Tuple pair instances. <strong>Note!</strong> Only non-null Tuples
     * returned by the transformer are mapped into the returned Map.
     *
     * @param source      The non-null source Collection holding T instances.
     * @param transformer The transformer used to produce each R element from a T element.
     * @param <T>         The source collection type.
     * @param <K>         The key type of the returned Map.
     * @param <V>         The value type of the returned Map.
     * @return A Map containing the <strong>non-null</strong> Tuples generated by the Tuple transformer.
     */
    public static <K, V, T> Map<K, V> map(final Collection<T> source, final Transformer<T, Tuple<K, V>> transformer) {

        // Check sanity
        Validate.notNull(source, CANNOT_HANDLE_NULL_SOURCE_ARGUMENT);
        Validate.notNull(transformer, CANNOT_HANDLE_NULL_TRANSFORMER_ARGUMENT);

        // Create a return instance
        Map<K, V> toReturn = new HashMap<K, V>();

        for (T current : source) {
            final Tuple<K, V> keyValuePair = transformer.transform(current);
            if (keyValuePair != null) {
                toReturn.put(keyValuePair.getKey(), keyValuePair.getValue());
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Extracts a typed old-style Enumeration from the provided Collection source.
     *
     * @param source The source Collection.
     * @param <T>    The type of element enumerated by the returned Enumeration.
     * @return An Enumeration iterating over the provided source.
     */
    public static <T> Enumeration<T> enumerate(final Collection<T> source) {

        final Iterator<T> it = source.iterator();

        return new Enumeration<T>() {
            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public T nextElement() {
                return it.next();
            }
        };
    }

    //
    // Private helpers
    //

    private static <T, C extends Collection<T>> C cloneEmptyFromType(final Class<C> type) {

        Validate.notNull(type, "Cannot handle null type argument.");
        C toReturn = null;

        try {
            // Do we have a default Constructor in type?
            final Constructor<C> defaultConstructor = type.getConstructor(new Class<?>[]{});
            if (defaultConstructor != null) {
                toReturn = defaultConstructor.newInstance();
            }
        } catch (Exception e) {
            // Ignore this - fallback to default instances.
        }

        if (toReturn == null) {
            // Fallback.
            if (Set.class.isAssignableFrom(type)) {
                toReturn = (C) new TreeSet<C>();
            } else if (List.class.isAssignableFrom(type)) {
                toReturn = (C) new ArrayList<T>();
            } else {
                // this is a collection (potentially abstract)
                toReturn = (C) new ArrayList<T>();
            }
        }

        // All done.
        return toReturn;
    }

    private static <T, C extends Collection<T>> C cloneEmpty(final C source) {
        return (C) cloneEmptyFromType(source.getClass());
    }
}
