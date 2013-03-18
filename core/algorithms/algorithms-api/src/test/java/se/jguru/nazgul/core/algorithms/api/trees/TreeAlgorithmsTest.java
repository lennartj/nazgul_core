/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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

package se.jguru.nazgul.core.algorithms.api.trees;

import org.junit.Assert;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Iterator;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TreeAlgorithmsTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullClassArgument() {

        // Assemble
        Class<Adjustment> clazz = null;

        // Act & Assert
        EnumMap<Adjustment, String> dummy = TreeAlgorithms.getEmptyEnumMap(clazz);
    }

    @Test
    public void validateCorrectlyCreatedEmptyEnumMap() {

        // Act
        final EnumMap<Adjustment, String> emptyEnumMap = TreeAlgorithms.getEmptyEnumMap(Adjustment.class);
        final Iterator<Adjustment> it = emptyEnumMap.keySet().iterator();

        // Assert
        for (Adjustment current : Adjustment.values()) {
            Assert.assertEquals(current, it.next());
        }
    }
}
