/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.api.distributed.async;

import se.jguru.nazgul.core.cache.api.distributed.DistributedCache;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;

/**
 * Service interface definition for a distributed (clustered) cache which
 * can execute Tasks on Nodes within a distributed cache.
 * <p/>
 * If the underlying cache provides the ExecutorService capability, this is
 * the interface specifying how to interact with it.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface DistributedExecutor<KeyType extends Serializable> extends DistributedCache<KeyType> {

    /**
     * @return The ExecutorService of the underlying cache implementation.
     */
    public ExecutorService getExecutorService();
}
