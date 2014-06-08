/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-test
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Abstract superclass for persistence tests, containing properly set up
 *
 * @param <D> The type of database Connection used by this AbstractPersistenceTest.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractPersistenceTest<D> {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(AbstractPersistenceTest.class);

    // Internal state
    private static final Object[] LOCK = new Object[0];
    private DatabaseType databaseType;
    private SchemaManager schemaManager;
    private DataManager dataManager;
    private ClassLoader originalClassloader;

    @Rule
    @SuppressWarnings("all")
    public final TestName testName = new TestName();

    /**
     * Creates an AbstractPersistenceTest using the supplied SchemaManager and DataManager classes to
     * generate a SchemaManager and DataManager to manage DB schema and data.
     *
     * @param schemaManagerClass The concrete type of SchemaManager used. Must have a default constructor.
     * @param dataManagerClass   The concrete type of DataManager used. Must have a default constructor.
     */
    protected AbstractPersistenceTest(final Class<? extends SchemaManager> schemaManagerClass,
                                      final Class<? extends DataManager> dataManagerClass) {

        // Check sanity
        Validate.notNull(schemaManagerClass, "Cannot handle null schemaManager argument.");
        Validate.notNull(dataManagerClass, "Cannot handle null dataManager argument.");

        // Make object using default constructors
        schemaManager = createObjectUsingDefaultConstructor(schemaManagerClass);
        dataManager = createObjectUsingDefaultConstructor(dataManagerClass);
    }

    /**
     * Creates an AbstractPersistenceTest wrapping the supplied SchemaManager and DataManager objects.
     *
     * @param schemaManager A non-null SchemaManager instance.
     * @param dataManager   A non-null DataManager instance.
     */
    protected AbstractPersistenceTest(final SchemaManager schemaManager,
                                      final DataManager dataManager) {

        // Check sanity
        Validate.notNull(schemaManager, "Cannot handle null schemaManager argument.");
        Validate.notNull(dataManager, "Cannot handle null dataManager argument.");

        // Assign internal state
        this.schemaManager = schemaManager;
        this.dataManager = dataManager;
    }

    /**
     * Standard setup method, which delegates the setup lifecycle to the {@code onSetup()} method
     * after all of its statements are executed.
     */
    @Before
    @SuppressWarnings("all")
    public final void setupSharedState() {

        // Stash the original ClassLoader
        this.originalClassloader = getClassLoader();
        if (log.isDebugEnabled()) {
            log.debug("Stashed original classloader [" + originalClassloader.getClass().getName() + "]");
        }

        // Initialize the internal state objects.
        final String testMethodName = testName.getMethodName();
        schemaManager.initialize(this, testMethodName, databaseType);
        dataManager.initialize(this, testMethodName, databaseType);

        // Delegate setup to subclasses
        preSetup();

        // Setup schema
        Assert.assertTrue("Could not acquire test method name.", testMethodName != null && testMethodName.length() > 0);
        schemaManager.createSchema(testMethodName);

        // Delegate setup to subclasses
        onSetup();

        // Populate schema with data
        dataManager.insertData(testMethodName);
    }

    /**
     * @return A database Connection (or wrapped connection, such as an EntityManager) used
     * by this AbstractPersistenceTest and its SchemaManager and DataManager.
     */
    public abstract D getDbConnection();

    /**
     * Implement this method to delegate setup to subclasses, rather than create a new @Before-annotated method.
     * (The execution order of @Before methods is unspecified, but this {@code preSetup()} method is invoked from
     * within the {@code setupSharedState()} method, before the database has been populated).
     */
    protected abstract void preSetup();

    /**
     * Implement this method to delegate setup to subclasses, rather than create a new @Before-annotated method.
     * (The execution order of @Before methods is unspecified, but this {@code onSetup()} method is invoked from
     * within the {@code setupSharedState()} method, after the database has been populated).
     */
    protected abstract void onSetup();

    /**
     * @return The database type used within this AbstractPersistenceTest.
     */
    public final DatabaseType getDatabaseType() {
        return this.databaseType;
    }

    /**
     * Standard tear down method, which delegates the setup lifecycle to the {@code onTeardown()} method before
     */
    @After
    @SuppressWarnings("all")
    public final void teardownSharedState() {

        // Delegate pre-teardown to subclasses
        preTeardown();

        // Tear down test data
        final String testMethodName = testName.getMethodName();
        Assert.assertTrue("Could not acquire test method name.", testMethodName != null && testMethodName.length() > 0);
        dataManager.shutdown(this, testMethodName);
        schemaManager.shutdown(this, testMethodName);

        // Delegate post teardown to subclasses.
        onTeardown();

        // Restore the original ClassLoader
        synchronized (LOCK) {
            if (log.isDebugEnabled()) {
                log.debug("Restoring original ClassLoader [" + originalClassloader.getClass().getName()
                        + "] to active thread [" + Thread.currentThread().getName() + "]");
            }
            Thread.currentThread().setContextClassLoader(originalClassloader);
        }
    }

    /**
     * Implement this method - rather than create a new @After-annotated method - to delegate teardown to subclasses.
     * (The ordering of @After methods is unspecified, but this onTeardown method is invoked from within the
     * {@code teardownSharedState()} method, before all its other statements have been executed).
     */
    protected abstract void preTeardown();

    /**
     * Implement this method - rather than create a new @After-annotated method - to delegate teardown to subclasses.
     * (The ordering of @After methods is unspecified, but this onTeardown method is invoked from within the
     * {@code teardownSharedState()} method, after database teardown has been executed).
     */
    protected abstract void onTeardown();

    /**
     * Creates an instance from the supplied class, by invoking the default constructor within the supplied class.
     *
     * @param aClass The non-null Class used to make an object.
     * @param <T>    The type of object which should be returned
     * @return an object, retrieved by invoking the default constructor within the supplied class.
     */
    protected final <T> T createObjectUsingDefaultConstructor(final Class<T> aClass) {

        Validate.notNull(aClass, "Cannot handle null aClass argument.");

        try {
            // Acquire the default constructor in the supplied class
            final Constructor<T> defaultConstructor = aClass.getConstructor(new Class[0]);
            Validate.notNull(defaultConstructor, "Class [" + aClass.getName() + "] must have a default constructor.");

            // All done.
            return defaultConstructor.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate [" + aClass.getName()
                    + "] using a default constructor. (None present?)", e);
        }
    }

    //
    // Private helpers
    //

    private ClassLoader getClassLoader() {

        synchronized (LOCK) {

            ClassLoader toReturn = Thread.currentThread().getContextClassLoader();
            if (toReturn == null) {
                toReturn = getClass().getClassLoader();
            }

            // All done.
            return toReturn;
        }
    }
}
