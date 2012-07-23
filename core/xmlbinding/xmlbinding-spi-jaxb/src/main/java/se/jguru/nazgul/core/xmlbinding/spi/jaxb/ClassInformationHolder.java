/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb;

import java.util.SortedSet;

/**
 * Specification for a type holding class information in String form.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ClassInformationHolder {

    /**
     * @return The fully qualified class names of all classes held within this ClassInformationHolder.
     */
    SortedSet<String> getClassInformation();
}
