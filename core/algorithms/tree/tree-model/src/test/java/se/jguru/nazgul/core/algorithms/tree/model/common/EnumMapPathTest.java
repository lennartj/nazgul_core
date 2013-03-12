/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.core.algorithms.tree.model.common;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.trees.TreeAlgorithms;
import se.jguru.nazgul.core.algorithms.tree.model.common.helpers.Adjustment;
import se.jguru.nazgul.core.algorithms.tree.model.common.helpers.AdjustmentPath;
import se.jguru.nazgul.core.algorithms.tree.model.common.helpers.ProcessPathSegments;
import se.jguru.nazgul.core.algorithms.tree.model.common.helpers.ProcessStringPath;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnumMapPathTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullSegments() {

        // Act & Assert
        new EnumMapPath<Adjustment, String>(null, Adjustment.class);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullEnumType() {

        // Assemble
        final EnumMap<Adjustment, String> segments = new EnumMap<Adjustment, String>(Adjustment.class);

        // Act & Assert
        new EnumMapPath<Adjustment, String>(segments, null);
    }

    @Test
    public void validateKeyPadding() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(Arrays.asList("Left"));

        // Act
        final int size = unitUnderTest.size();
        final int maxSize = unitUnderTest.getMaxSize();

        // Assert
        Assert.assertEquals(1, size);
        Assert.assertEquals(Adjustment.values().length, maxSize);
    }

    @Test
    public void validateSizeAndIteration() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(
                Arrays.asList("Left", "Center", "Right"));

        // Act
        final List<String> iterated = new ArrayList<String>();
        for (String aResult : unitUnderTest) {
            iterated.add(aResult);
        }

        // Assert
        Assert.assertEquals(3, unitUnderTest.size());
        Assert.assertEquals(Arrays.asList("Left", "Center", "Right"), iterated);
        Assert.assertEquals("Left", unitUnderTest.get(0));
        Assert.assertEquals("Center", unitUnderTest.get(1));
        Assert.assertEquals("Right", unitUnderTest.get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionOnTooBigIndex() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(Arrays.asList("Left", "Center"));

        // Act & Assert
        unitUnderTest.get(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionOnNegativeIndex() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(Arrays.asList("Left", "Center"));

        // Act & Assert
        unitUnderTest.get(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionAppendingSegmentToFullPath() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(
                Arrays.asList("Left", "Center", "Right"));

        // Act & Assert
        unitUnderTest.append("FooBar");
    }

    @Test
    public void validateAppendingSegmentToPath() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(Arrays.asList("Left", "Center"));

        // Act
        final EnumMapPath<Adjustment, String> three = unitUnderTest.append("Right");

        // Assert
        Assert.assertNotSame(unitUnderTest, three);
        Assert.assertEquals(2, unitUnderTest.size());
        Assert.assertEquals(3, three.size());

        Assert.assertEquals("Left", unitUnderTest.get(Adjustment.LEFT));
        Assert.assertEquals("Center", unitUnderTest.get(Adjustment.CENTER));
        Assert.assertEquals(null, unitUnderTest.get(Adjustment.RIGHT));

        Assert.assertEquals("Left", three.get(Adjustment.LEFT));
        Assert.assertEquals("Center", three.get(Adjustment.CENTER));
        Assert.assertEquals("Right", three.get(Adjustment.RIGHT));

        Assert.assertEquals("Right", three.get(2));
    }

    @Test
    public void validateComparison() {

        // Assemble
        final AdjustmentPath unitUnderTest1 = AdjustmentPath.create(Arrays.asList("Left", "Center"));
        final AdjustmentPath unitUnderTest2 = AdjustmentPath.create(
                Arrays.asList("Left", "Center", "Right"));
        final AdjustmentPath unitUnderTest3 = AdjustmentPath.create(Arrays.asList("Left"));
        final AdjustmentPath unitUnderTest4 = AdjustmentPath.create(Arrays.asList("Apa"));

        // Act
        final SortedSet<EnumMapPath<Adjustment, String>> sortedSet = new TreeSet<EnumMapPath<Adjustment, String>>();
        sortedSet.add(unitUnderTest1);
        sortedSet.add(unitUnderTest2);
        sortedSet.add(unitUnderTest3);
        sortedSet.add(unitUnderTest4);

        final List<EnumMapPath<Adjustment, String>> sortedList = new ArrayList<EnumMapPath<Adjustment, String>>();
        for (EnumMapPath<Adjustment, String> current : sortedSet) {
            sortedList.add(current);
        }

        // Assert
        Assert.assertEquals("Left".compareTo("Apa"), unitUnderTest3.compareTo(unitUnderTest4));
        Assert.assertSame(unitUnderTest4, sortedList.get(0));
        Assert.assertSame(unitUnderTest3, sortedList.get(1));
        Assert.assertSame(unitUnderTest1, sortedList.get(2));
        Assert.assertSame(unitUnderTest2, sortedList.get(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNullsWithinPath() {

        // Assemble
        final EnumMap<ProcessPathSegments, String> segments = TreeAlgorithms.getEmptyEnumMap(ProcessPathSegments.class);
        segments.put(ProcessPathSegments.NODE, "Something");

        // Act & Assert
        new ProcessStringPath(segments);
    }

    @Test
    public void validateToString() {

        // Assemble
        final String expected =
                "EnumMapPath { \n" +
                        "  LEFT=Foo\n" +
                        "  CENTER=Bar\n" +
                        "  RIGHT=Baz }";
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(Arrays.asList("Foo", "Bar", "Baz"));

        // Act
        final String debugString = unitUnderTest.toString();

        // Assert
        Assert.assertNotNull(debugString);
        Assert.assertEquals(expected, debugString);
    }

    @Test
    public void validateMarshalling() {

        // Assemble
        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<core:entityTransporter xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:core=\"http://www.jguru.se/nazgul/core\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <entityClasses>\n" +
                "        <entityClass>se.jguru.nazgul.core.algorithms.tree.model.common.helpers.AdjustmentPath</entityClass>\n" +
                "        <entityClass>se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter</entityClass>\n" +
                "    </entityClasses>\n" +
                "    <items>\n" +
                "        <item xsi:type=\"core:adjustmentPath\" enumType=\"se.jguru.nazgul.core.algorithms.tree.model.common.helpers.Adjustment\" version=\"0\">\n" +
                "            <mapPathSegments enumType=\"se.jguru.nazgul.core.algorithms.tree.model.common.helpers.Adjustment\">\n" +
                "                <values xsi:type=\"xs:string\">Left</values>\n" +
                "                <values xsi:type=\"xs:string\">Center</values>\n" +
                "                <values xsi:nil=\"true\"/>\n" +
                "            </mapPathSegments>\n" +
                "        </item>\n" +
                "    </items>\n" +
                "</core:entityTransporter>\n";

        final AdjustmentPath toMarshal = AdjustmentPath.create(Arrays.asList("Left", "Center"));
        final JaxbXmlBinder binder = new JaxbXmlBinder();

        // Act
        final String result = binder.marshal(toMarshal);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void validateUnmarshalling() {

        // Assemble
        final AdjustmentPath expected = AdjustmentPath.create(Arrays.asList("Left", "Center"));
        final JaxbXmlBinder binder = new JaxbXmlBinder();
        final String marshalled = readFully("testdata/jaxbAnnotatedEnumMap.xml");

        // Act
        final List<Object> unmarshalled = binder.unmarshal(new StringReader(marshalled));

        // Assert
        Assert.assertNotNull(unmarshalled);
        Assert.assertEquals(1, unmarshalled.size());

        AdjustmentPath resurrected = (AdjustmentPath) unmarshalled.get(0);
        Assert.assertEquals(expected.size(), resurrected.size());

        Assert.assertNull(expected.get(Adjustment.RIGHT));
        Assert.assertNull(resurrected.get(Adjustment.RIGHT));
        Assert.assertEquals(expected.get(Adjustment.LEFT), resurrected.get(Adjustment.LEFT));
        Assert.assertEquals(expected.get(Adjustment.CENTER), resurrected.get(Adjustment.CENTER));
        Assert.assertEquals(expected.get(Adjustment.RIGHT), resurrected.get(Adjustment.RIGHT));
    }

    //
    // Private helpers
    //

    private String readFully(final String path) {

        final InputStream resource = getClass().getClassLoader().getResourceAsStream(path);

        final BufferedReader tmp = new BufferedReader(new InputStreamReader(resource));
        final StringBuilder toReturn = new StringBuilder(50);

        try {
            for (String line = tmp.readLine(); line != null; line = tmp.readLine()) {
                toReturn.append(line).append('\n');
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Problem reading data from Reader", e);
        }

        // All done.
        return toReturn.toString();
    }
}
