/*-
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-test
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


package se.jguru.nazgul.test.xmlbinding;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.SortedMap;
import java.util.regex.Pattern;

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

        Assert.assertEquals(2, pathDiffMap.size());
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
        Assert.assertEquals("/a[1]/b[1]/@foo", diff1.getTestNodeDetail().getXpathLocation());

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
    }

    @Test
    public void validateCorrectReadFully() {

        // Assemble
        final String path = "testdata/trivial.xml";

        // Act & Assert
        Assert.assertTrue(XmlTestUtils.readFully(path).trim().endsWith(EXPECTED));
    }

    @Test(expected = AssertionError.class)
    public void validateExceptionOnNonTrivialPatternMatch() {

        // Assemble
        final Pattern bDiffsAreTrivialPattern = Pattern.compile("/a\\[\\d+\\]/b\\[\\d+\\]");

        // Act & Assert
        XmlTestUtils.validateIdenticalContent(EXPECTED, ACTUAL_EXTRA_ATTRIBUTE, bDiffsAreTrivialPattern);
    }

    @Test(expected = AssertionError.class)
    public void validateJunitAssertionFailureOnNonTrivialPatternMatch() {

        // Assemble
        final Pattern matchesNothingPattern = Pattern.compile("/no/match/here");

        // Act & Assert
        XmlTestUtils.validateIdenticalContent(EXPECTED, ACTUAL_EXTRA_ATTRIBUTE, matchesNothingPattern);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNonexistentStream() {

        // Act & Assert
        XmlTestUtils.readFully("a/non/existent/file");
    }
}
