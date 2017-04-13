/*-
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
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

import se.jguru.nazgul.core.algorithms.api.NetworkAlgorithms;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalhostIpResolver {

    public static final Function<InetAddress, Set<String>> GET_DNS = iaddr -> {

        final SortedSet<String> toReturn = new TreeSet<>();
        toReturn.add(iaddr.getCanonicalHostName());
        return toReturn;
    };

    public static final Function<InetAddress, Set<String>> GET_IP = iaddr -> {

        final SortedSet<String> toReturn = new TreeSet<>();
        toReturn.add(iaddr.getHostAddress());
        return toReturn;
    };
    
    // Internal state
    private String localhostDNS;
    private String localhostIP;

    public LocalhostIpResolver() {

        this.localhostDNS = NetworkAlgorithms.getAllLocalNetworkAddresses(null, GET_DNS)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Found no DNS name for localhost."));

        this.localhostIP = NetworkAlgorithms.getAllLocalNetworkAddresses(null, GET_IP)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Found no IP number for localhost."));

    }

    public String getLocalhostDNS() {
        return localhostDNS;
    }

    public String getLocalhostIP() {
        return localhostIP;
    }

    public String getClusterInetAddresses(final List<String> ports) {
        return ports.stream()
                .map(p -> getLocalhostDNS() + ":" + p)
                .reduce((l, r) -> l + "," + r)
                .orElse("");
    }
}
