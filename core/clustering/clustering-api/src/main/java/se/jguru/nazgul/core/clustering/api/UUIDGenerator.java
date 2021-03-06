/*-
 * #%L
 * Nazgul Project: nazgul-core-clustering-api
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

package se.jguru.nazgul.core.clustering.api;

import javax.xml.bind.annotation.XmlTransient;
import java.util.UUID;

/**
 * An IdGenerator, supplying a new UUID value for each call to {@code getIdentifier}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public final class UUIDGenerator implements IdGenerator {

    // Constants
    private static final UUIDGenerator INSTANCE = new UUIDGenerator();

    /*
     * Hide constructor for utility classes.
     */
    private UUIDGenerator() {
        // Do nothing
    }

    /**
     * @return {@code UUID.randomUUID().toString()}, implying a new/unique UUID for each call
     * to this method.
     */
    @Override
    public final String getIdentifier() {
        return UUID.randomUUID().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isIdentifierAvailable() {
        return true;
    }

    /**
     * Retrieves the Singleton UUIDGenerator instance.
     *
     * @return the Singleton UUIDGenerator instance.
     */
    public static UUIDGenerator getInstance() {
        return INSTANCE;
    }
}
