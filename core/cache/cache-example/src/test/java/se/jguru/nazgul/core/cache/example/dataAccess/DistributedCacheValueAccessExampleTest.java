/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.example.dataAccess;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.cache.api.distributed.DistributedCache;
import se.jguru.nazgul.core.cache.example.AbstractCacheExample;

import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DistributedCacheValueAccessExampleTest extends AbstractCacheExample {

    @Test
    public void useCase2_putAndGetValuesInDistributedMap() {

        // Acquire the cache.
        final DistributedCache<String> cache = getCache();

        // 1: Get or create a distributed Map, and put a key/value pair in it.
        final String distMapID = "aClusterUniqueIdForTheDistributedMap";
        Map<String, String> distMap = cache.getDistributedMap(distMapID);
        distMap.put("foo", "bar");

        // 2: Get the value from the distributed Map.
        //    Note that this can be done in another class
        //    or even on another JVM, as long as that JVM
        //    is part of the same cluster as this one.
        //
        //    ... and, of course, that we use the same ID
        //    string to acquire the Map ...
        Map<String, String> theSameDistMapInAnotherJVM = cache.getDistributedMap(distMapID);
        final String value = theSameDistMapInAnotherJVM.get("foo");

        // 3: Remove the key/value pair from the distributed Map.
        Map<String, String> theSameDistMapInYetAnotherJVM = cache.getDistributedMap(distMapID);
        final String removedValue = theSameDistMapInYetAnotherJVM.remove("foo");

        // Assert
        Assert.assertEquals("bar", value);
        Assert.assertEquals("bar", removedValue);
    }
}
