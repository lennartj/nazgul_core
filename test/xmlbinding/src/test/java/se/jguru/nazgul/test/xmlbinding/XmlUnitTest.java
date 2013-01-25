/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.xmlbinding;

import junit.framework.Assert;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.helpers.Beverage;

import java.util.List;
import java.util.SortedMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class XmlUnitTest {

    // Shared state
    private static final String EXPECTED = "<a><b/><c/></a>";
    private static final String ACTUAL_EXTRA_ATTRIBUTE = "<a><b foo=\"bar\"/><c/></a>";
    private static final String ACTUAL_EXTRA_WHITESPACE = "<a>    <b/> <c/>    </a>";
    private static final String ACTUAL_CHANGED_ORDER = "<a>    <c/><b/>     </a>";


    @Test
    public void validateXPathMappedDifferenceLists() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/trivial.xml");
        final String actual = XmlTestUtils.readFully("testdata/trivialWithCommentAndDiff.xml");

        // Act
        final SortedMap<String, List<Difference>> pathDiffMap
                = XmlTestUtils.getXPathLocationToDifferenceMap(expected, actual);

        // Assert
        final String xPathLocation = pathDiffMap.firstKey();
        final List<Difference> differences = pathDiffMap.get(xPathLocation);

        Assert.assertEquals(1, pathDiffMap.size());
        Assert.assertEquals(2, differences.size());
    }

    @Test
    public void validateXmlUnitOperationWithStaticXml() throws Exception {

        // Act & Assert #1
        Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(EXPECTED, ACTUAL_EXTRA_ATTRIBUTE);
        Assert.assertFalse(diff.similar());
        Assert.assertFalse(diff.identical());

        List<Difference> differences = new DetailedDiff(diff).getAllDifferences();
        Assert.assertEquals(2, differences.size());
        Difference diff0 = differences.get(0);
        Difference diff1 = differences.get(1);

        Assert.assertEquals(0, Integer.parseInt(diff0.getControlNodeDetail().getValue()));
        Assert.assertEquals(1, Integer.parseInt(diff0.getTestNodeDetail().getValue()));
        Assert.assertEquals("/a[1]/b[1]", diff0.getControlNodeDetail().getXpathLocation());
        Assert.assertEquals(diff0.getControlNodeDetail().getXpathLocation(),
                diff0.getTestNodeDetail().getXpathLocation());

        // Note! The value is not null, but a string holding the content "null"!
        Assert.assertEquals("null", diff1.getControlNodeDetail().getValue());
        Assert.assertEquals("foo", diff1.getTestNodeDetail().getValue());
        Assert.assertEquals("/a[1]/b[1]", diff1.getControlNodeDetail().getXpathLocation());
        Assert.assertEquals(diff1.getControlNodeDetail().getXpathLocation(),
                diff1.getTestNodeDetail().getXpathLocation());

        // Act & Assert #2
        diff = XmlTestUtils.compareXmlIgnoringWhitespace(EXPECTED, ACTUAL_EXTRA_WHITESPACE);
        Assert.assertTrue(diff.identical());
        Assert.assertTrue(diff.similar());
        Assert.assertEquals(0, new DetailedDiff(diff).getAllDifferences().size());

        // Act & Assert #3
        //
        // Sequence order difference yields 2 diffs:
        //  1 for the /a[1]/b[1], and
        //  1 for the /a[1]/c[1]
        //
        diff = XmlTestUtils.compareXmlIgnoringWhitespace(EXPECTED, ACTUAL_CHANGED_ORDER);
        Assert.assertFalse(diff.identical());
        Assert.assertTrue(diff.similar());
        Assert.assertEquals(2, new DetailedDiff(diff).getAllDifferences().size());

        /*
        differences = new DetailedDiff(diff).getAllDifferences();
        diff0 = differences.get(0);
        diff1 = differences.get(1);
        System.out.println("Got \"" + diff0.getDescription() + "\" difference ["
                + diff0.getControlNodeDetail().getValue() + " <-> " + diff0.getTestNodeDetail().getValue()
                + "] for element at XPath [" + diff0.getControlNodeDetail().getXpathLocation() + "] ==> " + diff0);
        System.out.println("Got \"" + diff1.getDescription() + "\" difference ["
                + diff1.getControlNodeDetail().getValue() + " <-> " + diff1.getTestNodeDetail().getValue()
                + "] for element at XPath [" + diff1.getControlNodeDetail().getXpathLocation() + "] ==> " + diff1);
        */
    }

    @Test
    public void validateCorrectReadFully() {

        // Assemble
        final String path = "testdata/trivial.xml";

        // Act & Assert
        Assert.assertEquals(EXPECTED, XmlTestUtils.readFully(path).trim());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNonexistentStream() {

        // Act & Assert
        XmlTestUtils.readFully("a/non/existent/file");
    }
}
