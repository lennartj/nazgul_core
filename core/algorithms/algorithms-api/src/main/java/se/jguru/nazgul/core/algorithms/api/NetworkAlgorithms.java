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

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>Collection of network-related algorithms. The address definitions are :</p>
 * <table style="text-align: left; border: 1px solid black; border-collapse: collapse;background: #f0f0f0;">
 * <tr>
 * <th>IP Protocol</th>
 * <th>Link-Local address range</th>
 * <th>Loopback addresses</th>
 * </tr>
 * <tr>
 * <td>IPv4</td>
 * <td><tt>169.254.0.1 -- 169.254.255.254</tt></td>
 * <td><tt>127.0.0.0/8</tt>, typically <tt>127.0.0.1</tt></td>
 * </tr>
 * <tr>
 * <td>IPv6</td>
 * <td><tt>fe80::/10</tt>, but for compliance reasons <tt>fe80::/64</tt></td>
 * <td><tt>::1</tt></td>
 * </tr>
 * </table>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see <a href="https://en.wikipedia.org/wiki/Link-local_address">Wikipedia: Link-local address definition</a>
 * @see <a href="https://en.wikipedia.org/wiki/Loopback">Wikipedia: Loopback address definition</a>
 */
@XmlTransient
@SuppressWarnings("all")
public final class NetworkAlgorithms {

    /**
     * Predicate identifying non-null IPv4 InetAddresses.
     */
    public static final Predicate<InetAddress> IPV4_FILTER = candidate -> candidate instanceof Inet4Address;

    /**
     * Predicate identifying non-null IPv6 InetAddresses.
     */
    public static final Predicate<InetAddress> IPV6_FILTER = candidate -> candidate instanceof Inet6Address;

    /**
     * Predicate identifying non-null LoopBackAddresses.
     */
    public static final Predicate<InetAddress> LOOPBACK_FILTER = InetAddress::isLoopbackAddress;

    /**
     * <p>Predicate identifying non-null IPv4 InetAddress objects that are neither LinkLocal nor Loopback addresses.</p>
     */
    public static final Predicate<InetAddress> PUBLIC_IPV4_FILTER = candidate ->
            IPV4_FILTER.test(candidate)
                    && !LOOPBACK_FILTER.test(candidate)
                    && !candidate.isLinkLocalAddress();

    /**
     * Comparator for InetAddress objects; failsafe in the sense that it will convert null values
     * (on either side of the comparison) to empty strings before comparing the objects.
     * It is, therefore, recommended to use this Comparator only after filtering an InetAddress
     * collection for null objects.
     */
    public static final Comparator<InetAddress> INETADDRESS_COMPARATOR = (l, r) -> {

        // Be paranoid
        final String left = l == null ? "" : l.getHostAddress();
        final String right = r == null ? "" : r.getHostAddress();

        // All Done.
        return left.compareTo(right);
    };

    /**
     * Compares NetworkInterface objects by their {@link NetworkInterface#toString()} value.
     * This Comparator is failsafe in the sense that it converts null values
     * (on either side of the comparison) to empty strings before comparing the objects.
     * It is, therefore, recommended to use this Comparator only after removing null objects
     * from the respective source Collection.
     */
    public static final Comparator<NetworkInterface> NETWORK_INTERFACE_COMPARATOR = (l, r) -> {

        // Be paranoid
        final String left = l == null ? "" : l.toString();
        final String right = r == null ? "" : r.toString();

        // All Done.
        return left.compareTo(right);
    };

    /**
     * Finds all non-broadcast InetAddresses from the supplied NetworkInterface.
     */
    public static final Function<NetworkInterface, SortedSet<InetAddress>> GET_INETADDRESSES = networkInterface -> {

        final SortedSet<InetAddress> toReturn = new TreeSet<>(INETADDRESS_COMPARATOR);

        if (networkInterface != null) {
            networkInterface.getInterfaceAddresses()
                    .stream()
                    .map(InterfaceAddress::getAddress)
                    .forEach(toReturn::add);
        }

        // All Done.
        return toReturn;
    };

