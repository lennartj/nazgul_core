/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.api.distributed;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DistributedCacheTest {

    @Test
    public void validateDistributedTypes() {

        // Assemble
        final DistributedCache.DistributedCollectionType[] values = DistributedCache.DistributedCollectionType.values();
        final List<String> expectedTypes = Arrays.asList("COLLECTION", "SET", "QUEUE");

        // Act

        // Assert
        Assert.assertEquals(values.length, expectedTypes.size());
        for(int i = 0; i < values.length; i++) {
            Assert.assertEquals(expectedTypes.get(i), values[i].toString());
        }
    }
}
