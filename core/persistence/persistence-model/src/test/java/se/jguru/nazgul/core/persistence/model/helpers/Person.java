/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-model
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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
package se.jguru.nazgul.core.persistence.model.helpers;

import se.jguru.nazgul.core.persistence.model.NazgulMutableIdEntity;
import se.jguru.nazgul.core.persistence.model.NazgulMutableIdEntityTest;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"firstName", "lastName", "age"})
@XmlAccessorType(XmlAccessType.FIELD)
@Access(value = AccessType.FIELD)
public class Person extends NazgulMutableIdEntity {

    // Internal state
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, namespace = NazgulMutableIdEntityTest.PERSON_NAMESPACE_URL)
    private String firstName;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true)
    private String lastName;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute(required = true)
    private int age;

    /**
     * JPA/JAXB-friendly constructor.
     */
    public Person() {
    }

    /**
     * Convenience constructor.
     *
     * @param firstName The first name of this Person.
     * @param lastName  The last name of this Person.
     * @param age       The age of this Person.
     */
    public Person(@NotNull final String firstName,
                  @NotNull final String lastName,
                  final int age) {
        this(0, firstName, lastName, age);
    }

    /**
     * Compound constructor.
     *
     * @param firstName The first name of this Person.
     * @param lastName  The last name of this Person.
     * @param age       The age of this Person.
     */
    public Person(final long id,
                  @NotNull final String firstName,
                  @NotNull final String lastName,
                  final int age) {

        super(id);

        // Assign internal state
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    /**
     * @return The first name of this Person.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return The last name of this Person.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return The age of this Person.
     */
    public int getAge() {
        return age;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(firstName, "firstName")
                .notNullOrEmpty(lastName, "lastName")
                .notTrue(age < 0, "age < 0")
                .endExpressionAndValidate();
    }
}
