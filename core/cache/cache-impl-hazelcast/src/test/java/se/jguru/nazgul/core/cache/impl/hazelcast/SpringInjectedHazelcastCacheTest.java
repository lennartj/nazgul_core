/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
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

import static org.junit.Assert.assertEquals;

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
