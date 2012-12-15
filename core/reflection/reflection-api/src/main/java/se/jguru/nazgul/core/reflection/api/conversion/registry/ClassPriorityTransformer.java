/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Transformer supplying difference calculations against a supplied {@code desiredType}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class ClassPriorityTransformer implements Transformer<Class<?>, Tuple<Integer, Class<?>>> {

    // Internal state
    private Class<?> desiredType;

    /**
     * Creates a ClassPriotityTransformer instance supplying difference calculations
     * against the supplied {@code desiredType}.
     *
     * @param desiredType The class against which we should perform tranformations.
     */
    public ClassPriorityTransformer(final Class<?> desiredType) {

        // Check sanity
        Validate.notNull(desiredType, "Cannot handle null desiredType argument.");

        // Assign internal state
        this.desiredType = desiredType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tuple<Integer, Class<?>> transform(final Class<?> candidateType) {
        return new Tuple<Integer, Class<?>>(getRelationDifference(desiredType, candidateType), candidateType);
    }

    /**
     * Finds the number of types between related classes source and target.
     *
     * @param source The source type.
     * @param target The target type.
     * @return The number of class hops (i.e. class/superclass relations) between the
     *         source and target types; if source is a superclass of target, the value
     *         is negative, and otherwise positive.
     * @throws IllegalArgumentException if source and target classes are not related.
     */
    public static int getRelationDifference(final Class<?> source, final Class<?> target)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(source, "Cannot handle null source argument.");
        Validate.notNull(target, "Cannot handle null target argument.");

        if (source.isAssignableFrom(target)) {

            int depth = 0;

            for (Class<?> current = target; current != null; current = current.getSuperclass()) {

                // Same type?
                if (current == source) {
                    break;
                }

                // Interface implemented by the current class?
                // Does the current class implement the desiredType, or *is* it the desiredType?
                List<Class<?>> interfacesImplemented = new ArrayList<Class<?>>();
                Collections.addAll(interfacesImplemented, current.getInterfaces());

                // Done?
                if (interfacesImplemented.contains(source)) {
                    break;
                }

                // Decrease one.
                depth--;
            }

            // All done.
            return depth;
        }

        if (target.isAssignableFrom(source)) {

            int depth = 0;

            for (Class<?> current = source; current != null; current = current.getSuperclass()) {

                // Same type?
                if (current == target) {
                    break;
                }

                // Interface implemented by the current class?
                // Does the current class implement the desiredType, or *is* it the desiredType?
                List<Class<?>> interfacesImplemented = new ArrayList<Class<?>>();
                Collections.addAll(interfacesImplemented, current.getInterfaces());

                // Done?
                if (interfacesImplemented.contains(target)) {
                    break;
                }

                // Decrease one.
                depth++;
            }

            // All done.
            return depth;

        }

        // Complain.
        throw new IllegalArgumentException("Types [" + source.getSimpleName() + "] and [" + target.getSimpleName()
                + "] are not related.");
    }
}
