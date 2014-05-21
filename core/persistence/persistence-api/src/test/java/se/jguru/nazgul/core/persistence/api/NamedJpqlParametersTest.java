/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
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
package se.jguru.nazgul.core.persistence.api;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.jguru.nazgul.core.persistence.api.helpers.NamedParametersPerson;
import se.jguru.nazgul.core.persistence.api.helpers.ParameterMapBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NamedJpqlParametersTest extends AbstractInMemoryJpaTest {

    // Internal state
    private NamedParametersPerson lennart;
    private NamedParametersPerson malin;
    private NamedParametersPerson ida;

    @Override
    protected String getPersistenceFileName() {
        return "NamedQueryParameter";
    }

    @Override
    protected void doCustomSetup() {

        // Debug some
        try {
            SortedMap<String, ClassLoader> classLoaderMap = new TreeMap<String, ClassLoader>();
            classLoaderMap.put("contextClassLoader", Thread.currentThread().getContextClassLoader());
            classLoaderMap.put("originalClassLoader", originalClassLoader);

            for (Map.Entry<String, ClassLoader> clEntry : classLoaderMap.entrySet()) {
                int i = 0;
                for (URL current : Collections.list(clEntry.getValue().getResources(""))) {
                    System.out.println("  (" + clEntry.getKey() + ") [" + i++ + "]: " + current);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not list resources", e);
        }

        // First, create a few objects
        lennart = new NamedParametersPerson("Lennart", 45);
        malin = new NamedParametersPerson("Malin", 25);
        ida = new NamedParametersPerson("Ida", 16);

        // Then persist them.
        unitUnderTest.create(lennart);
        unitUnderTest.create(malin);
        unitUnderTest.create(ida);

        // Commit the transaction.
        commitAndStartNewTransaction();
    }

    @Override
    protected void doCustomTeardown() {
        dropDbTable("NAMEDPARAMETERSPERSON");
    }

    @Ignore("Moved to JpaPersistenceOperationsTest instead.")
    @Test
    public void validateNormalOperationUsingNamedJpqlParameters() {

        // Assemble
        final Map<String, Object> parameters = ParameterMapBuilder.with("firstName", "Len%").build();

        // Act
        final List<NamedParametersPerson> result = unitUnderTest.fireNamedQuery("getPersonsByFirstName", parameters);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }
}
