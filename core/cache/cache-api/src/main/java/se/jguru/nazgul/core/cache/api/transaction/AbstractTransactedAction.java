/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
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
