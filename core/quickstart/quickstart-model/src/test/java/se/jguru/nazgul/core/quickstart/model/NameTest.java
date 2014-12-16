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
public class NameTest extends AbstractJaxbBinderTest {

    @Test(expected = InternalStateValidationException.class)
    public void validateExceptionOnEmptyName() {

        // Act & Assert
        new Name("prefix", "", "sometype");
    }

    @Test(expected = InternalStateValidationException.class)
    public void validateExceptionOnEmptyType() {

        // Act & Assert
        new Name(null, "somename", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnParsingNameWithFewerThan2Segments() {

        // Act & Assert
        Name.parse("incorrectNameWithoutAnySeparators");
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final Name unitUnderTest = new Name("somePrefix", "someName", "some-type-with-separators");
        final String expected = XmlTestUtils.readFully("testdata/singleName.xml");

        // Act
        final String result = binder.marshal(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() {

        // Assemble
        final Name expected = new Name("somePrefix", "someName", "some-type-with-separators");
        final String data = XmlTestUtils.readFully("testdata/singleName.xml");

        // Act
        final Name result = binder.unmarshalInstance(new StringReader(data));
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertEquals(result, expected);
        Assert.assertEquals(0, expected.compareTo(result));
        Assert.assertEquals(expected.hashCode(), result.hashCode());
        Assert.assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void validateNameParsing() {

        // Assemble
        final String toParse1 = "somePrefix-someName-someType";
        final String toParse2 = "someName-someType";
        final String toParse3 = "somePrefix-someName-some-type-with-separators";

        final Name expected1 = new Name("somePrefix", "someName", "someType");
        final Name expected2 = new Name(null, "someName", "someType");
        final Name expected3 = new Name("somePrefix", "someName", "some-type-with-separators");

        // Act
        final Name result1 = Name.parse(toParse1);
        final Name result2 = Name.parse(toParse2);
        final Name result3 = Name.parse(toParse3);

        // Assert
        Assert.assertEquals(expected1, result1);
        Assert.assertEquals(expected2, result2);
        Assert.assertEquals(expected3, result3);
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final String toParse1 = "somePrefix-someName-someType";
        final String toParse2 = "someName-someType";
        final String toParse3 = "somePrefix-someName-some-type-with-separators";

        final Name name1 = new Name("somePrefix", "someName", "someType");
        final Name name2 = new Name(null, "someName", "someType");
        final Name name3 = new Name("somePrefix", "someName", "some-type-with-separators");

        // Act & Assert
        Assert.assertEquals(0, Name.getStandardNullComparisonValue(null, null));
        Assert.assertEquals(1, Name.getStandardNullComparisonValue(name1, null));
        Assert.assertEquals(-1, Name.getStandardNullComparisonValue(null, name3));
        Assert.assertEquals(1, name1.compareTo(null));

        Assert.assertEquals(toParse2, name2.toString());
        Assert.assertEquals(toParse3, name3.toString());

        Assert.assertEquals(toParse1.compareTo(toParse3), name1.compareTo(name3));
        Assert.assertEquals(toParse3.compareTo(toParse1), name3.compareTo(name1));
        Assert.assertEquals("".compareTo("somePrefix"), name2.compareTo(name1));
        Assert.assertEquals("somePrefix".compareTo(""), name1.compareTo(name2));
    }
}
