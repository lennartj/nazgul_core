/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.Hazelcast;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.jguru.nazgul.core.cache.api.Cache;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/config/spring/cache-applicationContext.xml")
public class SpringInjectedHazelcastCacheTest {

    @Inject
    @Named(value = "cacheOne")
    private Cache<String> cacheOne;

    @Inject
    @Named(value = "cacheTwo")
    private Cache<String> cacheTwo;

    @AfterClass
    public static void tearDownHazelcastCacheInstance() {
        Hazelcast.shutdownAll();
        Hazelcast.shutdownAll();
    }

    @Test
    public void validateThatCacheIsInitialized() throws InterruptedException {

        // Assemble
        final String key = "test.key";
        final String value = "Test Value";

        // Act
        cacheOne.put(key, value);
        Thread.sleep(300);
        final Serializable cacheOneValue = cacheOne.get(key);
        final Serializable cacheTwoValue = cacheTwo.get(key);

        // Assert
        assertEquals(value, cacheOneValue);
        assertEquals(value, cacheTwoValue);
    }
}
