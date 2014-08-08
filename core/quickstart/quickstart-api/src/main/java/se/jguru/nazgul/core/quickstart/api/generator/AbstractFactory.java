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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
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

    /**
     * The TokenParser used to substitute tokens within character-data resource templates (i.e. POMs,
     * source code files, etc. which contain plain text). Override this method if your particular factory
     * requires another TokenParser implementation.
     * <p/>
     * The default (AbstractFactory) implementation emits a TokenParser which adds 3 ParserAgents:
     * <ol>
     * <li><strong>DefaultParserAgent</strong>, with additional tokens added:
     * <dl>
     * <dt>relativeDirPath</dt>
     * <dd>The value of the parameter relativeDirPath</dd>
     * <p/>
     * <dt>relativePackage</dt>
     * <dd>{@code relativeDirPath.replace("/", ".")}</dd>
     * </dl></li>
     * <li><strong>HostNameParserAgent</strong>, which implies that data for the actual host can be substituted
     * within any template.
     * </li>
     * <li><strong>FactoryParserAgent</strong>, which accesses data from the supplied project,
     * in addition to adding two static tokens:
     * <dl>
     * <dt>groupId</dt>
     * <dd>{@code groupIdPrefix + relativeDirPath.replace("/", ".")}, where groupIdPrefix is identical
     * to projectGroupIdPrefix, with a "." appended unless it already ends with a "."</dd>
     * <p/>
     * <dt>artifactId</dt>
     * <dd></dd>
     * </dl></li>
     * </ol>
     * <p/>
     * This TokenParser uses tokens on the form [token] to avoid clashing with Maven's variables on the form ${token}.
     *
     * @param pomType                  The SoftwareComponentPart for which a TokenParser should be retrieved.
     * @param relativeDirPath       The current/active directory path (relative to the project root directory) of the
     *                              project which should be tokenized by the retrieved TokenParser.
     * @param project               The active Project, from which tokens could be retrieved.
     * @param projectGroupIdPrefix  The prefix used for groupId within POMs and packages within Maven projects as
     *                              tokenized by this AbstractFactory.
     * @param optionalProjectSuffix The project suffix, which is used to create a compliant Name for the
     *                              SoftwareComponentPart (and, therefore,
     *                              only used when {@code SoftwareComponentPart.isSuffixRequired()} is true.
     * @return A fully configured TokenParser hosting 3 parser agents (DefaultParserAgent, HostNameParserAgent,
     * FactoryParserAgent) and initialized using the SingleBracketTokenDefinitions.
     * @see se.jguru.nazgul.core.parser.api.agent.DefaultParserAgent
     * @see se.jguru.nazgul.core.parser.api.agent.HostNameParserAgent
     * @see FactoryParserAgent
     */
    protected TokenParser getTokenParser(final PomType pomType,
                                         final String relativeDirPath,
                                         final Project project,
                                         final String projectGroupIdPrefix,
                                         final String optionalProjectSuffix) {

        // Check sanity
        Validate.isTrue((pomType == null && project == null) || (pomType != null && project != null),
                "'pomType' and 'project' arguments must both either be null or non-null.");
        Validate.notNull(relativeDirPath, "Cannot handle null relativeDirPath argument.");

        // Put the relative dir path into a static token.
        final Map<String, String> staticTokensMap = new TreeMap<>();
        staticTokensMap.put("relativeDirPath", relativeDirPath);
        staticTokensMap.put("relativePackage", relativeDirPath.replace("/", "."));

        // Create a default TokenParser, using tokens on the form [token],
        // to avoid clashing with Maven's variables on the form ${token}.
        final DefaultTokenParser toReturn = new DefaultTokenParser();
        toReturn.addAgent(new DefaultParserAgent(staticTokensMap));
        toReturn.addAgent(new HostNameParserAgent());
        if (pomType != null && project != null) {

            // Find the groupId and artifactId of the local Maven Project.
            final StringBuilder groupIdBuilder = new StringBuilder();
            if(projectGroupIdPrefix != null) {
                groupIdBuilder.append(projectGroupIdPrefix.replace("/", "."));
                groupIdBuilder.append(".");
            }
            groupIdBuilder.append(relativeDirPath.replace("/", "."));


            final StringBuilder artifactIdBuilder = new StringBuilder();
            final String projectPrefix = project.getPrefix();
            if(projectPrefix != null && projectPrefix.length() > 0) {
                artifactIdBuilder.append(projectPrefix).append(Name.DEFAULT_SEPARATOR);
            }
            artifactIdBuilder.append(project.getName());
            if(optionalProjectSuffix != null && optionalProjectSuffix.length() > 0) {
                artifactIdBuilder.append(Name.DEFAULT_SEPARATOR).append(optionalProjectSuffix);
            }

            // Add some typically useful static tokens for POM synthesis.
            final Map<String, String> pomValueMap = new TreeMap<>();
            pomValueMap.put("groupId", groupIdBuilder.toString());
            pomValueMap.put("artifactId", artifactIdBuilder.toString());

            // Add the FactoryParserAgent
            toReturn.addAgent(new FactoryParserAgent(project, pomType, pomValueMap));
        }
        toReturn.initialize(new SingleBracketTokenDefinitions());

        // All done.
        return toReturn;
    }
}
