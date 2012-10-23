/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.hazelcast;

import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalhostIpResolver {

    // Internal state
    private static final Filter<InetAddress> ipv4LoopbackAddressFilter = new Filter<InetAddress>() {
        @Override
        public boolean accept(final InetAddress candidate) {
            return candidate instanceof Inet4Address && candidate.isLoopbackAddress();
        }
    };
    private static final Filter<InetAddress> ipv4NonLoopbackAddressFilter = new Filter<InetAddress>() {
        @Override
        public boolean accept(final InetAddress candidate) {
            return candidate instanceof Inet4Address && !candidate.isLoopbackAddress();
        }
    };

    public static InetAddress getLocalhostNonLoopbackAddress() {

        try {
            final List<InetAddress> allByName = Arrays.asList(
                    InetAddress.getAllByName(InetAddress.getLocalHost().getHostName()));

            // Get all loopback adresses
            final List<InetAddress> ipV4LoopbackAddresses = CollectionAlgorithms.filter(
                    allByName, ipv4LoopbackAddressFilter);

            // Get all non-loopback adresses
            final List<InetAddress> ipv4NonLoopbackAddressAddresses = CollectionAlgorithms.filter(
                    allByName, ipv4NonLoopbackAddressFilter);

            if(ipv4NonLoopbackAddressAddresses.size() > 0) {
                return ipv4NonLoopbackAddressAddresses.get(0);
            }

            if(ipV4LoopbackAddresses.size() > 0) {
                return ipV4LoopbackAddresses.get(0);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("Currently, an IPv4 address is required "
                + "to build the HazelcastCache implementation module.");
    }

    public static String getLocalHostAddress() {
        return getLocalhostNonLoopbackAddress().getHostAddress();
    }

    public static String getClusterIpAddresses(final List<String> ports) {

        StringBuilder builder = new StringBuilder();
        for(String current : ports) {
            builder.append(getLocalHostAddress()).append(":").append(current).append(",");
        }

        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }


    public static void main(String[] args) throws Exception {

        final InetAddress[] allByName = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());

        for(InetAddress current : allByName) {
            System.out.println("Got: " + current + ", v4: " + (current instanceof Inet4Address)
                    + ", Loopback: " + current.isLoopbackAddress());
        }

        System.out.println("Got2: " + LocalhostIpResolver.getLocalhostNonLoopbackAddress());
    }
}
