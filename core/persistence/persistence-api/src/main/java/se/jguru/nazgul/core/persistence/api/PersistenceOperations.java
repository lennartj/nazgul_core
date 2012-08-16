/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.persistence.api;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;

import java.util.List;

/**
 * Specification for commonly used Persistence and JPA operations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface PersistenceOperations {

    /**
     * Creates the provided entity and persists it within a JPA-aware database.
     *
     * @param entity The entity to persist.
     * @throws PersistenceOperationException if the entity could not be created.
     */
    void create(NazgulEntity entity) throws PersistenceOperationException;

    /**
     * Updates the provided entity by merging its state into an existing
     * entity within a JPA-aware entity.
     *
     * @param entity The entity to update / merge.
     * @throws PersistenceOperationException if the entity could not be updated.
     */
    void update(NazgulEntity entity) throws PersistenceOperationException;

    /**
     * Replaces the state of the provided NazgulEntity with the database current state.
     *
     * @param entity The entity to refresh.
     * @throws PersistenceOperationException if the entity could not be refreshed.
     */
    void refresh(NazgulEntity entity) throws PersistenceOperationException;

    /**
     * Deletes the persistent state of the provided entity from a JPA-aware database.
     *
     * @param toDelete The entity to delete.
     * @throws PersistenceOperationException if the entity could not be deleted.
     */
    void delete(NazgulEntity toDelete) throws PersistenceOperationException;

    /**
     * Acquires a T-type Entity using the provided primary key.
     *
     * @param entityType The type of Entity expected.
     * @param primaryKey The primary key used to acquire the Entity.
     * @param <T>        The type of Entity expected.
     * @return The Entity of type T with the provided primaryKey.
     * @throws PersistenceOperationException if a JPA-related exception occurred while performing the operation.
     */
    <T extends NazgulEntity> T findByPrimaryKey(Class<T> entityType, Object primaryKey)
            throws PersistenceOperationException;

    /**
     * Fires a JPA NamedQuery, returning the results as a List.
     *
     * @param query      The name of the JPA NamedQuery to fire.
     * @param parameters The parameters for the NamedQuery.
     * @param <T>        The type of Entity expected.
     * @return The List of resulting Entities.
     * @throws PersistenceOperationException if a JPA-related exception occurred while performing the operation.
     */
    <T extends NazgulEntity> List<T> fireNamedQuery(String query, Object... parameters)
            throws PersistenceOperationException;
}
