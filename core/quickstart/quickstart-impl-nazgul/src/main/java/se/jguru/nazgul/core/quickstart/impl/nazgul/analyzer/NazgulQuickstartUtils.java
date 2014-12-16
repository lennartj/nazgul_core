/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-impl-nazgul
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
package se.jguru.nazgul.core.quickstart.impl.nazgul.analyzer;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;

import java.net.URL;

/**
 * Specification for how to access Nazgul template resources.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings("all")
public final class NazgulQuickstartUtils {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(NazgulQuickstartUtils.class.getName());

    // Singleton state
    private static final NamingStrategy nazgulNamingStrategy = new NazgulNamingStrategy();

    /**
     * The path where Nazgul-flavoured POM templates are found.
     */
    public static final String TEMPLATE_PATH_PREFIX = "nazgul/templates/";

    /**
     * Method retrieving a URL to a template for the supplied templateResourcePath.
     * Typically, this method should be implemented in a concrete Factory to emit valid JAR URLs for each resource type
     * which should be synthesized by AbstractFactory subclasses. Only URLs with either 'jar' or 'file' protocols may
     * be returned. (I.e. the template resource must either be packaged within a JAR or found within the local file
     * system).
     *
     * @param templateResourcePath The path of the resource template for which a URL should be retrieved.
     * @return a URL to a template for the supplied resourceType. May only return URLs with either
     * 'jar' or 'file' protocols.
     */
    public static URL getTemplateResourceURL(final String templateResourcePath) {

        // Check sanity
        Validate.notEmpty(templateResourcePath, "Cannot handle null or empty templateResourcePath argument.");
        final String enrichedPath = TEMPLATE_PATH_PREFIX + templateResourcePath;

        if (log.isDebugEnabled()) {
            log.debug("Acquiring template resource for [" + enrichedPath + "]");
        }

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return contextClassLoader.getResource(enrichedPath);
    }

    /**
     * Retrieves the singleton NamingStrategy for the Nazgul quickstart.
     *
     * @return the singleton NamingStrategy for the Nazgul quickstart.
     */
    public static NamingStrategy getNazgulNamingStrategy() {
        return nazgulNamingStrategy;
    }
}
