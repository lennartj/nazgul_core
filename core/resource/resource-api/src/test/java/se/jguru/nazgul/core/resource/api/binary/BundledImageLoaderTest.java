/*
 * #%L
 * Nazgul Project: nazgul-core-resource-api
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
package se.jguru.nazgul.core.resource.api.binary;

import org.junit.Assert;
import org.junit.Test;

import java.awt.Image;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BundledImageLoaderTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullResource() {

        // Act & Assert
        BundledImageLoader.getBundledImage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyResource() {

        // Act & Assert
        BundledImageLoader.getBundledImage("");
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullClassLoader() {

        // Act & Assert
        BundledImageLoader.getBundledImage("irrelevant", null);
    }

    @Test
    public void validateInitialSlashIsPeeledOff() {

        // Assemble
        final String relativePath = "binary/orange_ball.png";
        final String absolutePath = "/" + relativePath;

        // Act
        final Image absoluteImage = BundledImageLoader.getBundledImage(absolutePath);
        final Image relativeImage = BundledImageLoader.getBundledImage(relativePath);

        // Assert
        Assert.assertNotNull("Got null image using absolute path.", absoluteImage);
        Assert.assertNotNull("Got null image using relative path.", relativeImage);
    }

    @Test
    public void validateNullImageReturnedForNonexistentResourcePath() {

        // Assemble
        final String nonexistentImage = "a/non/existent/image.png";

        // Act & Assert
        Assert.assertNull(BundledImageLoader.getBundledImage(nonexistentImage));
    }
}
