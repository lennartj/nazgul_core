/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.clustering.api;

import java.io.Serializable;

/**
 * Specification for an object that could be stored within a data grid cluster.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Clusterable extends Serializable {

    /**
     * @return an Identifier, unique within the cluster.
     */
    String getId();
}
