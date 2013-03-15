/*
 * #%L
 * Nazgul Project: nazgul-core-cache-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.nazgul.core.cache.api.transaction;

/**
 * Specification of a Transacted action command to be performed within the Cache.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface TransactedAction {

    /**
     * Defines a method invoked within a Transactional
     * boundary, using the Cache as context.
     *
     * @throws RuntimeException if the implementation needs to signal
     *                          a rollback to the Cache Transaction manager.
     */
    void doInTransaction() throws RuntimeException;

    /**
     * @return An exception message logged if the TransactedAction failed.
     */
    String getRollbackErrorDescription();
}
