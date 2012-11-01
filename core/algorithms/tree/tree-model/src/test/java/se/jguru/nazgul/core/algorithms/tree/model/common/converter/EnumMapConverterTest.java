/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model.common.converter;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.tree.model.common.helpers.Adjustment;

import java.util.EnumMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnumMapConverterTest {

    @Test
    public void validateNullReceivedOnNullArguments() throws Exception {

        // Assemble
        final EnumMapTypeConverter<Adjustment, String> unitUnderTest = new EnumMapTypeConverter<Adjustment, String>();

        // Act
        final JaxbAnnotatedEnumMap marshalledResult = unitUnderTest.marshal(null);
        final EnumMap<Adjustment, String> unmarshalledResult = unitUnderTest.unmarshal(null);

        // Assert
        Assert.assertNull(marshalledResult);
        Assert.assertNull(unmarshalledResult);
    }
}
