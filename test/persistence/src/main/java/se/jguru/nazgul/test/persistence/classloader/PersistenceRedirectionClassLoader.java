/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-test
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.jguru.nazgul.test.persistence.classloader;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Helper utility to manage the pesky-ness related to persistence.xml
 * files which act as a singleton in the JPA specification.
 * <p/>
 * In particular, this ClassLoader provides the option to use another
 * file to define the persistence units instead of <code>META-INF/persistence.xml</code>.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class PersistenceRedirectionClassLoader extends ClassLoader {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(PersistenceRedirectionClassLoader.class);

    // Constants

    /**
     * The standard URL of the persistence.xml.
     */
    public static final String PERSISTENCE_XML = "META-INF/persistence.xml";

    // Internal state
    private ClassLoader parent;
    private String persistenceXmlRedirection;

    /**
     * Creates a new PersistenceRedirectionClassLoader delegating all class loads to the
     * provided parent, except loading of "META-INF/persistence.xml", which is redirected
     * to the provided persistenceXmlRedirection.
     *
     * @param parent                    The normal classloader, used for all resource loading
     *                                  except "META-INF/persistence.xml"
     * @param persistenceXmlRedirection The location to use when loading "META-INF/persistence.xml".
     *                                  An example would be "META-INF/dbprimer_persistence.xml"
     */
    public PersistenceRedirectionClassLoader(final ClassLoader parent, final String persistenceXmlRedirection) {

        // Check sanity
        Validate.notNull(parent, "Cannot handle null parent argument.");
        Validate.notEmpty(persistenceXmlRedirection,
                "Cannot handle null or empty persistenceXmlRedirection argument.");

        // Assign internal state
        this.parent = parent;
        this.persistenceXmlRedirection = persistenceXmlRedirection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected URL findResource(final String name) {

        if (PERSISTENCE_XML.equals(name)) {

            if (log.isDebugEnabled()) {
                log.debug("Redirected [" + name + "] ==> " + persistenceXmlRedirection);
            }

            // Redirect to the desired resource
            return parent.getResource(persistenceXmlRedirection);
        }

        // All done.
        return null;
    }

    /**
     * Finds all the resources with the given name. A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     * <p/>
     * <p>The name of a resource is a <tt>/</tt>-separated path name that
     * identifies the resource.
     * <p/>
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @param name The resource name
     * @return An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
     *         the resource.  If no resources could  be found, the enumeration
     *         will be empty.  Resources that the class loader doesn't have
     *         access to will not be in the enumeration.
     * @throws java.io.IOException If I/O errors occur
     * @see #findResources(String)
     * @since 1.2
     */
    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {

        Enumeration<URL> toReturn = null;

        if (PERSISTENCE_XML.equals(name)) {

            // Redirect to the desired resource
            toReturn = parent.getResources(persistenceXmlRedirection);

            if (log.isDebugEnabled()) {
                log.debug("Redirected [" + name + "] ==> " + persistenceXmlRedirection);
            }

        } else {

            // Delegate to the parent classloader.
            toReturn = parent.getResources(name);

            if (log.isDebugEnabled()) {
                log.debug("Delegated [" + name + "] ==> parent");
            }
        }

        // All done.
        return toReturn;
    }
}
