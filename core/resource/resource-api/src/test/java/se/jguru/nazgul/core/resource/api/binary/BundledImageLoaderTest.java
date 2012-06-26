/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.resource.api.binary;

import junit.framework.Assert;
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
