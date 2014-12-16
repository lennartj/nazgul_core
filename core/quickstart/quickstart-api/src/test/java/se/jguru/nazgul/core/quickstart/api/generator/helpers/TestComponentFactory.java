/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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
package se.jguru.nazgul.core.quickstart.api.generator.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.api.DefaultStructureNavigator;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestPomAnalyzer;
import se.jguru.nazgul.core.quickstart.api.generator.AbstractComponentFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TestComponentFactory extends AbstractComponentFactory {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(TestComponentFactory.class.getName());

    // Shared state
    public String testdataSubDir = "test";
    public List<String> callTrace;

    public TestComponentFactory(final NamingStrategy namingStrategy) {
        super(namingStrategy, new DefaultStructureNavigator(namingStrategy, new TestPomAnalyzer(namingStrategy)));

        callTrace = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected URL getTemplateResourceURL(final String templateResourcePath) {
        final String enrichedPath = "testdata/templates/" + testdataSubDir + "/" + templateResourcePath;

        if(log.isDebugEnabled()) {
            log.debug("Got enrichedPath: " + enrichedPath);
        }

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return contextClassLoader.getResource(enrichedPath);
    }
}
