package se.jguru.nazgul.test.blueprint;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import se.jguru.nazgul.core.test.bundles.hello.api.Hello;

import javax.inject.Inject;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class MockBlueprintPaxTest {

    // Shared state
    @Inject
    private BundleContext context;

    @Configuration
    public Option[] config() {

        return CoreOptions.options(

                // Add the relevant maven bundles.
                CoreOptions.mavenBundle(
                        "se.jguru.nazgul.test.bundles.hello.impl.blueprint",
                        "nazgul-test-hello-impl-blueprint",
                        "1.0.1-SNAPSHOT"),

                CoreOptions.mavenBundle(
                        "se.jguru.nazgul.test.bundles.hello.api",
                        "nazgul-test-hello-api",
                        "1.0.1-SNAPSHOT"),

                CoreOptions.mavenBundle(
                        "se.jguru.nazgul.test.blueprint",
                        "nazgul-core-blueprint-test",
                        "1.4.1-SNAPSHOT"),

                // Add Aries blueprint Bundles
                CoreOptions.mavenBundle(
                        "org.apache.aries.blueprint",
                        "org.apache.aries.blueprint",
                        "1.1.0"),
                CoreOptions.mavenBundle(
                        "org.apache.aries",
                        "org.apache.aries.util",
                        "1.1.0"),
                CoreOptions.mavenBundle(
                        "org.apache.aries.proxy",
                        "org.apache.aries.proxy",
                        "1.0.1"),

                // Add
                // CoreOptions.bundle("http://www.example.com/repository/foo-1.2.3.jar"),

                // Add JUnit bundles.
                CoreOptions.junitBundles()
                                  );
    }

    @Test
    public void validateBlueprint() throws Exception {

        // Assemble
        Assert.assertNotNull("Injected BundleContext is null", context);
        Thread.sleep(1000);

        // Act
        final Bundle[] bundles = context.getBundles();
        Bundle helloImplBlueprintBundle;
        ServiceReference<?>[] registeredHelloImplServices = null;

        for (int i = 0; i < bundles.length; i++) {
            final Bundle current = bundles[i];
            if(current.getSymbolicName().contains("nazgul-test-hello-impl-blueprint")) {

                helloImplBlueprintBundle = current;
                registeredHelloImplServices = current.getRegisteredServices();
            }

            // Simply printout some debug data.
            System.out.println(" Bundle [" + i + "]: " + current.getSymbolicName() + " (" + current.getVersion()
                    + "): " + BundleState.convert(current.getState()));
        }

        if(registeredHelloImplServices == null) {
            System.out.println("===> Found no Hello Services");
        } else {
            System.out.println("Got serviceRefs: "+ registeredHelloImplServices.length);
            for(ServiceReference current : registeredHelloImplServices) {
                Hello service = (Hello) context.getService(current);
                System.out.println(" ==> " + service.sayHello());
            }
        }

        // Assert
    }


    public enum BundleState {

        /**
         * The bundle is uninstalled and may not be used.
         *
         * @see Bundle#UNINSTALLED
         */
        UNINSTALLED(Bundle.UNINSTALLED),

        /**
         * The bundle is installed but not yet resolved.
         *
         * @see Bundle#INSTALLED
         */
        INSTALLED(Bundle.INSTALLED),

        /**
         * The bundle is resolved and is able to be started.
         *
         * @see Bundle#RESOLVED
         */
        RESOLVED(Bundle.RESOLVED),

        /**
         * The bundle is now running.
         *
         * @see Bundle#ACTIVE
         */
        ACTIVE(Bundle.ACTIVE);

        // Internal state
        private int bundleState;

        private BundleState(final int bundleState) {
            this.bundleState = bundleState;
        }

        /**
         * @return The OSGi Bundle constant defining the BundleState.
         */
        public int getBundleState() {
            return bundleState;
        }


        /**
         * Parses the supplied value retrieved from an OSGi {@code Bundle.getBundleState()} method
         * call to a BundleState instance.
         *
         * @param bundleState The OSGi {@code Bundle.getBundleState()} result.
         * @return The corresponding BundleState instance.
         */
        public static BundleState convert(final int bundleState) {

            for (BundleState current : values()) {
                if (current.bundleState == bundleState) {
                    return current;
                }
            }

            throw new IllegalArgumentException("No BundleState found for value [" + bundleState + "]");
        }

        /**
         * {@inheritDoc}
         *
         * @return The lower case name, i.e. {@code name().toLowerCase()}
         */
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
