/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.messaging.api.remote;

import org.aopalliance.intercept.MethodInvocation;
import se.jguru.nazgul.core.messaging.api.InteractionPattern;

/**
 * Specification for a factory returning MethodInvocation instances,
 * adhering to requested InteractionPatterns if applicable.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface InteractionPatternFactory {

    /**
     * Retrieves a MethodInvocation, using the supplied InteractionPattern if possible.
     * Implementations are required to adjust the InteractionPattern if the supplied
     *
     * @param requestedInteractionPattern The desired interaction pattern
     * @return
     */
    MethodInvocation getMethodInvocation(final InteractionPattern requestedInteractionPattern);
}
