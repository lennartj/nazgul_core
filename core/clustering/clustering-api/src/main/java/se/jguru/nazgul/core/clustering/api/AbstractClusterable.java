/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.clustering.api;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

/**
 * Abstract Clusterable implementation, with empty bodies and default behaviour.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"id"})
@XmlAccessorType(XmlAccessType.FIELD)
@MappedSuperclass
public abstract class AbstractClusterable implements Clusterable {

    // Internal state
    private IdGenerator idGenerator;
    String id;

    /**
     * Creates a new AbstractIdentifiable and assigns the internal ID state.
     *
     * @param idGenerator The ID generator used to acquire a cluster-unique
     *                    identifier for this AbstractClusterable instance.
     */
    protected AbstractClusterable(final IdGenerator idGenerator) {

        // Check sanity and assign internal state
        if(idGenerator == null) {
            this.id = UUID.randomUUID().toString();
        } else if(idGenerator.isIdentifierAvailable()) {
            this.id = idGenerator.getIdentifier();
        } else {
            this.idGenerator = idGenerator;
        }
    }

    /**
     * Creates a new AbstractIdentifiable and assigns the provided
     * cluster-unique ID to this AbstractClusterable instance.
     *
     * @param clusterUniqueID A cluster-unique Identifier.
     */
    protected AbstractClusterable(final String clusterUniqueID) {

        Validate.notEmpty(clusterUniqueID, "Cannot handle null or empty clusterUniqueID argument.");
        this.id = clusterUniqueID;
    }

    /**
     * @return an Identifier, unique within the cluster.
     */
    @Override
    public String getClusterId() {

        if(id == null && idGenerator != null) {
            if(idGenerator.isIdentifierAvailable()) {
                id = idGenerator.getIdentifier();
            }
        }

        if(id != null) {
            return id;
        }

        // This should not happen.
        final String idGeneratorType = idGenerator == null ? "<none>" : idGenerator.getClass().getSimpleName();
        throw new IllegalStateException("Cannot acquire ID; idGenerator [" + idGeneratorType + "] cannot generate ID.");
    }

    /**
     * Retrieves the assigned IdGenerator, or {@code null} if none is assigned.
     *
     * @return the assigned IdGenerator, or {@code null} if none is assigned.
     */
    protected final IdGenerator getIdGenerator() {
        return idGenerator;
    }

    /**
     * @return a debug string representation of this AbstractClusterable.
     */
    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + "::" + getClusterId() + "]";
    }
}
