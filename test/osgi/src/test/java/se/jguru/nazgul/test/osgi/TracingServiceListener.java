/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.osgi;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TracingServiceListener implements ServiceListener {

    public List<ServiceEvent> callTrace = new ArrayList<ServiceEvent>();
    public CountDownLatch onEventLatch;

    // Internal state
    private String id;

    /**
     * Creates a non-blocking TracingBundleListener instance.
     */
    public TracingServiceListener(final String id) {
        this(-1, id);
    }

    /**
     * Creates a TracingServiceListener instance holding a non-null onEventLatch with the given count.
     * The count of the onEventLatch is  decreased whenever the bundleChanged method is called.
     * For any value of countDownValue less than 1, no CountDownLatch will be created.
     *
     * @param countDownValue The countDown value of the created onEventLatch.
     * @see #onEventLatch
     */
    public TracingServiceListener(final int countDownValue, final String id) {

        this.id = id;
        if (countDownValue > 0) {
            onEventLatch = new CountDownLatch(countDownValue);
        }
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @Override
    public void serviceChanged(ServiceEvent event) {

        callTrace.add(event);

        if (onEventLatch != null) {
            onEventLatch.countDown();
        }
    }

    /**
     * @return The id of this TracingBundleListener.
     */
    public String getId() {
        return id;
    }
}
