/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.algorithms.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.NetworkAlgorithms;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NetworkAlgorithmsTest {

    // Shared state
    private SortedSet<NetworkInterface> allLocalInterfaces;
    private SortedSet<InetAddress> allLocalIPv4Addresses, allLocalIPv6Addresses;
    private boolean foundLocalNetworkInterfaces;
    private boolean foundLocalIPv4Addresses;
    private boolean foundLocalIPv6Addresses;

    @Before
    public void setupSharedState() {

        allLocalInterfaces = NetworkAlgorithms.getAllNetworkInterfaces(null);
        allLocalIPv4Addresses = new TreeSet<>(NetworkAlgorithms.INETADDRESS_COMPARATOR);
        allLocalIPv6Addresses = new TreeSet<>(NetworkAlgorithms.INETADDRESS_COMPARATOR);
        foundLocalNetworkInterfaces = !allLocalInterfaces.isEmpty();

        if (foundLocalNetworkInterfaces) {

            // Harvest all IPv4 InetAddresses
            allLocalInterfaces.stream()
                    .map(NetworkAlgorithms.GET_INETADDRESSES)
                    .forEach(c -> c.stream()
                            .filter(NetworkAlgorithms.IPV4_FILTER)
                            .forEach(allLocalIPv4Addresses::add));

            // Harvest all IPv6 InetAddresses
            allLocalInterfaces.stream()
                    .map(NetworkAlgorithms.GET_INETADDRESSES)
                    .forEach(c -> c.stream()
                            .filter(NetworkAlgorithms.IPV6_FILTER)
                            .forEach(allLocalIPv6Addresses::add));
        }

        foundLocalIPv4Addresses = !allLocalIPv4Addresses.isEmpty();
        foundLocalIPv6Addresses = !allLocalIPv6Addresses.isEmpty();
    }

    @Test
    public void validateNonLoopbackIpV4Filter() {

        if (foundLocalIPv4Addresses) {

            // Assemble
            final SortedSet<NetworkInterface> networkIFs = NetworkAlgorithms
                    .getAllNetworkInterfaces(NetworkAlgorithms.NETWORK_INTERFACE_COMPARATOR);

            // Act
            final SortedSet<Inet4Address> ipV4Addresses = new TreeSet<>(NetworkAlgorithms.INETADDRESS_COMPARATOR);
            networkIFs.stream()
                    .map(NetworkAlgorithms.GET_INETADDRESSES)
                    .forEach(ifs -> ifs.stream()
                            .filter(NetworkAlgorithms.NON_LOOPBACK_IPV4_FILTER)
                            .map(ip -> (Inet4Address) ip)
                            .forEach(ipV4Addresses::add));

            final SortedSet<String> sortedIFs = new TreeSet<>();
            ipV4Addresses.stream()
                    .map(NetworkAlgorithms.GET_ALL_ADRESSES)
                    .forEach(sortedIFs::addAll);

            // Assert
            Assert.assertFalse(sortedIFs.isEmpty());
            validateAdresses(ipV4Addresses, false, false);
        }
    }

    @Test
    public void validateLoopbackIpV4Filter() {

        if (foundLocalIPv4Addresses) {

            // Assemble
            final SortedSet<NetworkInterface> networkIFs = NetworkAlgorithms
                    .getAllNetworkInterfaces(NetworkAlgorithms.NETWORK_INTERFACE_COMPARATOR);

            // Act
            final SortedSet<Inet4Address> ipV4Addresses = new TreeSet<>(NetworkAlgorithms.INETADDRESS_COMPARATOR);
            networkIFs.stream()
                    .map(NetworkAlgorithms.GET_INETADDRESSES)
                    .forEach(ifs -> ifs.stream()
                            .filter(NetworkAlgorithms.LOOPBACK_FILTER)
                            .filter(NetworkAlgorithms.IPV4_FILTER)
                            .map(ip -> (Inet4Address) ip)
                            .forEach(ipV4Addresses::add));

            final SortedSet<String> sortedIFs = new TreeSet<>();
            ipV4Addresses.stream()
                    .map(NetworkAlgorithms.GET_ALL_ADRESSES)
                    .forEach(sortedIFs::addAll);

            // Assert
            Assert.assertFalse(sortedIFs.isEmpty());
            validateAdresses(ipV4Addresses, true, false);
        }
    }

    @Test
    public void validateNonLoopbackIpV6Filter() {

        if (foundLocalIPv6Addresses) {

            // Assemble
            final SortedSet<NetworkInterface> networkIFs = NetworkAlgorithms.getAllNetworkInterfaces(null);

            // Act
            final SortedSet<Inet6Address> ipV6Addresses = new TreeSet<>(NetworkAlgorithms.INETADDRESS_COMPARATOR);
            
            networkIFs.stream()
                    .map(NetworkAlgorithms.GET_INETADDRESSES)
                    .forEach(ifs -> ifs.stream()
                            .filter(NetworkAlgorithms.IPV6_FILTER)
                            .filter(candidate -> candidate != null && !candidate.isLoopbackAddress())
                            .map(ip -> (Inet6Address) ip)
                            .forEach(ipV6Addresses::add));

            final SortedSet<String> sortedIFs = new TreeSet<>();
            ipV6Addresses.stream()
                    .map(NetworkAlgorithms.GET_ALL_ADRESSES)
                    .forEach(sortedIFs::addAll);

            // Assert
            Assert.assertFalse(sortedIFs.isEmpty());
            validateAdresses(ipV6Addresses, false, true);

            // System.out.println("Got sorted IPv6 addresses: " + ipV6Addresses);
        }
    }

    //
    // Private helpers
    //

    private void validateAdresses(final SortedSet<? extends InetAddress> inetAddresses,
                                  final boolean shouldBeLoopback,
                                  final boolean shouldContainLinkLocalAddress) {

        Assert.assertFalse(inetAddresses.isEmpty());
        Assert.assertTrue(inetAddresses.size() <= allLocalInterfaces.size());

        inetAddresses.forEach(inetAddress -> {

            if (shouldBeLoopback) {
                Assert.assertTrue(inetAddress.isLoopbackAddress());
            } else {
                Assert.assertFalse(inetAddress.isLoopbackAddress());
            }
        });
        inetAddresses.forEach(inetAddress -> Assert.assertFalse(inetAddress.isAnyLocalAddress()));
        inetAddresses.forEach(inetAddress -> Assert.assertFalse(inetAddress.isMulticastAddress()));

        // Link-local unicast in IPv4 (169.254.0.0/16)
        if(!shouldContainLinkLocalAddress) {
            inetAddresses.forEach(inetAddress -> Assert.assertFalse("Address [" + inetAddress + "] is LinkLocal",
                    inetAddress.isLinkLocalAddress()));
        } else {
            Assert.assertTrue(inetAddresses.stream().anyMatch(InetAddress::isLinkLocalAddress));
        }

        // System.out.println("Got: " + sortedIFs);
    }
}
