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
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

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

        try {
            entityManager.persist(entity);
        } catch (Exception e) {
            throw new PersistenceOperationException("Could not persist object of type ["
                    + entity.getClass().getName() + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T extends NazgulEntity> T update(final T entity) throws PersistenceOperationException {

        try {
            return entityManager.merge(entity);
        } catch (Exception e) {
            throw new PersistenceOperationException("Could not update (JPA merge) object of type ["
                    + entity.getClass().getName() + "]", e);
        }
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

        try {
            entityManager.refresh(validateInternalState(entity));
        } catch (Exception e) {

            throw new PersistenceOperationException("Could not refresh ["
                    + entity.getClass().getName() + "] instance.", e);
        }
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

        try {

            // updateAuditDataAndWriteAuditTrailEntity(toDelete);

            // Remove the instance
            entityManager.remove(entity);

        } catch (Exception e) {

            throw new PersistenceOperationException("Could not delete ["
                    + entity.getClass().getName() + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NazgulEntity> T findByPrimaryKey(final Class<T> entityType,
                                                       final Object primaryKey)
            throws PersistenceOperationException {

        try {
            return validateInternalState(entityManager.find(entityType, primaryKey));
        } catch (InternalStateValidationException e) {
            throw new PersistenceOperationException("Found invalid NazgulEntity", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends NazgulEntity> List<T> fireNamedQuery(final String query,
                                                           final Object... parameters)
            throws PersistenceOperationException {

        // Create the query, and assign any parameters provided.
        Query namedQuery = entityManager.createNamedQuery(query);
        setParameters(namedQuery, parameters);

        // Fire the query; no null Lists are returned.
        List<T> toReturn = (List<T>) namedQuery.getResultList();
        for (T current : toReturn) {

            // Validate each NazgulEntity.
            try {
                this.validateInternalState(current);
            } catch (InternalStateValidationException e) {
                throw new PersistenceOperationException("NazgulEntity state validation failed", e);
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * @return The injected EntityManager.
     */
    protected final EntityManager getEntityManager() {
        return entityManager;
    }

    //
    // Private helpers
    //

    private <T extends NazgulEntity> T validateInternalState(final T entity)
            throws InternalStateValidationException {

        // Validate the internal state of this entity.
        if (entity != null) {
            entity.validateInternalState();
        }

        // All went well.
        return entity;
    }

    private void setParameters(final Query query, final Object... params) {

        // Assign all parameters, if any.
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                // Bump the parameter index value with
                // 1, to comply with JDBC...
                query.setParameter((i + 1), params[i]);
            }
        }
    }
}
