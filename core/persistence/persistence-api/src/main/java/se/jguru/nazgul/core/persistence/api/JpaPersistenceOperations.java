/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
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

package se.jguru.nazgul.core.persistence.api;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.persistence.api.helpers.JpaOperations;
import se.jguru.nazgul.core.persistence.api.helpers.QueryOperations;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

/**
 * Generic JPA implementation of the PersistenceOperations interface,
 * delegating all operations to a wrapped EntityManager.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JpaPersistenceOperations implements PersistenceOperations {

    // Internal state
    private EntityManager entityManager;

    /**
     * Creates a new JpaPersistenceOperations instance delegating all operations
     * to the provided EntityManager instance.
     *
     * @param entityManager The entity manager which should be used to carry out
     *                      all Jpa Persistence operations.
     */
    public JpaPersistenceOperations(final EntityManager entityManager) {

        Validate.notNull(entityManager, "Cannot handle null entityManager instance.");
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T extends NazgulEntity> void create(final T entity) throws PersistenceOperationException {

        // Don't persist nulls.
        if (entity == null) {
            return;
        }

        // Delegate
        JpaOperations.create(entity, entityManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T extends NazgulEntity> T update(final T entity) throws PersistenceOperationException {
        return JpaOperations.update(entity, entityManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T extends NazgulEntity> void refresh(final T entity) throws PersistenceOperationException {

        // Ignore nulls.
        if (entity == null) {
            return;
        }

        // Delegate
        JpaOperations.refresh(entity, entityManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T extends NazgulEntity> void delete(final T entity) throws PersistenceOperationException {

        // Ignore nulls.
        if (entity == null) {
            return;
        }

        // Delegate
        JpaOperations.delete(entity, entityManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NazgulEntity> T findByPrimaryKey(final Class<T> entityType,
                                                       final Object primaryKey)
            throws PersistenceOperationException {

        // Delegate
        return QueryOperations.findByPrimaryKey(entityType, entityManager, primaryKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NazgulEntity> List<T> fireNamedQuery(final String query,
                                                           final Object... parameters)
            throws PersistenceOperationException {

        // Delegate and return
        return QueryOperations.fireNamedQuery(query, entityManager, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NazgulEntity> List<T> fireNamedQueryWithResultLimit(final String query,
                                                                          final int maxResults,
                                                                          final Object... parameters)
            throws PersistenceOperationException {

        // Delegate
        return QueryOperations.fireNamedQueryWithResultLimit(query, entityManager, maxResults, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NazgulEntity> List<T> fireNamedQuery(final String query, final Map<String, Object> parameters)
            throws PersistenceOperationException {

        // Delegate
        // return fireNamedQueryWithResultLimit(query, Integer.MIN_VALUE, parameters);
        return QueryOperations.fireNamedQuery(query, entityManager, Integer.MIN_VALUE, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NazgulEntity> List<T> fireNamedQueryWithResultLimit(final String query,
                                                                          final int maxResults,
                                                                          final Map<String, Object> parameters)
            throws PersistenceOperationException {

        // Delegate and return
        return QueryOperations.fireNamedQuery(query, entityManager, maxResults, parameters);
    }

    /**
     * @return The injected EntityManager.
     */
    protected final EntityManager getEntityManager() {
        return entityManager;
    }
}
