/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.persistence.api;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Basic;
import javax.persistence.Column;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class IncorrectAnnotationlessEntity extends NazgulEntity {

    @Basic
    @Column(nullable = false)
    private String value;

    public IncorrectAnnotationlessEntity() {
    }

    public IncorrectAnnotationlessEntity(final String value) {
        this.value = value;
    }

    /**
     * Override this method to perform validation of the entity internal state of this Validatable.
     * <strong>Note!</strong> The first call within the validateEntityState method should be
     * {@code super.validateEntityState()}.
     *
     * @throws se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException
     *          if the state of this Validatable was in an incorrect
     *          state (i.e. invalid).
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {
    }
}
