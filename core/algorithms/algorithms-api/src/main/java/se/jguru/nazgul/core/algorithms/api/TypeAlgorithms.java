/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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


package se.jguru.nazgul.core.algorithms.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.types.TypeInformation;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Class- and Interface related algorithms.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public final class TypeAlgorithms {

    /**
     * Our logger.
     */
    public static final Logger log = LoggerFactory.getLogger(TypeAlgorithms.class);

    /**
     * Default property delimiter, used to construct property path expressions.
     * Typically an expression on the form {@code anObject.foo.bar.baz} implies that a set of JavaBean
     * getters should be invoked on the {@code anObject} object.
     *
     * @see #FIND_JAVABEAN_GETTERS
     * @see #getProperty(Object, String)
     */
    public static final String PROPERTY_DELIMITER = ".";

    /**
     * Compares two classes by comparing their fully qualified ClassNames.
     * Any null argument is replaced with an empty string before comparing.
     */
    public static final Comparator<Class<?>> CLASSNAME_COMPARATOR =
            Comparator.comparing(aClass -> (aClass == null ? "" : aClass.getName()));

    /**
     * Compares two Annotations by comparing the fully qualified ClassNames of their annotation types.
     * Any null argument is replaced with an empty string before comparing.
     *
     * @see Annotation#annotationType()
     */
    public static final Comparator<Annotation> ANNOTATION_COMPARATOR =
            Comparator.comparing(anAnnotation -> (anAnnotation == null ? "" : anAnnotation.annotationType().getName()));

    /**
     * Compares two Members by comparing their respective Declaring Class + toString value.
     * Any null argument is replaced with an empty string before comparing.
     */
    public static final Comparator<Member> MEMBER_COMPARATOR = (l, r) -> {

        final String leftSortKey = l == null ? "" : l.getDeclaringClass().getName() + l.toString();
        final String rightSortKey = r == null ? "" : r.getDeclaringClass().getName() + r.toString();

        // All Done.
        return leftSortKey.compareTo(rightSortKey);
    };

    /**
     * <p>Standard Supplied to create a SortedSet of Class'es using the {@link TypeAlgorithms#CLASSNAME_COMPARATOR}
     * to determine order within the SortedSet yielded. The typical application of this Supplier is</p>
     * <pre>
     * <code>
     *     [someStream].collect(Collectors.toCollection(TypeAlgorithms.SORTED_CLASS_SUPPLIER));
     * </code>
     * </pre>
     */
    public static final Supplier<SortedSet<Class<?>>> SORTED_CLASS_SUPPLIER =
            () -> new TreeSet<>(TypeAlgorithms.CLASSNAME_COMPARATOR);

    /**
     * <p>Standard Collector to create a SortedSet of Class'es using the {@link TypeAlgorithms#SORTED_CLASS_SUPPLIER}
     * supplied. Typically used as follows:</p>
     * <pre>
     * <code>
     *     [someStream].collect(TypeAlgorithms.SORTED_CLASSNAME_COLLECTOR));
     * </code>
     * </pre>
     */
    public static final Collector<Class<?>, ?, SortedSet<Class<?>>> SORTED_CLASSNAME_COLLECTOR =
            Collectors.toCollection(TypeAlgorithms.SORTED_CLASS_SUPPLIER);

    /**
     * <p>Standard Collector to create a SortedSet of Constructors using the
     * {@link TypeAlgorithms#SORTED_CONSTRUCTOR_SUPPLIER}. Typically used as follows:</p>
     * <pre>
     * <code>
     *     [someStream].collect(TypeAlgorithms.SORTED_CONSTRUCTOR_COLLECTOR));
     * </code>
     * </pre>
     */
    public static final Collector<Constructor<?>, ?, SortedSet<Constructor<?>>> SORTED_CONSTRUCTOR_COLLECTOR =
            Collectors.toCollection(TypeAlgorithms.SORTED_CONSTRUCTOR_SUPPLIER);

    /**
     * <p>Standard Supplied to create a SortedSet of Members using the {@link TypeAlgorithms#MEMBER_COMPARATOR}
     * to determine order within the SortedSet yielded. The typical application of this Supplier is</p>
     * <pre>
     * <code>
     *     [someStream].collect(Collectors.toCollection(() -> new TreeSet<>(TypeAlgorithms.MEMBER_COMPARATOR));
     * </code>
     * </pre>
     */
    public static final Supplier<SortedSet<Member>> SORTED_MEMBER_SUPPLIER =
            () -> new TreeSet<>(TypeAlgorithms.MEMBER_COMPARATOR);

    /**
     * <p>Standard Supplied to create a SortedSet of Method using the {@link TypeAlgorithms#MEMBER_COMPARATOR}
     * to determine order within the SortedSet yielded. The typical application of this Supplier is</p>
     * <pre>
     * <code>
     *     [someStream].collect(Collectors.toCollection(TypeAlgorithms.SORTED_METHOD_SUPPLIER));
     * </code>
     * </pre>
     */
    public static final Supplier<SortedSet<Method>> SORTED_METHOD_SUPPLIER =
            () -> new TreeSet<>(TypeAlgorithms.MEMBER_COMPARATOR);

    /**
     * <p>Standard Supplied to create a SortedSet of Constructor using the {@link TypeAlgorithms#MEMBER_COMPARATOR}
     * to determine order within the SortedSet yielded. The typical application of this Supplier is</p>
     * <pre>
     * <code>
     *     [someStream].collect(Collectors.toCollection(TypeAlgorithms.SORTED_CONSTRUCTOR_SUPPLIER));
     * </code>
     * </pre>
     */
    public static final Supplier<SortedSet<Constructor<?>>> SORTED_CONSTRUCTOR_SUPPLIER =
            () -> new TreeSet<>(TypeAlgorithms.MEMBER_COMPARATOR);

    /**
     * Function returning all public methods (including those inherited from superclasses) within a given class.
     * The sort order of the returned SortedSet is given by {@link #MEMBER_COMPARATOR}.
     */
    public static final Function<Class<?>, SortedSet<Method>> FIND_PUBLIC_METHODS = c -> {

        final SortedSet<Method> toReturn = new TreeSet<>(MEMBER_COMPARATOR);
        toReturn.addAll(Arrays.asList(c.getMethods()));

        // All Done.
        return toReturn;
    };

    /**
     * Function retrieving a SortedSet relating readable JavaBean property (names) to
     * their corresponding getter Methods. The JavaBean getter methods within the Object class
     * (i.e. "getClass()" will be ignored).
     */
    public static final Function<Class<?>, SortedMap<String, Method>> FIND_JAVABEAN_GETTERS = c -> {

        final SortedMap<String, Method> name2GetterMap = new TreeMap<>();

        try {

            // Find the BeanInfo of the current class.
            final BeanInfo beanInfo = Introspector.getBeanInfo(c, Object.class);

            // Find the method corresponding to each JavaBean property
            Arrays.stream(beanInfo.getPropertyDescriptors())
                    .forEach(propertyDescriptor -> {

                        final String name = propertyDescriptor.getName();
                        final Method getter = propertyDescriptor.getReadMethod();

                        if (getter != null && name != null) {

                            // Should we overwrite the current name2GetterMap value?
                            boolean writeProperty;

                            final Method existingGetterMethod = name2GetterMap.get(name);
                            if (existingGetterMethod != null) {

                                final Class<?> currentDeclaringClass = existingGetterMethod.getDeclaringClass();
                                final Class<?> candidateDeclaringClass = getter.getDeclaringClass();

                                writeProperty = currentDeclaringClass.isAssignableFrom(candidateDeclaringClass);
                            } else {
                                writeProperty = true;
                            }

                            // (Over)write the property?
                            if (writeProperty) {
                                name2GetterMap.put(name, getter);
                            }
                        }
                    });
        } catch (Exception e) {

            // Complain
            throw new IllegalArgumentException("Could not extract BeanInfo/JavaBeanGetters for class "
                    + c.getName(), e);
        }

        // All Done.
        return name2GetterMap;
    };

    /**
     * Function retrieving a SortedSet relating writable JavaBean property (names) to
     * their corresponding setter Methods.
     */
    public static final Function<Class<?>, SortedMap<String, Method>> FIND_JAVABEAN_SETTERS = c -> {

        final SortedMap<String, Method> name2SetterMap = new TreeMap<>();

        try {

            // Find the BeanInfo of the current class.
            final BeanInfo beanInfo = Introspector.getBeanInfo(c);

            // Find the method corresponding to each JavaBean property
            Arrays.stream(beanInfo.getPropertyDescriptors())
                    .forEach(propertyDescriptor -> {

                        final String name = propertyDescriptor.getName();
                        final Method setter = propertyDescriptor.getWriteMethod();

                        if (setter != null && name != null && !name2SetterMap.containsKey(name)) {
                            name2SetterMap.put(name, setter);
                        }
                    });
        } catch (Exception e) {

            // Complain
            throw new IllegalArgumentException("Could not extract BeanInfo/JavaBeanSetters for class "
                    + c.getName(), e);
        }

        // All Done.
        return name2SetterMap;
    };

    /*
     * Hide the constructor for utility classes.
     */
    private TypeAlgorithms() {
        // Do nothing
    }

    /**
     * Collects a Set containing all types held by the supplied aClass (including its supertypes).
     *
     * @param aClass The class to inspect, and retrieve all types for.
     * @return A TypeInformation object with data extracted for the supplied Class.
     */
    @NotNull
    public static TypeInformation getAllTypesFor(@NotNull final Class<?> aClass) {

        // Check sanity
        Validate.notNull(aClass, "aClass");

        // All Done
        return new TypeInformation(aClass);
    }

    /**
     * <p>Retrieves a property from the supplied pointOfOrigin Object using the supplied propertyExpression as the
     * definition of a property path. The property path should be on the form {@code foo.bar.baz} providing the
     * JavaBean property names ("foo", "bar" and "baz"). Such JavaBean property names do - in turn - imply methods
     * with the signature {@code getFoo(), getBar()} and {@code getBaz()} respectively.</p>
     * <p>The code above should yield an implementation chain similar to the following:</p>
     * <pre>
     *     <code>
     *         // The call:
     *         final SomeType result = TypeAlgorithms.getProperty(pointOfOrigin, "foo.bar.baz");
     *
     *         // ... is equal to calling the JavaBean getter methods in the
     *         // order given within the propertyExpression:
     *         final SomeType result = pointOfOrigin.getFoo().getBar().getBaz();
     *     </code>
     * </pre>
     *
     * @param pointOfOrigin      The object in which we should start the JavaBean invocation chain.
     * @param propertyExpression The property expression defining a chained
     * @return The result of the
     * @throws IllegalArgumentException if a JavaBean getter method is not present within the current/intermediary
     *                                  type in which it was requested to be invoked.
     */
    public static Object getProperty(@NotNull final Object pointOfOrigin,
                                     @NotNull final String propertyExpression) throws IllegalArgumentException {

        // #1) Check sanity
        Validate.notNull(pointOfOrigin, "pointOfOrigin");
        Validate.notEmpty(propertyExpression, "propertyExpression");

        // #2) Splice the propertyExpression into tokens, and define the intermediary state
        final List<String> tokens = splicePropertyExpression(propertyExpression);
        if (log.isDebugEnabled()) {
            log.debug("Split propertyExpression [" + propertyExpression + "] into " + tokens.size() + " tokens: "
                    + tokens);
        }

        // #3) Execute the JavaBean invocation chain.
        Object currentResult = null;
        final AtomicInteger index = new AtomicInteger();
        for (Object current = pointOfOrigin; index.get() < tokens.size(); current = currentResult,
                index.incrementAndGet()) {

            // #1) Get the current class in which to invoke a JavaBean getter.
            final Class<?> currentClass = current.getClass();

            // #2) Find the current JavaBean property
            final String currentPropertyName = tokens.get(index.get());

            // #3) Find the JavaBean getters for this Class, and - more specifically - the Method
            //     corresponding to the currentPropertyName.
            final TypeInformation currentTypeInformation = new TypeInformation(currentClass);
            final SortedMap<String, Method> currentGetters = currentTypeInformation.getJavaBeanGetterMethods();
            final Method getter = currentGetters.get(currentPropertyName);

            if (getter == null) {
                throw new IllegalArgumentException("Nonexistent expected JavaBean getter for property [" +
                        currentPropertyName + "] within class [" + currentClass.getName()
                        + "]. Found JavaBean properties: "
                        + currentGetters.keySet().stream().reduce((l, r) -> l + ", " + r).orElse("<none>"));
            }

            // #4) Invoke the getter, update the result.
            try {
                currentResult = getter.invoke(current);
            } catch (Exception e) {
                throw new IllegalArgumentException("JavaBean property [" + currentPropertyName
                        + "] getter method call failed in class [" + currentClass.getName() + "]", e);
            }
        }

        // All done.
        return currentResult;
    }

    //
    // Private helpers
    //
    private static List<String> splicePropertyExpression(@NotNull final String propertyExpression) {

        final List<String> toReturn = new ArrayList<>();

        final StringTokenizer tok = new StringTokenizer(propertyExpression, PROPERTY_DELIMITER, false);
        while (tok.hasMoreTokens()) {

            final String candidate = tok.nextToken().trim();

            // Check sanity, at least trivially.
            if (candidate.contains(" ")) {
                throw new IllegalArgumentException("PropertyExpressions cannot contain internal whitespace. " +
                        "(Found token '" + candidate + "' within propertyExpression [" + propertyExpression + "]");
            }

            // Add the candidate
            toReturn.add(candidate);
        }

        // All Done.
        return toReturn;
    }
}
