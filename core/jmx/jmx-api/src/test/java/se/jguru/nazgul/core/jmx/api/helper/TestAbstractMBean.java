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

import se.jguru.nazgul.core.jmx.api.AbstractMBean;

import javax.management.MBeanServer;
import javax.management.NotificationEmitter;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class TestAbstractMBean extends AbstractMBean {

    // Shared state
    public boolean throwPreRegisterException = false;
    public boolean throwPostRegisterException = false;
    public boolean throwPreDeregisterException = false;
    public boolean throwPostDeregisterException = false;

    public boolean preRegisterInvoked = false;
    public boolean postRegisterInvoked = false;
    public boolean preDeregisterInvoked = false;
    public boolean postDeregisterInvoked = false;

    /**
     * {@inheritDoc}
     */
    public TestAbstractMBean(final Class<?> mbeanInterface, final NotificationEmitter delegate) {
        super(mbeanInterface, delegate);
    }

    /**
     * {@inheritDoc}
     */
    public TestAbstractMBean(final Class<?> mbeanInterface, final boolean isMXBean, final NotificationEmitter delegate) {
        super(mbeanInterface, isMXBean, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ObjectName customPreregister(final MBeanServer server, final ObjectName objectName) {
        preRegisterInvoked = true;
        throwExceptionIf(throwPreRegisterException, "customPreregister");
        return super.customPreregister(server, objectName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void customPostregister() {
        postRegisterInvoked = true;
        throwExceptionIf(throwPostRegisterException, "customPostregister");
        super.customPostregister();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void customPreDeregister() {
        preDeregisterInvoked = true;
        throwExceptionIf(throwPreDeregisterException, "customPreDeregister");
        super.customPreDeregister();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void customPostDeregister() {
        postDeregisterInvoked = true;
        throwExceptionIf(throwPostDeregisterException, "customPostDeregister");
        super.customPostDeregister();
    }

    //
    // Private helpers
    //

    private void throwExceptionIf(final boolean throwException, final String originMethodName) {
        if (throwException) {
            throw new IllegalArgumentException("Exception thrown in: " + originMethodName);
        }
    }
}
