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
package se.jguru.nazgul.test.persistence;

/**
 * Enumeration of several JPA persistence providers and their
 * specific required properties.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public enum PersistenceProviderType {

    /**
     * The OpenJPA 2 PersistenceProvider.
     */
    OPENJPA_2("org.apache.openjpa.persistence.PersistenceProviderImpl"),

    /**
     * The EclipseLink PersistenceProvider.
     */
    ECLIPSELINK_2("org.eclipse.persistence.jpa.PersistenceProvider");

    // Internal state
    private String persistenceProviderClass;

    private PersistenceProviderType(final String persistenceProviderClass) {
        this.persistenceProviderClass = persistenceProviderClass;
    }

    /**
     * Retrieves the persistenceProvider class for this PersistenceProviderType.
     *
     * @return the persistenceProvider class for this PersistenceProviderType,
     *         as required in a persistence.xml definition.
     */
    public String getPersistenceProviderClass() {
        return persistenceProviderClass;
    }
}
