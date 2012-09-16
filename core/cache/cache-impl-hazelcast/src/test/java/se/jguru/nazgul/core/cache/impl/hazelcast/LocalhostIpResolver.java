/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.hazelcast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalhostIpResolver {

    public static InetAddress getLocalhostNonLoopbackAddress() {

        try {
            final InetAddress[] allByName = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            for(InetAddress current : allByName) {
                if(!current.isLoopbackAddress()) {
                    return current;
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
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
}
