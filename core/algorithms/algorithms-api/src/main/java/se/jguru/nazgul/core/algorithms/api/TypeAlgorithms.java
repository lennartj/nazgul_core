/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

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
     * Compares two classes by comparing their fully qualified ClassNames.
     * Any null argument is replaced with an empty string before comparing.
     */
    public static final Comparator<Class<?>> CLASSNAME_COMPARATOR = (l, r) ->
            (l == null ? "" : l.getName()).compareTo(r == null ? "" : r.getName());

    /**
     * Compares two Annotations by comparing the fully qualified ClassNames of their annotation types.
     * Any null argument is replaced with an empty string before comparing.
     *
     * @see Annotation#annotationType()
     */
    public static final Comparator<Annotation> ANNOTATION_COMPARATOR = (l, r) ->
            (l == null ? "" : l.annotationType().getName()).compareTo(r == null ? "" : r.annotationType().getName());

    /**
     * Compares two Methods by comparing their respective Names.
     * Any null argument is replaced with an empty string before comparing.
     */
    public static final Comparator<Method> METHOD_COMPARATOR = (l, r) ->
            (l == null ? "" : l.getName()).compareTo(r == null ? "" : r.getName());

    /**
     * Function returning all public methods (including those inherited from superclasses) within a given class.
     * The sort order of the returned SortedSet is given by {@link #METHOD_COMPARATOR}.
     */
    public static final Function<Class<?>, SortedSet<Method>> FIND_PUBLIC_METHODS = c -> {

        final SortedSet<Method> toReturn = new TreeSet<>(METHOD_COMPARATOR);
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
                            if(existingGetterMethod != null) {

                                final Class<?> currentDeclaringClass = existingGetterMethod.getDeclaringClass();
                                final Class<?> candidateDeclaringClass = getter.getDeclaringClass();

                                writeProperty = currentDeclaringClass.isAssignableFrom(candidateDeclaringClass);
                            } else {
                                writeProperty = true;
                            }

                            // (Over)write the property?
                            if(writeProperty) {
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
}
