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
import se.jguru.nazgul.core.parser.api.DefaultTokenParser;
import se.jguru.nazgul.core.parser.api.SingleBracketTokenDefinitions;
import se.jguru.nazgul.core.parser.api.TokenParser;
import se.jguru.nazgul.core.parser.api.agent.DefaultParserAgent;
import se.jguru.nazgul.core.parser.api.agent.HostNameParserAgent;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.analyzer.AbstractNamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.generator.parser.FactoryParserAgent;
import se.jguru.nazgul.core.quickstart.model.Name;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.resource.api.extractor.JarExtractor;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
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
            log.debug("PomType [" + aPomType + "] yielded name [" + toReturn + "]");
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
    protected abstract URL getTemplateResource(final String templateResourcePath);

    /**
     * Reads resource template data from the supplied relativeResourcePath and returns it after substituting all its
     * contained tokens with actual values. Token substitution will only be done if the {@code isTokenizable} method
     * returns true.
     * <p/>
     * The default implementation calculates the actual resource template path as
     * {@code getTemplateResource() + "/" + relativeResourcePath}.
     * Override this method if your template resource data is retrieved in another fashion.
     *
     * @param templateResourcePath The path to the template resource to synthesize. Fed to the
     *                             {@code getTemplateResource} method to retrieve the actual template URL.
     * @param pomType              The type of POM whose data should be retrieved. Optional convenience argument; null
     *                             if the resource is not a POM.
     * @param project              The active Project, from which tokens could be retrieved. Required; cannot be null.
     * @return all data found in the resource template given by the relativeResourcePath
     * (with token substitution performed if applicable).
     */
    protected String synthesizeResource(final String templateResourcePath,
                                        final PomType pomType,
                                        final Project project) {

        // Check sanity
        Validate.notEmpty(templateResourcePath, "Cannot handle null or empty relativeResourcePath argument.");
        Validate.notNull(project, "Cannot handle null project argument.");

        // Read the data from the resource, and validate that we have a supported URL protocol.
        final URL templateResourceURL = getTemplateResource(templateResourcePath);
        Validate.notNull(templateResourceURL, "ResourceType [" + templateResourcePath + "] yielded no templateResourceURL.");

        final String protocol = templateResourceURL.getProtocol();
        Validate.isTrue("jar".equalsIgnoreCase(protocol) || "file".equalsIgnoreCase(protocol),
                "Can only synthesize resources from URLs using 'jar' or 'file' protocols. (Got [" + protocol + "]).");

        String resourceData = null;
        if ("jar".equalsIgnoreCase(protocol)) {

            final JarFile templateJarFile = JarExtractor.getJarFileFor(templateResourceURL);
            final String entryName = JarExtractor.getEntryNameFor(templateResourceURL);

        } else if("file".equalsIgnoreCase(protocol)) {
            resourceData = FileUtils.readFile(templateResourceURL.toString());
        }

        // Tokenize if required
        return isTokenizable(templateResourceURL)
                ? getTokenParser(pomType, templateResourceURL, project).substituteTokens(resourceData)
                : resourceData;
    }

    /**
     * Checks if the supplied resourceURL and pomType should be tokenized.
     * <p/>
     * The default implementation handles "file" and "jar" URLs and returns true for all non-directory resources
     * found within either a File system or a JAR. Override this method if your local Factory uses another algorithm
     * to determine if resources should be tokenized.
     *
     * @param resourceURL The non-null resourceURL of the resource template which could be tokenized.
     * @return {@code true} if the supplied resource URL should be tokenized.
     */
    protected boolean isTokenizable(final URL resourceURL) {

        // Check sanity
        Validate.notNull(resourceURL, "Cannot handle null resourceURL argument.");

        final String protocol = resourceURL.getProtocol().toLowerCase();
        boolean toReturn = true;

        if (protocol.equals("file")) {
            final File resourceFile = new File(resourceURL.getPath());

            // Only tokenize content in files.
            toReturn = resourceFile.isFile();
        } else if (protocol.equals("jar")) {

            // Find the resource within the JAR
            final JarFile jarFile = JarExtractor.getJarFileFor(resourceURL);
            final String name = JarExtractor.getEntryNameFor(resourceURL);
            final JarEntry entry = jarFile.getJarEntry(name);

            // Only tokenize content in files.
            toReturn = !entry.isDirectory();
        }

        // All done.
        return toReturn;
    }

    /**
     * The TokenParser used to substitute tokens within POM templates.
     * This TokenParser uses tokens on the form [token] to avoid clashing with Maven's variables on the form ${token}.
     * Override this method if your particular factory requires another TokenParser implementation.
     *
     * @param pomType         The type of POM whose data should be retrieved.
     * @param relativeDirPath The directory path (relative to the project root directory) of the POM to retrieve.
     * @param project         The active Project, from which tokens could be retrieved.
     * @return A fully configured TokenParser hosting 3 parser agents (DefaultParserAgent, HostNameParserAgent,
     * FactoryParserAgent) and initialized using the SingleBracketTokenDefinitions.
     */
    protected TokenParser getTokenParser(final PomType pomType,
                                         final String relativeDirPath,
                                         final Project project) {

        // Put the relative dir path into a static token.
        final Map<String, String> staticTokensMap = new TreeMap<>();
        staticTokensMap.put("relativeDirPath", relativeDirPath);

        // Create a default TokenParser, using tokens on the form [token],
        // to avoid clashing with Maven's variables on the form ${token}.
        final DefaultTokenParser toReturn = new DefaultTokenParser();
        toReturn.addAgent(new DefaultParserAgent(staticTokensMap));
        toReturn.addAgent(new HostNameParserAgent());
        if (pomType != null) {
            toReturn.addAgent(new FactoryParserAgent(project, pomType));
        }
        toReturn.initialize(new SingleBracketTokenDefinitions());

        // All done.
        return toReturn;
    }
}
