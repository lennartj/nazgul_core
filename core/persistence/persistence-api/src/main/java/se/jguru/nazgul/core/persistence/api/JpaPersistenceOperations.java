/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
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
