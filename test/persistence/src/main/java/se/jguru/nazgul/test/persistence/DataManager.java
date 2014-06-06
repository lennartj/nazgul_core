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

/**
 * Specification for how to populate database tables (in an existing schema),
 * or other database objects (such as indexes, sequences etc).
 *
 * @param <T> The explicit AbstractPersistenceTest subclass for which this DataManager should operate.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface DataManager<T extends AbstractPersistenceTest> extends LifecycleAware<T> {

    /**
     * Inserts all data identified by the supplied, non-empty, identifier.
     *
     * @param dataIdentifier A non-null identifier for test data.
     * @throws IllegalStateException if inserting the data represented by the given identifier could not be
     *                               performed properly.
     */
    void insertData(final String dataIdentifier) throws IllegalStateException;
}
