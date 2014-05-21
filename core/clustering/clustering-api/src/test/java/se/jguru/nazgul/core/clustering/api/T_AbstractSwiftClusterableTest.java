/*
 * #%L
 * Nazgul Project: nazgul-core-clustering-api
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

package se.jguru.nazgul.core.clustering.api;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractSwiftClusterableTest {

    // Shared state
    final String id = "testID";
    final String name = "testName";
    final int age = 42;

    @Test
    public void validateDefaultIdentityOnCreation() {

        // Assemble

        // Act
        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable();

        // Assert
        final String id = unitUnderTest.getClusterId();
        Assert.assertTrue(id != null && !id.equals(""));
    }

    @Test
    public void validateIdentityOnCreation() {

        // Assemble

        // Act
        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable(id, name, age);

        // Assert
        Assert.assertEquals(id, unitUnderTest.getClusterId());
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnIncorrectCreation() {

        // Assemble
        final String incorrectNullID = null;

        // Act & Assert
        new TestAbstractSwiftClusterable(incorrectNullID, name, age);
    }

    @Test
    public void validateCorrectIdInitialization() {

        // Assemble
        MockIdGenerator.counter.set(0);
        final String idPrefix = "testIdPrefix";
        final MockIdGenerator idGenerator = new MockIdGenerator(idPrefix);

        // Act
        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable(
                idGenerator,
                "testName",
                42);

        // Assert
        Assert.assertEquals(idPrefix + "_1", unitUnderTest.getClusterId());
    }

    /*
    @Test(expected = IllegalStateException.class)
    public void validateIncorrectIdInitialization() {

        // Assemble
        final String newId = "theNewID";
        final String incorrectAttemptToReassignId = "anotherNewID";
        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable();

        // Act & Assert
        unitUnderTest.initializeID(newId);
        unitUnderTest.initializeID(incorrectAttemptToReassignId);
    }
    */

    @Test
    public void validateExternalizableCalls() throws Exception {

        // Assemble
        final String id = "testID";
        final String name = "testName";
        final int age = 42;
        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable(id, name, age);

        final ByteArrayOutputStream transportChannelSimulator = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(transportChannelSimulator);

        // Act
        out.writeObject(unitUnderTest);
        final ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(transportChannelSimulator.toByteArray()));
        final TestAbstractSwiftClusterable result = (TestAbstractSwiftClusterable) in.readObject();

        // Assert
        Assert.assertNotSame(result, unitUnderTest);
        Assert.assertEquals(unitUnderTest.getClusterId(), result.getClusterId());

        Assert.assertEquals(1, result.callTrace.size());
        Assert.assertEquals("performReadExternal [testName, 42]", result.callTrace.get(0));

        Assert.assertEquals(age, result.getAge());
        Assert.assertEquals(name, result.getName());


        Assert.assertEquals("[" + unitUnderTest.getClass().getSimpleName() + "::" + id + "]", unitUnderTest.toString());
        Assert.assertEquals("[" + result.getClass().getSimpleName() + "::" + id + "]", result.toString());
    }
}
