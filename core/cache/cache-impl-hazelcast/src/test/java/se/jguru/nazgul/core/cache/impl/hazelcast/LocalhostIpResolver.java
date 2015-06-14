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

import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalhostIpResolver {

    // Internal state
    private static final Filter<InterfaceAddress> ipv4LoopbackAddressFilter = new Filter<InterfaceAddress>() {
        @Override
        public boolean accept(final InterfaceAddress candidate) {
            return candidate != null
                    && candidate.getAddress() instanceof Inet4Address
                    && candidate.getAddress().isLoopbackAddress();
        }
    };
    private static final Filter<InterfaceAddress> ipv4NonLoopbackAddressFilter = new Filter<InterfaceAddress>() {
        @Override
        public boolean accept(final InterfaceAddress candidate) {
            return candidate != null
                    && candidate.getAddress() instanceof Inet4Address
                    && !candidate.getAddress().isLoopbackAddress();
        }
    };
    private static final Transformer<InterfaceAddress, InetAddress> addressTransformer
            = new Transformer<InterfaceAddress, InetAddress>() {
        @Override
        public InetAddress transform(InterfaceAddress input) {
            return input.getAddress();
        }
    };

    public static InetAddress getLocalhostNonLoopbackAddress() {

        try {
            final List<InetAddress> ipv4NonLoopbackAddresses = new ArrayList<InetAddress>();
            final List<InetAddress> ipv4LoopbackAddresses = new ArrayList<InetAddress>();

            for (NetworkInterface current : Collections.list(NetworkInterface.getNetworkInterfaces())) {

                // Find all non-loopback addresses
                final List<InterfaceAddress> nonLoopbackAddresses = CollectionAlgorithms.filter(
                        current.getInterfaceAddresses(), ipv4NonLoopbackAddressFilter);
                ipv4NonLoopbackAddresses.addAll(
                        CollectionAlgorithms.transform(nonLoopbackAddresses, addressTransformer));

                // Find all loopback addresses
                final List<InterfaceAddress> loopbackAddresses = CollectionAlgorithms.filter(
                        current.getInterfaceAddresses(), ipv4LoopbackAddressFilter);
                ipv4LoopbackAddresses.addAll(
                        CollectionAlgorithms.transform(loopbackAddresses, addressTransformer));
            }

            if (ipv4NonLoopbackAddresses.size() > 0) {
                return ipv4NonLoopbackAddresses.get(0);
            }

            if (ipv4LoopbackAddresses.size() > 0) {
                return ipv4LoopbackAddresses.get(0);
            }

        } catch (Exception e) {
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
        for (String current : ports) {
            builder.append(getLocalHostAddress()).append(":").append(current).append(",");
        }

        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }


    public static void main(String[] args) throws Exception {

        for (NetworkInterface current : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            System.out.println("[" + current.getName() + "] .... ");

            for (InterfaceAddress address : current.getInterfaceAddresses()) {
                final InetAddress inetAddress = address.getAddress();
                final String msg = inetAddress.isLoopbackAddress() ? "[LoopBack]" : "";
                if (inetAddress instanceof Inet4Address) {

                    System.out.println(" .... " + inetAddress.getHostAddress() + " " + msg);
                }
            }
        }

        /*
        final InetAddress[] allByName = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());

        for(InetAddress current : allByName) {
            System.out.println("Got: " + current + ", v4: " + (current instanceof Inet4Address)
                    + ", Loopback: " + current.isLoopbackAddress());
        }
        */
    }
}
