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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;
import javax.management.ObjectName;
import javax.management.StandardEmitterMBean;

/**
 * Abstract StandardEmitterMBean implementation adhering to the StandardLifecycle.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public abstract class AbstractMBean extends StandardEmitterMBean implements LifecycleStateful {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractMBean.class);

    /**
     * The name of the state attribute.
     */
    public static final String STATE_CHANGE_ATTRIBUTENAME = "state";

    // Internal state
    private LifecycleState state = LifecycleState.UNINITIALIZED;
    private int eventSequenceId = 1;
    private final Object lock = new Object();

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
     *                                  Management Interfaces, or if {@code this} does not implement the specified
     *                                  interface, or if {@code delegate} is null.
     * @see javax.management.StandardEmitterMBean#StandardEmitterMBean(Class, boolean,
     * javax.management.NotificationEmitter)
     */
    public AbstractMBean(final Class<?> mbeanInterface,
                         final NotificationEmitter delegate) {
        this(mbeanInterface, true, delegate);
    }

    /**
     * <p>Creates an AbstractMBean with the management interface {@code mbeanInterface}, and where notifications are
     * handled by the given {@code NotificationEmitter}. This constructor can be used to make either Standard MBeans
     * or MXBeans. The resultant MBean implements the {@code NotificationEmitter} interface by forwarding its methods
     * to {@code delegate}.</p>
     * <p>If {@code delegate} is an instance of {@code NotificationBroadcasterSupport} then the MBean's {@link
     * #sendNotification sendNotification} method will delegate its invocation to {@code delegate.}
     * {@link javax.management.NotificationBroadcasterSupport#sendNotification(Notification)}.</p>
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
    public AbstractMBean(final Class<?> mbeanInterface,
                         final boolean isMXBean,
                         final NotificationEmitter delegate) {
        super(mbeanInterface, isMXBean, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final LifecycleState getState() {
        return state;
    }

    /**
     * <p>MBeanRegistration lifecycle method that delegates execution to (in order):</p>
     * <ol>
     * <li>{@code customPreregister()}, where subclasses may manipulate the JMX ObjectName.</li>
     * <li>{@code super.preRegister()}, performing the actual registration of this AbstractMBean
     * in the MBeanServer.</li>
     * </ol>
     * {@inheritDoc}
     *
     * @see #customPreregister(javax.management.MBeanServer, javax.management.ObjectName)
     */
    @Override
    public final ObjectName preRegister(final MBeanServer server, final ObjectName name) throws Exception {

        // Check sanity
        if (name == null) {
            throw new IllegalArgumentException("You should define an ObjectName for the MBean ["
                    + getClass().getName() + "]. Nameless AbstractMBeans are not permitted.");
        }

        // Perform any customizations on the ObjectName to be retrieved.
        ObjectName objectName = customPreregister(server, name);
        ObjectName toReturn = null;

        if (log.isDebugEnabled()) {
            log.debug("Preregistering object of type [" + getClass().getName() + "] under name ["
                    + objectName.getCanonicalName() + "]");
        }

        try {

            // Perform normal preregistration.
            toReturn = super.preRegister(server, objectName);

            // Preregistration successful; adjust the state.
            performStateTransition(true, LifecycleState.UNINITIALIZED, LifecycleState.STARTING);

        } catch (Exception e) {

            // Complain
            performStateTransition(false, LifecycleState.UNINITIALIZED, LifecycleState.ERROR);

            // Re-throw
            throw e;
        }

        if (log.isDebugEnabled()) {
            log.debug("PreRegister complete for [" + getClass().getName() + "]");
        }

        // All done
        return toReturn;
    }

    /**
     * Delegates to the {@code #customPostregister} method after invoking {@code #postRegister} method
     * in the superclass.
     * {@inheritDoc}
     *
     * @see #customPostregister()
     */
    @Override
    public final void postRegister(final Boolean successfulRegistration) {

        // Delegate to superclass
        super.postRegister(successfulRegistration);

        // Perform custom postRegistering?
        if (successfulRegistration) {

            try {
                customPostregister();

                // Indicate that we are started
                performStateTransition(true, LifecycleState.STARTING, LifecycleState.STARTED);

            } catch (Exception e) {

                log.error("Custom postRegistration failed in [" + getClass().getName() + "]", e);
                performStateTransition(false, LifecycleState.STARTING, LifecycleState.ERROR);
            }
        } else {

            // Indicate that registration failed.
            if (getState() != LifecycleState.ERROR) {
                performStateTransition(false, LifecycleState.STARTING, LifecycleState.ERROR);
            }
        }
    }

    /**
     * Delegates any custom execution to the {@code #customPreDeregister} method before calling the
     * {@code #preDeregister} method in the superclass.
     * {@inheritDoc}
     *
     * @see #customPreDeregister()
     */
    @Override
    public final void preDeregister() throws Exception {

        // Adjust state
        performStateTransition(true, LifecycleState.STARTED, LifecycleState.STOPPING);

        // Delegate to custom logic
        if (getState() != LifecycleState.ERROR) {
            try {
                customPreDeregister();
            } catch (Exception e) {

                log.error("Custom preDeRegistration failed in [" + getClass().getName() + "]", e);
                performStateTransition(false, LifecycleState.STARTED, LifecycleState.ERROR);
            }
        } else if (log.isDebugEnabled()) {
            log.debug("" + LifecycleState.ERROR + " state, so customPreDeregister not called.");
        }

        // Delegate to superclass
        try {
            super.preDeregister();
        } catch (Exception e) {

            // Change the state
            if (getState() != LifecycleState.ERROR) {
                performStateTransition(false, LifecycleState.STARTED, LifecycleState.ERROR);
            }

            // Re-throw
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void postDeregister() {

        // Delegate to the custom postDeregister method.
        try {
            customPostDeregister();
        } catch (Exception e) {
            log.error("customPostDeregister failed.", e);

            performStateTransition(false, LifecycleState.STOPPING, LifecycleState.ERROR);
        }

        try {

            super.postDeregister();

            // Adjust state, if possible
            final LifecycleState targetState = state == LifecycleState.ERROR
                    ? LifecycleState.ERROR
                    : LifecycleState.STOPPED;

            performStateTransition(false, LifecycleState.STOPPING, targetState);

        } catch (Exception e) {
            log.error("super.postDeregister() failed.", e);

            performStateTransition(false, LifecycleState.STOPPING, LifecycleState.ERROR);
        }
    }

    /**
     * Override this method to perform customization of the ObjectName under which this AbstractMBean should
     * be registered within the supplied MBeanServer. <strong>Note!</strong> This method is invoked from within the
     * {@code preRegister} method, implying that this method may not register this AbstractMBean in the MBean server.
     *
     * @param server     The MBeanServer instance.
     * @param objectName The object name of the MBean, which is {@code null} if the name parameter to one of the
     *                   <code>createMBean</code> or <code>registerMBean</code> methods in the {@link javax.management.MBeanServer}
     *                   interface is {@code null}. In that case, this method must return a non-null ObjectName for
     *                   the new MBean.
     * @return A non-null ObjectName under which this AbstractMBean will be registered in the MBeanServer.
     */
    protected ObjectName customPreregister(final MBeanServer server, final ObjectName objectName) {
        return objectName;
    }

    /**
     * Override this method to perform any customized actions after this AbstractMBean has been
     * successfully registered in the MBeanServer. This method will only be invoked if the registration
     * in the MBeanServer was successful (i.e. if the {@code registrationDone} parameter received by the
     * postRegister method was {@code true}. This method should not throw Exceptions.
     *
     * @see #postRegister(Boolean)
     */
    protected void customPostregister() {
        // By default, do nothing.
    }

    /**
     * Override this method to perform any customized actions before this AbstractMBean will be de-registered
     * in the MBeanServer. This method will be invoked immediately before the {@code super.preDeregister()} method
     * is called - but only if this AbstractMBean is not in {@code LifecycleState.ERROR} state.
     * (The customPostDeregister() method is always called, irrespective of state).
     */
    protected void customPreDeregister() {
        // By default, do nothing.
    }

    /**
     * Override this mthod to perform any customized actions before this AbstractMBean will be de-registered
     * in the MBeanServer. This method will be invoked immediately before the {@code super.preDeregister()} method
     * is called. This method should not throw Exceptions.
     */
    protected void customPostDeregister() {
        // By default, do nothing.
    }

    /**
     * Helper method which creates a JMX AttributeChangeNotification instance from the supplied
     * data, and notifies all registered NotificationListener instances by invoking {@code sendNotification()}
     * with the newly constructed AttributeChangeNotification as an argument.
     *
     * @param attributeName The name of the attribute which changed, such as "state". Cannot be null or empty.
     * @param eventMessage  A human-readable AttributeChangeNotification eventMessage,
     *                      such as "LifecycleState changed". Cannot be null or empty.
     * @param objectType    The attribute Class. Cannot be null.
     * @param oldValue      The value before the change.
     * @param newValue      The value after the change.
     * @param <T>           The attribute Class.
     */
    protected final <T> void sendAttributeChangeEvent(final String attributeName,
                                                      final String eventMessage,
                                                      final Class<T> objectType,
                                                      final T oldValue,
                                                      final T newValue) {

        // Check sanity
        Validate.notEmpty(attributeName, "attributeName");
        Validate.notEmpty(eventMessage, "eventMessage");
        Validate.notNull(objectType, "objectType");

        // Notify our listeners about the state change.
        final Notification notification = new AttributeChangeNotification(
                this,
                eventSequenceId++,
                System.currentTimeMillis(),
                eventMessage,
                attributeName,
                objectType.getName(),
                oldValue,
                newValue);

        if (log.isDebugEnabled()) {
            log.debug("Sending AttributeChangeEvent [" + eventSequenceId + ", " +
                    "newValue: " + newValue + "]: " + notification);
        }

        sendNotification(notification);
    }

    /**
     * Performs a state transition in this AbstractMBean which may be validated (in the sense that the
     * from state must match the result from {@code getState()} to permit the state transition).
     * If the stat transition was actually made, an AttributeChangeNotification is sent by this AbstractMBean to
     * permit any state listeners to react to the event.
     *
     * @param validateFromState if {@code true}, the result of {@code getState()} must be equal to from to permit the
     *                          state transition. Otherwise, the state transition will not be performed.
     * @param from              The expected state of this AbstractMBean.
     * @param to                The state to transition to.
     * @return {@code true} if the transition was successfully made, and {@code false} otherwise.
     */
    protected final boolean performStateTransition(final boolean validateFromState,
                                                   final LifecycleState from,
                                                   final LifecycleState to) {

        if (validateFromState && getState() != from) {
            if (log.isWarnEnabled()) {
                log.warn("Aborting illegal state transition [" + getState() + " --> " + to
                        + "]. (Expected " + from + ") in AbstractMBean [" + getClass().getName() + "]");
            }

            // State update not performed.
            return false;
        }

        synchronized (lock) {

            // Perform the state transition itself.
            state = to;

            // Notify our listeners about the state change.
            sendAttributeChangeEvent(STATE_CHANGE_ATTRIBUTENAME, "LifecycleState changed", LifecycleState.class, from, to);
        }

        // All done.
        return true;
    }

    /**
     * @return An AttributeChangeNotificationFilter which will listen only to state change events emitted by
     * an AbstractMBean subclass (e.g. AttributeChangeEvent with attributeName set to the value of constant
     * {@code STATE_CHANGE_ATTRIBUTENAME}).
     */
    public static AttributeChangeNotificationFilter getStateTransitionFilter() {

        final AttributeChangeNotificationFilter toReturn = new AttributeChangeNotificationFilter();
        toReturn.enableAttribute(AbstractMBean.STATE_CHANGE_ATTRIBUTENAME);

        // All done.
        return toReturn;
    }
}
