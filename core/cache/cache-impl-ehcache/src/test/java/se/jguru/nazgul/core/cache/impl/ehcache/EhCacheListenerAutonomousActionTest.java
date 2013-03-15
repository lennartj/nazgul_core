/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-ehcache
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.cache.impl.ehcache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.TransactionController;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EhCacheListenerAutonomousActionTest extends AbstractCacheTest {

    /**
     * @return The classpath-relative configuration file for EhCache to be used within this TestCase.
     */
    @Override
    protected String getEhCacheConfiguration() {
        return "ehcache/config/LocalHostUnitTestQuickEvictionConfig.xml";
    }

    @Test
    public void validateEvictionActions() throws InterruptedException {

        // Assemble
        final int maxElementsInMemory = 5;
        final NonDistributedEhCache unitUnderTest = getCache();
        final String key = "Nyckel";
        final String value = "åäöÅÄÖ";
        final CountDownLatch evictionLatch = new CountDownLatch(3);

        final MockCacheListener listener = new MockCacheListener("testListener");
        unitUnderTest.addListener(listener);
        listener.setEvictionLatch(evictionLatch);

        // Act
        for (int i = 0; i < maxElementsInMemory + 1; i++) {
            unitUnderTest.put(key + "_" + i, value + "_" + i);
        }

        boolean onAutonomousEvictCalled3Times = evictionLatch.await(5, TimeUnit.SECONDS);

        // Assert
        final List<String> callTrace = listener.callStack;

        for (int i = 0; i < maxElementsInMemory; i++) {
            Assert.assertEquals("onPut [" + key + "_" + i + "]: " + value + "_" + i, callTrace.get(i));
        }

        // The next elements inserted causes EhCache to perform eviction.
        for (int i = maxElementsInMemory; i < callTrace.size() - maxElementsInMemory; i += 2) {

            Assert.assertEquals("onAutonomousEvict [" + key + "_" + (i - 1) + "]: ", callTrace.get(i));
            Assert.assertEquals("onPut [" + key + "_" + i + "]: " + value + "_" + i, callTrace.get(i + 1));
        }

        // The last element is expired, yielding a null value.
        Assert.assertTrue(onAutonomousEvictCalled3Times);
        Assert.assertTrue(callTrace.get(callTrace.size() - 1).startsWith("onAutonomousEvict [" + key + "_"));
        Assert.assertTrue(callTrace.get(callTrace.size() - 1).endsWith("]: null"));

        /*
         * onPut [Nyckel_0]: åäöÅÄÖ_0,
         * onPut [Nyckel_1]: åäöÅÄÖ_1,
         * onPut [Nyckel_2]: åäöÅÄÖ_2,
         * onPut [Nyckel_3]: åäöÅÄÖ_3,
         * onPut [Nyckel_4]: åäöÅÄÖ_4,
         * onPut [Nyckel_5]: åäöÅÄÖ_5,
         * onAutonomousEvict [Nyckel_4]: null,
         * onAutonomousEvict [Nyckel_5]: null,
         * onAutonomousEvict [Nyckel_2]: null
         */
    }

    @Test
    public void validateClear() throws InterruptedException {

        // Assemble
        final int maxElementsInMemory = 5;
        final NonDistributedEhCache unitUnderTest = getCache();
        final String key = "Nyckel";
        final String value = "åäöÅÄÖ";
        final int numElements = maxElementsInMemory - 1;

        final MockCacheListener listener = new MockCacheListener("testListener");
        unitUnderTest.addListener(listener);

        final Ehcache instance = unitUnderTest.getCacheInstance();
        final TransactionController tx = instance.getCacheManager().getTransactionController();


        // Act
        for (int i = 0; i < numElements; i++) {
            unitUnderTest.put(key + "_" + i, value + "_" + i);
        }

        tx.begin();
        instance.removeAll();
        tx.commit();

        // Assert
        final List<String> callTrace = listener.callStack;

        for (int i = 0; i < numElements; i++) {
            Assert.assertEquals("onPut [" + key + "_" + i + "]: " + value + "_" + i, callTrace.get(i));
        }
        Assert.assertEquals("onClear", callTrace.get(callTrace.size() - 1));
    }
}
