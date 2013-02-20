/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
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
