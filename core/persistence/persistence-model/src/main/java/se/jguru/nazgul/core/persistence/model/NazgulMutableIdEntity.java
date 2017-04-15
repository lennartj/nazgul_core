/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-model
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

package se.jguru.nazgul.core.persistence.model;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Abstract superclass for Entities where a need exists to alter the ID of a NazgulEntity after the required
 * construction. This is typically the case when needing synthesized/generated identifiers or when the entity
 * in question does not correspond to tables - such as when creating JPA Entities that wraps rows from dynamic
 * views instead of tables.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see se.jguru.nazgul.core.persistence.model.NazgulEntity
 */
@MappedSuperclass
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
@Access(value = AccessType.FIELD)
public abstract class NazgulMutableIdEntity extends NazgulEntity {

    // Internal state
    private static final long serialVersionUID = 8829990012L;

    /**
     * JAXB / JPA-friendly constructor.
     */
    public NazgulMutableIdEntity() {
    }

    /**
     * Convenience constructor to assign the ID/primary key value of this NazgulViewEntity.
     *
     * @param id The id to assign.
     */
    public NazgulMutableIdEntity(final long id) {
        this();

        // Delegate
        setId(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(final long id) {
        super.setId(id);
    }
}
