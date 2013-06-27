/*
 * #%L
 * Nazgul Project: nazgul-core-osgi-test
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

package se.jguru.nazgul.test.osgi.event;

import org.junit.Assert;
import org.junit.Test;

import se.jguru.nazgul.test.osgi.TracingBundleListener;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
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
        Assert.assertEquals(id1, adapter1.getClusterId());
        Assert.assertEquals(id2, adapter2.getClusterId());

        Assert.assertEquals(listener1.hashCode(), adapter1.hashCode());
        Assert.assertEquals(listener2.hashCode(), adapter2.hashCode());
        Assert.assertTrue(listener1.equals(listener1));
        Assert.assertFalse(listener1.equals(listener2));
        Assert.assertFalse(listener1.equals(adapter1));

        Assert.assertEquals(new Integer(adapter1.hashCode()).compareTo(adapter2.hashCode()),
                adapter1.compareTo(adapter2));
    }
}
