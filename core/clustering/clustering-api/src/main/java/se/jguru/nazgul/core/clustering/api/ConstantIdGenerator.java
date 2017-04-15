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

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Trivial IdGenerator which always returns a constant string.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public final class ConstantIdGenerator implements IdGenerator {

    // Internal state
    private String id;

    /**
     * Compound constructor creating a ConstantIdGenerator which always returns the provided id String.
     *
     * @param id The constant id.
     */
    public ConstantIdGenerator(@NotNull final String id) {

        // Check sanity
        Validate.notEmpty(id, "id");

        // Assign internal state
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getIdentifier() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isIdentifierAvailable() {
        return true;
    }
}
