package se.jguru.nazgul.core.cache.example;

import de.kalpatec.pojosr.framework.launch.ClasspathScanner;
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistry;
import de.kalpatec.pojosr.framework.launch.PojoServiceRegistryFactory;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AbstractBlueprintTest {

    // Internal state
    protected PojoServiceRegistry registry;

    @Before
    public void setupSharedState() throws Exception {

        // Create a PojoServiceRegistry
        Map config = new HashMap();
        config.put(PojoServiceRegistryFactory.BUNDLE_DESCRIPTORS, new ClasspathScanner().scanForBundles());
        ServiceLoader<PojoServiceRegistryFactory> loader = ServiceLoader.load(PojoServiceRegistryFactory.class);
        registry = loader.iterator().next().newPojoServiceRegistry(config);
    }

    @Test
    public void validateFoo() throws Exception {

        final BundleContext ctx = registry.getBundleContext();
        for(Bundle current : ctx.getBundles()) {
            String key = current.getSymbolicName() == null ? current.getLocation() : current.getSymbolicName();
            System.out.println("Found bundle [" + key + "] ==> " + current.getBundleId());
        }

        /*
        for(ServiceReference current : registry.getServiceReferences(null, null)) {
            final Bundle bundle = current.getBundle();
            System.out.println("Got reference from [" + bundle.getSymbolicName() + "] ==> " + bundle.getLocation());
        }
        */
    }
}
