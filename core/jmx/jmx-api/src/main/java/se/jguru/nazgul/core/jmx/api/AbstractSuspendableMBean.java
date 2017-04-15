/*
 * #%L
 * Nazgul Project: nazgul-core-jmx-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.jmx.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.AttributeChangeNotificationFilter;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;

/**
 * Abstract implementation of a Suspendable AbstractMBean, sporting LifecycleState
 * validation for the suspend and resume methods.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public abstract class AbstractSuspendableMBean extends AbstractMBean implements Suspendable {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractSuspendableMBean.class);

    /**
     * The attribute name in the Notification emitted when this AbstractSuspendableMBean is successfully suspended.
     */
    public static final String SUSPENDED_ATTRIBUTENAME = "suspended";

    /**
     * The attribute name in the Notification emitted when this AbstractSuspendableMBean is successfully resumed.
     */
    public static final String RESUMED_ATTRIBUTENAME = "resumed";

    /**
     * <p>Convenience constructor, creating a new AbstractMBean wrapping the supplied data.
     * All NotificationEmitter operations executed in this AbstractMBean are delegated to
     * the provided NotificationEmitter object. Thus, all JMX notifications fired by this
     * AbstractMBean are simply delegated to the supplied NotificationEmitter.</p>
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
    protected AbstractSuspendableMBean(final Class<?> mbeanInterface, final NotificationEmitter delegate) {
        super(mbeanInterface, delegate);
    }

    /**
     * <p>Creates an AbstractMBean with the management interface {@code mbeanInterface}, and where notifications are
     * handled by the given {@code NotificationEmitter}. This constructor can be used to make either Standard MBeans
     * or MXBeans. The resultant MBean implements the {@code NotificationEmitter} interface by forwarding its methods
     * to {@code delegate}.</p>
     * <p>If {@code delegate} is an instance of {@code NotificationBroadcasterSupport} then the MBean's
     * {@link javax.management.NotificationBroadcasterSupport#sendNotification(Notification)} method will delegate
     * its invocation to {@code delegate.}{@link javax.management.NotificationBroadcasterSupport#sendNotification(Notification)}.</p>
     * <p>The array returned by {@link #getNotificationInfo()} on the new MBean is a copy of the array returned by
     * {@code emitter.}{@link NotificationBroadcaster#getNotificationInfo()} at the time of
     * construction. If the array returned by {@code emitter.getNotificationInfo()} later changes, that will have no
     * effect on this object's {@code getNotificationInfo()}.</p>
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
    protected AbstractSuspendableMBean(final Class<?> mbeanInterface, final boolean isMXBean, final NotificationEmitter delegate) {
        super(mbeanInterface, isMXBean, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean suspend() throws IllegalStateException {

        // Check sanity
        if (getState() == LifecycleState.STOPPED) {

            if (log.isInfoEnabled()) {
                log.info("AbstractSuspendableMBean [" + getClass().getName() + "] was already in ["
                        + LifecycleState.STOPPED + "]. Aborting suspend.");
            }

            // All done.
            return true;
        }
        if (getState() != LifecycleState.STARTED) {
            throw new IllegalStateException("Cannot suspend from state [" + getState() + "]. (Required: "
                    + LifecycleState.STARTED + ").");
        }

        // Delegate & make the state transition
        if (suspendMBeanOperations()
                && performStateTransition(false, LifecycleState.STARTED, LifecycleState.STOPPED)) {

            // Send an explicit "suspend successful" message
            sendAttributeChangeEvent(SUSPENDED_ATTRIBUTENAME,
                    "MBean Suspended. Type: [" + getClass().getName() + "]",
                    LifecycleState.class,
                    LifecycleState.STARTED,
                    LifecycleState.STOPPED);

            // All done.
            return true;
        }

        // Don't make a state transition.
        return false;
    }

    /**
     * {@code}
     */
    @Override
    public final boolean resume() throws IllegalStateException {

        // Check sanity
        if (getState() == LifecycleState.STARTED) {

            if (log.isInfoEnabled()) {
                log.info("AbstractSuspendableMBean [" + getClass().getName() + "] was already in ["
                        + LifecycleState.STARTED + "]. Aborting resume.");
            }

            // All done.
            return true;
        }
        if (getState() != LifecycleState.STOPPED) {
            throw new IllegalStateException("Cannot resume from state [" + getState() + "]. (Required: "
                    + LifecycleState.STOPPED + ").");
        }


        // Delegate
        if (resumeMBeanOperations()
                && performStateTransition(false, LifecycleState.STOPPED, LifecycleState.STARTED)) {

            // Send an explicit "resume successful" message
            sendAttributeChangeEvent(RESUMED_ATTRIBUTENAME,
                    "MBean Resumed. Type: [" + getClass().getName() + "]",
                    LifecycleState.class,
                    LifecycleState.STOPPED,
                    LifecycleState.STARTED);

            // All done.
            return true;
        }

        // Don't make a state transition.
        return false;
    }

    /**
     * Implement this method to define what should be done to suspend (operations within) this MBean.
     * Invoked from the {@code suspend()} method; this method should not throw Exceptions.
     *
     * @return {@code true} if this AbstractSuspendableMBean was successfully
     * suspended, and {@code false} otherwise.
     */
    protected abstract boolean suspendMBeanOperations();

    /**
     * Implement this method to define what should be done to resume (operations within) this MBean.
     * Invoked from the {@code resume()} method; this method should not throw Exceptions.
     *
     * @return {@code true} if this AbstractSuspendableMBean was successfully
     * resumed, and {@code false} otherwise.
     */
    protected abstract boolean resumeMBeanOperations();

    /**
     * @return An AttributeChangeNotificationFilter which will listen only to state change events emitted by
     * an AbstractSuspendableMBean subclass (e.g. AttributeChangeEvent with attributeName set to the value of
     * constants {@code SUSPENDED_ATTRIBUTENAME} or {@code RESUMED_ATTRIBUTENAME}).
     */
    public static AttributeChangeNotificationFilter getSuspendResumeFilter() {

        final AttributeChangeNotificationFilter toReturn = new AttributeChangeNotificationFilter();
        toReturn.enableAttribute(AbstractSuspendableMBean.SUSPENDED_ATTRIBUTENAME);
        toReturn.enableAttribute(AbstractSuspendableMBean.RESUMED_ATTRIBUTENAME);

        // All done.
        return toReturn;
    }
}
