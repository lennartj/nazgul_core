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

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@NamedQueries({
        @NamedQuery(
                name = Seed.GET_SEEDS_BY_CATEGORY,
                query = "select s from Seed s where s.category like ?1 order by s.name"),
        @NamedQuery(
                name = Seed.GET_SEEDS_BY_BIRD_NAME,
                query = "select s from Seed s join s.eatenBy b where b.name like ?1 order by s.name"),
})
public class Seed extends NazgulEntity {

    public static final String GET_SEEDS_BY_CATEGORY = "getSeedsByCategory";
    public static final String GET_SEEDS_BY_BIRD_NAME = "getSeedsByBirdName";

    @Basic(optional = false)
    @Column(nullable = false, length = 64)
    private String name;

    @Basic(optional = false)
    @Column(nullable = false, length = 128)
    private String category;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Bird> eatenBy;

    public Seed() {
    }

    public Seed(final String name, final String category) {
        this.name = name;
        this.category = category;

        this.eatenBy = new ArrayList<Bird>();
    }

    public Seed(final String name, final String category, final List<Bird> eatenBy) {
        this.name = name;
        this.category = category;
        this.eatenBy = eatenBy;
    }

    public List<Bird> getEatenBy() {
        return eatenBy;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Seed: [" + getName() + ", " + getCategory() + "], eaten by: " + getEatenBy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(name, "name")
                .notNullOrEmpty(category, "category")
                .notNull(eatenBy, "eatenBy")
                .endExpressionAndValidate();
    }
}
