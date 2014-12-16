/*
 * #%L
 * Nazgul Project: nazgul-core-osgi-test
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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

package se.jguru.nazgul.test.osgi;

import org.apache.commons.lang3.Validate;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

/**
 * Mock implementation of an OSGi ServiceReference, usable in unit tests.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MockServiceReference implements ServiceReference {

    // Internal state
    private final Bundle bundle;
    private final List<String> classes;
    private Dictionary<String, Object> registrationProperties;

    /**
     * Creates a new MockServiceReference for use in test cases.
     *
     * @param bundle    The Bundle referred by this ServiceReference.
     * @param className The class name of the interface for the published bundle.
     */
    public MockServiceReference(final Bundle bundle, final String className) {
        this(bundle,
                Arrays.asList(className),
                new Properties(),
                bundle.getSymbolicName(),
                0);
    }

    /**
     * Creates a new MockServiceReference for use in test cases.
     *
     * @param bundle                 The Bundle referred by this ServiceReference.
     * @param classNames             The class names under which the bundle should publish its interfaces.
     * @param registrationProperties The OSGi registration properties for the ServiceReference.
     * @param serviceID              The service ID of this ServiceReference.
     * @param serviceRanking         The service ranking of this ServiceReference.
     */
	public MockServiceReference(final Bundle bundle,
                                final List<String> classNames,
                                final Dictionary registrationProperties,
                                final String serviceID,
                                final int serviceRanking) {

        // Assign internal state
        this.bundle = bundle;
        this.classes = classNames;
        this.registrationProperties = registrationProperties;

        // Set the serviceID
        setServiceID(serviceID);
        setServiceRanking(serviceRanking);

        // Assign the classnames
        String[] clazzNames = new String[classNames.size()];
        classNames.toArray(clazzNames);
        registrationProperties.put(Constants.OBJECTCLASS, clazzNames);
    }

    /**
     * @return The active registration properties.
     */
    public Dictionary getRegistrationProperties() {
        return registrationProperties;
    }

    /**
     * Assigns the serviceID of this MockServiceReference.
     *
     * @param serviceID the serviceID to assign.
     */
    public final void setServiceID(final String serviceID) {

        // Check sanity
        Validate.notEmpty(serviceID, "Cannot handle null or empty serviceID property.");

        // Assign internal state.
        registrationProperties.put(Constants.SERVICE_ID, serviceID);
    }

    /**
     * Assigns the serviceRanking of this MockServiceReference.
     *
     * @param serviceRanking THe service ranking to assign.
     */
    public final void setServiceRanking(final int serviceRanking) {
        registrationProperties.put(Constants.SERVICE_RANKING, serviceRanking);
    }

    /**
     * Returns the property value to which the specified property key is mapped
     * in the properties {@code Dictionary} object of the service
     * referenced by this {@code ServiceReference} object.
     * <p/>
     * <p/>
     * Property keys are case-insensitive.
     * <p/>
     * <p/>
     * This method must continue to return property values after the service has
     * been unregistered. This is so references to unregistered services (for
     * example, {@code ServiceReference} objects stored in the log) can
     * still be interrogated.
     *
     * @param key The property key.
     * @return The property value to which the key is mapped; {@code null}
     *         if there is no property named after the key.
     */
    @Override
    public Object getProperty(final String key) {
        return registrationProperties.get(key);
    }

    /**
     * Returns an array of the keys in the properties {@code Dictionary}
     * object of the service referenced by this {@code ServiceReference}
     * object.
     * <p/>
     * This method will continue to return the keys after the service has been
     * unregistered. This is so references to unregistered services (for
     * example, {@code ServiceReference} objects stored in the log) can
     * still be interrogated.
     * <p/>
     * This method is <i>case-preserving </i>; this means that every key in the
     * returned array must have the same case as the corresponding key in the
     * properties {@code Dictionary} that was passed to the
     * {@link org.osgi.framework.BundleContext#registerService(String[], Object, java.util.Dictionary)} or
     * {@link org.osgi.framework.ServiceRegistration#setProperties} methods.
     *
     * @return An array of property keys.
     */
    @Override
    public String[] getPropertyKeys() {
        final String[] toReturn = new String[registrationProperties.size()];
        Collections.list(registrationProperties.keys()).toArray(toReturn);
        return toReturn;
    }

    /**
     * Returns the bundle that registered the service referenced by this
     * {@code ServiceReference} object.
     * <p/>
     * <p/>
     * This method must return {@code null} when the service has been
     * unregistered. This can be used to determine if the service has been
     * unregistered.
     *
     * @return The bundle that registered the service referenced by this
     *         {@code ServiceReference} object; {@code null} if that
     *         service has already been unregistered.
     * @see org.osgi.framework.BundleContext#registerService(String[], Object, java.util.Dictionary)
     */
    @Override
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Returns the bundles that are using the service referenced by this
     * {@code ServiceReference} object. Specifically, this method returns
     * the bundles whose usage count for that service is greater than zero.
     *
     * @return An array of bundles whose usage count for the service referenced
     *         by this {@code ServiceReference} object is greater than
     *         zero; {@code null} if no bundles are currently using that
     *         service.
     * @since 1.1
     */
    @Override
    public Bundle[] getUsingBundles() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Tests if the bundle that registered the service referenced by this
     * {@code ServiceReference} and the specified bundle use the same
     * source for the package of the specified class name.
     * <p/>
     * This method performs the following checks:
     * <ol>
     * <li>Get the package name from the specified class name.</li>
     * <li>For the bundle that registered the service referenced by this
     * {@code ServiceReference} (registrant bundle); find the source for
     * the package. If no source is found then return {@code true} if the
     * registrant bundle is equal to the specified bundle; otherwise return
     * {@code false}.</li>
     * <li>If the package source of the registrant bundle is equal to the
     * package source of the specified bundle then return {@code true};
     * otherwise return {@code false}.</li>
     * </ol>
     *
     * @param bundle    The {@code Bundle} object to check.
     * @param className The class name to check.
     * @return {@code true} if the bundle which registered the service
     *         referenced by this {@code ServiceReference} and the
     *         specified bundle use the same source for the package of the
     *         specified class name. Otherwise {@code false} is returned.
     * @throws IllegalArgumentException If the specified {@code Bundle} was
     *                                  not created by the same framework instance as this
     *                                  {@code ServiceReference}.
     * @since 1.3
     */
    @Override
    public boolean isAssignableTo(final Bundle bundle, final String className) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Compares this {@code ServiceReference} with the specified
     * {@code ServiceReference} for order.
     * <p/>
     * <p/>
     * If this {@code ServiceReference} and the specified
     * {@code ServiceReference} have the same {@link org.osgi.framework.Constants#SERVICE_ID
     * service id} they are equal. This {@code ServiceReference} is less
     * than the specified {@code ServiceReference} if it has a lower
     * {@link org.osgi.framework.Constants#SERVICE_RANKING service ranking} and greater if it has a
     * higher service ranking. Otherwise, if this {@code ServiceReference}
     * and the specified {@code ServiceReference} have the same
     * {@link org.osgi.framework.Constants#SERVICE_RANKING service ranking}, this
     * {@code ServiceReference} is less than the specified
     * {@code ServiceReference} if it has a higher
     * {@link org.osgi.framework.Constants#SERVICE_ID service id} and greater if it has a lower
     * service id.
     *
     * @param reference The {@code ServiceReference} to be compared.
     * @return Returns a negative integer, zero, or a positive integer if this
     *         {@code ServiceReference} is less than, equal to, or greater
     *         than the specified {@code ServiceReference}.
     * @throws IllegalArgumentException If the specified
     *                                  {@code ServiceReference} was not created by the same
     *                                  framework instance as this {@code ServiceReference}.
     * @since 1.4
     */
    @Override
    public int compareTo(final Object reference) {

        if (!(reference instanceof ServiceReference)) {
            throw new ClassCastException("Could not compare ServiceReference to ["
                    + reference.getClass().getName() + "]");
        }

        // Compare the service ranking of both ServiceReferences.
        final int serviceRanking = (Integer) getProperty(Constants.SERVICE_RANKING);
        final int thatServiceRanking = (Integer) ((ServiceReference) reference).getProperty(Constants.SERVICE_RANKING);
        if (serviceRanking != thatServiceRanking) {
            return serviceRanking - thatServiceRanking;
        }

        // Compare the service IDs of both ServiceReferences.
        final String serviceID = (String) getProperty(Constants.SERVICE_ID);
        final String thatServiceID = (String) ((ServiceReference) reference).getProperty(Constants.SERVICE_ID);
        return serviceID == null ? 1 : serviceID.compareTo(thatServiceID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object that) {
        return compareTo(that) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(classes.toArray());
    }
}
