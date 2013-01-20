/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.ehcache;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.cache.api.transaction.AbstractTransactedAction;

import java.io.Serializable;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalEhCacheTest extends AbstractCacheTest {

    /**
     * @return The classpath-relative configuration file for EhCache to be used within this TestCase.
     */
    @Override
    protected String getEhCacheConfiguration() {
        return "ehcache/config/LocalHostUnitTestStandaloneConfig.xml";
    }

    @Test
    public void validateNormalCacheLifecycle() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final String key = "key";

        // Act
        final boolean keyInCache_before = unitUnderTest.containsKey(key);
        final Serializable before = unitUnderTest.put(key, "value");

        final boolean keyInCache_mid = unitUnderTest.containsKey(key);
        final Serializable mid = unitUnderTest.put(key, "value2");
        final Serializable after = unitUnderTest.get(key);
        final Serializable removed = unitUnderTest.remove(key);

        final boolean keyInCache_removed = unitUnderTest.containsKey(key);
        final Serializable shouldBeNull = unitUnderTest.get(key);

        // Assert
        Assert.assertNull(before);
        Assert.assertEquals("value", mid);
        Assert.assertEquals("value2", after);
        Assert.assertEquals("value2", removed);
        Assert.assertNull(shouldBeNull);
        Assert.assertFalse(keyInCache_before);
        Assert.assertTrue(keyInCache_mid);
        Assert.assertFalse(keyInCache_removed);
    }

    @Test
    public void validateCacheNotChangedWhenTransactionRollsBack() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final String key = "key";
        final String errMsg = "Oops.";

        // Act
        unitUnderTest.performTransactedAction(new AbstractTransactedAction(errMsg) {
            @Override
            public void doInTransaction() throws RuntimeException {
                unitUnderTest.put(key, "valueFromTransaction");
                throw new RuntimeException();
            }
        });

        final Serializable result = unitUnderTest.get(key);

        // Assert
        Assert.assertNull(result);
    }

    @Test
    public void validateNullResultWhenRemovingNonexistentElement() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final String key = "key";

        // Act
        final Serializable result = unitUnderTest.remove(key);

        // Assert
        Assert.assertNull(result);
    }
}
