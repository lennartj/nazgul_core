/*-
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
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

package se.jguru.nazgul.core.quickstart.api.analyzer.patterns;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultProjectParentPatternsTest {

    // Shared state
    private DefaultProjectParentPatterns unitUnderTest;

    @Before
    public void setupSharedState() {

        unitUnderTest = new DefaultProjectParentPatterns();
    }

    @Test
    public void validateInitialization() {

        // Assert
        Assert.assertTrue(unitUnderTest.nullPatternImpliesValid());
        Assert.assertSame(DefaultProjectParentPatterns.ARTIFACT_ID_PATTERN, unitUnderTest.getArtifactIdPattern());
        Assert.assertSame(DefaultProjectParentPatterns.GROUP_ID_PATTERN, unitUnderTest.getGroupIdPattern());

        Assert.assertSame(DefaultProjectParentPatterns.PARENT_GROUP_ID_PATTERN,
                unitUnderTest.getParentGroupIdPattern());
        Assert.assertSame(DefaultProjectParentPatterns.PARENT_ARTIFACT_ID_PATTERN,
                unitUnderTest.getParentArtifactIdPattern());
    }
}
