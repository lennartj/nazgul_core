/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.quickstart.api.generator.parser;

import se.jguru.nazgul.core.algorithms.api.TypeAlgorithms;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.core.parser.api.agent.AbstractParserAgent;
import se.jguru.nazgul.core.quickstart.model.Project;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Parser agent which performs token substitutions using data within a Project.
 * Readable JavaBean getters are accessible using expressions such as <code>${project:reactorParent.groupId}</code>,
 * or simple properties such <code>${project:name}</code>.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FactoryParserAgent extends AbstractParserAgent {

    /**
     * Prefix indicating that a token replacement value should
     * be acquired from Project data using <code>project.getSomeProperty()</code>,
     * given that the token is <code>${project:someProperty}</code>.
     */
    public static final String PROJECT_PREFIX = "project:";


    // Internal state
    private Project project;

    /**
     * Convenience constructor creating a FactoryParserAgent wrapping the supplied project object,
     * from which readable JavaBean getters are accessible using expressions such as
     * <code>${project:reactorParent.groupId}</code>. No extra static tokens are supplied.
     *
     * @param project The Project from which this FactoryParserAgent should read token data.
     */
    public FactoryParserAgent(@NotNull final Project project) {
        this(project, null);
    }

    /**
     * Compound constructor creating a FactoryParserAgent wrapping the supplied project object,
     * from which readable JavaBean getters are accessible using expressions such as
     * <code>${proj:reactorParent.groupId}</code>.
     *
     * @param project      The Project from which this FactoryParserAgent should read token data.
     * @param staticTokens An optional Map containing static tokens for substitution.
     */
    public FactoryParserAgent(@NotNull final Project project,
                              final Map<String, String> staticTokens) {

        // Check sanity
        Validate.notNull(project, "Cannot handle null project argument.");

        // Assign internal state
        this.project = project;

        if (staticTokens != null) {
            for (Map.Entry<String, String> current : staticTokens.entrySet()) {
                addStaticReplacement(current.getKey(), current.getValue());
            }
        }

        // Add the standard dynamic replacement tokens.
        dynamicTokens.add(PROJECT_PREFIX + ".*");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String performDynamicReplacement(final String token) {

        // Is this a Project property?
        if (token.startsWith(PROJECT_PREFIX)) {

            // Peel of the project prefix
            final String key = token.substring(PROJECT_PREFIX.length());

            // Delegate.
            return "" + TypeAlgorithms.getProperty(project, key);
        }

        // Unknown token. Complain.
        throw new IllegalArgumentException("Cannot handle dynamic token [" + token + "].");
    }
}
