/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.example;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.cache.api.Cache;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CacheValueAccessExampleTest extends AbstractCacheExample {

    @Test
    public void useCase1_putAndGetValuesInCache() throws InterruptedException {

        // Acquire the cache.
        final Cache<String> cache = getCache();

        // 1: Put a value in the cache.
        cache.put("foo", "bar");

        // 2: Simulate some work in the system
        Thread.sleep(200);

        // 3: Get the value from the cache.
        //
        //    NOTE: You will get the same result even if you
        //    execute this call in a DistributedCache instance
        //    within the same group running in another JVM.
        //
        final String value = (String) cache.get("foo");


        // Assert
        Assert.assertEquals("bar", value);
    }
}
