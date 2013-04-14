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

package se.jguru.nazgul.test.osgi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceEvent;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockServiceRegistrationTest {

    // Shared state
    private String osgiStringVersion = "1.2.3.SNAPSHOT";
    private MockBundle bundle;
    private MockServiceReference serviceReference;
    private MockServiceRegistration unitUnderTest;

    @Before
    public void setupSharedState() {
        bundle = new MockBundle(osgiStringVersion);

        serviceReference = new MockServiceReference(bundle, Date.class.getName());
        serviceReference.setServiceID("testServiceID");
        serviceReference.setServiceRanking(2);

        unitUnderTest = new MockServiceRegistration(serviceReference, (MockBundleContext) bundle.getBundleContext());
    }

    @Test
    public void validateLifecycleWithoutException() {

        // Assemble
        final TracingServiceListener listener = new TracingServiceListener("testID");
        final Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("foo", "bar");

        // Act
        bundle.getBundleContext().addServiceListener(listener);
        unitUnderTest.setProperties(properties);
        unitUnderTest.unregister();

        // Assert
        final List<ServiceEvent> callTrace = listener.callTrace;
        Assert.assertEquals(ServiceEvent.MODIFIED, callTrace.get(0).getType());
        Assert.assertEquals(ServiceEvent.UNREGISTERING, callTrace.get(1).getType());
        Assert.assertEquals(2, callTrace.size());
    }
}
