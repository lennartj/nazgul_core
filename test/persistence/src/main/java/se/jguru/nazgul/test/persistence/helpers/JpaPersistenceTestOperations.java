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
package se.jguru.nazgul.test.persistence.helpers;

import se.jguru.nazgul.core.persistence.api.JpaPersistenceOperations;
import se.jguru.nazgul.test.persistence.PersistenceTestOperations;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * JPA implementation of the PersistenceTestOperations specification, which exposes
 * extra methods to simplify firing custom JPQL queries against a supplied
 * EntityManager instance.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JpaPersistenceTestOperations extends JpaPersistenceOperations implements PersistenceTestOperations {

    /**
     * Creates a new JpaPersistenceTestOperations instance wrapping the supplied EntityManager
     *
     * @param entityManager The entity manager which should be used to carry out
     *                      all JPA test operations.
     */
    public JpaPersistenceTestOperations(final EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Fires an arbitrary JPA Query, returning the results as a List.
     *
     * @param <T>   The type of entity retrieved.
     * @param query The JPA Query to fire.
     * @return The List of resulting Entities.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> fireJpaQuery(final String query) {

        // Fire the provided query.
        final Query jpqlQuery = getEntityManager().createQuery(query);
        return (List<T>) jpqlQuery.getResultList();
    }
}
