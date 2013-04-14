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
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
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
 * Abstract implementation of the TransactedAction specification, providing
 * all implementation userdetails with the exception of
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractTransactedAction implements TransactedAction {

    // Internal state
    private String rollbackErrorMessage;

    /**
     * Creates a new AbstractTransactedAction with the provided message
     * to be logged on rollback / transaction failure.
     *
     * @param rollbackErrorMessage An exception message logged if the TransactedAction failed.
     */
    protected AbstractTransactedAction(final String rollbackErrorMessage) {
        this.rollbackErrorMessage = rollbackErrorMessage;
    }

    /**
     * @return An exception message logged if the TransactedAction failed.
     */
    public String getRollbackErrorDescription() {
        return rollbackErrorMessage;
    }

    /**
     * Callback method to be implemented if some custom state cleanup
     * should be done on transactional rollback.
     */
    public void onRollback() {
        // Override to provide custom actions on rollback.
    }
}
