/*
 * #%L
 * Nazgul Project: nazgul-core-cache-api
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
package se.jguru.nazgul.core.cache.api;

import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.validation.constraints.NotNull;
import java.util.Iterator;

/**
 * An immutable Iterator implementation, preventing modification of the underlying data structure.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class  ReadOnlyIterator<E> implements Iterator<E> {

    // Internal state
    private Iterator<E> delegate;

    /**
     * Creates a read-only iterator backed by the provided delegate.
     *
     * @param delegate The iterator made readonly by this ReadOnlyIterator wrapper.
     */
    public ReadOnlyIterator(@NotNull final Iterator<E> delegate) {

        // Check sanity
        Validate.notNull(delegate, "delegate");

        // Assign internal state
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasNext() {
        return delegate.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final E next() {
        return delegate.next();
    }

    /**
     * Throws an UnsupportedOperationException exception.
     * The ReadOnlyIterator does not permit remove operations.
     *
     * @throws UnsupportedOperationException always.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove elements from a ReadOnlyIterator.");
    }
}
