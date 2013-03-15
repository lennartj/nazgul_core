/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-test
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.test.xmlbinding;

import org.apache.commons.lang3.Validate;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Utility class to simplify working with XML data in tests.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings("PMD.PreserveStackTrace")
public abstract class XmlTestUtils {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(XmlTestUtils.class);

    /**
     * Compares XML documents provided by the two Readers.
     *
     * @param expected The expected document data.
     * @param actual   The actual document data.
     * @return A DetailedDiff object, describing all differences in documents supplied.
     * @throws SAXException If a SAXException was raised during parsing of the two Documents.
     * @throws IOException  If an I/O-related exception was raised while acquiring the data from the Readers.
     */
    public static Diff compareXmlIgnoringWhitespace(final String expected, final String actual) throws SAXException,
            IOException {

        // Check sanity
        Validate.notNull(expected, "Cannot handle null expected argument.");
        Validate.notNull(actual, "Cannot handle null actual argument.");

        // Ignore whitespace - and also normalize the Documents.
        XMLUnit.setNormalize(true);
        XMLUnit.setIgnoreWhitespace(true);

        // Compare and return
        return XMLUnit.compareXML(expected, actual);
    }

    /**
     * Compares the two supplied XML-formatted streams
     *
     * @param expected The expected document data.
     * @param actual   The actual document data.
     * @return An XPathLocation to Difference Map for the given Diff.
     */
    public static SortedMap<String, List<Difference>> getXPathLocationToDifferenceMap(final String expected,
                                                                                      final String actual) {

        try {
            return getXPathLocationToDifferenceMap(compareXmlIgnoringWhitespace(expected, actual));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not acquire XPath2Difference map", e);
        }
    }

    /**
     * Maps the Difference instances within the supplied Diff to their respective XPath locations.
     * Should the XPath location (within a single Difference instance) be different between the
     * expected and actual {@code NodeDetail}s, the Difference is mapped to both XPaths.
     *
     * @param diff The Diff from which all Differences should be extracted and mapped to their respective
     *             XPath locations. Should the XPath location (within a single Difference instance) be different between the
     *             expected and actual {@code NodeDetail}s, the Difference is mapped to both XPaths.
     * @return An XPathLocation to Difference Map for the given Diff.
     */
    public static SortedMap<String, List<Difference>> getXPathLocationToDifferenceMap(final Diff diff) {

        // Check sanity
        Validate.notNull(diff, "Cannot handle null diff argument.");

        final SortedMap<String, List<Difference>> toReturn = new TreeMap<String, List<Difference>>();
        final List<Difference> allDifferences = (List<Difference>) new DetailedDiff(diff).getAllDifferences();

        for (Difference current : allDifferences) {

            // Map the difference to its XPathLocation.
            final String expectedPartXPath = current.getControlNodeDetail().getXpathLocation();
            final String actualPartXPath = current.getTestNodeDetail().getXpathLocation();

            if (expectedPartXPath.equals(actualPartXPath)) {
                addDifference(toReturn, expectedPartXPath, current);
            } else {

                // Is this really sane?
                log.warn("Different XPath locations for Difference [" + current + "]. Mapping to two locations.");
                addDifference(toReturn, expectedPartXPath, current);
                addDifference(toReturn, actualPartXPath, current);
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Utility method to read all (string formatted) data from the given classpath-relative
     * file and return the data as a string.
     *
     * @param path The classpath-relative file path.
     * @return The content of the supplied file.
     */
    public static String readFully(final String path) {

        final StringBuilder toReturn = new StringBuilder(50);

        try {

            // Will produce a NPE if the path was not directed to a file.
            final InputStream resource = XmlTestUtils.class.getClassLoader().getResourceAsStream(path);
            final BufferedReader tmp = new BufferedReader(new InputStreamReader(resource));

            for (String line = tmp.readLine(); line != null; line = tmp.readLine()) {
                toReturn.append(line).append('\n');
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException("Resource [" + path + "] not readable.");
        }

        // All done.
        return toReturn.toString();
    }

    //
    // Private helpers
    //

    private static void addDifference(final SortedMap<String, List<Difference>> resultMap,
                                      final String xPath,
                                      final Difference toAdd) {

        List<Difference> diffList = resultMap.get(xPath);
        if (diffList == null) {
            diffList = new ArrayList<Difference>();
            resultMap.put(xPath, diffList);
        }

        // Add the difference
        diffList.add(toAdd);
    }
}
