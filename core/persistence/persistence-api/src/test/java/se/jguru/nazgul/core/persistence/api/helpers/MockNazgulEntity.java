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
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({

        @NamedQuery(name = "getAllMockEntities", query = "select a from MockNazgulEntity a order by a.id"),
        @NamedQuery(name = "getMockEntityByName", query = "select a from MockNazgulEntity a where a.value like ?1 "
                + "order by a.value")})
@Entity
public class MockNazgulEntity extends NazgulEntity {

    // Constants
    private static final long serialVersionUID = 8829990019L;

    @Transient
    public List<String> callTrace = new ArrayList<String>();

    public transient boolean throwValidationException = false;

    @Basic
    @Column(nullable = false)
    private String value;

    public MockNazgulEntity() {
    }

    public MockNazgulEntity(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ID: " + getId() + ", Version: " + getVersion() + ", Value: " + getValue();
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Override this method to perform validation of the entity internal state of this Validatable.
     * <strong>Note!</strong> The first call within the validateEntityState method should be
     * {@code super.validateEntityState()}.
     *
     * @throws se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException if the state of this Validatable was in an incorrect
     *                                                                                         state (i.e. invalid).
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {
        callTrace.add("validateEntityState");

        if (throwValidationException) {
            InternalStateValidationException.create()
                    .addDescription("Throwing ISV as instructed.")
                    .endExpressionAndValidate();
        }
    }
}
