/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.example;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.distributed.async.DestinationProvider;
import se.jguru.nazgul.core.cache.impl.hazelcast.AbstractHazelcastInstanceWrapper;
import se.jguru.nazgul.core.cache.impl.hazelcast.clients.HazelcastCacheMember;

import java.lang.reflect.Field;
import java.net.URL;

/**
 * Common functionality for cache examples.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractCacheExample {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractCacheExample.class);

    // Shared state
    private DestinationProvider<String> cache;

    /**
     * Acquires a AbstractHazelcastInstanceWrapper instance, invigorated by the provided
     * configuration file.
     *
     * @param configFile The classpath-relative resource path to a Hazelcast configuration file.
     * @return A fully set-up AbstractHazelcastInstanceWrapper instance.
     */
    protected static HazelcastCacheMember getCache(final String configFile) {

        // Add a local, non-loopback interface.
        return new HazelcastCacheMember(HazelcastCacheMember.readConfigFile(configFile));
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

    protected DestinationProvider<String> getCache() {

        if(cache == null) {
            cache = getCache("config/hazelcast/StandaloneConfig.xml");
        }

        // All done.
        return cache;
    }

    /*
    protected void purgeCache(final AbstractHazelcastInstanceWrapper cache) {
        if (cache == null) {
            log.warn("Can not purge null cache");
            return;
        }
        try {
            final HazelcastInstance current = getInternalInstance(cache);

            for (final Instance instance : current.getInstances()) {
                final String instanceId = (String) instance.getId();
                // We don't want to destroy our internal maps for the cache
                if (!(instanceId.endsWith(GridOperations.CLUSTER_ADMIN_TOPIC))) {

                    final Instance.InstanceType instanceType = instance.getInstanceType();

                    // see http://code.google.com/p/hazelcast/issues/detail?id=516
                    if (instanceType.isMultiMap()) {
                        MultiMap map = (MultiMap) instance;
                        map.clear();
                    } else if (instanceType.isMap()) {
                        Map map = (Map) instance;
                        map.clear();
                    } else if (instanceType.isList() || instanceType.isSet() || instanceType.isQueue()) {
                        Collection collection = (Collection) instance;
                        collection.clear();
                    }

                    final Set<String> listeners = new TreeSet<String>(cache.getListenerIDsFor(instance));
                    for (final String listenerId : listeners) {
                        cache.removeListenerFor(instance, listenerId);
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

        } catch (final Exception e) {
            log.warn("Unable to purge cache", e);
        }
    }
    */

    protected HazelcastInstance getInternalInstance(final AbstractHazelcastInstanceWrapper cache) {

        try {
            Field instanceField = cache.getClass().getSuperclass().getDeclaredField("cacheInstance");
            instanceField.setAccessible(true);

            return (HazelcastInstance) instanceField.get(cache);

        } catch (Exception e) {
            throw new IllegalArgumentException("Could not acquire the cacheInstance", e);
        }
    }
}
