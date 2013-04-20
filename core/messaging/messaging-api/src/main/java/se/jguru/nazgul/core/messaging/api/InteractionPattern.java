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

package se.jguru.nazgul.core.messaging.api;

/**
 * Enumeration defining the available messaging client interaction types.
 * That is, each ClientType definition defines the underlying JMS Session
 * interaction pattern.
 * <p/>
 * InteractionPattern selection can typically be used to clarify the runtime
 * and non-functional properties of invocations and results in distributed systems.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public enum InteractionPattern {

    /**
     * True asynchronous interaction with one or more endpoints.
     * After firing the message, the sending thread exits the sending method.
     * <p/>
     * Typically maps well to methods returning {@code void}, and incompatible
     * with most scenarios where a return value is expected from a method.
     */
    FIRE_AND_FORGET,

    /**
     * True or faked synchronous interaction with [typically] one server endpoint.
     * The response is assumed to exist on the sending thread before continuing/returning
     * from the sending method.
     * <p/>
     * Typically maps well to methods returning a value (which may be a {@code Future}),
     * or methods having side effects [such as modifying system state] when those side
     * effects are required immediately following the call.
     */
    PSEUDO_SYNCHRONOUS,

    /**
     * Asynchronous interaction, where a response is expected, but the response is retrieved
     * on another thread than the method sender. The response call is received within a
     * listener (such as a {@code MessageListener} or equivalent) which is invoked by the
     * server-side endpoint when a response is available.
     *
     * Typically maps well to methods returning a value or methods having side effects [such
     * as modifying system state] when those side effects are <strong>not</strong> required
     * immediately follwing the sending call.
     */
    EVENT_CALLBACK
}
