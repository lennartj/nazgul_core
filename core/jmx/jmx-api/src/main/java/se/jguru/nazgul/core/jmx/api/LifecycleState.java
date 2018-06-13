/*-
 * #%L
 * Nazgul Project: nazgul-core-jmx-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.core.jmx.api;

/**
 * Lifecycle state definitions for MBeans adhering to a standard lifecycle.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public enum LifecycleState {

    /**
     * The MBean is not yet initialized.
     */
    UNINITIALIZED,

    /**
     * The MBean is starting, implying that its normal function can not be regarded as available.
     * Business operations cannot properly be called during starting state.
     */
    STARTING,

    /**
     * The MBean is started/running normally.
     */
    STARTED,

    /**
     * The MBean is stopping, implying that its normal function can not be regarded as available.
     * Business operations cannot properly be called during stopping state.
     */
    STOPPING,

    /**
     * The MBean is stopped, implying that it has been started/initialized.
     */
    STOPPED,

    /**
     * The MBean is in an error state.
     * Business operations cannot properly be called during error state.
     */
    ERROR
}
