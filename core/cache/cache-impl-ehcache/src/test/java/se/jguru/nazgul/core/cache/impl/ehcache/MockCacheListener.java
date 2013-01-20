/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.ehcache;

import se.jguru.nazgul.core.cache.api.AbstractCacheListener;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockCacheListener extends AbstractCacheListener<String> {

    private CountDownLatch evictionLatch;
    public List<String> callStack = new ArrayList<String>();

    /**
     * Creates a new AbstractCacheListener and assigns the internal ID state.
     *
     * @param id The identifier of this CacheListenerAdapter.
     */
    public MockCacheListener(String id) {
        super(id);
    }

    /**
     * Serialization-usable constructor, and not part of the public API of this
     * AbstractCacheListener. <strong>This is for framework use only</strong>.
     */
    public MockCacheListener() {
    }

    public void setEvictionLatch(final CountDownLatch evictionLatch) {
        this.evictionLatch = evictionLatch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doOnPut(String key, Serializable value) {
        callStack.add("onPut [" + key + "]: " + value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doOnUpdate(String key, Serializable newValue, Serializable oldValue) {
        callStack.add("onUpdate [" + key + "]: " + oldValue + " --> " + newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doOnRemove(String key, Serializable value) {
        callStack.add("onRemove [" + key + "]: " + value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClear() {
        callStack.add("onClear");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doOnAutonomousLoad(String key, Serializable value) {
        callStack.add("onAutonomousLoad [" + key + "]: " + value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doOnAutonomousEvict(String key, Serializable value) {

        callStack.add("onAutonomousEvict [" + key + "]: " + value);
        if (evictionLatch != null) {
            evictionLatch.countDown();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performWriteExternal(ObjectOutput out) throws IOException {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performReadExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // Do nothing
    }
}