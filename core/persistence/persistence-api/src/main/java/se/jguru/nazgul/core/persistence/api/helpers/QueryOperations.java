/*-
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.core.persistence.api.PersistenceOperationException;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * Utility methods to simplify JPA Query usage and increase its usability.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class QueryOperations {

    /**
     * Hide constructor for utility classes.
     */
    private QueryOperations() {
        // Do nothing
    }

    /**
     * Acquires a T-type Entity using the provided primary key.
     *
     * @param entityType    The type of Entity expected.
     * @param entityManager The active EntityManager.
     * @param primaryKey    The primary key used to acquire the Entity.
     * @param <T>           The type of Entity expected.
     * @return The Entity of type T with the provided primaryKey.
     * @throws PersistenceOperationException if a JPA-related exception occurred while performing the operation.
     */
    public static <T extends NazgulEntity> T findByPrimaryKey(final Class<T> entityType,
                                                              final EntityManager entityManager,
                                                              final Object primaryKey)
            throws PersistenceOperationException {

        try {
            return JpaOperations.validateInternalState(entityManager.find(entityType, primaryKey));
        } catch (InternalStateValidationException e) {
            throw new PersistenceOperationException("Found invalid NazgulEntity", e);
        }
    }

    /**
     * Fires a JPA NamedQuery, returning the results as a List.
     *
     * @param query         The name of the JPA NamedQuery to fire.
     * @param entityManager The active EntityManager.
     * @param parameters    The parameters for the NamedQuery.
     * @param <T>           The type of Entity expected.
     * @return The List of resulting Entities.
     * @throws PersistenceOperationException if a JPA-related exception occurred while performing the operation.
     */
    public static <T extends NazgulEntity> List<T> fireNamedQuery(final String query,
                                                                  final EntityManager entityManager,
                                                                  final Object... parameters)
            throws PersistenceOperationException {

        // Delegate and return
        return fireNamedQueryWithResultLimit(query, entityManager, Integer.MIN_VALUE, parameters);
    }

    /**
     * Fires a JPA NamedQuery, returning the results as a List.
     *
     * @param query         The name of the JPA NamedQuery to fire.
     * @param entityManager The active EntityManager. Cannot be {@code null}.
     * @param parameters    The parameters for the NamedQuery, assuming that the named query contains
     *                      named parameters matching the key values within the supplied parameters Map.
     * @param <T>           The type of Entity expected.
     * @return The List of resulting Entities.
     * @throws PersistenceOperationException if a JPA-related exception occurred while performing the operation.
     */
    public static <T extends NazgulEntity> List<T> fireNamedQuery(final String query,
                                                                  final EntityManager entityManager,
                                                                  final Map<String, Object> parameters)
            throws PersistenceOperationException {

        // Delegate
        return fireNamedQuery(query, entityManager, Integer.MIN_VALUE, parameters);
    }

    /**
     * Fires a JPA NamedQuery, returning the maximum given results as a List.
     *
     * @param query         The name of the JPA NamedQuery to fire.
     * @param entityManager The active EntityManager. Cannot be {@code null}.
     * @param parameters    The parameters for the NamedQuery.
     * @param maxResults    The maximum number of results returned. Must not be a negative number.
     * @param <T>           The type of Entity expected.
     * @return The List of resulting Entities.
     * @throws PersistenceOperationException if a JPA-related exception occurred while performing the operation.
     */
    @SuppressWarnings("unchecked")
    public static <T extends NazgulEntity> List<T> fireNamedQueryWithResultLimit(final String query,
                                                                                 final EntityManager entityManager,
                                                                                 final int maxResults,
                                                                                 final Object... parameters)
            throws PersistenceOperationException {

        // Check sanity
        final Query namedQuery = getQuery(query, entityManager, maxResults);
        setParameters(namedQuery, parameters);

        // All done.
        return fireQuery(namedQuery);
    }

    /**
     * Fires a JPA NamedQuery, returning the maximum given results as a List.
     *
     * @param query         The name of the JPA NamedQuery to fire.
     * @param entityManager The active EntityManager. Cannot be {@code null}.
     * @param parameters    The parameters for the NamedQuery, assuming that the named query contains
     *                      named parameters matching the key values within the supplied parameters Map.
     * @param maxResults    The maximum number of results returned. Must not be a negative number.
     * @param <T>           The type of Entity expected.
     * @return The List of resulting Entities.
     * @throws PersistenceOperationException if a JPA-related exception occurred while performing the operation.
     */
    public static <T extends NazgulEntity> List<T> fireNamedQuery(final String query,
                                                                  final EntityManager entityManager,
                                                                  final int maxResults,
                                                                  final Map<String, Object> parameters)
            throws PersistenceOperationException {

        final Query namedQuery = getQuery(query, entityManager, maxResults);

        // Assign the supplied parameters.
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            namedQuery.setParameter(entry.getKey(), entry.getValue());
        }

        // Fire the query; no null Lists are returned.
        return fireQuery(namedQuery);
    }

    /**
     * Assigns the supplied indexed JPQL parameters to the supplied query.
     *
     * @param query  The Query whose indexed parameters should be set.
     * @param params The parameters to set.
     */
    public static void setParameters(final Query query,
                                     final Object... params) {

        // Assign all parameters, if any.
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                // Bump the parameter index value with
                // 1, to comply with JDBC...
                query.setParameter((i + 1), params[i]);
            }
        }
    }

    //
    // Private helpers
    //

    /**
     * Retrieves a JPA Query from the supplied EntityManager, sporting the supplied maxResults.
     *
     * @param query         The JPA NamedQuery to fire.
     * @param entityManager The active EntityManager. Cannot be {@code null}.
     * @param maxResults    Either a positive integer, or {@code Integer.MIN_VALUE} to indicate that the max result
     *                      value should not be set.
     * @return The JPA NamedQuery with the supplied name.
     */
    private static Query getQuery(@NotNull @Size(min = 1) final String query,
                                  @NotNull final EntityManager entityManager,
                                  final int maxResults) {

        // Check sanity
        Validate.notEmpty(query, "query");
        Validate.notNull(entityManager, "entityManager");

        // Create the query, and handle the amount of results returned.
        Query namedQuery = entityManager.createNamedQuery(query);
        if (maxResults != Integer.MIN_VALUE) {
            namedQuery.setMaxResults(maxResults);
        }

        // All done.
        return namedQuery;
    }

    @SuppressWarnings("unchecked")
    private static <T extends NazgulEntity> List<T> fireQuery(final Query namedQuery) {

        // Fire the query; no null Lists are returned.
        List<T> toReturn = (List<T>) namedQuery.getResultList();
        for (T current : toReturn) {

            // Validate each NazgulEntity.
            try {
                JpaOperations.validateInternalState(current);
            } catch (InternalStateValidationException e) {
                throw new PersistenceOperationException("NazgulEntity state validation failed", e);
            }
        }

        // All done.
        return toReturn;
    }
}
