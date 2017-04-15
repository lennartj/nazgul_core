/*
 * #%L
 * Nazgul Project: nazgul-core-clustering-api
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
package se.jguru.nazgul.core.clustering.api;

import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

/**
 * Abstract Clusterable implementation, with empty bodies and default behaviour.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "http://www.jguru.se/nazgul/core", propOrder = {"id"})
@XmlAccessorType(XmlAccessType.FIELD)
@MappedSuperclass
@SuppressWarnings("ValidExternallyBoundObject")
public abstract class AbstractClusterable implements Clusterable {

    // Internal state
    String id;

    /**
     * The IdGenerator of this AbstractClusterable. It may/should be null after providing an ID.
     */
    protected IdGenerator idGenerator;

    /**
     * Creates a new AbstractIdentifiable and assigns the internal ID state.
     *
     * @param idGenerator The ID generator used to acquire a cluster-unique identifier for this AbstractClusterable
     *                    instance. If a {@code null} IdGenerator is supplied, the ID will be calculated using a
     *                    {@code UUID.randomUUID.toString()} call.
     */
    protected AbstractClusterable(final IdGenerator idGenerator) {

        // Check sanity and assign internal state
        if (idGenerator == null) {
            this.id = UUID.randomUUID().toString();
        } else if (idGenerator.isIdentifierAvailable()) {
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
    protected AbstractClusterable(@NotNull @Size(min = 1) final String clusterUniqueID) {

        Validate.notEmpty(clusterUniqueID, "clusterUniqueID");
        this.id = clusterUniqueID;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getClusterId() {

        // Return fast if possible
        if (id != null) {
            return id;
        }

        // Create the ID if not present
        if (idGenerator != null && idGenerator.isIdentifierAvailable()) {
            id = idGenerator.getIdentifier();

            // No need for the IdGenerator anymore.
            this.idGenerator = null;
        }
        
        // This should not happen.
        final String idGeneratorType = idGenerator == null ? "<none>" : idGenerator.getClass().getSimpleName();
        throw new IllegalStateException("Cannot acquire ID; idGenerator [" + idGeneratorType + "] cannot generate ID.");
    }

    /**
     * @return a debug string representation of this AbstractClusterable.
     */
    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + "::" + getClusterId() + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Fail fast
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractClusterable)) {
            return false;
        }

        // Delegate to internal state
        final AbstractClusterable that = (AbstractClusterable) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
