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
package se.jguru.nazgul.core.quickstart.api.generator;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.analyzer.AbstractNamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.model.Name;
import se.jguru.nazgul.core.resource.api.extractor.JarExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Abstract utility implementations for all Factory classes, including POM synthesis and naming validation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractFactory {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractFactory.class.getName());

    // Internal state
    private NamingStrategy namingStrategy;

    /**
     * Creates a new AbstractFactory using the supplied NamingStrategy.
     *
     * @param namingStrategy The active NamingStrategy, used to validate supplied data.
     */
    protected AbstractFactory(final NamingStrategy namingStrategy) {
        Validate.notNull(namingStrategy, "Cannot handle null namingStrategy argument.");
        this.namingStrategy = namingStrategy;
    }

    /**
     * Synthesizes the directory name from the supplied Project and PomType.
     * The resulting name should adhere to the NamingStrategy's
     * {@code NamingStrategy.isPrefixRequiredOnAllFolders()} property.
     *
     * @param projectName   The name of the active project.
     * @param projectPrefix The optional prefix of the active project. Must be supplied (and non-empty) if
     *                      the NamingStrategy requires prefixes to be present on all folders' names.
     * @param aPomType      The PomType for which a directory name should be synthesized.
     * @return The name of the directory.
     */
    protected String getDirectoryName(final String projectName, final PomType aPomType, final String projectPrefix) {

        if (namingStrategy.isPrefixRequiredOnAllFolders() && (projectPrefix == null || projectPrefix.isEmpty())) {
            throw new IllegalArgumentException("NamingStrategy [" + namingStrategy.getClass().getSimpleName()
                    + "] requires that project prefix is required on all folders. Therefore, "
                    + "a nonempty projectPrefix argument must be supplied.");
        }

        // Reactor directories should only be called their respective component name.
        if (aPomType == PomType.ROOT_REACTOR || aPomType == PomType.REACTOR) {
            return namingStrategy.isPrefixRequiredOnAllFolders()
                    ? projectPrefix : "";
        }


        final String dirPrefix = (namingStrategy.isPrefixRequiredOnAllFolders()
                ? projectPrefix + Name.DEFAULT_SEPARATOR + projectName
                : projectName)
                + Name.DEFAULT_SEPARATOR;

        String dirSuffix = aPomType.name().toLowerCase().replace("_", "-");
        if (aPomType == PomType.OTHER_PARENT) {
            dirSuffix = AbstractNamingStrategy.PARENT_SUFFIX;
        }

        final String toReturn = dirPrefix + dirSuffix;
        if (log.isDebugEnabled()) {
            log.debug("PomType [" + aPomType + "] yields directory name [" + toReturn + "]");
        }

        // All done.
        return toReturn;
    }

    /**
     * Retrieves the active NamingStrategy used by this AbstractFactory.
     *
     * @return the active NamingStrategy used by this AbstractFactory.
     */
    protected final NamingStrategy getNamingStrategy() {
        return this.namingStrategy;
    }

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
    protected abstract URL getTemplateResourceURL(final String templateResourcePath);

    /**
     * Retrieves a template resource data from the supplied templateResourcePath (relative to the Classpath).
     *
     * @param templateResourcePath The path to the template resource to synthesize. Fed to the
     *                             {@code getTemplateResourceURL} method to retrieve the actual
     *                             template URL.
     * @return all data found in the resource template given by the relativeResourcePath
     * (with token substitution performed if applicable).
     */
    protected String getTemplateResource(final String templateResourcePath) {

        // Check sanity
        Validate.notEmpty(templateResourcePath, "Cannot handle null or empty relativeResourcePath argument.");

        // Read the data from the resource, and validate that we have a supported URL protocol.
        final URL templateResourceURL = getTemplateResourceURL(templateResourcePath);
        Validate.notNull(templateResourceURL, "ResourceType [" + templateResourcePath + "] yielded no templateResourceURL.");

        final String protocol = templateResourceURL.getProtocol();
        Validate.isTrue("jar".equalsIgnoreCase(protocol) || "file".equalsIgnoreCase(protocol),
                "Can only synthesize resources from URLs using 'jar' or 'file' protocols. (Got [" + protocol + "]).");

        String resourceData = null;
        if ("jar".equalsIgnoreCase(protocol)) {

            final JarFile templateJarFile = JarExtractor.getJarFileFor(templateResourceURL);
            final String entryName = JarExtractor.getEntryNameFor(templateResourceURL);
            final JarEntry jarEntry = templateJarFile.getJarEntry(entryName);
            final StringBuilder builder = new StringBuilder();

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(templateJarFile.getInputStream(jarEntry)))) {

                String aLine = null;
                while ((aLine = in.readLine()) != null) {
                    builder.append(aLine).append("\n");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not fully read entry [" + entryName
                        + "] from templateResourceURL [" + templateResourceURL.toString() + "]", e);
            }

            resourceData = builder.toString();

        } else if ("file".equalsIgnoreCase(protocol)) {
            resourceData = FileUtils.readFile(new File(templateResourceURL.getPath()));
        }

        // All done
        return resourceData;
    }

    /**
     * Acquires a FileFilter which accepts Files whose content should be run through a TokenParser before returning.
     * The default implementation simply retrieves the {@code FileUtils.CHARACTER_DATAFILE_FILTER}; override this
     * method if your particular Factory requires a different implementation.
     *
     * @return A FileFiler which accepts Files whose content should be Tokenized.
     */
    protected FileFilter getShouldTokenizeFilter() {
        return FileUtils.CHARACTER_DATAFILE_FILTER;
    }
}
