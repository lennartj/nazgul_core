/*
 * #%L
 * Nazgul Project: nazgul-core-jmx-test
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
package se.jguru.nazgul.core.jmx.test.mbeanserver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MBeanServerUnregisterException extends RuntimeException {

    // Internal state
    private String attemptedOperation;
    private List<Exception> exceptions = new ArrayList<>();

    /**
     * Creates an MBeanServerUnregisterException originating from the supplied attempted operation.
     *
     * @param attemptedOperation The operation attempted by the MBeanServer.
     */
    public MBeanServerUnregisterException(final String attemptedOperation) {
        this.attemptedOperation = attemptedOperation;
    }

    /**
     * Retrieves the List of Exceptions which originated from the MBeanServer.
     *
     * @return the List of Exceptions which originated from the MBeanServer.
     */
    public List<Exception> getExceptions() {
        return exceptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Found [" + exceptions.size() + "] exceptions while performing MBeanServer [" + attemptedOperation + "]";
    }
}
