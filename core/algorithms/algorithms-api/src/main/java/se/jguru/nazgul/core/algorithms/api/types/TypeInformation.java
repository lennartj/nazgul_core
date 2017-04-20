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

package se.jguru.nazgul.core.algorithms.api.types;

import se.jguru.nazgul.core.algorithms.api.TypeAlgorithms;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Holder for type information, extracted from a source Class.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public class TypeInformation implements Serializable, Comparable<TypeInformation> {

    // Internal state
    private Class<?> source;
    private List<Class<?>> classHierarchy;
    private SortedMap<Class<?>, List<Class<?>>> class2InterfaceMap;
    private SortedMap<Class<?>, List<Annotation>> class2AnnotationMap;

    /**
     * Default constructor.
     */
    public TypeInformation() {

        // Create internal state
        this.classHierarchy = new ArrayList<>();
        this.class2InterfaceMap = new TreeMap<>(TypeAlgorithms.CLASSNAME_COMPARATOR);
        this.class2AnnotationMap = new TreeMap<>(TypeAlgorithms.CLASSNAME_COMPARATOR);
    }

    /**
     * Compound constructor creating a TypeInformation instance populated with
     * the state extracted from the supplied Class.
     *
     * @param aClass The Class which should be
     */
    public TypeInformation(@NotNull Class<?> aClass) {

        // Check sanity
        Validate.notNull(aClass, "aClass");

        // Assign internal state
        initializeWith(aClass);
    }

    /**
     * Initializes this TypeInformation object by extracting data from the supplied Class.
     * This implementation ignores the Object.class within the interfaceMap and annotationMap, since Object.class
     * does not sport interfaces or annotations.
     *
     * @param pointOfOrigin A non-null Class.
     */
    public final void initializeWith(@NotNull Class<?> pointOfOrigin) {

        // Check sanity
        Validate.notNull(pointOfOrigin, "pointOfOrigin");

        // Create internal state
        this.source = pointOfOrigin;
        this.classHierarchy = new ArrayList<>();
        this.class2InterfaceMap = new TreeMap<>(TypeAlgorithms.CLASSNAME_COMPARATOR);
        this.class2AnnotationMap = new TreeMap<>(TypeAlgorithms.CLASSNAME_COMPARATOR);

        // Parse and insert the information.
        for (Class current = pointOfOrigin; current != null; current = current.getSuperclass()) {

            // #1) Add the current Class
            classHierarchy.add(current);

            // We can safely ignore Object.class since it does not add any Interfaces or Annotations.
            if (current != Object.class) {

                // #2) Add the Interface types implemented by the current Class unless already added
                //     ... and ignore adding Object.class, since it does not add any Interfaces.
                final List<Class<?>> currentInterfaceList = class2InterfaceMap.computeIfAbsent(
                        current,
                        k -> new ArrayList<>());

                final Class[] interfaces = current.getInterfaces();
                if (interfaces != null && interfaces.length > 0) {

                    Arrays.stream(interfaces)
                            .filter(anInterface -> !currentInterfaceList.contains(anInterface))
                            .forEach(currentInterfaceList::add);
                }

                // #3) Add the annotation types for the current Class unless already added
                final List<Annotation> currentAnnotationList = class2AnnotationMap.computeIfAbsent(
                        current,
                        k -> new ArrayList<>());

                final Annotation[] annotations = current.getAnnotations();
                if (annotations != null && annotations.length > 0) {

                    Arrays.stream(annotations)
                            .filter(anAnnotation -> !currentAnnotationList.contains(anAnnotation))
                            .forEach(currentAnnotationList::add);
                }
            }
        }
    }

    /**
     * @return The Class which is the source of this TypeInformation, i.e. the Class
     * from which all information was ultimately collected.
     */
    public Class<?> getSource() {
        return source;
    }

    /**
     * Retrieves the list of classes within the inheritance hierarchy of the source class (for which this
     * TypeInformation was {@link #initializeWith(Class)}'d).
     *
     * @return the list of classes within the inheritance hierarchy of the source class (for which this TypeInformation
     * was {@link #initializeWith(Class)}'d).
     */
    @NotNull
    public List<Class<?>> getClassHierarchy() {
        return classHierarchy;
    }

    /**
     * Retrieves a SortedMap relating each class in the {@link #getClassHierarchy()} to the Interfaces it implements.
     *
     * @return a SortedMap relating each class in the {@link #getClassHierarchy()} to the Interfaces it implements.
     * All values within this Map are non-null, implying that an empty List will be returned for classes not
     * implementing any interfaces.
     */
    @NotNull
    public SortedMap<Class<?>, List<Class<?>>> getClass2InterfaceMap() {
        return class2InterfaceMap;
    }

    /**
     * Retrieves a SortedSet containing the Interface types implemented by the source class (or any of its supertypes).
     *
     * @return a SortedSet containing the Interface types implemented by the source class (or any of its supertypes).
     * @see TypeAlgorithms#CLASSNAME_COMPARATOR
     */
    @NotNull
    public SortedSet<Class<?>> getAllInterfaces() {

        final SortedSet<Class<?>> toReturn = new TreeSet<>(TypeAlgorithms.CLASSNAME_COMPARATOR);

        getClass2InterfaceMap().entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .forEach(interfaces -> {

                    // Add all Interfaces not already added.
                    interfaces
                            .stream()
                            .filter(anInterface -> !toReturn.contains(anInterface))
                            .forEach(toReturn::add);
                });

        // All Done.
        return toReturn;
    }

    /**
     * Retrieves a SortedMap relating each class in the {@link #getClassHierarchy()} to the Annotations it implements.
     *
     * @return a SortedMap relating each class in the {@link #getClassHierarchy()} to the Annotations it implements.
     */
    @NotNull
    public SortedMap<Class<?>, List<Annotation>> getClass2AnnotationMap() {
        return class2AnnotationMap;
    }

    /**
     * Retrieves a SortedSet containing the Annotations implemented by the source class (or any of its supertypes),
     * for each Annotation which is preserved at runtime (and therefore present within the compiled bytecode file of
     * the {@link #getSource()} class. Hence, this method can only retrieve Annotations which have
     * {@link RetentionPolicy#RUNTIME }
     *
     * @return a SortedSet containing the Annotations implemented by the source class (or any of its supertypes),
     * for Annotations having {@link RetentionPolicy#RUNTIME}.
     */
    @NotNull
    public SortedSet<Annotation> getAllAnnotations() {

        final SortedSet<Annotation> toReturn = new TreeSet<>(TypeAlgorithms.ANNOTATION_COMPARATOR);

        getClass2AnnotationMap().entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .forEach(annotations -> {

                    // Add all Annotations not already added.
                    annotations
                            .stream()
                            .filter(anAnnotation -> !toReturn.contains(anAnnotation))
                            .forEach(toReturn::add);
                });

        // All Done.
        return toReturn;
    }

    /**
     * Retrieves a Map relating the name of readable JavaBean property names to their respective getter methods.
     *
     * @return a Map relating the name of readable JavaBean property names to their respective getter methods.
     */
    @NotNull
    public SortedMap<String, Method> getJavaBeanGetterMethods() {
        return TypeAlgorithms.FIND_JAVABEAN_GETTERS.apply(getSource());
    }

    /**
     * Retrieves a Map relating the name of writeable JavaBean property names to their respective setter methods.
     *
     * @return a Map relating the name of writeable JavaBean property names to their respective setter methods.
     */
    @NotNull
    public SortedMap<String, Method> getJavaBeanSetterMethods() {
        return TypeAlgorithms.FIND_JAVABEAN_SETTERS.apply(getSource());
    }

    /**
     * Retrieves all Methods found within the {@link #getSource()} class.
     *
     * @param declaredMethods if true, retrieves the {@link Class#getDeclaredMethods()}
     *                        and otherwise {@link Class#getMethods()}.
     * @return A SortedSet containing all methods from the {@link #getSource()} class, sorted
     * according to the {@link TypeAlgorithms#MEMBER_COMPARATOR}.
     * @see TypeAlgorithms#MEMBER_COMPARATOR
     */
    @NotNull
    @SuppressWarnings("all")
    public SortedSet<Method> getMethods(final boolean declaredMethods) {

        final SortedSet<Method> toReturn = new TreeSet<>(TypeAlgorithms.MEMBER_COMPARATOR);
        final Method[] methods = declaredMethods ? getSource().getDeclaredMethods() : getSource().getMethods();

        for (int i = 0, methodsLength = methods.length; i < methodsLength; i++) {
            toReturn.add(methods[i]);
        }

        // All Done.
        return toReturn;
    }

    /**
     * Retrieves all Fields found within the {@link #getSource()} class.
     *
     * @param declaredFields if true, retrieves the {@link Class#getDeclaredFields()} ()}
     *                       and otherwise {@link Class#getFields()} ()}.
     * @return A SortedSet containing all fields from the {@link #getSource()} class, sorted
     * according to the {@link TypeAlgorithms#MEMBER_COMPARATOR}.
     * @see TypeAlgorithms#MEMBER_COMPARATOR
     */
    @NotNull
    @SuppressWarnings("all")
    public SortedSet<Field> getFields(final boolean declaredFields) {

        final SortedSet<Field> toReturn = new TreeSet<>(TypeAlgorithms.MEMBER_COMPARATOR);
        final Field[] fields = declaredFields ? getSource().getDeclaredFields() : getSource().getFields();

        for (int i = 0, fieldsLength = fields.length; i < fieldsLength; i++) {
            toReturn.add(fields[i]);
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Fail fast
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        // Delegate to internal state
        final TypeInformation that = (TypeInformation) o;
        return Objects.equals(source, that.source)
                && Objects.equals(classHierarchy, that.classHierarchy)
                && Objects.equals(class2InterfaceMap, that.class2InterfaceMap)
                && Objects.equals(class2AnnotationMap, that.class2AnnotationMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(source, classHierarchy, class2InterfaceMap, class2AnnotationMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final TypeInformation that) {

        // Fail fast
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Delegate to internal state
        return getSource().getName().compareTo(that.getSource().getName());
    }
}
