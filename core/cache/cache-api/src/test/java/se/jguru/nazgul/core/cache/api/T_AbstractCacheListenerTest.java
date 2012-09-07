/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.api;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Trivial state tests.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractCacheListenerTest {

    @Test
    public void validateDefaultIdentityOnCreation() {

        // Assemble
        final AbstractCacheListener unitUnderTest = new DebugCacheListener();

        // Act
        final String id = unitUnderTest.getId();

        // Assert
        Assert.assertTrue(id != null && !id.equals(""));
    }

    @Test
    public void validateIdentityOnCreation() {

        // Assemble
        final String id = "testID";
        final String name = "testName";
        final int age = 42;

        // Act
        final DebugCacheListener unitUnderTest = new DebugCacheListener(id, name, age);

        // Assert
        Assert.assertEquals(id, unitUnderTest.getId());
        Assert.assertEquals(0, unitUnderTest.callTrace.size());
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnIncorrectCreation() {

        // Assemble
        final String incorrectNullID = null;
        final String name = "testName";
        final int age = 42;

        // Act & Assert
        new DebugCacheListener(incorrectNullID, name, age);
    }

    @Test
    public void validateExternalizableCalls() throws Exception {

        // Assemble
        final String id = "testID";
        final String name = "testName";
        final int age = 42;
        final AbstractCacheListener unitUnderTest = new DebugCacheListener(id, name, age);

        final ByteArrayOutputStream transportChannelSimulator = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(transportChannelSimulator);

        // Act
        out.writeObject(unitUnderTest);
        final ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(transportChannelSimulator.toByteArray()));
        final DebugCacheListener result = (DebugCacheListener) in.readObject();

        // Assert
        Assert.assertNotSame(result, unitUnderTest);
        Assert.assertEquals(unitUnderTest.getId(), result.getId());

        Assert.assertEquals(1, result.callTrace.size());
        Assert.assertEquals("performReadExternal [testName, 42]", result.callTrace.get(0));

        Assert.assertEquals(age, result.getAge());
        Assert.assertEquals(name, result.getName());


        Assert.assertEquals("[" + unitUnderTest.getClass().getSimpleName() + "::" + id + "]", unitUnderTest.toString());
        Assert.assertEquals("[" + result.getClass().getSimpleName() + "::" + id + "]", result.toString());
    }

    @Test
    public void validateNoChangeInInternalStateFollowingEventMethodCallbacks() {

        // Assemble
        final String id = "testID";
        final String name = "testName";
        final int age = 42;
        final AbstractCacheListener unitUnderTest = new DebugCacheListener(id, name, age);

        // Act
        unitUnderTest.onPut("foo", "bar");
        unitUnderTest.onRemove("foo", "bar");
        unitUnderTest.onUpdate("foo", "bar", "baz");
        unitUnderTest.onClear();
        unitUnderTest.onAutonomousEvict("foo", "bar");
        unitUnderTest.onAutonomousLoad("foo", "bar");

        // Assert
        Assert.assertEquals(id, unitUnderTest.getId());
    }

    @Test
    public void validateFilteringEvents() {

        // Assemble
        final String filterSpec = "gnat.*";

        // Assemble
        final String id = "testID";
        final String name = "testName";
        final int age = 42;
        final DebugCacheListener unitUnderTest = new DebugCacheListener(id, name, age);
        unitUnderTest.setFilter(filterSpec);

        // Act
        unitUnderTest.onPut("foo", "bar");
        unitUnderTest.onPut("gnats", "bar");
        unitUnderTest.onRemove("foo", "bar");
        unitUnderTest.onRemove("gnats", "bar");
        unitUnderTest.onUpdate("foo", "bar", "baz");
        unitUnderTest.onUpdate("gnats", "bar", "baz");
        unitUnderTest.onAutonomousEvict("foo", "bar");
        unitUnderTest.onAutonomousEvict("gnats", "bar");
        unitUnderTest.onAutonomousLoad("foo", "bar");
        unitUnderTest.onAutonomousLoad("gnats", "bar");

        // Assert
        final List<String> callTrace = unitUnderTest.callTrace;
        Assert.assertEquals(5, callTrace.size());

        Assert.assertEquals("onPut [gnats, bar]", callTrace.get(0));
        Assert.assertEquals("onRemove [gnats, bar]", callTrace.get(1));
        Assert.assertEquals("onUpdate [gnats, bar, baz]", callTrace.get(2));
        Assert.assertEquals("onAutonomousEvict [gnats, bar]", callTrace.get(3));
        Assert.assertEquals("onAutonomousLoad [gnats, bar]", callTrace.get(4));
    }
}
