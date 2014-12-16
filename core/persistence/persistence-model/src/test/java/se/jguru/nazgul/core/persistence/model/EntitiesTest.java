/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-model
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
package se.jguru.nazgul.core.persistence.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EntitiesTest {

    @Test
    public void validateEqualsBuilder() {

        // Assemble
        final String foo1 = "foo";
        final String foo2 = "foo";
        final String bar = "bar";

        // Act & Assert
        Assert.assertTrue(Entities.equals(foo1, foo2, Object.class));
        Assert.assertFalse(Entities.equals(foo1, bar, Object.class));
    }

    @Test
    public void validateHashcodeBuilder() {

        // Assemble
        final String foo1 = "foo";
        final String foo2 = "foo";
        final String bar = "bar";

        // Act & Assert
        Assert.assertEquals(Entities.hashCode(foo1, Object.class), Entities.hashCode(foo2, Object.class));
        Assert.assertNotEquals(Entities.hashCode(foo1, Object.class), Entities.hashCode(bar, Object.class));
    }

    @Test
    public void validateComparisonBuilder() {

        // Assemble
        final String foo1 = "foo";
        final String foo2 = "foo";
        final String bar = "bar";

        // Act & Assert
        Assert.assertEquals(0, Entities.compare(foo1, foo2, Object.class));
        Assert.assertEquals(1, Entities.compare(foo1, bar, Object.class));
    }
}
