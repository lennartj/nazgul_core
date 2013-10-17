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

import java.util.ArrayList;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulEntityTest {

    @Test
    public void validateCreation() {

        // Assemble
        final String name = "foobar";
        final Long value = 42L;
        final MockEntity mockEntity = new MockEntity(name, value);

        // Act
        // ... do nothing...

        // Assert
        final ArrayList<String> callTrace = mockEntity.callTrace;
        Assert.assertTrue("callTrace size: " + callTrace.size(), callTrace.size() > 0);
    }

    @Test
    public void validateEqualityAndIdentity() {

        // Assemble
        final String name = "foobar";
        final Long value = 42L;
        final MockEntity mockEntity = new MockEntity(name, value);
        final MockEntity mockEntity2 = new MockEntity(name, value);

        // Act
        final boolean equalityByComparison = mockEntity.equals(mockEntity2);
        final boolean equalityByNull = mockEntity.equals(null);
        final boolean hashCodeEqual = mockEntity.hashCode() == mockEntity2.hashCode();

        // Assert
        Assert.assertTrue(equalityByComparison);
        Assert.assertFalse(equalityByNull);
        Assert.assertTrue(hashCodeEqual);
    }

    @Test
    public void validateCloningAndCopying() throws Exception {

        // Assemble
        final String name = "foobar";
        final Long value = 42L;
        final MockEntity mockEntity = new MockEntity(name, value);

        // Act

        // Assert
        Assert.assertEquals(mockEntity, mockEntity.copy());
        Assert.assertEquals(mockEntity, mockEntity.clone());
        Assert.assertNotSame(mockEntity, mockEntity.copy());
    }

    @Test(expected = IllegalStateException.class)
    public void validateHandlingCloneException() {

        // Assemble
        final MockEntity mockEntity = new MockEntity("gnat", 15L);
        mockEntity.throwExceptionOnClone = true;

        // Act & Assert
        mockEntity.copy();
    }
}
