/*
 * #%L
 * Nazgul Project: nazgul-core-parser-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.nazgul.core.parser.api.agent;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ParserAgent that handles dynamic subtitutions for DNS and InetAddres-related tokens.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HostNameParserAgent extends AbstractParserAgent {

    /**
     * Token key indicating that the replacement value should
     * be acquired using the local host's canonical name, i.e:
     * <code>InetAddress.getLocalHost().getCanonicalHostName()</code>.
     */
    public static final String HOST_CANONICAL_NAME = "host:canonicalName";

    /**
     * Token key indicating that the replacement value should
     * be acquired using the local host's canonical name, i.e:
     * <code>InetAddress.getLocalHost().getHostAddress()</code>.
     */
    public static final String HOST_ADDRESS = "host:address";

    /**
     * Token key indicating that the replacement value should
     * be acquired using the local host's canonical name, i.e:
     * <code>InetAddress.getLocalHost().getHostName()</code>.
     */
    public static final String HOST_NAME = "host:name";

    /**
     * Default constructor.
     */
    public HostNameParserAgent() {

        // Add the dynamic replacement tokens.
        dynamicTokens.add(HOST_CANONICAL_NAME);
        dynamicTokens.add(HOST_ADDRESS);
        dynamicTokens.add(HOST_NAME);
    }

    /**
     * Perform dynamic replacement for the provided token.
     *
     * @param token The token to dynamically replace.
     * @return The replacement value.
     */
    @Override
    protected String performDynamicReplacement(final String token) {

        final InetAddress localhost = getLocalhostNonLoopbackAddress();

        if (token.equals(HOST_CANONICAL_NAME)) {
            return localhost.getCanonicalHostName();
        }
        if (token.equals(HOST_ADDRESS)) {
            return localhost.getHostAddress();
        }
        if (token.equals(HOST_NAME)) {
            return localhost.getHostName();
        }

        throw new IllegalArgumentException("Cannot handle dynamic token [" + token + "].");
    }

    /**
     * Acquires the first non-loopback InetAddress for the localhost node.
     *
     * @return the first found non-loopback InetAddress for the localhost node.
     */
    public static InetAddress getLocalhostNonLoopbackAddress() {

        final List<InetAddress> ipv4NonLoopbackAddresses = new ArrayList<InetAddress>();
        final List<InetAddress> ipv6NonLoopbackAddresses = new ArrayList<InetAddress>();

        try {

            outer:
            for (NetworkInterface current : Collections.list(NetworkInterface.getNetworkInterfaces())) {

                for (InterfaceAddress currentAddress : current.getInterfaceAddresses()) {

                    if (currentAddress != null && !currentAddress.getAddress().isLoopbackAddress()) {

                        if (currentAddress.getAddress() instanceof Inet4Address) {

                            // This is a non-loopback address.
                            ipv4NonLoopbackAddresses.add(currentAddress.getAddress());
                            break outer;
                        } else if (currentAddress.getAddress() instanceof Inet6Address) {

                            // This is a non-loopback address
                            ipv6NonLoopbackAddresses.add(currentAddress.getAddress());
                            break outer;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not acquire a non-loopback address [IPv4 or IPv6]", e);
        }

        // Return the first non-loopback address found.
        if (ipv4NonLoopbackAddresses.size() > 0) {
            return ipv4NonLoopbackAddresses.get(0);
        }
        if (ipv6NonLoopbackAddresses.size() > 0) {
            return ipv6NonLoopbackAddresses.get(0);
        }

        throw new IllegalStateException("Currently, a non-loopback address is required "
                + "to perform Hostname parser substitutions.");
    }
}
