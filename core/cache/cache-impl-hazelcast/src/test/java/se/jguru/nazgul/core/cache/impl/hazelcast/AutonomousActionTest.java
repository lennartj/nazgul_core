/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.impl.hazelcast.clients.HazelcastCacheMember;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AutonomousActionTest extends AbstractHazelcastCacheTest {

    private static final String LOGBACK_CONFIGURATION_PATH = "config/logging/logback-test.xml";

    private static final Logger log = LoggerFactory.getLogger(AutonomousActionTest.class);
    // Shared state
    final String quickEvictionMapId = "quickEvictionMap";
    final String key = "foo";
    final String value = "bar";
    final String quickEvictConfigFile = "config/hazelcast/QuickEvictionConfig.xml";

    @BeforeClass
    public static void initialize() {
        configureLogging(LOGBACK_CONFIGURATION_PATH);
    }

    @Test
    public void validateQuickEvictionConfigurationFile() {

        // Assemble
        final int expectedMaxSize = 4;
        final Config config = HazelcastCacheMember.readConfigFile(quickEvictConfigFile);

        // Act
        final MapConfig mapConfig = config.getMapConfig(quickEvictionMapId);
        final int maxSize = mapConfig.getMaxSizeConfig().getSize();

        // Assert
        Assert.assertEquals("The configuration file is corrupt. The map '" + quickEvictionMapId + "' must be defined.",
                quickEvictionMapId, mapConfig.getName());
        Assert.assertEquals("Corrupt configuration file. The map '" + quickEvictionMapId
                + "' must define max-size as " + expectedMaxSize + ".", expectedMaxSize, maxSize);
    }

    @Test
    public void validateEvictionInLifecycle() throws InterruptedException {

        // Assemble
        final Config config = HazelcastCacheMember.readConfigFile(quickEvictConfigFile);
        final MapConfig mapConfig = config.getMapConfig(quickEvictionMapId);

        final HazelcastCacheMember cache = new HazelcastCacheMember(config);
        final Map<String, String> quickEvictionMap = cache.getDistributedMap(quickEvictionMapId);
        final DebugCacheListener unitUnderTest1 = new DebugCacheListener("listener_1");

        cache.addListenerFor(quickEvictionMap, unitUnderTest1);
        Thread.sleep(200);

        // Act
        putValues(mapConfig.getMaxSizeConfig().getSize() + 1, quickEvictionMap);
        final TreeMap<String, String> result = getValueMap(quickEvictionMap);

        // Assert
        final String shouldBeEvicted = key + "_" + 0;
        Assert.assertEquals(mapConfig.getMaxSizeConfig().getSize(), result.size());
        Assert.assertFalse(result.keySet().contains(shouldBeEvicted));

        for (int i = 1; i <= mapConfig.getMaxSizeConfig().getSize(); i++) {
            String currentKey = key + "_" + i;
            log.debug("Validating key '" + currentKey + "'");
            Assert.assertTrue(result.keySet().contains(currentKey));
        }
    }

    private void putValues(final int numValues, final Map<String, String> map) {
        for (int i = 0; i < numValues; i++) {
            map.put(key + "_" + i, value + "_" + i);
        }
    }

    private TreeMap<String, String> getValueMap(final Map<String, String> source) {
        TreeMap<String, String> toReturn = new TreeMap<String, String>();
        for (String current : source.keySet()) {
            toReturn.put(current, source.get(current));
        }

        return toReturn;
    }
}
