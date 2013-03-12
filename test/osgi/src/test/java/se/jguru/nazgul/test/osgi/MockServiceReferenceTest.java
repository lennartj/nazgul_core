/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.test.osgi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockServiceReferenceTest {

    // Shared state
    private String osgiStringVersion = "1.2.3.SNAPSHOT";
    private MockBundle bundle;
    private MockServiceReference unitUnderTest;

    @Before
    public void setupSharedState() {
        bundle = new MockBundle(osgiStringVersion);
        unitUnderTest = new MockServiceReference(bundle, Date.class.getName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateAssignableToNotImplemented() {

        // Act & Assert
        unitUnderTest.isAssignableTo(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateBundleUsageNotImplemented() {

        // Act & Assert
        unitUnderTest.getUsingBundles();
    }

    @Test(expected = ClassCastException.class)
    public void validateExceptionOnComparingToNonMockServiceReference() {

        // Act & Assert
        unitUnderTest.compareTo("fooBar");
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final MockBundle bundle1 = new MockBundle("1.2.3");
        final MockBundle bundle2 = new MockBundle("1.2.3.SNAPSHOT");

        final MockServiceReference ref1 = new MockServiceReference(bundle1, Date.class.getName());
        final MockServiceReference ref2 = new MockServiceReference(bundle2, Date.class.getName());

        ref2.setServiceRanking(2);

        // Act & Assert
        Assert.assertTrue(ref1.compareTo(ref2) != 0);
        Assert.assertEquals(ref1, unitUnderTest);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullServiceID() {

        // Assemble
        final List<String> serviceClassNames = new ArrayList<String>();
        serviceClassNames.add(Date.class.getName());

        final Hashtable<String, String> registrationProperties = new Hashtable<String, String>();
        final int ranking = 42;

        // Act & Assert
        new MockServiceReference(bundle, serviceClassNames, registrationProperties, null, ranking);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyServiceID() {

        // Assemble
        final List<String> serviceClassNames = new ArrayList<String>();
        serviceClassNames.add(Date.class.getName());

        final Hashtable<String, String> registrationProperties = new Hashtable<String, String>();
        final int ranking = 42;

        // Act & Assert
        new MockServiceReference(bundle, serviceClassNames, registrationProperties, "", ranking);
    }

    @Test
    public void validatePropertyLifecycle() {

        // Assemble
        final List<String> serviceClassNames = new ArrayList<String>();
        serviceClassNames.add(Date.class.getName());

        final Hashtable<String, String> registrationProperties = new Hashtable<String, String>();
        registrationProperties.put("foo", "bar");
        registrationProperties.put("baz", "gnat");

        final int registrationPropertiesOriginalSize = registrationProperties.size();

        final String serviceID = "serviceID";
        final int ranking = 42;
        unitUnderTest = new MockServiceReference(bundle, serviceClassNames, registrationProperties, serviceID, ranking);

        // Act
        final List<String> propertyKeys = Arrays.asList(unitUnderTest.getPropertyKeys());
        final Dictionary regProps = unitUnderTest.getRegistrationProperties();

        // Assert
        Assert.assertEquals(3 + registrationPropertiesOriginalSize, propertyKeys.size());
        Assert.assertEquals("bar", unitUnderTest.getProperty("foo"));
        Assert.assertEquals("gnat", unitUnderTest.getProperty("baz"));
        Assert.assertNotNull(unitUnderTest.getProperty(Constants.SERVICE_ID));
        Assert.assertNotNull(unitUnderTest.getProperty(Constants.SERVICE_RANKING));
        Assert.assertSame(bundle, unitUnderTest.getBundle());
        Assert.assertNotNull(regProps);
    }
}
