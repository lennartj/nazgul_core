/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.resource.api;

import java.util.Locale;

/**
 * Resource management API for handling locally stored Resources. A lookup path starts
 * with the ResourceBundle definition for the chosen (or default if no Locale is explicitly
 * passed as an argument)
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface LocalResources {

    /**
     * Swedish Locale.
     */
    public static final Locale SWEDISH = new Locale("sv", "se");

    /**
     * Retrieves a localized String from the underlying local resources, using the default Locale and
     * no static Key=Value substitution.
     *
     * @param key          The key within the underlying ResourceBundle structure for the value to acquire.
     * @param defaultValue The fallback/default value to return if no value was present within the underlying
     *                     ResourceBundle structure.
     * @return The localized String acquired from the underlying ResourceBundle structure at key
     *         <code>key</code>, or the defaultValue should no value exist for the given key within the underlying
     *         ResourceBundle structure.
     */
    String getLocalized(String key, String defaultValue);

    /**
     * Retrieves a localized String from the underlying local resources, using the default Locale and
     * the provided key=value substitutions.
     *
     * @param key            The key within the underlying ResourceBundle structure for the value to acquire.
     * @param defaultValue   The fallback/default value to return if no value was present within the underlying
     *                       ResourceBundle structure.
     * @param keyValueTokens Either <code>null</code>, or a list holding strings on the form key=value where
     *                       both key and value must be non-empty. The keyValueTokens will be substituted before
     *                       the result is returned.
     * @return The String acquired from the underlying ResourceBundle structure at key
     *         <code>key</code>, or the defaultValue should no value exist for the given key within the underlying
     *         ResourceBundle structure.
     */
    String getLocalized(String key, String defaultValue, String... keyValueTokens);

    /**
     * Retrieves a localized String from the underlying local resources, using the provided Locale and
     * no static Key=Value substitution.
     *
     * @param key          The key within the underlying ResourceBundle structure for the value to acquire.
     * @param defaultValue The fallback/default value to return if no value was present within the underlying
     *                     ResourceBundle structure.
     * @param locale       The locale for which the resource should be retrieved.
     * @return The localized String acquired from the underlying ResourceBundle structure at key
     *         <code>key</code>, or the defaultValue should no value exist for the given key within the underlying
     *         ResourceBundle structure.
     */
    String getLocalized(String key, String defaultValue, Locale locale);

    /**
     * Retrieves a localized String from the underlying local resources, using the provided Locale and
     * static Key=Value token substitution map.
     *
     * @param key            The key within the underlying ResourceBundle structure for the value to acquire.
     * @param defaultValue   The fallback/default value to return if no value was present within the underlying
     *                       ResourceBundle structure.
     * @param locale         The locale for which the resource should be retrieved.
     * @param keyValueTokens Either <code>null</code>, or a list holding strings on the form key=value where
     *                       both key and value must be non-empty.
     * @return The localized String acquired from the underlying ResourceBundle structure at key
     *         <code>key</code>, or the defaultValue should no value exist for the given key within the underlying
     *         ResourceBundle structure.
     */
    String getLocalized(String key, String defaultValue, Locale locale, String... keyValueTokens);

    /**
     * (Re-)assigns the locale used should no Locale be submitted with
     * the lookup call. The defaultLocale is kept as long as the LocalResources
     * instance lives.
     *
     * @param defaultLocale The new default locale, used if no Locale is submitted with calls.
     * @return <code>true</code> if the defaultLocale was successfully set, and false otherwise.
     */
    boolean setDefaultLocale(Locale defaultLocale);
}