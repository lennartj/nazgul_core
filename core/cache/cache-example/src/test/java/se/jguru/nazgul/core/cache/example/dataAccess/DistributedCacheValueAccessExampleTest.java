/*
 * #%L
 * Nazgul Project: nazgul-core-cache-example
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
