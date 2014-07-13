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
}
