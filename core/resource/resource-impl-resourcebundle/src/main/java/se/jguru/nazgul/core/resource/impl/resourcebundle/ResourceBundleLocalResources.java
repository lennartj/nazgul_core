/*
 * #%L
 * Nazgul Project: nazgul-core-resource-impl-resourcebundle
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.resource.impl.resourcebundle;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.resource.api.LocalResources;
import se.jguru.nazgul.core.resource.impl.resourcebundle.parser.CompoundParser;
import se.jguru.nazgul.core.resource.impl.resourcebundle.parser.Utf8ResourceBundle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * ResourceBundle-delegating implementation of the LocalResources
 * specification using a backing ResourceBundle to realize the protocol.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ResourceBundleLocalResources implements LocalResources {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(ResourceBundleLocalResources.class);

    // Internal state
    private final Object[] lock = new Object[0];
    private String resourceBundleBaseName;
    private Locale defaultLocale;
    private Map<Locale, ResourceBundle> resourceBundleCache = new HashMap<Locale, ResourceBundle>();

    /**
     * Creates a new ResourceBundleLocalResources delegating typed calls to the provided ResourceBundle.
     *
     * @param resourceBundle         A non-null ResourceBundle.
     * @param resourceBundleBaseName The non-null baseName of the provided ResourceBundle.
     * @param defaultLocale          The default/fallback locale for all lookups.
     */
    public ResourceBundleLocalResources(final ResourceBundle resourceBundle,
                                        final String resourceBundleBaseName,
                                        final Locale defaultLocale) {

        // Check sanity
        Validate.notNull(resourceBundle, "Cannot handle null resourceBundle argument.");
        Validate.notNull(defaultLocale, "Cannot handle null defaultLocale argument.");
        Validate.notEmpty(resourceBundleBaseName, "Cannot handle null resourceBundleBaseName argument.");

        // Assign internal state
        this.resourceBundleBaseName = resourceBundleBaseName;
        this.defaultLocale = defaultLocale;

        final Locale internalLocale = resourceBundle.getLocale();
        if (internalLocale == null || !internalLocale.equals(defaultLocale)) {
            if (!setDefaultLocale(defaultLocale)) {
                throw new IllegalArgumentException("Improper resourceBundleBaseName [" + resourceBundleBaseName
                        + "] given (not found).");
            }
        } else {
            resourceBundleCache.put(this.defaultLocale, resourceBundle);
        }
    }

    /**
     * Creates a new ResourceBundleLocalResources delegating typed calls to the provided ResourceBundle.
     *
     * @param resourceBundleBaseName The non-null baseName to generate a ResourceBundle using
     *                               <code>ResourceBundle.getBundle(resourceBundleBaseName, defaultLocale)</code>
     * @param defaultLocale          The default/fallback locale for all lookups. Also used to create the
     *                               internal ResourceBundle using the call
     *                               <code>ResourceBundle.getBundle(resourceBundleBaseName, defaultLocale)</code>.
     */
    public ResourceBundleLocalResources(final String resourceBundleBaseName,
                                        final Locale defaultLocale) {

        this(Utf8ResourceBundle.getBundle(resourceBundleBaseName, defaultLocale),
                resourceBundleBaseName,
                defaultLocale);
    }

    /**
     * Creates a new ResourceBundleLocalResources delegating typed calls to the provided ResourceBundle.
     *
     * @param resourceBundleBaseName The non-null baseName to generate a ResourceBundle using
     *                               <code>ResourceBundle.getBundle(resourceBundleBaseName, Locale.getDefault())</code>
     */
    public ResourceBundleLocalResources(final String resourceBundleBaseName) {
        this(resourceBundleBaseName, Locale.getDefault());
    }

    /**
     * Retrieves a localized String from the underlying local resources, using the currently active Locale.
     *
     * @param key          The key within the underlying ResourceBundle structure for the value to acquire.
     * @param defaultValue The fallback/default value to return if no value was present within the underlying
     *                     ResourceBundle structure.
     * @return The localized T instance acquired from the underlying ResourceBundle structure at key
     *         <code>key</code>, or the defaultValue should no value exist for the given key within the underlying
     *         ResourceBundle structure.
     */
    @Override
    public final String getLocalized(final String key, final String defaultValue) {
        return getLocalized(key, defaultValue, defaultLocale);
    }

    /**
     * Retrieves a localized T instance from the underlying local resources.
     *
     * @param key          The key within the underlying ResourceBundle structure for the value to acquire.
     * @param defaultValue The fallback/default value to return if no value was present within the underlying
     *                     ResourceBundle structure.
     * @param locale       The locale for which the resource should be retrieved.
     * @return The localized T instance acquired from the underlying ResourceBundle structure at key
     *         <code>key</code>, or the defaultValue should no value exist for the given key within the underlying
     *         ResourceBundle structure.
     */
    @Override
    public final String getLocalized(final String key, final String defaultValue, final Locale locale) {
        return getLocalized(key, defaultValue, locale, null);
    }

    /**
     * Retrieves a localized T instance from the underlying local resources,
     * using the currently active Locale.
     *
     * @param key            The key within the underlying ResourceBundle structure for the value to acquire.
     * @param defaultValue   The fallback/default value to return if no value was present within the underlying
     *                       ResourceBundle structure.
     * @param keyValueTokens Either <code>null</code>, or a list holding strings on the form key=value where
     *                       both key and value must be non-empty. The keyValueTokens will be substituted before
     *                       the result is returned.
     * @return The localized T instance acquired from the underlying ResourceBundle structure at key
     *         <code>key</code>, or the defaultValue should no value exist for the given key within the underlying
     *         ResourceBundle structure.
     */
    @Override
    public final String getLocalized(final String key, final String defaultValue, final String... keyValueTokens) {
        return getLocalized(key, defaultValue, defaultLocale, keyValueTokens);
    }

    /**
     * Retrieves a localized T instance from the underlying local resources, and using the provided arguments
     * as token placeholders.
     *
     * @param key            The key within the underlying ResourceBundle structure for the value to acquire.
     * @param defaultValue   The fallback/default value to return if no value was present within the underlying
     *                       ResourceBundle structure.
     * @param locale         The locale for which the resource should be retrieved.
     * @param keyValueTokens Either <code>null</code>, or a list holding strings on the form key=value where
     *                       both key and value must be non-empty.
     * @return The localized T instance acquired from the underlying ResourceBundle structure at key
     *         <code>key</code>, or the defaultValue should no value exist for the given key within the underlying
     *         ResourceBundle structure.
     */
    @Override
    public String getLocalized(final String key,
                               final String defaultValue,
                               final Locale locale,
                               final String... keyValueTokens) {

        // Create the parser
        final CompoundParser parser = CompoundParser.create(keyValueTokens);

        // Should we acquire the localized ResourceBundle?
        if (!resourceBundleCache.keySet().contains(locale)) {
            cacheResourceBundleForLocale(locale);
        }

        // Acquire and return the result.
        String toReturn = defaultValue;
        try {
            toReturn = resourceBundleCache.get(locale).getString(key);
        } catch (MissingResourceException e) {
            // Just ignore this.
        }

        // All done.
        return toReturn == null ? null : parser.substituteTokens(toReturn);
    }

    /**
     * (Re-)assigns the locale used should no Locale be submitted with
     * the lookup call. The defaultLocale is kept as long as the LocalResources
     * instance lives.
     *
     * @param defaultLocale The new default locale, used if no Locale is submitted with calls.
     * @return <code>true</code> if the defaultLocale was successfully set, and false otherwise.
     */
    @Override
    public final boolean setDefaultLocale(final Locale defaultLocale) {

        // Check sanity
        Validate.notNull(defaultLocale, "Cannot handle null defaultLocale argument.");

        // Is the defaultLocale already present within the cache?
        if (this.resourceBundleCache.keySet().contains(defaultLocale)) {
            this.defaultLocale = defaultLocale;
            return true;
        }

        // Nopes. Let's acquire the ResourceBundle for the provided defaultLocale.
        final boolean success = cacheResourceBundleForLocale(defaultLocale);
        if (success) {
            this.defaultLocale = defaultLocale;
        }

        // All done.
        return success;
    }

    //
    // Private helpers
    //

    private boolean cacheResourceBundleForLocale(final Locale locale) {

        synchronized (lock) {

            // We need to re-set the ResourceBundle, after manipulating the Default Locale briefly.
            final Locale previousLocale = Locale.getDefault();

            try {
                // The ResourceBundle loader always uses the default
                // locale to load the ResourceBundle internally ...
                Locale.setDefault(locale);

                try {
                    final ResourceBundle bundle = Utf8ResourceBundle.getBundle(resourceBundleBaseName, locale);
                    resourceBundleCache.put(locale, bundle);
                } catch (MissingResourceException e) {
                    // Ignore this error... and note that the locale is not set.
                    log.warn("Not caching nonexistent ResourceBundle for missing base name ["
                            + resourceBundleBaseName + "]", e);
                    return false;
                }
            } finally {
                Locale.setDefault(previousLocale);
            }
        }

        // All done.
        return true;
    }
}
