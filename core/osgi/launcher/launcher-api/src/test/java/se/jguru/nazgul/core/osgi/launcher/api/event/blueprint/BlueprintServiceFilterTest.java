/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api.event.blueprint;

import junit.framework.Assert;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import se.jguru.nazgul.test.osgi.MockBundle;
import se.jguru.nazgul.test.osgi.MockServiceReference;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BlueprintServiceFilterTest {

    // Shared state
    private MockServiceReference serviceReference;
    private MockBundle bundle;

    @Before
    public void setupSharedState() {
        bundle = new MockBundle("1.2.3.FooBar");
        serviceReference = new MockServiceReference(bundle, SimpleDateFormat.class.getName());
    }

    @Test
    public void validateMockedNameForBlueprintServices() {

        // Assemble
        final String name = "foobar";
        final ServiceReference mockReference = EasyMock.createMock(ServiceReference.class);

        EasyMock.expect(mockReference.getProperty(BlueprintServiceFilter.BLUEPRINT_NAME_KEY)).andReturn(name);
        EasyMock.replay(mockReference);

        // Act
        final String result = BlueprintServiceFilter.getBlueprintName(mockReference);

        // Assert
        EasyMock.verify(mockReference);
        Assert.assertEquals(name, result);
    }

    @Test
    public void validateBlueprintContainerFilter() {

        // Assemble
        final String expected = "(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)";

        // Act
        final String result = BlueprintServiceFilter.getBlueprintContainerServiceLDAPFilter();

        // Assert
        Assert.assertEquals(expected, result);
    }

    @Test
    public void validateAcceptingBlueprintServices() {

        // Assemble
        final BlueprintServiceFilter unitUnderTest = new BlueprintServiceFilter();
        final MockServiceReference altServiceReference = new MockServiceReference(bundle, Date.class.getName());
        altServiceReference.getRegistrationProperties().put(BlueprintServiceFilter.BLUEPRINT_NAME_KEY, "someName");

        // Act & Assert
        Assert.assertFalse(unitUnderTest.accept(serviceReference));
        Assert.assertTrue(unitUnderTest.accept(altServiceReference));
    }
}
