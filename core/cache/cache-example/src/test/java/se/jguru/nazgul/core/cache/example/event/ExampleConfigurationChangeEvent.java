/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.example.event;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Example of an event carrying information about a configuration change, implying
 * that the configuration data is stored within a [distributed] cache.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ExampleConfigurationChangeEvent<T> implements Serializable {

    // Internal state
    private String propertyName;
    private T newValue;
    private T oldValue;
    private DateTime eventTimestamp;

    /**
     * Compound constructor, creating an ExampleConfigurationChangeEvent instance from the supplied data.
     *
     * @param propertyName The name of the configuration property.
     * @param newValue     The new value of the configuration property.
     * @param oldValue     The old value of the configuration property.
     */
    public ExampleConfigurationChangeEvent(final String propertyName,
                                           final T newValue,
                                           final T oldValue) {
        this.propertyName = propertyName;
        this.newValue = newValue;
        this.oldValue = oldValue;
        eventTimestamp = new DateTime();
    }

    /**
     * @return The name of the configuration property which changed.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @return The new value of the configuration property.
     */
    public T getNewValue() {
        return newValue;
    }

    /**
     * @return The old (previous) value of the configuration property.
     */
    public T getOldValue() {
        return oldValue;
    }

    /**
     * @return The timestamp when this event was created.
     */
    public DateTime getEventTimestamp() {
        return eventTimestamp;
    }
}
