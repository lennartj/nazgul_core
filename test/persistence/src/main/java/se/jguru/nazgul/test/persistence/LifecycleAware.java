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

/**
 * Trivial Lifecycle specification for managed objects.
 *
 * @param <T> The explicit AbstractPersistenceTest subclass.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface LifecycleAware<T extends AbstractPersistenceTest> {

    /**
     * The name of the root folder where test data is found.
     */
    String DATA_ROOT_DIRECTORY = "testdata";

    /**
     * Initializes this LifecycleAware instance.
     *
     * @param testClass      The currently executing AbstractPersistenceTest (subclass).
     * @param testMethodName The name of the currently executing test method.
     * @param databaseType   The type of database used by this LifecycleAware.
     */
    void initialize(T testClass, String testMethodName, final DatabaseType databaseType);

    /**
     * Shuts this LifecycleAware instance down, cleaning up any state which was set up in the initialize method.
     *
     * @param testClass      The currently executing AbstractPersistenceTest (subclass).
     * @param testMethodName The name of the currently executing test method.
     */
    void shutdown(T testClass, String testMethodName);
}
