/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-model
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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
package se.jguru.nazgul.core.quickstart.model;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import java.io.StringReader;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SimpleArtifactTest extends AbstractJaxbBinderTest {

    // Shared state
    private String groupId = "se.jguru.nazgul.core.quickstart.model";
    private String artifactId = "nazgul-core-quickstart-model";
    private String mavenVersion = "1.6.1-SNAPSHOT";

    @Test(expected = InternalStateValidationException.class)
    public void validateExceptionOnEmptyGroupId() {

        // Act & Assert
        new SimpleArtifact("", artifactId, mavenVersion);
    }

    @Test(expected = InternalStateValidationException.class)
    public void validateExceptionOnNullGroupId() {

        // Act & Assert
        new SimpleArtifact(null, artifactId, mavenVersion);
    }

    @Test(expected = InternalStateValidationException.class)
    public void validateExceptionOnEmptyArtifactId() {

        // Act & Assert
        new SimpleArtifact(groupId, "", mavenVersion);
    }

    @Test(expected = InternalStateValidationException.class)
    public void validateExceptionOnNullArtifactId() {

        // Act & Assert
        new SimpleArtifact(groupId, null, mavenVersion);
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final String artifactId2 = "nazgul-core-quickstart-reactor";
        final SimpleArtifact art1 = new SimpleArtifact(groupId, artifactId, mavenVersion);
        final SimpleArtifact art2 = new SimpleArtifact(groupId, artifactId2, mavenVersion);
        final SimpleArtifact art3 = new SimpleArtifact(groupId, artifactId, mavenVersion);

        // Act & Assert
        Assert.assertEquals(art1, art3);
        Assert.assertEquals(art1.hashCode(), art3.hashCode());
        Assert.assertNotSame(art1, art3);
        Assert.assertEquals(0, art1.compareTo(art3));
        Assert.assertEquals(artifactId.compareTo(artifactId2), art1.compareTo(art2));
        Assert.assertTrue(art1.equals(art3));
        Assert.assertFalse(art1.equals(art2));
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final SimpleArtifact unitUnderTest = new SimpleArtifact(groupId, artifactId, mavenVersion);
        final String expected = XmlTestUtils.readFully("testdata/simpleArtifact.xml");

        // Act
        final String result = binder.marshal(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/simpleArtifact.xml");
        final SimpleArtifact expected = new SimpleArtifact(groupId, artifactId, mavenVersion);

        // Act
        final SimpleArtifact result = binder.unmarshalInstance(new StringReader(data));

        // Assert
        Assert.assertEquals(expected, result);
        Assert.assertNotSame(expected, result);
    }
}
