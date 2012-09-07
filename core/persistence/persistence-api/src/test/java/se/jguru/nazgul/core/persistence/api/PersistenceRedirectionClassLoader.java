/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.persistence.api;

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
    private static final String PERSISTENCE_XML = "META-INF/persistence.xml";

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
        if (parent == null) {
            throw new IllegalArgumentException("Cannot handle null parent ClassLoader");
        }
        if (persistenceXmlRedirection == null) {
            throw new IllegalArgumentException("Cannot handle null parent persistenceXmlRedirection");
        }

        this.parent = parent;
        this.persistenceXmlRedirection = persistenceXmlRedirection;
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

        return toReturn;
    }
}