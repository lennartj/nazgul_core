/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.resource.api;

import java.util.Locale;

/**
 * Standard localization interface for classes/types.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Localizable {

    /**
     * Assigns the active Locale, which is used to find
     * resources such as messages within ResourceBundles.
     *
     * @param activeLocale The active Locale to set.
     */
    void setActiveLocale(Locale activeLocale);
}
