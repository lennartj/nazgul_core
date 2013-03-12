/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */
package se.jguru.nazgul.core.clustering.api;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class UUIDGeneratorTest {

    @Test
    public void validateUniqueUUIDForEachCall() {

        // Assemble
        final UUIDGenerator unitUnderTest = new UUIDGenerator();

        // Act
        final boolean hasResult = unitUnderTest.isIdentifierAvailable();
        final String result1 = unitUnderTest.getIdentifier();
        final String result2 = unitUnderTest.getIdentifier();

        // Assert
        Assert.assertTrue(hasResult);
        Assert.assertNotNull(result1);
        Assert.assertNotNull(result2);
        Assert.assertFalse(result1.equals(result2));
    }
}
