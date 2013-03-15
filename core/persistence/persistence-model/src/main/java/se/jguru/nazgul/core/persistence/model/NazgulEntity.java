/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-model
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
package se.jguru.nazgul.core.persistence.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.annotation.PostConstruct;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Abstract superclass of all Nazgul-style Entity classes, holding commonly used
 * mechanics for equality, cloning and validation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"version"})
@XmlAccessorType(XmlAccessType.FIELD)
@Access(value = AccessType.FIELD)
public abstract class NazgulEntity implements Serializable, Cloneable, Validatable {

    // Internal state
    private static final long serialVersionUID = 8829990002L;
    private static final String[] EQUALITY_DISREGARD_FIELDS = {
            "version",
            "id",
            "pcVersionInit",
            "pcStateManager",
            "pcDetachedState"};

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @XmlTransient
    private int id;

    @Version
    @XmlAttribute(required = false)
    private int version;

    /**
     * JAXB / JPA-friendly constructor.
     */
    public NazgulEntity() {
    }

    /**
     * @return the primary key value (i.e. Database-generated ID) of this NazgulEntity.
     */
    public final int getId() {
        return id;
    }

    /**
     * @return the Database-generated version/revision of this NazgulEntity.
     */
    public int getVersion() {
        return version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false, NazgulEntity.class, EQUALITY_DISREGARD_FIELDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(17, 37, this, false, NazgulEntity.class, EQUALITY_DISREGARD_FIELDS);
    }

    /**
     * Convenience clone method, which does not require the caller to handle CloneNotSupportedException.
     *
     * @param <T> The exact subtype of NazgulEntity used.
     * @return A clone of this instance, or an IllegalStateException.
     * @throws IllegalStateException if cloning this instance failed with a {@code CloneNotSupportedException}.
     */
    @SuppressWarnings("unchecked")
    public final <T extends NazgulEntity> T copy() throws IllegalStateException {

        try {
            return (T) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Could not clone [" + getClass().getName() + "] instance.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {

        final NazgulEntity clone = (NazgulEntity) super.clone();
        clone.version = this.version;

        return clone;
    }

    /**
     * Performs validation of the internal state of this Validatable.
     *
     * @throws InternalStateValidationException
     *          if the state of this Validatable was in an incorrect
     *          state (i.e. invalid).
     */
    @PostConstruct
    @PrePersist
    @Override
    public final void validateInternalState() throws InternalStateValidationException {

        // Delegate
        validateEntityState();
    }

    /**
     * Override this method to perform validation of the entity internal state of this Validatable.
     * <strong>Note!</strong> The first call within the validateEntityState method should be
     * {@code super.validateEntityState()}.
     *
     * @throws InternalStateValidationException
     *          if the state of this Validatable was in an incorrect
     *          state (i.e. invalid).
     */
    protected abstract void validateEntityState() throws InternalStateValidationException;
}
