/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.blueprint;

import de.kalpatec.pojosr.framework.launch.ClasspathScanner;
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistry;
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistryFactory;
import org.apache.aries.blueprint.ComponentDefinitionRegistry;
import org.apache.aries.blueprint.Interceptor;
import org.apache.aries.blueprint.services.ParserService;
import org.apache.commons.lang.Validate;
import org.junit.Assert;
import org.junit.Before;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.ServiceMetadata;
import org.osgi.service.blueprint.reflect.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

/**
 * Abstract superclass for OSGi Blueprint-oriented testing, implying that
 * unit tests can be launched without the increased complexity of Pax.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractBlueprintTest {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(AbstractBlueprintTest.class);

    /**
     * Default path to the blueprint configuration.
     */
    public static final String DEFAULT_TEST_BLUEPRINT_CONFIGURATION =
            "/blueprint/test/context/config.xml";

    // Internal state
    private Map<String, Object> serviceRegistryConfiguration;
    private boolean scanClasspathForBundles;
    private String blueprintConfigurationPath;

    /**
     * The OSGi PojoServiceRegistry instance.
     */
    protected PojoServiceRegistry registry;

    /**
     * Creates a new AbstractBlueprintTest instance, applying the bundle
     * scan algorithm implied by the {@code scanClasspathForBundles} for
     * OSGi bundle discovery and the default blueprint [resource] configuration path,
     * as given by {@code DEFAULT_TEST_BLUEPRINT_CONFIGURATION}.
     *
     * @param scanClasspathForBundles if {@code true}, bundles from the
     *                                classpath are installed into the
     *                                PojoServiceRegistry instance.
     */
    protected AbstractBlueprintTest(boolean scanClasspathForBundles) {
        this(scanClasspathForBundles, DEFAULT_TEST_BLUEPRINT_CONFIGURATION);
    }

    /**
     * Creates a new AbstractBlueprintTest instance, applying the bundle
     * scan algorithm implied by the {@code scanClasspathForBundles} for
     * OSGi bundle discovery and the given blueprint [resource] configuration path.
     *
     * @param scanClasspathForBundles    if {@code true}, bundles from the
     *                                   classpath are installed into the
     *                                   PojoServiceRegistry instance.
     * @param blueprintConfigurationPath The path to the Blueprint configuration.
     *                                   Cannot be null or empty.
     */
    public AbstractBlueprintTest(final boolean scanClasspathForBundles,
                                 final String blueprintConfigurationPath) {

        // Check sanity
        Validate.notEmpty(blueprintConfigurationPath,
                "Cannot handle null or empty blueprintConfigurationPath argument.");

        // Assign internal state
        this.blueprintConfigurationPath = blueprintConfigurationPath;
        this.scanClasspathForBundles = scanClasspathForBundles;
    }

    @Before
    public final void setupSharedState() throws Exception {

        // Acquire a PojoServiceRegistry configuration
        serviceRegistryConfiguration = new TreeMap<String, Object>();
        if (scanClasspathForBundles) {

            // Load and start all bundles found on the classpath.
            serviceRegistryConfiguration.put(PojoServiceRegistryFactory.BUNDLE_DESCRIPTORS, new ClasspathScanner().scanForBundles());
        }

        // Create the PojoServiceRegistry instance.
        final ServiceLoader<PojoServiceRegistryFactory> loader = ServiceLoader.load(PojoServiceRegistryFactory.class);
        registry = loader.iterator().next().newPojoServiceRegistry(serviceRegistryConfiguration);

        // Kickstart the D.I. blueprint configuration.
        final ServiceReference parserServiceReference = registry.getServiceReference(ParserService.class.getName());
        final ParserService parserService = (ParserService) registry.getService(parserServiceReference);
        Assert.assertNotNull("Could not find the blueprint ParserService", parserService);

        final URL testBlueprintXml = getClass().getClassLoader().getResource(blueprintConfigurationPath);
        Assert.assertNotNull("Could not find the blueprint XML configuration at ["
                + blueprintConfigurationPath + "]", testBlueprintXml);

        final ComponentDefinitionRegistry result = parserService.parse(
                testBlueprintXml, registry.getBundleContext().getBundle());

        for (String current : result.getComponentDefinitionNames()) {
            final ComponentMetadata componentDefinition = result.getComponentDefinition(current);

            if (componentDefinition instanceof ServiceMetadata) {

                final ServiceMetadata serviceMetadata = (ServiceMetadata) componentDefinition;
                final Target serviceComponent = serviceMetadata.getServiceComponent();
                System.out.println("Got serviceComponent: " + serviceComponent);
                final List<Interceptor> interc = result.getInterceptors(serviceMetadata);
                System.out.println("Got getInterceptors: " + interc);

                // Register the service within the pojoSR registry.
                final List<String> interfaces = serviceMetadata.getInterfaces();
                final String[] registeredInterfaces = new String[interfaces.size()];
                interfaces.toArray(registeredInterfaces);

                // final ServiceRegistration serviceRegistration = registry.registerService(registeredInterfaces, );
                // System.out.println("Got: " + serviceMetadata);
            }
        }
    }

    /**
     * Assigns the given value to the system property with the supplied key,
     * unless a value is already set and the {@code overwriteExistingValue}
     * variable is set to {@code false}.
     *
     * @param key                    The system property key.
     * @param value                  The value to assign.
     * @param overwriteExistingValue if {@code false}, the given system property will not
     *                               be overwritten if it is set.
     */
    protected void setSystemProperty(final String key, final String value, boolean overwriteExistingValue) {

        final String currentValue = System.getProperty(key);
        if (!overwriteExistingValue && currentValue != null) {

            log.warn("As requested, not overwriting system property [" + key + "] with present value [" +
                    currentValue + "]");
        } else {

            // Overwrite the given property with the supplied value.
            System.setProperty(key, value);
        }
    }
}
