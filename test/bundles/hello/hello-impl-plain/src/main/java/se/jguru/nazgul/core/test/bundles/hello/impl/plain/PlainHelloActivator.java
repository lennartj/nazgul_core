/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.test.bundles.hello.impl.plain;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.test.bundles.hello.api.Hello;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Standard OSGi BundleActivator implementation for the HelloImpl bundle.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PlainHelloActivator implements BundleActivator {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(PlainHelloActivator.class);

    // Internal state
    private List<ServiceRegistration<Hello>> helloServices = new ArrayList<ServiceRegistration<Hello>>();

    /**
     * Called when this bundle is started so the Framework can perform the
     * bundle-specific activities necessary to start this bundle. This method
     * can be used to register services or to allocate any resources that this
     * bundle needs.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being started.
     * @throws Exception If this method throws an exception, this
     *                   bundle is marked as stopped and the Framework will remove this
     *                   bundle's listeners, unregister all services registered by this
     *                   bundle, and release all services used by this bundle.
     */
    @Override
    public void start(final BundleContext context) throws Exception {

        // Add references to the two registered services.
        helloServices.add(registerService(context, "Lennart"));
        helloServices.add(registerService(context, "Malin"));
    }

    private ServiceRegistration<Hello> registerService(final BundleContext context, final String name) {

        // Define serviceRegistration properties
        Hashtable<String, String> serviceProperties = new Hashtable<String, String>();
        serviceProperties.put("name", name);

        // Register the service
        final ServiceRegistration<Hello> serviceRegistration = context.registerService(
                Hello.class, new PlainHello(name), serviceProperties);

        // Log somewhat and return
        log.debug("Bound '" + name + "' HelloService");
        return serviceRegistration;
    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle. In general, this
     * method should undo the work that the {@code BundleActivator.start}
     * method started. There should be no active threads that were started by
     * this bundle when this bundle returns. A stopped bundle must not call any
     * Framework objects.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the
     *                   bundle is still marked as stopped, and the Framework will remove
     *                   the bundle's listeners, unregister all services registered by the
     *                   bundle, and release all services used by the bundle.
     */
    @Override
    public void stop(BundleContext context) throws Exception {

        // Unbind all known services.
        for(ServiceRegistration<Hello> current : helloServices) {

            try {
                // Unregister the provided service
                current.unregister();
            } catch (Exception e) {
                log.error("Could not unregister HelloService", e);
            }
        }
    }
}
