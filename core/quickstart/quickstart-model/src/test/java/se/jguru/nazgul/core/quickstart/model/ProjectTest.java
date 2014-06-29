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
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ProjectTest extends AbstractStandardizedTimezoneTest {

    // Shared state
    private JaxbXmlBinder binder;

    /**
     * {@inheritDoc}
     */
    @Before
    public void setupSharedState() {
        binder = new JaxbXmlBinder();
    }

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

        // Act

        // Assert
    }
}
