/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-model
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.persistence.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.persistence.model.helpers.Person;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import java.io.StringReader;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulMutableIdEntityTest extends AbstractStandardizedTimezoneTest {

    /**
     * An example namespace.
     */
    public static final String PERSON_NAMESPACE_URL = "http://www.acme.org/person";

    // Shared state
    private JaxbXmlBinder binder;
    private JaxbNamespacePrefixResolver prefixResolver;

    @Before
    public void setupSharedState() {

        prefixResolver = new JaxbNamespacePrefixResolver();
        prefixResolver.put(PERSON_NAMESPACE_URL, "acmePeople");

        binder = new JaxbXmlBinder(prefixResolver);
    }

    @After
    public void teardownSharedState() {

    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expectedData = XmlTestUtils.readFully("testdata/people.xml");
        final Person unitUnderTest1 = new Person(1, "Mr", "Testo", 27);
        final Person unitUnderTest2 = new Person(2, "Miss", "Testa", 35);

        // Act
        final String result = binder.marshal(unitUnderTest1, unitUnderTest2);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expectedData, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/people.xml");
        final Person expected1 = new Person(1, "Mr", "Testo", 27);
        final Person expected2 = new Person(2, "Miss", "Testa", 35);

        // Act
        final List<Object> result = binder.unmarshal(new StringReader(data));

        // Assert
        Assert.assertEquals(2, result.size());
        final Person unmarshalled1 = (Person) result.get(0);
        final Person unmarshalled2 = (Person) result.get(1);

        Assert.assertNotSame(expected1, unmarshalled1);
        Assert.assertEquals(expected1, unmarshalled1);

        Assert.assertNotSame(expected2, unmarshalled2);
        Assert.assertEquals(expected2, unmarshalled2);
        Assert.assertEquals(2, unmarshalled2.getId());

        unmarshalled2.setId(24);
        Assert.assertNotEquals(expected2.getId(), unmarshalled2.getId());
    }
}
