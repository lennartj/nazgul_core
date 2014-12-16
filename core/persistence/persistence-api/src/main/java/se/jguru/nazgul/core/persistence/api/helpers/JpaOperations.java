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
package se.jguru.nazgul.core.persistence.api.helpers;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.persistence.api.PersistenceOperationException;
import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;

/**
 * Utility methods to simplify JPA usage and increase its usability.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class JpaOperations {

    /**
     * Hide the constructor for a utility class.
     */
    private JpaOperations() {
    }

    /**
     * Persists the provided entity within a database to which the
     * supplied EntityManager is connected.
     *
     * @param entity        The entity to persist. Cannot be {@code null}.
     * @param entityManager The active EntityManager. Cannot be {@code null}.
     * @param <T>           The entity type.
     * @throws PersistenceOperationException if the entity could not be created.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public static <T> void create(final T entity, final EntityManager entityManager)
            throws PersistenceOperationException {

        // Check sanity
        Validate.notNull(entity, "Cannot handle null entity argument.");
        Validate.notNull(entityManager, "Cannot handle null entityManager argument.");

        // Persist the entity
        try {
            entityManager.persist(entity);
        } catch (Exception e) {
            throw new PersistenceOperationException("Could not persist object of type ["
                    + entity.getClass().getName() + "]", e);
        }
    }

    /**
     * Updates the provided entity by merging its state into an existing
     * entity within a JPA-aware entity.
     *
     * @param entity        The entity to update / merge. Cannot be {@code null}.
     * @param entityManager The active EntityManager. Cannot be {@code null}.
     * @param <T>           The entity type.
     * @return The updated entity, to be used instead of the supplied argument entity.
     * @throws PersistenceOperationException if the entity could not be updated.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public static <T> T update(final T entity, final EntityManager entityManager)
            throws PersistenceOperationException {

        // Check sanity
        Validate.notNull(entity, "Cannot handle null entity argument.");
        Validate.notNull(entityManager, "Cannot handle null entityManager argument.");

        try {

            T toReturn = entity;

            // Merge the T instance, if required
            if (!entityManager.contains(entity)) {
                toReturn = entityManager.merge(entity);
            }

            // All done.
            return toReturn;

        } catch (Exception e) {
            throw new PersistenceOperationException("Could not update (JPA merge) object of type ["
                    + entity.getClass().getName() + "]", e);
        }
    }

    /**
     * Replaces the state of the provided NazgulEntity with the database current state.
     *
     * @param entity The entity to refresh.
     * @param <T>    The entity type.
     * @throws PersistenceOperationException if the entity could not be refreshed.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public static <T> void refresh(final T entity, final EntityManager entityManager)
            throws PersistenceOperationException {

        // Check sanity
        Validate.notNull(entity, "Cannot handle null entity argument.");
        Validate.notNull(entityManager, "Cannot handle null entityManager argument.");

        try {

            // Refresh first
            entityManager.refresh(entity);

            // Validate, as required
            if (entity instanceof Validatable) {
                validateInternalState((Validatable) entity);
            }
        } catch (Exception e) {

            throw new PersistenceOperationException("Could not refresh ["
                    + entity.getClass().getName() + "] instance.", e);
        }
    }

    /**
     * Deletes the persistent state of the provided entity from a JPA-aware database.
     *
     * @param entity The entity to delete.
     * @param <T>    The entity type.
     * @throws PersistenceOperationException if the entity could not be deleted.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public static <T> void delete(final T entity, final EntityManager entityManager)
            throws PersistenceOperationException {

        // Check sanity
        Validate.notNull(entity, "Cannot handle null entity argument.");
        Validate.notNull(entityManager, "Cannot handle null entityManager argument.");

        try {
            // Remove the instance
            entityManager.remove(entity);
        } catch (Exception e) {
            throw new PersistenceOperationException("Could not delete ["
                    + entity.getClass().getName() + "]", e);
        }
    }

    /**
     * Validates the internal state of the supplied entity, implying that the
     * {@code validateInternalState} of the supplied NazgulEntity subclass
     * should be invoked.
     *
     * @param entity The entity to validate.
     * @param <T>    The NazgulEntity subtype.
     * @return The entity object supplied as argument.
     * @throws InternalStateValidationException If the NazgulEntity failed validation.
     */
    public static <T extends Validatable> T validateInternalState(final T entity)
            throws InternalStateValidationException {

        // Validate the internal state of this entity.
        if (entity != null) {
            entity.validateInternalState();
        }

        // All went well.
        return entity;
    }
}
