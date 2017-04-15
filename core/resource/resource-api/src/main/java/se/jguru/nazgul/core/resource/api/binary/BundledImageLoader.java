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
package se.jguru.nazgul.core.resource.api.binary;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

/**
 * Convenience class used to load pre-bundled images from the local
 * classpath - frequently JAR(s).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class BundledImageLoader {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(BundledImageLoader.class);

    // Internal state
    private static Toolkit toolkit;

    /**
     * Loads/creates an Image as a resource using the ClassLoader of BundledImageLoader.
     *
     * @param classpathRelativeResource The classpath-relative resource to create an Image from.
     * @return The loaded bundled Image.
     */
    public static Image getBundledImage(final String classpathRelativeResource) {
        return getBundledImage(classpathRelativeResource, BundledImageLoader.class.getClassLoader());
    }

    /**
     * Loads/creates an Image as a resource using the provided ClassLoader.
     *
     * @param classpathRelativeResource The classpath-relative resource to create an Image from.
     * @param loader                    The ClassLoader to use in loading the image.
     * @return The loaded bundled Image.
     * @throws IllegalArgumentException if any argument was null or empty or the actual call to getImage
     */
    public static Image getBundledImage(final String classpathRelativeResource, final ClassLoader loader)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notEmpty(classpathRelativeResource, "Cannot handle null or empty classpathRelativeResource.");
        Validate.notNull(loader, "Cannot handle null loader argument.");

        // Peel off any beginning "/".
        final String resourcePath = (classpathRelativeResource.charAt(0) == '/'
                ? classpathRelativeResource.substring(1)
                : classpathRelativeResource);

        final URL imgURL = loader.getResource(resourcePath);
        log.debug("Loading image (ResourcePath: " + resourcePath + ") from URL [" + imgURL + "] ");

        // No point in trying to load a nonexistent resource.
        if (imgURL == null) {
            return null;
        }

        try {
            return getToolkit().getImage(imgURL);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not load image from classpathRelativeResource ["
                    + classpathRelativeResource + "]", e);
        }
    }

    //
    // Private helpers
    //

    /**
     * Acquires the default Toolkit.
     *
     * @return the default Toolkit.
     */
    @SuppressWarnings("PMD")
    private static Toolkit getToolkit() {
        if (toolkit == null) {
            toolkit = Toolkit.getDefaultToolkit();
        }

        return toolkit;
    }
}
