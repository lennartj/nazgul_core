/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.resource.api;

import java.util.Locale;

/**
 * Enumeration of well-known Locale definitions.
 * Convenience enumeration to have said data available immediately.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public enum WellKnownLocale {

    /**
     * Swedish locale.
     */
    SWEDISH("sv", "SE"),

    /**
     * American english locale.
     */
    US("en", "US"),

    /**
     * British english locale.
     */
    BRITISH("en", "GB");

    // Internal state
    private Locale activeLocale;

    /**
     * Construct a locale from language, country, variant.
     * NOTE: ISO 639 is not a stable standard; some of the language codes it defines
     * (specifically iw, ji, and in) have changed.  This constructor accepts both the
     * old codes (iw, ji, and in) and the new codes (he, yi, and id), but all other
     * API on Locale will return only the OLD codes.
     *
     * @param language lowercase two-letter ISO-639 code.
     * @param country  uppercase two-letter ISO-3166 code.
     * @param variant  vendor and browser specific code. See class description.
     * @throws NullPointerException thrown if any argument is null.
     */
    private WellKnownLocale(final String language,
                            final String country,
                            final String variant) {

        // Create internal state.
        this.activeLocale = new Locale(language, country, variant);
    }

    /**
     * Construct a locale from language, country.
     * NOTE: ISO 639 is not a stable standard; some of the language codes it defines
     * (specifically iw, ji, and in) have changed.  This constructor accepts both the
     * old codes (iw, ji, and in) and the new codes (he, yi, and id), but all other
     * API on Locale will return only the OLD codes.
     *
     * @param language lowercase two-letter ISO-639 code.
     * @param country  uppercase two-letter ISO-3166 code.
     * @throws NullPointerException thrown if any argument is null.
     */
    private WellKnownLocale(final String language,
                            final String country) {
        this(language, country, "");
    }

    /**
     * @return The java.util.Locale for this WellKnownLocales instance.
     */
    public final Locale getLocale() {
        return activeLocale;
    }
}
