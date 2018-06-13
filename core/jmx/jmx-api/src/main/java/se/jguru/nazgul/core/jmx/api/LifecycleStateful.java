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

import javax.management.MXBean;

/**
 * Specification for standard JMX Lifecycle methods, to provide a
 * standard operational interface to all MBeans that contain operational
 * state in regards to their MBeanServer.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
@MXBean
public interface LifecycleStateful {

    /**
     * The current state of this MXBean.
     *
     * @return The current state of this MXBean. This value must never be {@code null}.
     */
    LifecycleState getState();
}
