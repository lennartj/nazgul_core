/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.types;

import java.io.Serializable;

/**
 * Type definition to be used whenever {@code java.lang.Void} is desired, but a {@code Serializable}
 * instance must be provided. An example of this situation is prevalent in clustering, when generic
 * methods are specified to return a value. If void is desired as a return value, use this SerializableVoid
 * class instead:
 * <p/>
 * <pre>
 *     public interface SomeActorDefinition&lt;T extends Serializable&gt; extends Serializable {
 *         T perform();
 *     }
 * </pre>
 * <p/>
 * If we desire a concrete implementation of the SomeActorDefinition, which should not return a value from the
 * perform method (which would require much more resources than a fully asynchronous call returning void) we must
 * declare the implementation thus:
 * <p/>
 * <pre>
 *     public class AConcreteActor implements SomeActorDefinition&lt;SerializableVoid&gt; {
 *         public SerializableVoid perform() {
 *             // Do something
 *             return null;
 *         }
 *     }
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class SerializableVoid implements Serializable {

    /**
     * No objects from this type; it is Void.
     */
    private SerializableVoid() {
    }
}
