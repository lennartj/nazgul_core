/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.resource.impl.resourcebundle.parser;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class Utf8ResourceBundleTest {

    @Test
    public void validateGettingKeysEnumeration() {

        // Assemble
        final ResourceBundle bundle = Utf8ResourceBundle.getBundle("test/resources/utf8chars", new Locale("sv"));
        final List<String> bundleKeys = new ArrayList<String>();

        // Act
        final Enumeration<String> keys = bundle.getKeys();
        while(keys.hasMoreElements()) {
            bundleKeys.add(keys.nextElement());
        }

        // Assert
        Assert.assertTrue(bundleKeys.contains("nonAsciiLetters"));
        Assert.assertTrue(bundleKeys.contains("emptyValue"));
    }
}
