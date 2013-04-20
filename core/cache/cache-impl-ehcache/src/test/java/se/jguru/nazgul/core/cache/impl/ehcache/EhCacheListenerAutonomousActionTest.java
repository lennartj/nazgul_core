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
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
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
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EhCacheListenerAutonomousActionTest extends AbstractCacheTest {

    // Shared state
    private int maxNumElements;

    @Override
    protected void performCustomSetup() {

        final String maxElementsInMemoryAttribute = "maxElementsInMemory=\"";

        final String configFile = XmlTestUtils.readFully(getEhCacheConfiguration());
        final BufferedReader buffer = new BufferedReader(new StringReader(configFile));

        try {
            for (String aLine = null; (aLine = buffer.readLine()) != null; ) {
                if (aLine.contains(maxElementsInMemoryAttribute)) {

                    int startIndex = aLine.indexOf(maxElementsInMemoryAttribute)
                            + maxElementsInMemoryAttribute.length();
                    int endIndex = aLine.indexOf("\"", startIndex + 1);
                    maxNumElements = Integer.parseInt(aLine.substring(startIndex, endIndex));
                    break;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not find [" + maxElementsInMemoryAttribute
                    + "] in EhCache configuration file [" + getEhCacheConfiguration() + "]");
        }
    }

    /**
     * @return The classpath-relative configuration file for EhCache to be used within this TestCase.
     */
    @Override
    protected final String getEhCacheConfiguration() {
        return "ehcache/config/LocalHostUnitTestQuickEvictionConfig.xml";
    }

    @Test
    public void validateEhCacheSignalsEvictionOnNextGet() throws InterruptedException {

        // Assemble
        final String valuePrefix = "value_";
        final String keyPrefix = "key_";
        final NonDistributedEhCache unitUnderTest = getCache();
        final CountDownLatch evictionLatch = new CountDownLatch(1);

        final MockCacheListener listener = new MockCacheListener("testListener");
        unitUnderTest.addListener(listener);
        listener.setEvictionLatch(evictionLatch);

        // Act #1: Populate the cache to its limit
        for (int i = 0; i < maxNumElements; i++) {
            unitUnderTest.put(keyPrefix + i, valuePrefix + i);
        }

        // Act #2: Put one more element in the cache.
        //         This should evict an existing element.
        unitUnderTest.put("foo", "bar");
        boolean onAutonomousEvictCalled1Time = evictionLatch.await(5, TimeUnit.SECONDS);

        // Assert
        Assert.assertTrue(onAutonomousEvictCalled1Time);
        final List<String> callTrace = listener.callStack;

        // The callTrace should be similar to the following:
        /*
         * onPut [key_0]: value_0,
         * onPut [key_1]: value_1,
         * onPut [key_2]: value_2,
         * onAutonomousEvict [key_0]: [B@451c0d60,
         * onPut [foo]: bar
         */

        Assert.assertEquals(maxNumElements + 2, callTrace.size());

        for(int i = 0; i < maxNumElements; i++) {
            Assert.assertEquals("onPut [key_" + i + "]: value_" + i, callTrace.get(i));
        }

        Assert.assertTrue(callTrace.get(callTrace.size()-2).startsWith("onAutonomousEvict [key_0]:"));
        Assert.assertEquals("onPut [foo]: bar", callTrace.get(callTrace.size() - 1));
    }

    @Test
    public void validateExpiry() throws InterruptedException {

        // Assemble
        final String valuePrefix = "value_";
        final String keyPrefix = "key_";
        final NonDistributedEhCache unitUnderTest = getCache();
        final CountDownLatch evictionLatch = new CountDownLatch(5);

        final MockCacheListener listener = new MockCacheListener("testListener");
        unitUnderTest.addListener(listener);
        listener.setEvictionLatch(evictionLatch);

        // Act #1: Populate the cache to its limit
        for (int i = 0; i < maxNumElements; i++) {
            unitUnderTest.put(keyPrefix + i, valuePrefix + i);
        }

        // Act #2: Ensure to wait longer than the expiry within the cache.
        final boolean reachedCountdownLimit = evictionLatch.await(3, TimeUnit.SECONDS);

        // Assert
        Assert.assertFalse(reachedCountdownLimit);

        for(int i = 0; i < maxNumElements; i++) {
            Assert.assertNull(unitUnderTest.get(keyPrefix + i));
        }

        final List<String> callTrace = listener.callStack;
        Assert.assertEquals(2 * maxNumElements, callTrace.size());
        for(int i = 0; i < maxNumElements; i++) {
            Assert.assertEquals("onPut [key_" + i + "]: value_" + i, callTrace.get(i));
        }
        for(int i = maxNumElements; i < 2 * maxNumElements; i++) {
            Assert.assertEquals("onAutonomousEvict [key_" + (i - maxNumElements) + "]: null", callTrace.get(i));
        }
        System.out.println("CallTrace: " + callTrace);

        /*
        onPut [key_0]: value_0,
        onPut [key_1]: value_1,
        onPut [key_2]: value_2,
        onAutonomousEvict [key_0]: null,
        onAutonomousEvict [key_1]: null,
        onAutonomousEvict [key_2]: null
         */
    }

    @Test
    public void validateEvictionActions() throws InterruptedException {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final String key = "Nyckel";
        final String value = "åäöÅÄÖ";
        final CountDownLatch evictionLatch = new CountDownLatch(3);

        final MockCacheListener listener = new MockCacheListener("testListener");
        unitUnderTest.addListener(listener);
        listener.setEvictionLatch(evictionLatch);

        // Act
        for (int i = 0; i < maxNumElements + 1; i++) {
            unitUnderTest.put(key + "_" + i, value + "_" + i);
        }

        boolean onAutonomousEvictCalled3Times = evictionLatch.await(2, TimeUnit.SECONDS);

        // Assert
        final List<String> callTrace = listener.callStack;

        for (int i = 0; i < maxNumElements; i++) {
            Assert.assertEquals("onPut [" + key + "_" + i + "]: " + value + "_" + i, callTrace.get(i));
        }

        // The next elements inserted causes EhCache to perform eviction.
        for (int i = maxNumElements; i < callTrace.size() - maxNumElements; i += 2) {

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
        final NonDistributedEhCache unitUnderTest = getCache();
        final String key = "Nyckel";
        final String value = "åäöÅÄÖ";
        final int numElements = maxNumElements - 1;

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
