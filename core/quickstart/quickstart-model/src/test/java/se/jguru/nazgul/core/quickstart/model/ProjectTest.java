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

import java.io.StringReader;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ProjectTest extends AbstractJaxbBinderTest {

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final SimpleArtifact reactorParent = new SimpleArtifact(
                "reactorGroupId", "reactorArtifactId", "reactorMavenVersion");
        final SimpleArtifact parentParent = new SimpleArtifact("groupId", "artifactId", "mavenVersion");
        final Project unitUnderTest = new Project("prefix", "name", "reactorName", reactorParent, parentParent);

        final String expected = XmlTestUtils.readFully("testdata/project.xml");

        // Act
        final String result = binder.marshal(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/project.xml");

        final SimpleArtifact reactorParent = new SimpleArtifact(
                "reactorGroupId", "reactorArtifactId", "reactorMavenVersion");
        final SimpleArtifact parentParent = new SimpleArtifact("groupId", "artifactId", "mavenVersion");
        final Project expected = new Project("prefix", "name", "reactorName", reactorParent, parentParent);

        // Act
        final Project result = binder.unmarshalInstance(new StringReader(data));

        // Assert
        Assert.assertEquals(expected, result);
        Assert.assertNotSame(expected, result);
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final SimpleArtifact reactorParent = new SimpleArtifact(
                "reactorGroupId", "reactorArtifactId", "reactorMavenVersion");
        final SimpleArtifact parentParent = new SimpleArtifact("groupId", "artifactId", "mavenVersion");

        final Project project1 = new Project("prefix", "name", "reactorName", reactorParent, parentParent);
        final Project project2 = new Project("anotherPrefix", "anotherName", "anotherReactorName",
                reactorParent, parentParent);
        final Project project3 = new Project("prefix", "name", "reactorName", reactorParent, parentParent);

        // Act & Assert
        Assert.assertEquals(project1, project3);
        Assert.assertEquals(project1.hashCode(), project3.hashCode());
        Assert.assertNotSame(project1, project3);
        Assert.assertEquals(0, project1.compareTo(project3));
        Assert.assertEquals(project1.getName().compareTo(project2.getName()), project1.compareTo(project2));
        Assert.assertTrue(project1.equals(project3));
        Assert.assertFalse(project1.equals(project2));
    }
}
