package se.jguru.nazgul.test.blueprint.pax;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;

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
                        "1.0.0"),

                CoreOptions.mavenBundle(
                        "se.jguru.nazgul.test.bundles.hello.api",
                        "nazgul-test-hello-api",
                        "1.0.0"),

                // Add
                // CoreOptions.bundle("http://www.example.com/repository/foo-1.2.3.jar"),

                // Add JUnit bundles.
                CoreOptions.junitBundles()
        );
    }

    @Test
    public void getHelloService() {

        // Assemble
        Assert.assertNotNull("Injected BundleContext is null", context);

        /*
        assertNotNull(helloService);
        assertEquals("Hello Pax!", helloService.getMessage());
        */
    }
}
