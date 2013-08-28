/*
 * #%L
 * Nazgul Project: nazgul-core-analyzer-impl-nazgul
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
package se.jguru.nazgul.core.quickstart.analyzer.impl.nazgul;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.quickstart.analyzer.api.AbstractPatternBasedProjectNamingStrategy;

import java.util.regex.Pattern;

/**
 * Nazgul Framework-style implementation of the AbstractPatternBasedProjectNamingStrategy.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulProjectNamingStrategy extends AbstractPatternBasedProjectNamingStrategy {

    /**
     * Pattern identifying valid NazgulFramework-style project names.
     */
    public static final Pattern PROJECT_NAME_PATTERN = Pattern.compile(
            "[a-z][a-z0-9]*(-[a-z0-9]+)?", Pattern.CANON_EQ);

    /**
     * Pattern identifying valid NazgulFramework-style project prefixes.
     */
    public static final Pattern PROJECT_PREFIX_PATTERN = Pattern.compile(
            "[a-z][a-z0-9]*([.-][a-z0-9]+)*", Pattern.CANON_EQ);

    /**
     * Default constructor using the defined Patterns to prime the standard project
     * definition patterns for name and prefix.
     *
     * @see #PROJECT_NAME_PATTERN
     * @see #PROJECT_PREFIX_PATTERN
     */
    public NazgulProjectNamingStrategy() {
        super(PROJECT_NAME_PATTERN.pattern(), PROJECT_PREFIX_PATTERN.pattern());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createRootDirectoryNameFrom(final String validProjectName) {
        return validProjectName.substring(validProjectName.indexOf("-") + 1).trim();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createTopLevelPackageFrom(final String reverseOrganisationDNS) {
        return reverseOrganisationDNS.trim();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProjectName(final boolean isReactorData,
                                    final String parentGroupID,
                                    final String parentArtifactID,
                                    final String groupID,
                                    final String artifactID) {

        // Check sanity
        Validate.notEmpty(artifactID, "Cannot handle null or empty artifactID argument.");

        // Project names should be found within the artifactID
        return artifactID.substring(0, artifactID.indexOf("-"));
    }
}
