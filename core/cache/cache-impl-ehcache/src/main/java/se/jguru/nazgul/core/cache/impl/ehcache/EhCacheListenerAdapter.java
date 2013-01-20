/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.ehcache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.transaction.SoftLock;
import se.jguru.nazgul.core.cache.api.CacheListener;

import java.io.Serializable;

/**
 * EhCache-tailored CacheListener adapter.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EhCacheListenerAdapter implements CacheEventListener, Serializable {

    // Internal state
    private CacheListener<String> listener;

    /**
     * Wraps the provided CacheListener within this EhCacheListenerAdapter.
     *
     * @param listener The CacheListener to wrap.
     */
    @SuppressWarnings("unchecked")
    public EhCacheListenerAdapter(final CacheListener listener) {

        // Check sanity
        if (listener == null) {
            throw new IllegalArgumentException("Cannot handle null listener argument.");
        }

        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementRemoved(final Ehcache cache, final Element element) throws CacheException {
        listener.onRemove("" + element.getObjectKey(), getSerializableValue(element));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementPut(final Ehcache cache, final Element element) throws CacheException {
        listener.onPut("" + element.getObjectKey(), getSerializableValue(element));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementUpdated(final Ehcache cache, final Element element) throws CacheException {
        listener.onUpdate("" + element.getObjectKey(), getSerializableValue(element), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementExpired(final Ehcache cache, final Element element) {
        listener.onAutonomousEvict("" + element.getObjectKey(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyElementEvicted(final Ehcache cache, final Element element) {
        listener.onAutonomousEvict("" + element.getObjectKey(), getSerializableValue(element));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyRemoveAll(final Ehcache cache) {
        listener.onClear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
    }

    /**
     * Retrieves the hashCode value from the wrapped listener, implying that
     * the EhCacheListenerAdapter instance will be identified in the same
     * manner as its wrapped listener.
     *
     * @return a hash code value for this object, being identical to the hashCode
     *         value of the wrapped listener.
     * @see Object#equals(Object)
     * @see java.util.Hashtable
     */
    @Override
    public int hashCode() {
        return listener.hashCode();
    }

    /**
     * @return The identifier of the contained CacheListener.
     */
    public final String getId() {
        return listener.getId();
    }

    /**
     * @return the wrapped CacheListener instance.
     */
    public CacheListener getCacheListener() {
        return listener;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return the ID of this object.
     */
    @Override
    public String toString() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    //
    // Private helpers
    //

    @SuppressWarnings("unchecked")
    private Serializable getSerializableValue(final Element element) {

        if (element == null) {
            return null;
        }

        // NonSerializable Element values normally are transmitted
        // when a localTransaction commit fails. For other types of
        // actions, the element should be serializable.
        if (element.isSerializable()) {
            return (Serializable) element.getObjectValue();
        }

        // Is this a problematic transactional commit?
        Object value = element.getObjectValue();
        if (value instanceof SoftLock) {

            // This is just a SoftLock.
            // Ignore it for the purposes of notification.
            return "";
        }

        throw new IllegalArgumentException("Cannot handle an Element value of type ["
                + value.getClass().getName() + "]");
    }
}