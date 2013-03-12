/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.core.cache.example.dataAccess;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.cache.api.Cache;
import se.jguru.nazgul.core.cache.example.AbstractCacheExample;

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
