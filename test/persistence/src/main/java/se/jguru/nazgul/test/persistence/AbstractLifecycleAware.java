/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-test
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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
package se.jguru.nazgul.test.persistence;

import org.apache.commons.lang3.Validate;

import java.io.File;
import java.net.URL;

/**
 * Abstract implementation of a LifecycleAware type which holds a
 *
 * @param <T> The explicit AbstractPersistenceTest subclass.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractLifecycleAware<T extends AbstractPersistenceTest> implements LifecycleAware<T> {

    // Internal state
    private T testClass;
    private DatabaseType databaseType;

    /**
     * Required default constructor, as the AbstractPersistenceTest can use the default
     * constructor through reflection to create this AbstractLifecycleAware instance.
     */
    protected AbstractLifecycleAware() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void initialize(final T testClass, final String testMethodName, final DatabaseType databaseType) {

        // Check sanity
        Validate.notNull(testClass, "Cannot handle null testClass argument.");
        Validate.notNull(databaseType, "Cannot handle null databaseType argument.");
        Validate.notEmpty(testMethodName, "Cannot handle null or empty testMethodName argument.");

        // Assign internal state
        this.testClass = testClass;

        // Delegate
        onInitialize(testClass, testMethodName);
    }

    /**
     * Perform the initialization of this LifecycleAware instance.
     *
     * @param testClass      The currently executing AbstractPersistenceTest (subclass), guaranteed to be non-null.
     * @param testMethodName The name of the currently executing test method, guaranteed to be non-empty.
     */
    protected abstract void onInitialize(final T testClass, final String testMethodName);

    /**
     * {@inheritDoc}
     */
    @Override
    public final void shutdown(final T testClass, final String testMethodName) {

        // Check sanity
        Validate.notNull(testClass, "Cannot handle null testClass argument.");
        Validate.notEmpty(testMethodName, "Cannot handle null or empty testMethodName argument.");

        // Delegate
        onShutdown(testClass, testMethodName);

        // Cleanup internal staet
        this.testClass = null;
    }

    /**
     * @return The active unit test.
     */
    protected final T getTestClass() {
        return this.testClass;
    }

    /**
     * @return The active DatabaseType.
     */
    protected final DatabaseType getDatabaseType() {
        return this.databaseType;
    }

    /**
     * Perform the shutdown of this LifecycleAware instance, cleaning up any state
     * which was set up in the initialize method.
     *
     * @param testClass      The currently executing AbstractPersistenceTest (subclass), guaranteed to be non-null.
     * @param testMethodName The name of the currently executing test method, guaranteed to be non-empty.
     */
    protected abstract void onShutdown(final T testClass, final String testMethodName);

    /**
     * Retrieves a File pointing to the test data directory for the supplied testClass and testMethod.
     *
     * @param testClass  The active unit test class.
     * @param testMethod The active unit test method.
     * @return a File pointing to the directory {@code LifecycleAware.DATA_ROOT_DIRECTORY
     * + "/" + testClass.getSimpleName() + "/" + testMethod}
     */
    @SuppressWarnings("all")
    public static File getTestDataDirectory(final Class<?> testClass, final String testMethod) {

        // Check sanity
        Validate.notNull(testClass, "Cannot handle null testClass argument.");
        Validate.notEmpty(testMethod, "Cannot handle null testMethod argument.");

        final String resourcePath = LifecycleAware.DATA_ROOT_DIRECTORY
                + "/" + testClass.getSimpleName()
                + "/" + testMethod;

        final URL resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        Validate.notNull(resource, "Resource path [" + resourcePath + "] could not be resolved into a directory.");

        final File toReturn = new File(resource.getPath());
        Validate.isTrue(toReturn.exists() && toReturn.isDirectory(), "Resource path [" + resourcePath
                + "] is not an existing directory.");

        // All done.
        return toReturn;
    }
}
