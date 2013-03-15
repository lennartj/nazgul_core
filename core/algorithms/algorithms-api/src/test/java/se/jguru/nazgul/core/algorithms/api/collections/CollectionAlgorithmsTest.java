/*
 * #%L
 * Nazgul Project: se.jguru.nazgul.core.algorithms.api.nazgul-core-algorithms-api
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

package se.jguru.nazgul.core.algorithms.api.collections;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Aggregator;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Operation;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CollectionAlgorithmsTest {

    @Test
    public void validateTransformationForCollections() {

        // Assemble
        final List<String> source = new ArrayList<String>();
        final List<Integer> expectedResult = new ArrayList<Integer>();

        source.add("42");
        source.add("40");
        source.add("43");

        expectedResult.add(42);
        expectedResult.add(40);
        expectedResult.add(43);

        final Transformer<String, Integer> transformer = new Transformer<String, Integer>() {
            @Override
            public Integer transform(final String input) {
                return Integer.parseInt(input);
            }
        };

        // Act
        final List<Integer> result = CollectionAlgorithms.transform(source, transformer);

        // Assert
        Assert.assertEquals(expectedResult.size(), result.size());
        for (int i = 0; i < result.size(); i++) {
            Assert.assertEquals(expectedResult.get(i), result.get(i));
        }
    }

    @Test
    public void validateTransformationForMaps() {

        // Assemble
        final Map<String, Integer> source = new HashMap<String, Integer>();
        final Map<Double, Boolean> expectedResult = new HashMap<Double, Boolean>();

        source.put("foo", 2);
        source.put("gnat", 2415);

        expectedResult.put(3d, true);
        expectedResult.put(4d, false);

        final Transformer<Tuple<String, Integer>, Tuple<Double, Boolean>> transformer
                = new Transformer<Tuple<String, Integer>, Tuple<Double, Boolean>>() {
            @Override
            public Tuple<Double, Boolean> transform(Tuple<String, Integer> input) {

                return new Tuple<Double, Boolean>(
                        Double.parseDouble("" + input.getKey().length()),
                        input.getValue() % 2 == 0);
            }
        };

        // Act
        final Map<Double, Boolean> result = CollectionAlgorithms.transform(source, transformer);

        // Assert
        Assert.assertEquals(expectedResult.size(), result.size());
        for (Double current : expectedResult.keySet()) {
            Assert.assertEquals(expectedResult.get(current), result.get(current));
        }
    }

    @Test
    public void validateFlatten() {

        // Assemble
        final Map<String, Integer> source = new HashMap<String, Integer>();
        final List<String> expectedResult = new LinkedList<String>();

        source.put("foo", 2);
        source.put("gnat", 2415);

        expectedResult.add("foo:2");
        expectedResult.add("gnat:2415");

        final Transformer<Tuple<String, Integer>, String> transformer = new Transformer<Tuple<String, Integer>, String>() {
            @Override
            public String transform(Tuple<String, Integer> input) {
                return input.getKey() + ":" + input.getValue();
            }
        };

        // Act
        final Collection<String> result = CollectionAlgorithms.flatten(source, transformer);

        // Assert
        Assert.assertArrayEquals(expectedResult.toArray(), result.toArray());
    }

    @Test
    public void validateFilteringForCollections() {

        // Assemble
        final List<String> source = new ArrayList<String>();
        final List<String> expectedResult = new ArrayList<String>();

        source.add("42");
        source.add("40");
        source.add("43");

        expectedResult.add("42");
        expectedResult.add("40");

        final Filter<String> filter = new Filter<String>() {
            @Override
            public boolean accept(final String candidate) {
                return !candidate.equals("43");
            }
        };

        // Act
        final List<String> result = CollectionAlgorithms.filter(source, filter);

        // Assert
        Assert.assertEquals(expectedResult.size(), result.size());
        for (int i = 0; i < expectedResult.size(); i++) {
            Assert.assertEquals(expectedResult.get(i), result.get(i));
        }
    }

    @Test
    public void validateFilteringForMaps() {

        // Assemble
        final Map<String, Integer> source = new HashMap<String, Integer>();
        final Map<String, Integer> expectedResult = new HashMap<String, Integer>();

        source.put("foo", 4);
        source.put("bar", 6);
        source.put("baz", 8);
        source.put("gnat", 10);

        expectedResult.put("foo", 4);
        expectedResult.put("baz", 8);

        final Filter<Tuple<String, Integer>> filter = new Filter<Tuple<String, Integer>>() {
            @Override
            public boolean accept(Tuple<String, Integer> candidate) {
                return !candidate.getKey().equals("bar") && candidate.getValue() != 10;
            }
        };

        // Act
        final Map<String, Integer> result = CollectionAlgorithms.filter(source, filter);

        // Assert
        Assert.assertEquals(expectedResult.size(), result.size());
        for (String current : result.keySet()) {
            Assert.assertEquals(expectedResult.get(current), result.get(current));
        }
    }

    @Test
    public void validateMapping() {

        // Assemble
        final List<String> source = new ArrayList<String>();
        final Map<String, String[]> expectedResult = new HashMap<String, String[]>();

        source.add("42");
        source.add("40");
        source.add("43");

        expectedResult.put("42", new String[]{"42", "42i", "42ii"});
        expectedResult.put("40", new String[]{"40", "40i", "40ii"});
        expectedResult.put("43", new String[]{"43", "43i", "43ii"});

        final Transformer<String, Tuple<String, String[]>> mapper =
                new Transformer<String, Tuple<String, String[]>>() {
                    @Override
                    public Tuple<String, String[]> transform(final String input) {

                        final String[] value = new String[3];
                        for (int i = 0; i < value.length; i++) {

                            StringBuilder builder = new StringBuilder(input);

                            for (int j = 0; j < i; j++) {
                                builder.append("i");
                            }

                            value[i] = builder.toString();
                        }

                        return new Tuple<String, String[]>(input, value);
                    }
                };

        // Act
        final Map<String, String[]> result = CollectionAlgorithms.map(source, mapper);

        // Assert
        for (String current : expectedResult.keySet()) {
            String[] expectedValues = expectedResult.get(current);
            String[] resultValues = result.get(current);

            Assert.assertEquals(expectedValues.length, resultValues.length);
            Assert.assertArrayEquals(expectedValues, resultValues);
        }
    }

    @Test
    public void validateMappingWithNullTransformerReturnElements() {

        // Assemble
        final List<String> source = new ArrayList<String>();
        final Map<String, String[]> expectedResult = new HashMap<String, String[]>();

        source.add("42");
        source.add("40");
        source.add("43");

        expectedResult.put("42", new String[]{"42", "42i", "42ii"});
        expectedResult.put("43", new String[]{"43", "43i", "43ii"});

        final Transformer<String, Tuple<String, String[]>> mapper =
                new Transformer<String, Tuple<String, String[]>>() {
                    @Override
                    public Tuple<String, String[]> transform(final String input) {

                        if (input.equals("40")) {
                            return null;
                        }

                        final String[] value = new String[3];
                        for (int i = 0; i < value.length; i++) {

                            StringBuilder builder = new StringBuilder(input);
                            for (int j = 0; j < i; j++) {
                                builder.append("i");
                            }
                            value[i] = builder.toString();
                        }

                        return new Tuple<String, String[]>(input, value);
                    }
                };

        // Act
        final Map<String, String[]> result = CollectionAlgorithms.map(source, mapper);

        // Assert
        for (String current : expectedResult.keySet()) {
            String[] expectedValues = expectedResult.get(current);
            String[] resultValues = result.get(current);

            Assert.assertEquals(expectedValues.length, resultValues.length);
            Assert.assertArrayEquals(expectedValues, resultValues);
        }
    }

    @Test
    public void validateAggregation() {

        // Assemble
        final List<String> source = new ArrayList<String>();
        final int expectedResult = 42 + 40 + 43;

        source.add("42");
        source.add("40");
        source.add("43");

        final Aggregator<String, Integer> aggregator = new Aggregator<String, Integer>() {
            @Override
            public Integer aggregate(final Integer current, final String candidate) {

                int tmp = current == null ? 0 : current;
                return tmp + Integer.parseInt(candidate);
            }
        };

        // Act
        final Integer result = CollectionAlgorithms.aggregate(source, aggregator);

        // Assert
        Assert.assertEquals(expectedResult, result.intValue());
    }

    @Test(expected = NullPointerException.class)
    public void validateAggregationWithNullSourceList() {

        // Assemble
        final List<String> source = null;
        final Aggregator<String, Integer> aggregator = new Aggregator<String, Integer>() {
            @Override
            public Integer aggregate(final Integer current, final String candidate) {

                int tmp = current == null ? 0 : current;
                return tmp + Integer.parseInt(candidate);
            }
        };

        // Act & Assert
        CollectionAlgorithms.aggregate(source, aggregator);
    }

    @Test(expected = NullPointerException.class)
    public void validateAggregationWithNullAggregator() {

        // Assemble
        final List<String> source = new ArrayList<String>();
        final Aggregator<String, Integer> aggregator = null;

        // Act & Assert
        CollectionAlgorithms.aggregate(source, aggregator);
    }

    @Test
    public void validateForAll() {

        // Assemble
        final List<StringBuffer> source = new ArrayList<StringBuffer>();
        final String suffix = "_is_nice";

        source.add(new StringBuffer("IceCream"));
        source.add(new StringBuffer("MountainBiking"));
        source.add(new StringBuffer("FunctionalProgramming"));

        final Operation<StringBuffer> operation = new Operation<StringBuffer>() {
            @Override
            public void operate(final StringBuffer target) {
                target.append(suffix);
            }
        };

        // Act
        CollectionAlgorithms.forAll(source, operation);

        // Assert
        for (StringBuffer current : source) {
            Assert.assertTrue(current.toString().endsWith(suffix));
        }
    }

    @Test
    public void validateCreate() {

        // Assemble
        final List<String> expected = Arrays.asList("foo", "baz");
        final Properties props = new Properties();

        props.setProperty("foo", "bar");
        props.setProperty("baz", "gnat");

        // Act
        final List<Object> result = CollectionAlgorithms.create(props.keys());

        // Assert
        Assert.assertEquals(expected.size(), result.size());
        for (Object current : result) {
            String currentKey = "" + current;
            Assert.assertTrue(expected.contains(currentKey));
        }
    }

    @Test
    public void validateEnumerate() {

        // Assemble
        final List<String> expected = Arrays.asList("blah", "blue", "blee");

        // Act
        final Enumeration<String> result = CollectionAlgorithms.enumerate(expected);

        // Assert
        final Iterator<String> iterator = expected.iterator();

        while (result.hasMoreElements()) {
            Assert.assertEquals(iterator.next(), result.nextElement());
        }
    }
}
