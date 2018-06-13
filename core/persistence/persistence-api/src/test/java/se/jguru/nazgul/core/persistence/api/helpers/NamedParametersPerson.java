/*-
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.core.persistence.api.helpers;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = "getPersonsByFirstName",
                query = "select a from NamedParametersPerson a where a.firstName like :firstName order by a.firstName"),
        @NamedQuery(name = "getPersonsByAge",
                query = "select a from NamedParametersPerson a where a.age = :age order by a.firstName")})
@Entity
public class NamedParametersPerson extends NazgulEntity {

    @Basic
    @Column(nullable = false)
    private String firstName;

    @Basic
    @Column(nullable = false)
    private Integer age;

    public NamedParametersPerson() {
    }

    public NamedParametersPerson(final String firstName,
                                 final int age) {

        this.firstName = firstName;
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

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
                .notTrue(age <= 0, "age <= 0")
                .endExpressionAndValidate();
    }
}
