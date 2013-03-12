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
package se.jguru.nazgul.core.algorithms.event.api.consumer;

import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.util.EventListener;

/**
 * EventConsumer/EventListener specification with callback methods invoked when events occur.
 * EventConsumer instances should function correctly in a clustered environment, and must also
 * be Comparable to enforce a natural ordering and comparison between instances.
 *
 * @param <E> The exact subtype of EventConsumer in effect
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see EventListener
 */
public interface EventConsumer<E extends EventConsumer<E>> extends EventListener, Comparable<E>, Clusterable {
}
