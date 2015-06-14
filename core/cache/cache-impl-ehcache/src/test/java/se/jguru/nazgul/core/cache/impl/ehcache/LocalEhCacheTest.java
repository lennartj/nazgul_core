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

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.cache.api.transaction.AbstractTransactedAction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalEhCacheTest extends AbstractCacheTest {

    /**
     * @return The classpath-relative configuration file for EhCache to be used within this TestCase.
     */
    @Override
    protected String getEhCacheConfiguration() {
        return "ehcache/config/LocalHostUnitTestStandaloneConfig.xml";
    }

    @Test
    public void validateNormalCacheLifecycle() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final String key = "key";

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
    }

    @Test
    public void validateCacheNotChangedonTransactionRollback() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final String key = "key";
        final String errMsg = "Oops.";

        // Act
        unitUnderTest.performTransactedAction(new AbstractTransactedAction(errMsg) {
            @Override
            public void doInTransaction() throws RuntimeException {
                unitUnderTest.put(key, "valueFromTransaction");
                throw new RuntimeException();
            }
        });

        final Serializable result = unitUnderTest.get(key);

        // Assert
        Assert.assertNull(result);
    }

    @Test
    public void validateNullResultWhenRemovingNonexistentElement() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final String key = "key";

        // Act
        final Serializable result = unitUnderTest.remove(key);

        // Assert
        Assert.assertNull(result);
    }

    @Test
    public void validateIterationOverAllKnownKeys() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = getCache();
        final Map<String, String> data = new HashMap<String, String>();
        data.put("fooo", "bar");
        data.put("gnat", "baz");

        for (String current : data.keySet()) {
            unitUnderTest.put(current, data.get(current));
        }

        // Act & Assert
        for (String current : unitUnderTest) {
            Assert.assertEquals(unitUnderTest.get(current), data.get(current));
        }
    }
}
