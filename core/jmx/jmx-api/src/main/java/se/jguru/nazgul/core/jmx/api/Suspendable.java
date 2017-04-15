/*
 * #%L
 * Nazgul Project: nazgul-core-jmx-api
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
package se.jguru.nazgul.core.jmx.api;

import javax.management.MXBean;

/**
 * Specification for a Suspendable LifecycleStateful instance, where operations can
 * be suspended (put in a wait state) and resumed (resurrected from wait state).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
@MXBean
public interface Suspendable extends LifecycleStateful {

    /**
     * Performs a graceful and synchronous suspension of this Suspendable, implying that any operations
     * invoked within this Suspendable are not executed until the resume method has been
     * called. Calling suspend multiple times should have no effect. When returning from this suspend
     * method, the LifecycleStateful instance should be properly suspended.
     *
     * @return {@code true} if this Suspendable was successfully suspended when this method
     * has executed, and {@code false} otherwise.
     * @throws IllegalStateException if this Suspendable was not in state {@code LifecycleStateful.STARTED}.
     */
    boolean suspend() throws IllegalStateException;

    /**
     * Resumes operation of this suspendable, implying that any operations invoked within this Suspendable are
     * executed after this resume method has been called. Calling resume multiple times should have no effect.
     * When returning from this resume method, the LifecycleStateful instance should be properly resumed.
     *
     * @return {@code true} if this Suspendable was successfully resumed when this method
     * has executed, and {@code false} otherwise.
     * @throws IllegalStateException if this Suspendable was not in state {@code LifecycleStateful.STOPPED}.
     */
    boolean resume() throws IllegalStateException;
}
