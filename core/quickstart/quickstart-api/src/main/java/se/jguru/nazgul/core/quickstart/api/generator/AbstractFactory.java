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
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.analyzer.AbstractNamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.generator.parser.FactoryParserAgent;
import se.jguru.nazgul.core.quickstart.model.Name;
import se.jguru.nazgul.core.quickstart.model.Project;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

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
     * Reads data from a POM template, and uses the TokenParser to replace any tokens before handing the resulting
     * data back as a String. The default implementation assumes that the POM template is found in the URL given by
     * the {@code getPomTemplateURL} method.
     * <p/>
     * Acquires the data within the POM given by the supplied data.
     * The resulting String should be correctly formed data of the POM (i.e. fully tokenized if applicable).
     *
     * @param pomType         The type of POM whose data should be retrieved.
     * @param relativeDirPath The directory path (relative to the project root directory) of the POM to retrieve.
     * @param project         The active Project, from which tokens could be retrieved.
     * @return correctly formed data of the POM (i.e. fully tokenized if applicable).
     */
    protected String getPom(final PomType pomType,
                            final String relativeDirPath,
                            final Project project) {

        // Check sanity
        Validate.notNull(pomType, "Cannot handle null pomType argument.");
        Validate.notNull(project, "Cannot handle null project argument.");
        Validate.notNull(relativeDirPath, "Cannot handle null relativeDirPath argument.");

        // Get a POM template
        final String pomURL = getPomTemplateURL(pomType);
        final StringBuilder builder = new StringBuilder();
        try (BufferedReader data = new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(pomURL)))) {
            for (String aLine = data.readLine(); aLine != null; aLine = data.readLine()) {
                builder.append(aLine).append("\n");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read data from [" + pomURL + "]", e);
        }

        // Tokenize and return.
        return getTokenParser(pomType, relativeDirPath, project).substituteTokens(builder.toString());
    }

    /**
     * Method retrieving an URL to a template for a pom of the supplied PomType.
     * Default implementation assumes that POM templates URLs are synthesized
     * on the form {@code "templates/standard/" + pomType.name().toLowerCase() + ".xml"}.
     * Override this method if your local Factory uses other algorithms.
     *
     * @param pomType The type of POM whose template should be retrieved.
     * @return The content of the POM template data.
     */
    protected String getPomTemplateURL(final PomType pomType) {
        return "templates/standard/" + pomType.name().toLowerCase() + ".xml";
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
        toReturn.addAgent(new DefaultParserAgent());
        toReturn.addAgent(new HostNameParserAgent());
        toReturn.addAgent(new FactoryParserAgent(project, pomType, staticTokensMap));
        toReturn.initialize(new SingleBracketTokenDefinitions());

        // All done.
        return toReturn;
    }
}
