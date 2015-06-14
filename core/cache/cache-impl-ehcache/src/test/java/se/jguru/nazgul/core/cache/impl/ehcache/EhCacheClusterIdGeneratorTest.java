/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-ehcache
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EhCacheClusterIdGeneratorTest extends AbstractCacheTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getEhCacheConfiguration() {
        return "ehcache/config/LocalHostUnitTestStandaloneConfig.xml";
    }

    @Test(expected = NullPointerException.class)
    public void validateNullPointerExceptionIfNotSettingCacheCluster() {

        // Assemble
        final EhCacheClusterIdGenerator unitUnderTest = new EhCacheClusterIdGenerator();

        // Act & Assert
        Assert.assertFalse(unitUnderTest.isIdentifierAvailable());
        unitUnderTest.getIdentifier();
    }

    @Test
    public void validateIdRetrieval() {

        // Assemble
        final EhCacheClusterIdGenerator unitUnderTest = new EhCacheClusterIdGenerator();
        unitUnderTest.setCacheManager(getCache().getCacheInstance().getCacheManager());

        // Act
        Assert.assertTrue(unitUnderTest.isIdentifierAvailable());
        final String identifier = unitUnderTest.getIdentifier();

        // Assert
        Assert.assertNotNull(identifier);
        Assert.assertEquals(getCache().getClusterId(), identifier);
    }
}
