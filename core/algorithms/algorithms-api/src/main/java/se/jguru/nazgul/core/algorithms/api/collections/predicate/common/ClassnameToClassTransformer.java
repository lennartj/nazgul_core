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

package se.jguru.nazgul.core.algorithms.api.collections.predicate.common;

import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;

/**
 * Transforms fully qualified class names to Class instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ClassnameToClassTransformer implements Transformer<String, Class<?>> {

    // Internal state
    private ClassLoader classLoader;

    /**
     * Creates a ClassnameToClassTransformer using the current thread context ClassLoader
     * to load the classes from given class names. If a contextClassLoader could not be found,
     * falls back to the class loader of this ClassnameToClassTransformer instance.
     */
    public ClassnameToClassTransformer() {
        this(null);
    }

    /**
     * Creates a ClassnameToClassTransformer using the provided ClassLoader to load the classes
     * from given class names. If classloader is {@code null}, fallback to default behaviour.
     *
     * @param classLoader The ClassLoader which should be used to load the Classes for each
     *                    given fully qualified class name. If classloader is {@code null}, fallback
     *                    to default behaviour.
     * @see #ClassnameToClassTransformer()
     */
    public ClassnameToClassTransformer(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Transforms a fully qualified class name to its corresponding Class instance.
     *
     * @param input The fully qualified class name to be transformed.
     * @return the Class of the provided fully qualified class name.
     */
    @Override
    public Class<?> transform(final String input) {

        // Do we have a given ClassLoader?
        ClassLoader loader = (classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader());
        if (loader == null) {
            loader = ClassnameToClassTransformer.class.getClassLoader();
        }

        try {
            return loader.loadClass(input);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find class [" + input + "] in ClassLoader ["
                    + loader + "]", e);
        }
    }
}
