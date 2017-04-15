/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.persistence.api.helpers;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ParameterMapBuilderTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullFirstParameterName() {

        // Assemble
        final String name = null;

        // Act & Assert
        ParameterMapBuilder.with(name, "irrelevant");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyFirstParameterName() {

        // Assemble
        final String name = "       ";

        // Act & Assert
        ParameterMapBuilder.with(name, "irrelevant");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptySecondParameterName() {

        // Assemble
        final String name = "       ";

        // Act & Assert
        ParameterMapBuilder.with("foo", "bar").and(name, "irrelevant");
    }

    @Test
    public void validateNormalCreation() {

        // Assemble
        final String key1 = "key1";
        final String value1 = "value1";
        final String key2 = "key2";
        final Integer value2 = 42;

        // Act
        final Map<String, Object> result = ParameterMapBuilder.with(key1, value1).and(key2, value2).build();

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(value1, result.get(key1));
        Assert.assertEquals(value2, result.get(key2));
    }
}
