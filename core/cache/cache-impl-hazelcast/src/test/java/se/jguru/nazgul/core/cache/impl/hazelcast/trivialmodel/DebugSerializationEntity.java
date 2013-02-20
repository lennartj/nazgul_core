/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.hazelcast.trivialmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DebugSerializationEntity implements Serializable {

    // Internal state
    public String name;
    public Collection<String> friends;

    public DebugSerializationEntity(String name, Collection<String> friends) {
        this.name = name;
        this.friends = friends;
    }

    @Override
    public boolean equals(Object obj) {
        DebugSerializationEntity that = (DebugSerializationEntity) obj;
        return (new EqualsBuilder()).append(name, that.name).isEquals();
    }

    @Override
    public int hashCode() {
        return (new HashCodeBuilder()).append(name).hashCode();
    }

    @Override
    public String toString() {
        return "(" + name + ": " + friends + ") ";
    }
}
