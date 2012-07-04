/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion;

import org.apache.commons.lang3.Validate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract TypeConverter implementation, providing generic type information method implementations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractTypeConverter<From, To> implements TypeConverter<From, To> {

    // Internal state
    private Class<To> toClass;
    private Class<From> fromClass;

    /**
     * Default constructor, extracting type information for convenience generic type methods.
     */
    protected AbstractTypeConverter() {

        // Get the generic types of this AbstractTypeConverter in declaration order
        final List typeArguments = getTypeArguments(AbstractTypeConverter.class,
                (Class<AbstractTypeConverter>) getClass());

        // Assign internal state
        fromClass = (Class<From>) typeArguments.get(0);
        toClass = (Class<To>) typeArguments.get(1);

        // Check sanity
        Validate.notNull(fromClass, "Cannot handle null fromClass type. Please use the compound constructor instead.");
        Validate.notNull(toClass, "Cannot handle null toClass type. Please use the compound constructor instead.");
    }

    /**
     * Fallback, compound constructor for use in cases where the From and To types cannot be autodetected.
     *
     * @param fromClass The class to convert from.
     * @param toClass   The class to convert to.
     * @throws NullPointerException if any argument is {@code null}.
     */
    protected AbstractTypeConverter(final Class<From> fromClass, final Class<To> toClass) throws NullPointerException {

        // Check sanity
        Validate.notNull(fromClass, "Cannot handle null fromClass instance.");
        Validate.notNull(toClass, "Cannot handle null toClass instance.");

        // Assign internal state
        this.toClass = toClass;
        this.fromClass = fromClass;
    }

    /**
     * Validates if this TypeConverter is able to convert the provided instance.
     *
     * @param instance The instance which should be validated for conversion.
     * @return {@code true} if this AbstractTypeConverter can
     *         package the provided instance for transport and {@code false} otherwise.
     */
    @Override
    public final boolean canConvert(final From instance) {
        return instance != null && isConvertible(instance);
    }

    /**
     * Validates if this TypeConverter is able to convert the provided instance.
     *
     * @param nonNullInstance The instance which should be validated for conversion.
     * @return {@code true} if this AbstractTypeConverter can
     *         package the provided instance for transport and {@code false} otherwise.
     */
    protected abstract boolean isConvertible(From nonNullInstance);

    /**
     * @return The target type of this TypeConverter.
     */
    @Override
    public final Class<To> getToType() {
        return toClass;
    }

    /**
     * @return The source type of this TypeConverter.
     */
    @Override
    public final Class<From> getFromType() {
        return fromClass;
    }

    /**
     * Get the underlying class for a type, or null if the type is a variable type.
     *
     * @param type the Type for which we want the Class.
     * @return the Class of the type.
     * @throws NullPointerException if {@code type} was {@code null}.
     */
    protected static Class<?> getClass(final Type type) throws NullPointerException {

        if (type instanceof Class) {

            // Already a class. All done.
            return (Class) type;
        } else if (type instanceof ParameterizedType) {

            // Retrieve the raw type of the ParametrizedType.
            return getClass(((ParameterizedType) type).getRawType());
        }

        // Keep this in case we find some way to inject
        // ArrayTypes into the AbstractTypeConverter.
        /*
            else if (type instanceof GenericArrayType) {

            // This is an Array; dig out the generic array type.
            Type genericComponentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> arrayComponentClass = getClass(genericComponentType);
            if (arrayComponentClass != null) {

                // Return an empty array with the correct type.
                return Array.newInstance(arrayComponentClass, 0).getClass();
            } else {

                // Give up.
                return null;
            }
        } */
        else {

            // Can't handle this Type.
            return null;
        }
    }

    /**
     * Get the actual type arguments a child class has used to extend a generic base class.
     *
     * @param baseClass  the base class
     * @param childClass the child class
     * @return a list of the raw classes for the actual type arguments.
     */
    protected static <T> List<Class<?>> getTypeArguments(final Class<T> baseClass,
                                                         final Class<? extends T> childClass) {

        Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
        Type type = childClass;

        // Start walking up the inheritance hierarchy until we hit baseClass
        while (!getClass(type).equals(baseClass)) {
            if (type instanceof Class) {
                // there is no useful information for us in raw types, so just keep going.
                type = ((Class) type).getGenericSuperclass();
            }

            // Keep this in case we find some way to inject
            // ArrayTypes into the AbstractTypeConverter.
            /*
                else {
                            ParameterizedType parameterizedType = (ParameterizedType) type;
                            Class<?> rawType = (Class) parameterizedType.getRawType();

                            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                            TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
                            for (int i = 0; i < actualTypeArguments.length; i++) {
                                resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
                            }

                            if (!rawType.equals(baseClass)) {
                                type = rawType.getGenericSuperclass();
                            }
                        } */
        }


        // For each actual type argument provided to baseClass.
        // Determine (if possible) the raw class for that type argument.
        Type[] actualTypeArguments;
        if (type instanceof Class) {
            actualTypeArguments = ((Class) type).getTypeParameters();
        } else {
            actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        }
        List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
        // resolve types by chasing down type variables.
        for (Type baseType : actualTypeArguments) {
            while (resolvedTypes.containsKey(baseType)) {
                baseType = resolvedTypes.get(baseType);
            }
            typeArgumentsAsClasses.add(getClass(baseType));
        }
        return typeArgumentsAsClasses;
    }
}
