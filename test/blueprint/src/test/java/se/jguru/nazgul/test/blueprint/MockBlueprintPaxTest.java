package se.jguru.nazgul.test.blueprint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.test.bundles.hello.api.Hello;
import se.jguru.nazgul.core.test.bundles.hello.api.calltrace.CallTrace;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class MockBlueprintPaxTest {

    // Shared state
    @Inject
    private BundleContext context;

    private Map<Integer, String> bundleState2NameMap;

    @Before
    public void setupSharedState() {

        bundleState2NameMap = new TreeMap<Integer, String>();
        bundleState2NameMap.put(Bundle.UNINSTALLED, "UNINSTALLED");
        bundleState2NameMap.put(Bundle.INSTALLED, "INSTALLED");
        bundleState2NameMap.put(Bundle.RESOLVED, "RESOLVED");
        bundleState2NameMap.put(Bundle.STARTING, "STARTING");
        bundleState2NameMap.put(Bundle.STOPPING, "STOPPING");
        bundleState2NameMap.put(Bundle.ACTIVE, "ACTIVE");
    }

    @Configuration
    public Option[] config() {

        return CoreOptions.options(

                // Add the OSGi Blueprint test bundles to the container.
                CoreOptions.mavenBundle("se.jguru.nazgul.test.bundles.hello.impl.blueprint",
                        "nazgul-test-hello-impl-blueprint").versionAsInProject(),
                CoreOptions.mavenBundle("se.jguru.nazgul.test.bundles.hello.api",
                        "nazgul-test-hello-api").versionAsInProject(),
                CoreOptions.mavenBundle("se.jguru.nazgul.test.bundles.hello.client.blueprint",
                        "nazgul-test-hello-client-blueprint").versionAsInProject(),

                // Add an in-project dependency to the container.
                CoreOptions.mavenBundle("se.jguru.nazgul.core.reflection.api",
                        "nazgul-core-reflection-api").versionAsInProject(),
                CoreOptions.mavenBundle("org.apache.commons",
                        "commons-lang3").versionAsInProject(),
                CoreOptions.mavenBundle("se.jguru.nazgul.core.algorithms.api",
                        "nazgul-core-algorithms-api").versionAsInProject(),

                // Add Aries blueprint Bundles
                CoreOptions.mavenBundle("org.apache.aries.blueprint",
                        "org.apache.aries.blueprint").versionAsInProject(),
                CoreOptions.mavenBundle("org.apache.aries",
                        "org.apache.aries.util").versionAsInProject(),
                CoreOptions.mavenBundle("org.apache.aries.proxy",
                        "org.apache.aries.proxy").versionAsInProject(),

                // Add logback as a logging backend to SLF4J
                CoreOptions.mavenBundle("ch.qos.logback", "logback-core").versionAsInProject(),
                CoreOptions.mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject(),

                // Add JUnit bundles.
                CoreOptions.junitBundles()
                                  );
    }

    @Test
    public void validateBlueprint() throws Exception {

        // Assemble
        final long SERVICE_LOOKUP_WAIT = 2000l;
        final long CALLTRACE_WAIT = 15000l;
        Assert.assertNotNull("Injected BundleContext is null", context);

        // Act
        final Bundle[] bundles = context.getBundles();

        for (int i = 0; i < bundles.length; i++) {
            final Bundle current = bundles[i];

            // Simply printout some debug data.
            System.out.println(" Bundle [" + i + "]: " + current.getSymbolicName() + " (" + current.getVersion()
                    + "): " + bundleState2NameMap.get(current.getState()));
        }

        final CallTrace callTraceService = ServiceLookup.getService(context, CallTrace.class, SERVICE_LOOKUP_WAIT);
        System.out.println("\n\n   ==> Waiting for " + (CALLTRACE_WAIT / 1000) + " seconds for completion <==\n\n");
        Thread.sleep(CALLTRACE_WAIT);

        final Hello service = ServiceLookup.getService(context, Hello.class, SERVICE_LOOKUP_WAIT);
        final String result = service.sayHello();
        final List<String> callTrace = callTraceService.getCallTrace();

        // Assert
        Assert.assertNotNull("Got null service result", result);
        Assert.assertNotNull("Got null callTrace result", callTrace);

        /*
        Typical call trace:

        [Call 1/5]: No Hello services injected.
        Added helloservice [Service ID:10, objectClasses: [se.jguru.nazgul.core.test.bundles.hello.api.Hello]]
        Added helloservice [Service ID:9, objectClasses: [se.jguru.nazgul.core.test.bundles.hello.api.Hello]]
        Added helloservice [Service ID:11, objectClasses: [se.jguru.nazgul.core.test.bundles.hello.api.Hello]]
        [Call 2/5, Service 1/3]: Yo, Malin
        [Call 2/5, Service 2/3]: Nice to see you!
        [Call 2/5, Service 3/3]: Yo, Lennart
        [Call 3/5, Service 1/3]: Yo, Malin
        [Call 3/5, Service 2/3]: Nice to see you!
        [Call 3/5, Service 3/3]: Yo, Lennart
        [Call 4/5, Service 1/3]: Yo, Malin
        [Call 4/5, Service 2/3]: Nice to see you!
        [Call 4/5, Service 3/3]: Yo, Lennart
        [Call 5/5, Service 1/3]: Yo, Malin
        [Call 5/5, Service 2/3]: Nice to see you!
        [Call 5/5, Service 3/3]: Yo, Lennart
         */
        final List<String> serviceAddedLines = CollectionAlgorithms.filter(callTrace, new Filter<String>() {
            @Override
            public boolean accept(final String candidate) {

                // Typical line:
                //
                // Added helloservice [Service ID:14,
                // objectClasses: [se.jguru.nazgul.core.test.bundles.hello.api.Hello]]
                return candidate.startsWith("Added helloservice [Service ID:")
                        && candidate.endsWith("objectClasses: [se.jguru.nazgul.core.test.bundles.hello.api.Hello]]");
            }
        });

        Assert.assertEquals(3, serviceAddedLines.size());
    }
}
