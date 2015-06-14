/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
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
package se.jguru.nazgul.core.cache.impl.hazelcast;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.hazelcast.config.Config;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.impl.hazelcast.clients.HazelcastCacheMember;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.GridOperations;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Trivial abstract class which shuts down Hazelcast completely after each testcase.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractHazelcastCacheTest {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractHazelcastCacheTest.class);

    public static final String DEFAULT_LOGBACK_CONFIGURATION_PATH = "config/logging/logback-test.xml";

    /**
     * Acquires a AbstractHazelcastInstanceWrapper instance, invigorated by the provided
     * configuration file.
     *
     * @param configFile The classpath-relative resource path to a Hazelcast configuration file.
     * @return A fully set-up AbstractHazelcastInstanceWrapper instance.
     */
    protected static HazelcastCacheMember getCache(final String configFile) {

        // Add a local, non-loopback interface.
        final Config config = HazelcastCacheMember.readConfigFile(configFile);

        final TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
        tcpIpConfig.addMember(LocalhostIpResolver.getLocalHostAddress());
        log.info("Got Config: " + config);

        return new HazelcastCacheMember(config);
    }

    public static void configureLogging() {
        configureLogging(DEFAULT_LOGBACK_CONFIGURATION_PATH);
    }

    public static void configureLogging(final String logbackConfigResource) {

        // Really close the Hazelcast cluster.
        Hazelcast.shutdownAll();

        // Make certain that Hazelcast uses the slf4j logging factory.
        System.setProperty("hazelcast.logging.type", "slf4j");

        // Load the Logback configuration using the native JoranConfigurator
        final ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
        final URL logbackConfiguration = ctxClassLoader.getResource(logbackConfigResource);

        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);

        try {
            configurator.doConfigure(logbackConfiguration);
        } catch (JoranException e) {
            e.printStackTrace();
        }

        if (log.isDebugEnabled()) {
            log.debug("Read SLF4J config from: " + logbackConfiguration.toString());
        }
    }

    @AfterClass
    public static void teardownHazelcastCacheInstance() {
        Hazelcast.shutdownAll();
        Hazelcast.shutdownAll();
    }

    protected void purgeCache(final AbstractHazelcastInstanceWrapper cache) {
        if (cache == null) {
            log.warn("Can not purge null cache");
            return;
        }
        try {
            final HazelcastInstance current = getInternalInstance(cache);

            if (current != null) {
                for (final DistributedObject currentDistributedObject : current.getDistributedObjects()) {

                    final String instanceName = currentDistributedObject.getName();

                    // We don't want to destroy our internal maps for the cache
                    if (!(instanceName.endsWith(GridOperations.CLUSTER_ADMIN_TOPIC))) {

                        if (currentDistributedObject instanceof MultiMap) {
                            MultiMap map = (MultiMap) currentDistributedObject;
                            map.clear();
                        } else if (currentDistributedObject instanceof Map) {
                            Map map = (Map) currentDistributedObject;
                            map.clear();
                        } else if (currentDistributedObject instanceof Collection) {
                            Collection collection = (Collection) currentDistributedObject;
                            collection.clear();
                        }

                        final Set<String> listeners = new TreeSet<String>(cache.getListenerIDsFor(currentDistributedObject));
                        for (final String listenerId : listeners) {
                            cache.removeListenerFor(currentDistributedObject, listenerId);
                        }

                        //                    can't use this - it clears internal state of hazelcast
                        //                    if (!(instanceId.endsWith(CLUSTERWIDE_SHARED_CACHE_MAP) ||
                        //                            instanceId.endsWith(CLUSTERWIDE_LISTENERID_MAP))) {
                        //                        instance.destroy();
                        //                    }
                    }
                }

                final Set<String> listeners = new TreeSet<String>(cache.getLocallyRegisteredListeners().keySet());
                for (final String listenerId : listeners) {
                    cache.removeInstanceListener(listenerId);
                }
            }

        } catch (final Exception e) {
            log.warn("Unable to purge cache", e);
        }
    }

    protected HazelcastInstance getInternalInstance(final AbstractHazelcastInstanceWrapper cache) {

        final String fieldName = "cacheInstance";

        try {
            Field instanceField = cache.getClass().getSuperclass().getDeclaredField(fieldName);
            instanceField.setAccessible(true);

            final HazelcastInstance toReturn = (HazelcastInstance) instanceField.get(cache);
            if (toReturn == null) {
                log.warn("Found null HazelcastInstance in field '" + fieldName + "'.");
            }

            return toReturn;

        } catch (Exception e) {
            throw new IllegalArgumentException("Could not acquire the cacheInstance", e);
        }
    }
}
