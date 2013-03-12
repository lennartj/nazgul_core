/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

/**
 * Metadata information specification for JAXB transport type pairs, indicating
 * which TransportType corresponds to a particular OriginalType.
 * The Metadata information implies that an OriginalType instance is converted
 * to a TransportType instance for JAXB serialization, and vice versa.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public interface TransportMetaData {

    /**
     * Acquires the OriginalType for the supplied TransportType.
     *
     * @param transportType   The JAXB-annotated TransportType corresponding to the supplied for which we want
     *                        to acquire
     *                        the corresponding TransportType.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The OriginalType for which we would like to acquire the JAXB-annotated TransportType.
     * @return The Class of the TransportType for the supplied OriginalType.
     */
    <OriginalType, TransportType> Class<OriginalType> getOriginalType(final Class<TransportType> transportType);

    /**
     * Acquires the TransportType for the supplied originalType.
     *
     * @param originalType    The original (i.e. non-transport) type for which we want to acquire
     *                        the corresponding TransportType.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The OriginalType for which we would like to acquire the JAXB-annotated TransportType.
     * @return The Class of the TransportType for the supplied OriginalType.
     */
    <TransportType, OriginalType> Class<TransportType> getTransportType(final Class<OriginalType> originalType);
}
