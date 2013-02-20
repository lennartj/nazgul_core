/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.cache.impl.hazelcast.clients.HazelcastCacheMember;

import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheFactoryTest {

    // Shared config
    String ownIp = LocalhostIpResolver.getLocalHostAddress();
    int ownPort = 5701;

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullClusterID() {

        // Act & Assert
        HazelcastCacheMember.create(null, ownIp, ownPort, "irrelevant");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyClusterID() {

        // Act & Assert
        HazelcastCacheMember.create("", ownIp, ownPort, "irrelevant");
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullIp() {

        // Act & Assert
        HazelcastCacheMember.create("irrelevant", null, ownPort, "irrelevant");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyIp() {

        // Act & Assert
        HazelcastCacheMember.create("irrelevant", "", ownPort, "irrelevant");
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullClusterMembers() {

        // Act & Assert
        HazelcastCacheMember.create("irrelevant", ownIp, ownPort, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyClusterMembers() {

        // Act & Assert
        HazelcastCacheMember.create("irrelevant", ownIp, ownPort, "");
    }

    @Test
    public void validateExceptionOnIncorrectClusterMembersFormat() {

        // Act & Assert
        try {
            HazelcastCacheMember.create("irrelevant", ownIp, ownPort, "thisIsNotAnAddressAndPort");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().startsWith(
                    "Expected clusterMember definition on the form (ip/dns):(port)."));
        }
    }

    @Test(expected = NumberFormatException.class)
    public void validateExceptionOnIncorrectClusterMembersFormatWithoutPort() {

        // Act & Assert
        HazelcastCacheMember.create("irrelevant", ownIp, ownPort, "aDnsName:notAPort");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnIncorrectClusterPortNumberTooLow() {

        // Act & Assert
        HazelcastCacheMember.create("irrelevant", ownIp, ownPort, "dnsAddress:-2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnIncorrectClusterPortNumberTooHigh() {

        // Act & Assert
        HazelcastCacheMember.create("irrelevant", ownIp, ownPort, "dnsAddress:100200");
    }

    @Test
    public void validateExceptionOnUnresolvableDnsName() {

        // Act & Assert
        try {
            HazelcastCacheMember.create("irrelevant", ownIp, ownPort, "non.existing.dns:4242");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getCause() instanceof UnknownHostException);
        }
    }

    @Test
    public void validateOkFactoryMethodCall() {

        // Assemble
        final String clusterID = "unitTestCluster";
        final String clusterMembers = ownIp + ":10000," + ownIp + ":10001";
        final String mainConfigurationFile = "config/cache/hazelcast/MainConfiguration.xml";

        // Act
        final HazelcastCacheMember cache1 = HazelcastCacheMember.create(clusterID, ownIp,
                10000, clusterMembers, mainConfigurationFile);
        final HazelcastCacheMember cache2 = HazelcastCacheMember.create(clusterID, ownIp,
                10001, clusterMembers, mainConfigurationFile);

        final HazelcastInstance instance1 = getInstance(cache1);
        final HazelcastInstance instance2 = getInstance(cache2);

        final Cluster cluster1 = instance1.getCluster();
        final Cluster cluster2 = instance2.getCluster();

        final List<Member> members1 = new ArrayList<Member>(cluster1.getMembers());
        final List<Member> members2 = new ArrayList<Member>(cluster2.getMembers());

        // Assert
        Assert.assertEquals(members1.size(), members2.size());
        Assert.assertEquals(10000, (int) getSortedPortNumbers(members1).get(0));
        Assert.assertEquals(10001, (int) getSortedPortNumbers(members1).get(1));

        Assert.assertEquals(10000, (int) getSortedPortNumbers(members2).get(0));
        Assert.assertEquals(10001, (int) getSortedPortNumbers(members2).get(1));

    }

    /*
    @Test
    public void validateCreateCacheClient() {

        // Assemble
        final String ip = "127.0.0.1";
        final int port = 10010;
        final String clusterMembers = ip + ":" + port;
        final String mainConfigurationFile = "config/cache/hazelcast/MainConfiguration.xml";

        final HazelcastCacheMember cacheMember = HazelcastCacheMember.create(
                "testClusterID",
                ip,
                port,
                clusterMembers,
                mainConfigurationFile);

        final ClusterNodeInfo clusterNodeInfo = new ClusterNodeInfo(cluster, ip, port);
        final CacheFactory unitUndertest = new HazelcastCacheFactory();
        final String key = "test.key";
        final String value = "some value";

        // Act
        final DistributedCache<String> cacheClient = unitUndertest.createCacheClient(clusterNodeInfo);

        final String before = (String) cacheMember.get(key);
        cacheClient.put(key, value);
        final String after = (String) cacheMember.get(key);

        // Assert
        Assert.assertNotNull(cacheClient);
        Assert.assertNull(before);
        Assert.assertEquals(value, after);
    }
    */

    //
    // Private helpers
    //
    private List<Integer> getSortedPortNumbers(List<Member> members) {

        List<Integer> toReturn = new ArrayList<Integer>();

        for (Member current : members) {
            toReturn.add(current.getInetSocketAddress().getPort());
        }

        Collections.sort(toReturn);
        return toReturn;
    }

    private HazelcastInstance getInstance(HazelcastCacheMember cache) {

        try {
            Field toReturnField = cache.getClass().getSuperclass().getDeclaredField("cacheInstance");
            if (!toReturnField.isAccessible()) {
                toReturnField.setAccessible(true);
            }

            return (HazelcastInstance) toReturnField.get(cache);
        } catch (Exception e) {
            throw new IllegalStateException("whoops");
        }
    }
}
