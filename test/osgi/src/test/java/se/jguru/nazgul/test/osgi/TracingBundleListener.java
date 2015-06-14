/*
 * #%L
 * Nazgul Project: nazgul-core-osgi-test
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

package se.jguru.nazgul.test.osgi;

import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TracingBundleListener implements BundleListener {

    public List<BundleEvent> callTrace = new ArrayList<BundleEvent>();
    public CountDownLatch onEventLatch;

    // Internal state
    private String id;

    /**
     * Creates a non-blocking TracingBundleListener instance.
     */
    public TracingBundleListener(final String id) {
        this(-1, id);
    }

    /**
     * Creates a TracingBundleListener instance holding a non-null onEventLatch with the given count.
     * The count of the onEventLatch is  decreased whenever the bundleChanged method is called.
     * For any value of countDownValue less than 1, no CountDownLatch will be created.
     *
     * @param countDownValue The countDown value of the created onEventLatch.
     * @see #onEventLatch
     */
    public TracingBundleListener(final int countDownValue, final String id) {

        this.id = id;
        if (countDownValue > 0) {
            onEventLatch = new CountDownLatch(countDownValue);
        }
    }

    /**
     * Receives notification that a bundle has had a lifecycle change.
     *
     * @param event The {@code BundleEvent}.
     */
    @Override
    public void bundleChanged(final BundleEvent event) {

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
