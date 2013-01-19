/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.messaging.activemq;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.messaging.MessageBroker;

import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractActiveMqTestTest {

    @Test
    public void validateBrokerTypeAndLifecycle() throws Exception {

        // Assemble
        final MockActiveMqTest unitUnderTest = new MockActiveMqTest(true);
        final MessageBroker broker = unitUnderTest.getBroker();

        // Act
        unitUnderTest.startJmsBroker();
        unitUnderTest.stopJmsBroker();

        // Assert
        Assert.assertNotNull(broker);
        Assert.assertTrue(broker instanceof ActiveMQBroker);

        final List<String> callTrace = unitUnderTest.callTrace;
        Assert.assertEquals(2, callTrace.size());
        Assert.assertEquals("setupServices", callTrace.get(0));
        Assert.assertEquals("tearDownServices", callTrace.get(1));
    }
}
