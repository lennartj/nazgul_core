/*-
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-api
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

package se.jguru.nazgul.core.xmlbinding.api;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class WellKnownNamespaceTest {

    // Shared state
    private WellKnownNamespace[] allNamespaces = WellKnownNamespace.values();

    @Test
    public void validateNamespaceDataIsNeitherNullNorEmpty() {

        // Assert
        for (WellKnownNamespace current : allNamespaces) {

            // Validate that the namespaceURL is not null or empty
            Assert.assertNotNull(current.getNameSpaceUrl());
            Assert.assertFalse("".equals(current.getNameSpaceUrl()));

            // Validate that the prefix is not null or empty
            Assert.assertNotNull(current.getXsdPrefix());
            Assert.assertFalse("".equals(current.getXsdPrefix()));
        }
    }
}
