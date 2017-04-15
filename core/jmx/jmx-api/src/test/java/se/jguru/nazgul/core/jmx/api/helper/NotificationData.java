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
package se.jguru.nazgul.core.jmx.api.helper;

import javax.management.Notification;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class NotificationData {

    public int index;
    public Notification notification;
    public Object handback;

    public NotificationData(final int index, final Notification notification, final Object handback) {
        this.index = index;
        this.notification = notification;
        this.handback = handback;
    }

    /**
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "NotificationData [" + index + "] ===>\nNotification: " + notification + "\nHandback: " + handback;
    }
}
