/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.osgi.event;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.osgi.TracingBundleListener;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BundleListenerAdapterTest {

    @Test
    public void validateEqualityAndComparison() {

        // Assemble
        final TracingBundleListener listener1 = new TracingBundleListener("listener1");
        final TracingBundleListener listener2 = new TracingBundleListener("listener2");

        final String id1 = "Adapter1";
        final String id2 = "Adapter2";
        final BundleListenerAdapter adapter1 = new BundleListenerAdapter(id1, BundleListenerAdapter.class, listener1);
        final BundleListenerAdapter adapter2 = new BundleListenerAdapter(id2, BundleListenerAdapter.class, listener2);

        // Act & Assert
        Assert.assertEquals(id1, adapter1.getId());
        Assert.assertEquals(id2, adapter2.getId());

        Assert.assertEquals(listener1.hashCode(), adapter1.hashCode());
        Assert.assertEquals(listener2.hashCode(), adapter2.hashCode());

        Assert.assertEquals(new Integer(adapter1.hashCode()).compareTo(adapter2.hashCode()),
                adapter1.compareTo(adapter2));
    }
}
