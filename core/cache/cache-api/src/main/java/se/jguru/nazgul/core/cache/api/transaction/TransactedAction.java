/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
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
