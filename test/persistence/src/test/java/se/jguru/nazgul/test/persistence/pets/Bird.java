/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-test
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
package se.jguru.nazgul.test.persistence.pets;

import org.apache.commons.lang3.Validate;
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
@Entity
@NamedQueries({
        @NamedQuery(
                name = Bird.GET_BIRDS_BY_NAMED_CATEGORY,
                query = "select b from Bird b where b.category like :category order by b.name"),
        @NamedQuery(
                name = Bird.GET_BIRDS_BY_CATEGORY,
                query = "select b from Bird b where b.category like ?1 order by b.name"),
        @NamedQuery(
                name = Bird.GET_ALL_BIRDS,
                query = "select b from Bird b")
})
public class Bird extends NazgulEntity {

    public static final String GET_BIRDS_BY_NAMED_CATEGORY = "getBirdsByNamedCategory";
    public static final String GET_BIRDS_BY_CATEGORY = "getBirdsByCategory";
    public static final String GET_ALL_BIRDS = "getAllBirds";

    @Basic(optional = false)
    @Column(nullable = false, length = 64)
    private String name;

    @Basic(optional = false)
    @Column(nullable = false, length = 128)
    private String category;

    public Bird() {
    }

    public Bird(final String name, final String category) {
        super();
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(final String newName) {

        Validate.notEmpty(newName, "Cannot handle null or empty newName argument.");
        this.name = newName;
    }

    public String getCategory() {
        return category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(name, "name")
                .notNullOrEmpty(category, "category")
                .endExpressionAndValidate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Bird: [" + getName() + ", " + getCategory() + "]";
    }
}
