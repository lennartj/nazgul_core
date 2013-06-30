/*
 * #%L
 * Nazgul Project: nazgul-core-tree-model
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

package se.jguru.nazgul.core.algorithms.tree.model.common.converter;

import org.junit.Assert;
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
