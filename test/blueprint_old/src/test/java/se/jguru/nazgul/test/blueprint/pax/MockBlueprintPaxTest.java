/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2015 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.test.blueprint.pax;

import org.junit.Assert;
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
