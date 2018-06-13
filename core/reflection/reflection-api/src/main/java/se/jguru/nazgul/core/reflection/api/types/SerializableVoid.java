/*-
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
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

package se.jguru.nazgul.core.reflection.api.types;

import java.io.Serializable;

/**
 * <p>Type definition to be used whenever {@code java.lang.Void} is desired, but a {@code Serializable}
 * instance must be provided. An example of this situation is prevalent in clustering, when generic
 * methods are specified to return a value. If void is desired as a return value, use this SerializableVoid
 * class instead:</p>
 * <pre>
 *     public interface SomeActorDefinition&lt;T extends Serializable&gt; extends Serializable {
 *         T perform();
 *     }
 * </pre>
 * <p>If we desire a concrete implementation of the SomeActorDefinition, which should not return a value from the
 * perform method (which would require much more resources than a fully asynchronous call returning void) we must
 * declare the implementation thus:</p>
 * <pre>
 *     public class AConcreteActor implements SomeActorDefinition&lt;SerializableVoid&gt; {
 *         public SerializableVoid perform() {
 *             // Do something
 *             return null;
 *         }
 *     }
 * </pre>
 * The SerializableVoid class can not be instantiated.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class SerializableVoid implements Serializable {

    /**
     * No objects from this type; it is Void.
     */
    private SerializableVoid() {
    }
}
