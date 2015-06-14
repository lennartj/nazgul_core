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
package se.jguru.nazgul.core.jmx.api.helper;

import se.jguru.nazgul.core.jmx.api.AbstractSuspendableMBean;

import javax.management.NotificationEmitter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class TestSuspendableMBean extends AbstractSuspendableMBean {

    // Shared state
    public boolean suspendOK = true;
    public boolean resumeOK = true;

    public boolean suspendCalled = false;
    public boolean resumeCalled = false;

    /**
     * Convenience constructor, creating a new AbstractMBean wrapping the supplied data.
     * All NotificationEmitter operations executed in this AbstractMBean are delegated to
     * the provided NotificationEmitter object. Thus, all JMX notifications fired by this
     * AbstractMBean are simply delegated to the supplied NotificationEmitter.
     * <p/>
     * <p>This constructor must be called from a subclass that implements the provided {@code mbeanInterface}.</p>
     *
     * @param mbeanInterface a StandardMBean interface.
     * @param delegate       A non-null NotificationEmitter to which this AbstractMBean will delegate all
     *                       NotificationEmitter operations.
     * @throws IllegalArgumentException if the {@code mbeanInterface} does not follow JMX design patterns for
     *                                  Management Interfaces, or if {@code this} does not implement the
     *                                  specified
     *                                  interface, or if {@code delegate} is null.
     * @see javax.management.StandardEmitterMBean#StandardEmitterMBean(Class, boolean,
     * javax.management.NotificationEmitter)
     */
    public TestSuspendableMBean(final Class<?> mbeanInterface,
                                final NotificationEmitter delegate) {
        super(mbeanInterface, delegate);
    }

    /**
     * <p>Creates an AbstractMBean with the management interface {@code mbeanInterface}, and where notifications are
     * handled by the given {@code NotificationEmitter}. This constructor can be used to make either Standard MBeans
     * or MXBeans. The resultant MBean implements the {@code NotificationEmitter} interface by forwarding its methods
     * to {@code delegate}.</p>
     * <p/>
     * <p>If {@code delegate} is an instance of {@code NotificationBroadcasterSupport} then the MBean's {@link
     * #sendNotification sendNotification} method will delegate its invocation to {@code delegate.}{@link
     * NotificationBroadcasterSupport#sendNotification sendNotification}.</p>
     * <p/>
     * <p>The array returned by {@link #getNotificationInfo()} on the new MBean is a copy of the array returned by
     * {@code emitter.}{@link NotificationBroadcaster#getNotificationInfo getNotificationInfo()} at the time of
     * construction. If the array returned by {@code emitter.getNotificationInfo()} later changes, that will have no
     * effect on this object's {@code getNotificationInfo()}.</p>
     * <p/>
     * <p>This constructor must be called from a subclass that implements the given {@code mbeanInterface}.</p>
     *
     * @param mbeanInterface a StandardMBean interface.
     * @param isMXBean       If true, the {@code mbeanInterface} parameter
     *                       names an MXBean interface and the resultant MBean is an MXBean.
     * @param delegate       A non-null NotificationEmitter to which this AbstractMBean will delegate all
     *                       NotificationEmitter operations.
     * @throws IllegalArgumentException if the {@code mbeanInterface}
     *                                  does not follow JMX design patterns for Management Interfaces, or
     *                                  if {@code this} does not implement the specified interface, or
     *                                  if {@code delegate} is null.
     */
    public TestSuspendableMBean(final Class<?> mbeanInterface,
                                final boolean isMXBean,
                                final NotificationEmitter delegate) {
        super(mbeanInterface, isMXBean, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean suspendMBeanOperations() {
        suspendCalled = true;
        return suspendOK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean resumeMBeanOperations() {
        resumeCalled = true;
        return resumeOK;
    }
}
