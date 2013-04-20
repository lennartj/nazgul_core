package se.jguru.nazgul.core.cache.impl.inmemory;

/**
 * An enumeration of cache events, for use within listener handlers.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public enum CacheEvent {

    PUT,

    UPDATE,

    REMOVE,

    CLEAR,

    AUTONOMOUS_EVICT,

    AUTONOMOUS_LOAD
}
