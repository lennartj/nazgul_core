/*
 * #%L
 * Nazgul Project: nazgul-core-jmx-api
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
/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.jmx.api.event;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.jmx.api.helper.TestNotificationListener;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class DefaultNotificationEmitterTest {

    @Test
    public void validateNotification() {

        // Assemble
        final Notification notification = new AttributeChangeNotification(
                this, 1, 254, "testEventMessage", "testAttributeName", String.class.getName(),
                "nuffink", "somefink");
        final TestNotificationListener listener = new TestNotificationListener();
        final MBeanNotificationInfo info = new MBeanNotificationInfo(
                new String[]{AttributeChangeNotification.ATTRIBUTE_CHANGE},
                "state",
                "LifecycleState changed");
        final DefaultNotificationEmitter unitUnderTest = new DefaultNotificationEmitter("testId", info);

        // Act
        unitUnderTest.handleNotification(listener, notification, null);

        // Assert
        Assert.assertTrue(unitUnderTest.toString().contains("javax.management.MBeanNotificationInfo"));
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullId() {

        // Act & Assert
        final MBeanNotificationInfo info = new MBeanNotificationInfo(
                new String[]{AttributeChangeNotification.ATTRIBUTE_CHANGE},
                "state",
                "LifecycleState changed");
        new DefaultNotificationEmitter(null, info);
    }
}
