/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.hazelcast.clients;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.cache.impl.hazelcast.LocalhostIpResolver;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PropertyHolderTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullProperties() {

        // Assemble
        final Map<String, String> propertyMap = null;

        // Act & Assert
        new PropertyHolder(propertyMap);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullPort() {

        // Assemble
        final Map<String, String> propertyMap = getPopulatedPropertyMap();
        propertyMap.remove(PropertyHolder.CACHE_PORT_KEY);

        // Act & Assert
        new PropertyHolder(propertyMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyPort() {

        // Assemble
        final Map<String, String> propertyMap = getPopulatedPropertyMap();
        propertyMap.put(PropertyHolder.CACHE_PORT_KEY, "");

        // Act & Assert
        new PropertyHolder(propertyMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNonIntegerPort() {

        // Assemble
        final Map<String, String> propertyMap = getPopulatedPropertyMap();
        propertyMap.put(PropertyHolder.CACHE_PORT_KEY, "notANumber");

        // Act & Assert
        new PropertyHolder(propertyMap);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullCluster() {

        // Assemble
        final Map<String, String> propertyMap = getPopulatedPropertyMap();
        propertyMap.remove(PropertyHolder.CACHE_CLUSTER_KEY);

        // Act & Assert
        new PropertyHolder(propertyMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyCluster() {

        // Assemble
        final Map<String, String> propertyMap = getPopulatedPropertyMap();
        propertyMap.put(PropertyHolder.CACHE_CLUSTER_KEY, "");

        // Act & Assert
        new PropertyHolder(propertyMap);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullMembers() {

        // Assemble
        final Map<String, String> propertyMap = getPopulatedPropertyMap();
        propertyMap.remove(PropertyHolder.CACHE_MEMBERS_KEY);

        // Act & Assert
        new PropertyHolder(propertyMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyMembers() {

        // Assemble
        final Map<String, String> propertyMap = getPopulatedPropertyMap();
        propertyMap.put(PropertyHolder.CACHE_MEMBERS_KEY, "");

        // Act & Assert
        new PropertyHolder(propertyMap);
    }

    @Test
    public void validateNormalLifecycle() {

        // Assemble
        final Map<String, String> propertyMap = getPopulatedPropertyMap();
        final PropertyHolder unitUnderTest = new PropertyHolder(propertyMap);

        // Act
        final int localListeningPort = unitUnderTest.getLocalListeningPort();
        final String cluster = unitUnderTest.getCacheClusterId();
        final String members = unitUnderTest.getCacheMembers();

        // Assert
        Assert.assertEquals(5701, localListeningPort);
        Assert.assertEquals("nazgul_foo", cluster);
        Assert.assertEquals(LocalhostIpResolver.getLocalHostAddress() + ":5710,10.0.0.1:5701", members);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnUsingNonPrimedSystemProperties() {

        // Act & Assert
        new PropertyHolder();
    }

    @Test
    public void validateNormalLifecycleUsingPrimedSystemProperties() {

        // Assemble
        final String members = "daMembers";
        final String cluster = "daCluster";
        final String listeningIp = LocalhostIpResolver.getLocalHostAddress();
        final Integer listeningPort = 5701;
        System.setProperty(PropertyHolder.CACHE_CLUSTER_KEY, cluster);
        System.setProperty(PropertyHolder.CACHE_MEMBERS_KEY, members);
        System.setProperty(PropertyHolder.CACHE_IP_KEY, listeningIp);
        System.setProperty(PropertyHolder.CACHE_PORT_KEY, "" + listeningPort);

        // Act
        final PropertyHolder unitUnderTest = new PropertyHolder();

        // Assert
        Assert.assertEquals(members, unitUnderTest.getCacheMembers());
        Assert.assertEquals(cluster, unitUnderTest.getCacheClusterId());
        Assert.assertEquals(listeningIp, unitUnderTest.getLocalListeningIp());
        Assert.assertEquals(listeningPort, unitUnderTest.getLocalListeningPort());

        System.clearProperty(PropertyHolder.CACHE_CLUSTER_KEY);
        System.clearProperty(PropertyHolder.CACHE_MEMBERS_KEY);
        System.clearProperty(PropertyHolder.CACHE_IP_KEY);
        System.clearProperty(PropertyHolder.CACHE_PORT_KEY);
    }

    //
    // Private helpers
    //

    private TreeMap<String, String> getPopulatedPropertyMap() {

        TreeMap<String, String> toReturn = new TreeMap<String, String>();

        toReturn.put(PropertyHolder.CACHE_IP_KEY, LocalhostIpResolver.getLocalHostAddress());
        toReturn.put(PropertyHolder.CACHE_PORT_KEY, "5701");
        toReturn.put(PropertyHolder.CACHE_CLUSTER_KEY, "nazgul_foo");
        toReturn.put(PropertyHolder.CACHE_MEMBERS_KEY, LocalhostIpResolver.getLocalHostAddress()
                + ":5710,10.0.0.1:5701");

        return toReturn;
    }
}
