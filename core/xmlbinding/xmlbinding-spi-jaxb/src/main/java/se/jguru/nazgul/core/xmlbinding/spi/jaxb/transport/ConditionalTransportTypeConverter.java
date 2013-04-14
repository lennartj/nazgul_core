/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

/**
 * Type converter condition definition to check if instances can be packaged
 * within JAXB transport types - and vice versa.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ConditionalTransportTypeConverter {

    /**
     * Acquires a result defining if this ConditionalTransportTypeConverter can
     * be used to package the provided instance for [Serialized] transport.
     * The "Package" process simply implies converting the provided instance
     * into a transportType instance holding the state of the original instance.
     *
     * @param instance The instance for which to check packaging capability.
     * @return <code>true</code> if this ConditionalTransportTypeConverter can
     *         package the provided instance for transport and <code>false</code> otherwise.
     */
    boolean canPackageForTransport(Object instance);

    /**
     * Acquires a result defining if this ConditionalTransportTypeConverter can be used to revive the
     * provided instance after transport. The "Revival" process simply implies converting
     * the TransportType instance back to its original class, type and state.
     *
     * @param instance The instance to revive.
     * @return <code>true</code> if this ConditionalTransportTypeConverter can revive the provided
     *         instance from a transport form and <code>false</code> otherwise.
     */
    boolean canReviveAfterTransport(Object instance);
}
