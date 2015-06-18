/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

import se.jguru.nazgul.core.persistence.model.NazgulEntity;

/**
 * Specification for commonly used Persistence operations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface PersistenceOperations extends JpaQueryOperations {

    /**
     * Creates the provided entity and persists it within a JPA-aware database.
     *
     * @param entity The entity to persist.
     * @param <T>    The entity type.
     * @throws PersistenceOperationException if the entity could not be created.
     */
    <T extends NazgulEntity> void create(T entity) throws PersistenceOperationException;

    /**
     * Updates the provided entity by merging its state into an existing
     * entity within a JPA-aware entity.
     *
     * @param entity The entity to update / merge.
     * @param <T>    The entity type.
     * @return The updated entity, which should be used instead of the entity argument submitted.
     * @throws PersistenceOperationException if the entity could not be updated.
     */
    <T extends NazgulEntity> T update(T entity) throws PersistenceOperationException;

    /**
     * Replaces the state of the provided NazgulEntity with the database current state.
     *
     * @param entity The entity to refresh.
     * @param <T>    The entity type.
     * @throws PersistenceOperationException if the entity could not be refreshed.
     */
    <T extends NazgulEntity> void refresh(T entity) throws PersistenceOperationException;

    /**
     * Deletes the persistent state of the provided entity from a JPA-aware database.
     *
     * @param toDelete The entity to delete.
     * @param <T>      The entity type.
     * @throws PersistenceOperationException if the entity could not be deleted.
     */
    <T extends NazgulEntity> void delete(T toDelete) throws PersistenceOperationException;
}
