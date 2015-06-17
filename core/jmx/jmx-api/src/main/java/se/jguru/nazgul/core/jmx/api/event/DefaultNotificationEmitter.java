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
package se.jguru.nazgul.core.jmx.api.event;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationListener;
import java.io.Serializable;
import java.util.concurrent.Executor;

/**
 * Abstract NotificationEmitter implementation, sporting functionality for adding and removing listeners.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class DefaultNotificationEmitter extends NotificationBroadcasterSupport implements Serializable {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(DefaultNotificationEmitter.class);

    // Internal state
    private String id;

    /**
     * <p>Constructs a NotificationBroadcasterSupport with information about the notifications that may be sent. Each
     * listener is invoked by the thread sending the notification. This constructor is equivalent to
     * {@link javax.management.NotificationBroadcasterSupport#NotificationBroadcasterSupport(java.util.concurrent.Executor, javax.management.MBeanNotificationInfo[] info)}.</p>
     * <p>If the <code>info</code> array is not empty, then it is
     * cloned by the constructor as if by {@code info.clone()}, and
     * each call to {@link #getNotificationInfo()} returns a new clone.</p>
     *
     * @param id  a (unique) id of this DefaultNotificationEmitter.
     * @param info an array indicating, for each notification this MBean may send, the name of the Java class of the
     *             notification and the notification type. Can be null, which is equivalent to an empty array.
     */
    public DefaultNotificationEmitter(final String id,
                                      final MBeanNotificationInfo... info) {
        super(info);

        // Check sanity
        Validate.notEmpty(id, "Cannot handle null or empty 'id' argument.");

        // Assign internal state
        this.id = id;
    }

    /**
     * <p>Constructs a NotificationBroadcasterSupport with information about the notifications that may be sent,
     * and where each listener is invoked using the given {@link java.util.concurrent.Executor}.</p>
     * <p>When {@link #sendNotification sendNotification} is called, a
     * listener is selected if it was added with a null {@link javax.management.NotificationFilter}, or if
     * {@link javax.management.NotificationFilter#isNotificationEnabled(Notification)} returns true for the
     * notification being sent. The call to <code>NotificationFilter.isNotificationEnabled</code> takes place in the
     * thread that called <code>sendNotification</code>. Then, for each selected listener,
     * {@link java.util.concurrent.Executor#execute executor.execute} is called with a command that calls the
     * <code>handleNotification</code> method.</p>
     * <p>If the <code>info</code> array is not empty, then it is cloned by the constructor as if by
     * {@code info.clone()}, and each call to {@link #getNotificationInfo()} returns a new clone.</p>
     *
     * @param id  a (unique) id of this DefaultNotificationEmitter.
     * @param executor an executor used by the method <code>sendNotification</code> to send each notification. If it
     *                 is null, the thread calling <code>sendNotification</code> will invoke the
     *                 <code>handleNotification</code> method itself.
     * @param info     an array indicating, for each notification this MBean may send, the name of the Java class of
     *                 the notification and the notification type.  Can be null, which is equivalent to an empty array.
     */
    public DefaultNotificationEmitter(final String id,
                                      final Executor executor,
                                      final MBeanNotificationInfo... info) {

        super(executor, info);

        // Check sanity
        Validate.notEmpty(id, "Cannot handle null or empty 'id' argument.");

        // Assign internal state
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleNotification(final NotificationListener listener,
                                   final Notification notification,
                                   final Object handback) {

        // Log somewhat
        if (log.isDebugEnabled() && listener != null) {
            log.debug("Notifying listener of type [" + listener.getClass().getName() + "]");
        }

        // Delegate.
        super.handleNotification(listener, notification, handback);
    }

    /**
     * @return a debug String representation of the AbstractNotificationEmitter, printing its ID and its
     * corresponding list of MBeanNotificationInfo string-information.
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        final MBeanNotificationInfo[] notificationInfo = getNotificationInfo();
        final int size = notificationInfo == null ? 0 : notificationInfo.length;
        for (int i = 0; i < size; i++) {
            builder.append("  [").append(i).append("]: ").append(notificationInfo[i].toString()).append("\n");
        }

        // All done.
        return "[" + getClass().getName() + " :: " + id + "] with " + size + " "
                + "registered MBeanNotificationInfo elements ... " + builder
                + "\n.... End [" + getClass().getName() + " :: " + id + "]";
    }

    /**
     * Factory method to generate a StateChange DefaultNotificationEmitter instance with the supplied id.
     *
     * @param id The id of the returned DefaultNotificationEmitter. Cannot be null or empty.
     * @return a StateChange DefaultNotificationEmitter instance which emits events for
     * {@code AttributeChangeNotification.ATTRIBUTE_CHANGE} with the attribute name {@code state}.
     */
    public static DefaultNotificationEmitter getDefaultStateChangeNotificationEmitter(final String id) {

        // Check sanity
        Validate.notEmpty(id, "Cannot handle null or empty id argument.");

        // All done.
        return new DefaultNotificationEmitter(id,
                new MBeanNotificationInfo(
                        new String[]{AttributeChangeNotification.ATTRIBUTE_CHANGE},
                        "state",
                        "LifecycleState changed")
        );
    }
}
