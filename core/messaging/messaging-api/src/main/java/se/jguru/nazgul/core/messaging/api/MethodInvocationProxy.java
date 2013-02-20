/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.messaging.api;

import org.aopalliance.intercept.MethodInvocation;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface MethodInvocationProxy extends MethodInvocation {

    //  call [ void a(foo) ] --> FireAndForget / PseudoSynchronous
    //
    //       FireAndForget:
    //              a) Create temporary queue
    //              b) Invoke, return Future<SerializableVoid> (?)
    //              c) On return, execute listener (?)
}
