/*-
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-ehcache
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EhCacheListenerAdapterTest extends AbstractCacheTest {

    /**
     * @return The classpath-relative configuration file for EhCache to be used within this TestCase.
     */
    @Override
    protected String getEhCacheConfiguration() {
        return "ehcache/config/LocalHostUnitTestStandaloneConfig.xml";
    }

    @Test
    public void validateSilentRemovalOfNonExistentCacheListener() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final MockCacheListener listener1 = new MockCacheListener("listener1");
        final MockCacheListener listener2 = new MockCacheListener("listener2");

        // Act & Assert
        unitUnderTest.removeListener(listener1.getClusterId());
        unitUnderTest.addListener(listener1);
        unitUnderTest.removeListener(listener2.getClusterId());
    }

    @Test
    public void validateIgnoringAddingIdenticalListeners() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final MockCacheListener listener = new MockCacheListener("listener");

        // Act
        final List<String> before = unitUnderTest.getListenerIds();
        unitUnderTest.addListener(listener);
        final List<String> mid = unitUnderTest.getListenerIds();
        unitUnderTest.addListener(listener);
        final List<String> after = unitUnderTest.getListenerIds();

        // Assert
        Assert.assertEquals(0, before.size());
        Assert.assertEquals(1, mid.size());
        Assert.assertEquals(listener.getClusterId(), mid.get(0));
        Assert.assertEquals(1, after.size());
        Assert.assertEquals(listener.getClusterId(), after.get(0));
    }

    @Test
    public void validateCloningEhCacheListenerAdapter() throws Exception {

        // Assemble
        final MockCacheListener listener = new MockCacheListener("listener");
        final EhCacheListenerAdapter unitUnderTest = new EhCacheListenerAdapter(listener);

        // Act
        final EhCacheListenerAdapter result = (EhCacheListenerAdapter) unitUnderTest.clone();

        // Assert
        Assert.assertNotSame(unitUnderTest, result);
        Assert.assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNullListener() {

        // Assemble
        final MockCacheListener incorrectNull = null;

        // Act & Assert
        new EhCacheListenerAdapter(incorrectNull);
    }

    @Test
    public void validateLocalEhCacheListenerLifecycle() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final MockCacheListener listener = new MockCacheListener("listener");

        // Act
        final List<String> before = unitUnderTest.getListenerIds();
        unitUnderTest.addListener(listener);
        final List<String> mid = unitUnderTest.getListenerIds();
        unitUnderTest.removeListener(listener.getClusterId());
        final List<String> after = unitUnderTest.getListenerIds();

        // Assert
        Assert.assertEquals(0, before.size());
        Assert.assertEquals(1, mid.size());
        Assert.assertEquals(listener.getClusterId(), mid.get(0));
        Assert.assertEquals(0, after.size());
    }

    @Test
    public void validateEhCacheListenerAdapterCallbacks() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final String key = "key";

        final MockCacheListener listener = new MockCacheListener("testListener");
        unitUnderTest.addListener(listener);

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

        final List<String> callTrace = listener.callStack;

        // onPut [key]: value, onUpdate [key]: null --> value2, onRemove [key]: value2
        Assert.assertEquals(3, callTrace.size());
        Assert.assertEquals(callTrace.get(0), "onPut [" + key + "]: value");
        Assert.assertEquals(callTrace.get(1), "onUpdate [" + key + "]: null --> value2");
        Assert.assertEquals(callTrace.get(2), "onRemove [" + key + "]: value2");
    }

    @Test
    public void validateThatTheHashCodeCorrespondsToTheListenerHashCode() {

        // Assemble
        final MockCacheListener listener = new MockCacheListener("listener");
        final int expectedHashCode = listener.hashCode();
        final EhCacheListenerAdapter unitUnderTest = new EhCacheListenerAdapter(listener);

        // Act
        final int actualHashCode = unitUnderTest.hashCode();

        // Assert
        Assert.assertEquals(expectedHashCode, actualHashCode);
    }

    @Test
    public void validateThatToStringCorrespondsToTheListenerId() {

        // Assemble
        final MockCacheListener listener = new MockCacheListener("listener");
        final String expectedToString = listener.getClusterId();
        final EhCacheListenerAdapter unitUnderTest = new EhCacheListenerAdapter(listener);

        // Act
        final String actualToString = unitUnderTest.toString();

        // Assert
        Assert.assertEquals(expectedToString, actualToString);
    }

    @Test
    public void validateEquality() {

        // Assemble
        final MockCacheListener listener = new MockCacheListener("listener");
        final EhCacheListenerAdapter adapter1 = new EhCacheListenerAdapter(listener);
        final EhCacheListenerAdapter adapter2 = new EhCacheListenerAdapter(listener);

        // Act & Assert
        Assert.assertFalse(adapter1.equals("foo"));
        Assert.assertFalse(adapter1.equals(null));
        Assert.assertTrue(adapter1.equals(adapter1));
        Assert.assertTrue(adapter1.equals(adapter2));
    }

}
