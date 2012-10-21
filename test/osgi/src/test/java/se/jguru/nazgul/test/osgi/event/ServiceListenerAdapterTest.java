/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.osgi.event;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.osgi.TracingServiceListener;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ServiceListenerAdapterTest {

    @Test
    public void validateEqualityAndComparison() {

        // Assemble
        final TracingServiceListener listener1 = new TracingServiceListener("listener1");
        final TracingServiceListener listener2 = new TracingServiceListener("listener2");

        final String id1 = "Adapter1";
        final String id2 = "Adapter2";
        final ServiceListenerAdapter adapter1 = new ServiceListenerAdapter(id1, ServiceListenerAdapter.class, listener1);
        final ServiceListenerAdapter adapter2 = new ServiceListenerAdapter(id2, ServiceListenerAdapter.class, listener2);

        // Act & Assert
        Assert.assertEquals(id1, adapter1.getId());
        Assert.assertEquals(id2, adapter2.getId());

        Assert.assertEquals(listener1.hashCode(), adapter1.hashCode());
        Assert.assertEquals(listener2.hashCode(), adapter2.hashCode());

        Assert.assertEquals(new Integer(adapter1.hashCode()).compareTo(adapter2.hashCode()),
                adapter1.compareTo(adapter2));
    }
}
