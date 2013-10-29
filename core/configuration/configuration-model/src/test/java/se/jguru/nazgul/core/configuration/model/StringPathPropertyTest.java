/*
 * #%L
 * Nazgul Project: nazgul-core-configuration-model
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
package se.jguru.nazgul.core.configuration.model;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.tree.model.common.EnumMapPath;
import se.jguru.nazgul.core.configuration.model.helpers.DocumentPart;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import java.io.StringReader;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringPathPropertyTest {

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final EnumMapPath<DocumentPart> key = new EnumMapPath<DocumentPart>(
                "chapter 1/Section 3/Subsection 42", DocumentPart.class);
        final StringPathProperty<String> unitUnderTest = new StringPathProperty<String>(key, "subsection_content");
        // final String expected = XmlTestUtils.readFully("testdata/aStringPathPropertyUsingEnumMapPathKeys.xml");

        final JaxbXmlBinder binder = new JaxbXmlBinder();

        // Act
        final String result = binder.marshal(unitUnderTest);
        System.out.println("Result: " + result);

        // Assert
        // final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, result);
        // Assert.assertTrue("Detailed Diff: " + new DetailedDiff(diff), diff.identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String documentPart = "chapter 1/Section 3/Subsection 42";
        final EnumMapPath<DocumentPart> unitUnderTest = new EnumMapPath<DocumentPart>(
                documentPart, DocumentPart.class);
        final String data = XmlTestUtils.readFully("testdata/aStringPathPropertyUsingEnumMapPathKeys.xml");

        final JaxbXmlBinder binder = new JaxbXmlBinder();

        // Act
        final List<?> unmarshalled = binder.unmarshal(new StringReader(data));

        // Assert
        Assert.assertNotNull(unmarshalled);
        Assert.assertEquals(1, unmarshalled.size());

        final EnumMapPath<DocumentPart> emp = (EnumMapPath<DocumentPart>) unmarshalled.get(0);
        // System.out.println("Got: " + emp);
        Assert.assertEquals(unitUnderTest, emp);
    }
}
