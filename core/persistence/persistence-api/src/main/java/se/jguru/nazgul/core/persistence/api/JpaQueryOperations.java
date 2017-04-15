/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.persistence.api;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;

import java.util.List;
import java.util.Map;

/**
 * Specification for commonly used JPA Query-related operations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface JpaQueryOperations {

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

    /**
     * Fires a JPA NamedQuery, returning the results as a List.
     *
     * @param query      The name of the JPA NamedQuery to fire.
     * @param parameters The parameters for the NamedQuery, assuming that the named query contains
     *                   named parameters matching the key values within the supplied parameters Map.
     * @param <T>        The type of Entity expected.
     * @return The List of resulting Entities.
     * @throws PersistenceOperationException if a JPA-related exception occurred while performing the operation.
     */
    <T extends NazgulEntity> List<T> fireNamedQuery(String query, Map<String, Object> parameters)
            throws PersistenceOperationException;

    /**
     * Fires a JPA NamedQuery, returning the maximum given results as a List.
     *
     * @param query      The name of the JPA NamedQuery to fire.
     * @param parameters The parameters for the NamedQuery.
     * @param maxResults The maximum number of results returned. Must not be a negative number.
     * @param <T>        The type of Entity expected.
     * @return The List of resulting Entities.
     * @throws PersistenceOperationException if a JPA-related exception occurred while performing the operation.
     */
    <T extends NazgulEntity> List<T> fireNamedQueryWithResultLimit(String query,
                                                                   int maxResults,
                                                                   Object... parameters)
            throws PersistenceOperationException;

    /**
     * Fires a JPA NamedQuery, returning the maximum given results as a List.
     *
     * @param query      The name of the JPA NamedQuery to fire.
     * @param parameters The parameters for the NamedQuery, assuming that the named query contains
     *                   named parameters matching the key values within the supplied parameters Map.
     * @param maxResults The maximum number of results returned. Must not be a negative number.
     * @param <T>        The type of Entity expected.
     * @return The List of resulting Entities.
     * @throws PersistenceOperationException if a JPA-related exception occurred while performing the operation.
     */
    <T extends NazgulEntity> List<T> fireNamedQueryWithResultLimit(String query,
                                                                   int maxResults,
                                                                   Map<String, Object> parameters)
            throws PersistenceOperationException;
}
