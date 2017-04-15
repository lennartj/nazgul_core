/*
 * #%L
 * Nazgul Project: nazgul-core-resource-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
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
     * Norwegian locale.
     */
    NORWEGIAN("no", "NO"),

    /**
     * Plain/international english locale.
     */
    ENGLISH("en", "EN"),

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
