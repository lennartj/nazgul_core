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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.jmx.api.event.DefaultNotificationEmitter;
import se.jguru.nazgul.core.jmx.api.helper.TestAbstractMBean;
import se.jguru.nazgul.core.jmx.api.helper.TestNotificationListener;
import se.jguru.nazgul.core.jmx.test.mbeanserver.AbstractJmxTest;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class T_AbstractJmxTest extends AbstractJmxTest {

    // Shared state
    private ObjectName mbeanName;
    private TestAbstractMBean unitUnderTest;
    private DefaultNotificationEmitter emitter;
    private TestNotificationListener listener;
    private ListAppender<ILoggingEvent> debugAppender;

    /**
     * {@inheritDoc}
     */
    @Before
    public void customSetupSharedState() throws Exception {

        // Add a debug Logging adapter.
        // assume SLF4J is bound to logback in the current environment
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger abstractMbeanLogger = context.getLogger(AbstractMBean.class);
        Assert.assertNotNull("Null abstractMBeanLogger", abstractMbeanLogger);

        // Add and start a new Appender to collect all log statements.
        debugAppender = new ListAppender<ILoggingEvent>();
        abstractMbeanLogger.addAppender(debugAppender);
        debugAppender.start();

        // Create the unitUnderTest and register it using the test names.
        mbeanName = new ObjectName(getTestDomain(), "type", TestAbstractMBean.class.getSimpleName());
        emitter = DefaultNotificationEmitter.getDefaultStateChangeNotificationEmitter("testId");

        unitUnderTest = new TestAbstractMBean(LifecycleStateful.class, emitter);

        // Add a notification listener to receive notifications from the TestAbstractMBean.
        listener = new TestNotificationListener();
        unitUnderTest.addNotificationListener(listener, null, "handback!");
    }

    @Test
    public void validateLifecycleStateTransitions() throws Exception {

        // Assemble
        final MBeanServer mBeanServer = rule.getMBeanServer();
        final List<LifecycleState> expected = Arrays.asList(
                LifecycleState.STARTING,
                LifecycleState.STARTED,
                LifecycleState.STOPPING,
                LifecycleState.STOPPED);

        // Act
        final ObjectInstance mBean = mBeanServer.registerMBean(unitUnderTest, mbeanName);
        final LifecycleState state = LifecycleState.valueOf("" + getAttribute(mBean.getObjectName(), "State"));
        mBeanServer.unregisterMBean(mbeanName);

        // Assert
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(expected.get(i), listener.states.get(i));
        }
        Assert.assertEquals(LifecycleState.STARTED, state);
        Assert.assertTrue(unitUnderTest.preRegisterInvoked);
        Assert.assertTrue(unitUnderTest.postRegisterInvoked);
        Assert.assertTrue(unitUnderTest.preDeregisterInvoked);
        Assert.assertTrue(unitUnderTest.postDeregisterInvoked);
    }

    @Test
    public void validateExceptionWrappingOnPreRegistrationException() throws Exception {

        // Assemble
        final MBeanServer mBeanServer = rule.getMBeanServer();
        unitUnderTest.throwPreRegisterException = true;

        // Act & Assert
        try {
            mBeanServer.registerMBean(unitUnderTest, mbeanName);

            Assert.fail("Exceptions thrown during preRegistration should not be caught and handled by AbstractMBean.");
        } catch (RuntimeMBeanException e) {

            // Expected
            Assert.assertTrue("The inner exception, thrown by the AbstractMBean should be preserved as cause.",
                    e.getCause() instanceof IllegalArgumentException);

        } catch (Exception e) {
            Assert.fail("Expected RuntimeMBeanException, but got [" + e.getClass().getName() + "]");
        }

        Assert.assertTrue(unitUnderTest.preRegisterInvoked);
        Assert.assertFalse(unitUnderTest.postRegisterInvoked);
        Assert.assertFalse(unitUnderTest.preDeregisterInvoked);
        Assert.assertFalse(unitUnderTest.postDeregisterInvoked);
    }

    @Test
    public void validateLifecycleStateTransitionsOnExceptionInPostRegister() throws Exception {

        // Assemble
        final MBeanServer mBeanServer = rule.getMBeanServer();
        final List<LifecycleState> expected = Arrays.asList(
                LifecycleState.STARTING,
                LifecycleState.ERROR,
                LifecycleState.ERROR
                                                           );
        unitUnderTest.throwPostRegisterException = true;

        // Act
        final ObjectInstance mBean = mBeanServer.registerMBean(unitUnderTest, mbeanName);
        final LifecycleState state = LifecycleState.valueOf("" + getAttribute(mBean.getObjectName(), "State"));
        mBeanServer.unregisterMBean(mbeanName);

        // Assert
        Assert.assertEquals(LifecycleState.ERROR, state);

        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(expected.get(i), listener.states.get(i));
        }

        Assert.assertTrue(unitUnderTest.preRegisterInvoked);
        Assert.assertTrue(unitUnderTest.postRegisterInvoked);
        Assert.assertFalse(unitUnderTest.preDeregisterInvoked);
        Assert.assertTrue(unitUnderTest.postDeregisterInvoked);
    }

    @Test
    public void validateLifecycleStateTransitionOnExceptionInPreDeregister() throws Exception {

        // Assemble
        final MBeanServer mBeanServer = rule.getMBeanServer();
        final List<LifecycleState> expected = Arrays.asList(
                LifecycleState.STARTING,
                LifecycleState.STARTED,
                LifecycleState.STOPPING,
                LifecycleState.ERROR,
                LifecycleState.ERROR
                                                           );
        unitUnderTest.throwPreDeregisterException = true;

        // Act
        final ObjectInstance mBean = mBeanServer.registerMBean(unitUnderTest, mbeanName);
        final LifecycleState state = LifecycleState.valueOf("" + getAttribute(mBean.getObjectName(), "State"));
        mBeanServer.unregisterMBean(mbeanName);

        // Assert
        Assert.assertEquals(LifecycleState.STARTED, state);

        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(expected.get(i), listener.states.get(i));
        }

        Assert.assertTrue(unitUnderTest.preRegisterInvoked);
        Assert.assertTrue(unitUnderTest.postRegisterInvoked);
        Assert.assertTrue(unitUnderTest.preDeregisterInvoked);
        Assert.assertTrue(unitUnderTest.postDeregisterInvoked);
    }

    @Test
    public void validateLifecycleStateTransitionOnExceptionInPostDeregister() throws Exception {

        // Assemble
        final MBeanServer mBeanServer = rule.getMBeanServer();
        final List<LifecycleState> expected = Arrays.asList(
                LifecycleState.STARTING,
                LifecycleState.STARTED,
                LifecycleState.STOPPING,
                LifecycleState.ERROR,
                LifecycleState.ERROR
                                                           );
        unitUnderTest.throwPostDeregisterException = true;

        // Act
        final ObjectInstance mBean = mBeanServer.registerMBean(unitUnderTest, mbeanName);
        final LifecycleState state = LifecycleState.valueOf("" + getAttribute(mBean.getObjectName(), "State"));
        mBeanServer.unregisterMBean(mbeanName);

        // Assert
        Assert.assertEquals(LifecycleState.STARTED, state);

        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(expected.get(i), listener.states.get(i));
        }

        Assert.assertTrue(unitUnderTest.preRegisterInvoked);
        Assert.assertTrue(unitUnderTest.postRegisterInvoked);
        Assert.assertTrue(unitUnderTest.preDeregisterInvoked);
        Assert.assertTrue(unitUnderTest.postDeregisterInvoked);
    }

    @Test
    public void validateExceptionOnNullObjectName() throws Exception {

        // Assemble
        final MBeanServer mBeanServer = rule.getMBeanServer();

        // Act & Assert
        try {
            mBeanServer.registerMBean(unitUnderTest, null);

            Assert.fail("Should not be able to register an AbstractMBean with null objectName.");
        } catch (RuntimeMBeanException e) {

            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);

        } catch (Exception e) {
            Assert.fail("Expected RuntimeMBeanException, but got [" + e.getClass().getName() + "]");
        }
    }
}
