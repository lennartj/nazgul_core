/*-
 * #%L
 * Nazgul Project: nazgul-core-jmx-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.core.jmx.api;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.jmx.api.event.DefaultNotificationEmitter;
import se.jguru.nazgul.core.jmx.api.helper.TestAbstractMBean;
import se.jguru.nazgul.core.jmx.api.helper.TestNotificationListener;
import se.jguru.nazgul.core.jmx.api.helper.TestSuspendableMBean;
import se.jguru.nazgul.core.jmx.test.mbeanserver.AbstractJmxTest;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class T_AbstractSuspendableMBeanTest extends AbstractJmxTest {

    // Shared state
    private ObjectName mbeanName;
    private TestSuspendableMBean unitUnderTest;
    private DefaultNotificationEmitter emitter;
    private TestNotificationListener listener;
    private TestNotificationListener suspendStateListener;

    @Before
    public void customSetupSharedState() throws Exception {

        // Add a debug Logging adapter.
        // assume SLF4J is bound to logback in the current environment
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger abstractMbeanLogger = context.getLogger(AbstractMBean.class);
        Assert.assertNotNull("Null abstractMBeanLogger", abstractMbeanLogger);

        // Create the unitUnderTest and register it using the test names.
        mbeanName = new ObjectName(getTestDomain(), "type", TestAbstractMBean.class.getSimpleName());
        emitter = new DefaultNotificationEmitter("testId",
                new MBeanNotificationInfo(
                        new String[]{AttributeChangeNotification.ATTRIBUTE_CHANGE},
                        "state",
                        "LifecycleState changed")
        );

        unitUnderTest = new TestSuspendableMBean(Suspendable.class, emitter);

        // Add a notification listener to receive notifications from the TestAbstractMBean.
        listener = new TestNotificationListener();
        unitUnderTest.addNotificationListener(
                listener,
                AbstractMBean.getStateTransitionFilter(),
                AbstractMBean.STATE_CHANGE_ATTRIBUTENAME);

        suspendStateListener = new TestNotificationListener();
        unitUnderTest.addNotificationListener(
                suspendStateListener,
                AbstractSuspendableMBean.getSuspendResumeFilter(),
                "suspend/resume");
    }

    @Test
    public void validateLifecycleTransitions() throws Exception {

        // Assemble
        final MBeanServer mBeanServer = rule.getMBeanServer();
        final List<LifecycleState> expectedStateChangeEvents = Arrays.asList(
                LifecycleState.STARTING,
                LifecycleState.STARTED,
                LifecycleState.STOPPED,
                LifecycleState.STARTED,
                LifecycleState.STOPPING,
                LifecycleState.STOPPED
                                                                            );
        final List<LifecycleState> expectedSuspendResumeEvents = Arrays.asList(
                LifecycleState.STOPPED,
                LifecycleState.STARTED
                                                                              );

        // Act
        final ObjectInstance mBean = mBeanServer.registerMBean(unitUnderTest, mbeanName);
        final Suspendable proxy = getMXBeanProxy(mBean.getObjectName(), Suspendable.class);

        final LifecycleState stateBeforeSuspension = proxy.getState();
        final boolean suspended = proxy.suspend();
        final LifecycleState stateBeforeSuspension2 = proxy.getState();
        final boolean suspended2 = proxy.suspend();
        final LifecycleState stateAfterSuspension = proxy.getState();
        final boolean resumed = proxy.resume();
        final LifecycleState stateAfterResuming = proxy.getState();
        final boolean resumed2 = proxy.resume();
        final LifecycleState stateAfterResuming2 = proxy.getState();


        mBeanServer.unregisterMBean(mbeanName);

        // Assert
        Assert.assertEquals(LifecycleState.STARTED, stateBeforeSuspension);
        Assert.assertEquals(LifecycleState.STOPPED, stateAfterSuspension);
        Assert.assertEquals(LifecycleState.STARTED, stateAfterResuming);
        Assert.assertEquals(LifecycleState.STARTED, stateAfterResuming2);

        Assert.assertTrue(suspended);
        Assert.assertTrue(resumed);
        Assert.assertTrue(resumed2);

        for (int i = 0; i < expectedStateChangeEvents.size(); i++) {
            Assert.assertEquals(expectedStateChangeEvents.get(i), listener.states.get(i));
        }
        for (int i = 0; i < expectedSuspendResumeEvents.size(); i++) {
            Assert.assertEquals("Suspend/Resume event [" + i + "] incorrect.",
                    expectedSuspendResumeEvents.get(i),
                    suspendStateListener.states.get(i));
        }
    }
}
