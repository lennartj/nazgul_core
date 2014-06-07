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
 * Specification for how to set up and tear down a database structure.
 *
 * @param <T> The explicit AbstractPersistenceTest subclass for which this SchemaManager should operate.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface SchemaManager<T extends AbstractPersistenceTest> extends LifecycleAware<T> {

    /**
     * Creates a schema using the supplied specification, or dies trying.
     *
     * @param schemaSpecification A specification for the Schema to be created. If used by the
     *                            AbstractPersistenceTest, this parameter will be assigned the currently running test
     *                            method name.
     * @throws IllegalStateException If the schema could not be created as per the given specification.
     */
    void createSchema(final String schemaSpecification) throws IllegalStateException;
}