    /**
     * <p>Converts the InetAddress to a Set containing all String forms of the InetAddress, namely:</p>
     * <ol>
     * <li>{@link InetAddress#getCanonicalHostName()}</li>
     * <li>{@link InetAddress#getHostAddress()}</li>
     * <li>{@link InetAddress#getHostName()}</li>
     * </ol>
     */
    public static final Function<InetAddress, Set<String>> GET_ALL_ADRESSES = addr -> {

        final Set<String> toReturn = new TreeSet<>();

        if (addr != null) {
            toReturn.add(addr.getCanonicalHostName());
            toReturn.add(addr.getHostAddress());
            toReturn.add(addr.getHostName());
        }

        // All Done.
        return toReturn;
    };

    /*
     * Hide the constructor for utility classes.
     */
    private NetworkAlgorithms() {
        // Do nothing
    }

    /**
     * Retrieves a SortedSet containing all {@link NetworkInterface}s found.
     *
     * @param ifComparator A Comparator ordering the NetworkInterfaces found on the executing computer.
     *                     If a {@code null} value is retrieved, {@link #NETWORK_INTERFACE_COMPARATOR} is used.
     * @return A SortedSet containing the NetworkInterfaces on the executing computer.
     * @throws IllegalStateException if the {@link NetworkInterface#getNetworkInterfaces()} method fails.
     */
    @NotNull
    public static SortedSet<NetworkInterface> getAllNetworkInterfaces(final Comparator<NetworkInterface> ifComparator)
            throws IllegalStateException {

        // Check sanity
        final Comparator<NetworkInterface> comp = ifComparator == null ? NETWORK_INTERFACE_COMPARATOR : ifComparator;
        final SortedSet<NetworkInterface> toReturn = new TreeSet<>(comp);

        try {
            toReturn.addAll(Collections.list(NetworkInterface.getNetworkInterfaces()));
        } catch (Exception e) {
            throw new IllegalStateException("Could not retrieve NetworkInterfaces", e);
        }

        // All Done.
        return toReturn;
    }

    /**
     * Retrieves a set of string representations of the local {@link NetworkInterface}s found.
     *
     * @param addressFilter A filter applied to each NetworkInterface found.
     *                      If a {@code null} value is retrieved, {@link #IPV4_FILTER} is used.
     * @param addressMapper A Function applied to all InetAddress objects filtered by the addressFilter.
     *                      If a {@code null} value is retrieved, {@link #GET_ALL_ADRESSES} is used.
     * @return A SortedSet containing all String representations of the local {@link NetworkInterface}s found.
     */
    @NotNull
    public static SortedSet<String> getAllLocalNetworkAddresses(
            final Predicate<InetAddress> addressFilter,
            final Function<InetAddress, Set<String>> addressMapper) {

        // Check sanity
        final Predicate<InetAddress> filter = addressFilter == null ? IPV4_FILTER : addressFilter;
        final Function<InetAddress, Set<String>> mapper = addressMapper == null ? GET_ALL_ADRESSES : addressMapper;

        final SortedSet<String> toReturn = new TreeSet<>();

        getAllNetworkInterfaces(null).forEach(networkInterface -> {

            GET_INETADDRESSES.apply(networkInterface)
                    .stream()
                    .filter(filter)
                    .map(mapper)
                    .forEach(toReturn::addAll);
        });

        // All Done.
        return toReturn;
    }

    /**
     * Convenience method to find all public (i.e. non-loopback, non-linklocal) IPv4 addresses.
     *
     * @return A sorted set holding all Inet4Address objects which are neither loopback nor link-local
     * for all network interfaces on the local computer.
     * @see #INETADDRESS_COMPARATOR
     * @see #GET_INETADDRESSES
     * @see #PUBLIC_IPV4_FILTER
     */
    @NotNull
    public static SortedSet<Inet4Address> getPublicIPv4Addresses() {

        // Create the return value with the
        final SortedSet<Inet4Address> toReturn = new TreeSet<>(INETADDRESS_COMPARATOR);

        // Use the PUBLIC_IPV4_FILTER to
        NetworkAlgorithms.getAllNetworkInterfaces(null)
                .stream()
                .map(NetworkAlgorithms.GET_INETADDRESSES)
                .forEach(c -> c.stream()
                        .filter(NetworkAlgorithms.PUBLIC_IPV4_FILTER)
                        .map(addr -> (Inet4Address) addr)
                        .forEach(toReturn::add));

        // All Done.
        return toReturn;
    }
}
