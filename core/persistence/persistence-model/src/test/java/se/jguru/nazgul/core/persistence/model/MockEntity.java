/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.persistence.model;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"name", "value"})
@XmlAccessorType(XmlAccessType.FIELD)
@Access(value = AccessType.FIELD)
public class MockEntity extends NazgulEntity {

    // Internal state
    @XmlElement(nillable = false, required = true)
    private String name;

    @XmlAttribute(required = true)
    @Column(nullable = false)
    private Long value;

    @XmlTransient
    public ArrayList<String> callTrace = new ArrayList<String>();

    public MockEntity() {
    }

    public MockEntity(String name, Long value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Long getValue() {
        return value;
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

        callTrace.add("validateEntityState [" + getName() + ", " + getValue() + "]");

        InternalStateValidationException.create()
                .notNullOrEmpty(name, "name")
                .notNull(value, "value")
                .endExpressionAndValidate();
    }
}
