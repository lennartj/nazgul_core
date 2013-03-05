/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.test.blueprint;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BundleStateTest {

    // Shared state
    private Map<String, Integer> name2BundleStateMap;

    @Before
    public void setupSharedState() {

        name2BundleStateMap = new TreeMap<String, Integer>();
        name2BundleStateMap.put("UNINSTALLED", Bundle.UNINSTALLED);
        name2BundleStateMap.put("INSTALLED", Bundle.INSTALLED);
        name2BundleStateMap.put("RESOLVED", Bundle.RESOLVED);
        name2BundleStateMap.put("STARTING", Bundle.STARTING);
        name2BundleStateMap.put("STOPPING", Bundle.STOPPING);
        name2BundleStateMap.put("ACTIVE", Bundle.ACTIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnParsingUnknownBundleState() {

        // Act & Assert
        BundleState.convert(-3);
    }

    @Test
    public void validateConvertingBundleStateContantToEnum() {

        // Act
        for(Map.Entry<String, Integer> current : name2BundleStateMap.entrySet()) {

            final BundleState bundleState = BundleState.convert(current.getValue());
            Assert.assertEquals(current.getKey(), bundleState.name());
        }
    }

    @Test
    public void validateBundleStateData() throws Exception {

        // Act & Assert
        for (BundleState current : BundleState.values()) {

            Assert.assertEquals(current.name().toLowerCase(), current.toString());

            // Acquire the Bundle.state variable for the current BundleName.
            Field bundleField = Bundle.class.getField(current.name());
            int result = (Integer) bundleField.get(null);

            Assert.assertEquals(result, current.getOsgiBundleStateValue());
        }
    }
}
