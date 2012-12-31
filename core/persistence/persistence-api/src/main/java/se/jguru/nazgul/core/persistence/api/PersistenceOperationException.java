/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.persistence.api;

/**
 * Exception indicating problems with a persistence operation, as
 * used through the PersistenceOperations interface.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PersistenceOperationException extends RuntimeException {

    /**
     * Constructs a new PersistenceOperationException with the specified
     * (mandatory) detail message and cause.
     * <p/>
     * <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public PersistenceOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
