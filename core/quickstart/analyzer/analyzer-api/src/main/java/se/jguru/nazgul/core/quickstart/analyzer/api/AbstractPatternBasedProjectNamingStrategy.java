/*
 * #%L
 * Nazgul Project: nazgul-core-analyzer-api
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
package se.jguru.nazgul.core.quickstart.analyzer.api;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.quickstart.analyzer.api.patterns.DefaultProjectParentPatterns;
import se.jguru.nazgul.core.quickstart.analyzer.api.patterns.DefaultTopReactorPatterns;
import se.jguru.nazgul.core.quickstart.analyzer.api.patterns.MavenModelPatterns;

import java.util.regex.Pattern;

/**
 * Abstract ProjectNamingStrategy implementation using java Patterns (and, hence, regular expressions)
 * to identify valid project names. Subclasses should override given abstract methods to provide
 * implementation hooks.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractPatternBasedProjectNamingStrategy implements ProjectNamingStrategy {

    // Internal state
    private Pattern projectNamePattern;
    private Pattern projectPrefixPattern;
    private MavenModelPatterns topReactorPatterns;
    private MavenModelPatterns topParentPatterns;

    /**
     * Compound constructor, creating a new AbstractPatternBasedProjectNamingStrategy instance
     * from the supplied parameters. Using default implementations for the TopReactorPatterns
     * and TopParentPatterns parameters.
     *
     * @param projectNamePattern   The java regexp defining a valid project name.
     * @param projectPrefixPattern The java regexp defining a valid project prefix.
     */
    protected AbstractPatternBasedProjectNamingStrategy(final String projectNamePattern,
                                                        final String projectPrefixPattern) {
        // Delegate
        this(projectNamePattern, projectPrefixPattern,
                new DefaultTopReactorPatterns(), new DefaultProjectParentPatterns());
    }

    /**
     * Compound constructor, creating a new AbstractPatternBasedProjectNamingStrategy instance
     * from the supplied parameters.
     *
     * @param projectNamePattern   The java regexp defining a valid project name.
     * @param projectPrefixPattern The java regexp defining a valid project prefix.
     * @param topReactorPatterns   The Patterns required to validate the top-level reactor POM.
     * @param topParentPatterns    The Patterns required to validate the top-level parent POM.
     */
    protected AbstractPatternBasedProjectNamingStrategy(final String projectNamePattern,
                                                        final String projectPrefixPattern,
                                                        final MavenModelPatterns topReactorPatterns,
                                                        final MavenModelPatterns topParentPatterns) {

        // Check sanity
        Validate.notEmpty(projectNamePattern, "Cannot handle null or empty projectNamePattern argument.");
        Validate.notEmpty(projectPrefixPattern, "Cannot handle null or empty projectPrefixPattern argument.");
        Validate.notNull(topReactorPatterns, "Cannot handle null topReactorPatterns argument.");
        Validate.notNull(topParentPatterns, "Cannot handle null topParentPatterns argument.");

        // Assign internal state
        this.projectNamePattern = Pattern.compile(projectNamePattern, Pattern.CANON_EQ);
        this.projectPrefixPattern = Pattern.compile(projectPrefixPattern, Pattern.CANON_EQ);
        this.topReactorPatterns = topReactorPatterns;
        this.topParentPatterns = topParentPatterns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidProjectName(final String name) {
        return name != null && projectNamePattern.matcher(name).matches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getRootDirectoryName(final String projectName) throws IllegalArgumentException {

        // Check sanity
        Validate.isTrue(isValidProjectName(projectName), "[" + projectName + "] is not a valid project name "
                + "- it must match regexp [" + projectNamePattern.pattern() + "]");

        // Delegate
        return createRootDirectoryNameFrom(projectName);
    }

    /**
     * Creates the root directory name for a project originating from a valid project name.
     *
     * @param validProjectName A project name properly validated by the {@code #isValidProjectName} method.
     * @return the root directory name for a project originating from a valid project name.
     */
    protected abstract String createRootDirectoryNameFrom(final String validProjectName);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidProjectPrefix(final String projectPrefix) {
        return projectPrefix != null && projectPrefixPattern.matcher(projectPrefix).matches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getTopLevelPackage(final String reverseOrganisationDNS) throws IllegalArgumentException {

        // Check sanity
        Validate.isTrue(isValidProjectPrefix(reverseOrganisationDNS), "[" + reverseOrganisationDNS + "] is not a "
                + "valid project prefix - it must match regexp [" + projectPrefixPattern.pattern() + "]");

        // Delegate
        return createTopLevelPackageFrom(reverseOrganisationDNS);
    }

    /**
     * Creates the top-level package for a project originating from a valid project prefix.
     *
     * @param reverseOrganisationDNS A project prefix properly validated by the {@code #isValidProjectPrefix} method.
     * @return the top-level package for a project originating from a valid project prefix.
     */
    protected abstract String createTopLevelPackageFrom(final String reverseOrganisationDNS);

    /**
     * Retrieves the project name, if the supplied data yields {@code true} for a call to {@code isRootReactorPom}.
     *
     * @param parentGroupID    The parent groupId, as found within a POM.
     * @param parentArtifactID The parent artifactId, as found within a POM.
     * @param groupID          The groupId of a POM.
     * @param artifactID       The artifactId of a POM.
     * @return The project name, if the supplied data is for a root reactor POM.
     */
    @Override
    public final String getProjectName(final String parentGroupID,
                                       final String parentArtifactID,
                                       final String groupID,
                                       final String artifactID) {

        // Check sanity
        final String errorMessage = "Combination of Parent GroupID/ArtifactID [" + parentGroupID + "/"
                + parentArtifactID + "] and GroupID/ArtifactID [" + groupID + "/" + artifactID
                + "] was neither a root reactor POM nor a project parent POM.";
        final boolean isRootReactorPOM = isRootReactorPom(parentGroupID, parentArtifactID, groupID, artifactID);
        final boolean isProjectParentPom = isProjectParentPom(parentGroupID, parentArtifactID, groupID, artifactID);
        Validate.isTrue(isProjectParentPom || isRootReactorPOM, errorMessage);

        // Delegate and return
        return getProjectName(isRootReactorPOM, parentGroupID, parentArtifactID, groupID, artifactID);
    }

    /**
     * Retrieves the project name for the supplied data.
     *
     * @param isReactorData    {@code true} if the supplied data is reactor POM data, and
     *                         {@code false} if the supplied data is parent POM data.
     * @param parentGroupID    The parent groupId, as found within a POM.
     * @param parentArtifactID The parent artifactId, as found within a POM.
     * @param groupID          The groupId of a POM.
     * @param artifactID       The artifactId of a POM.
     * @return The project name.
     */
    protected abstract String getProjectName(boolean isReactorData,
                                             final String parentGroupID,
                                             final String parentArtifactID,
                                             final String groupID,
                                             final String artifactID);

    /**
     * Standard implementation, override in subclasses to supply custom functionality.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean isRootReactorPom(final String parentGroupID,
                                    final String parentArtifactID,
                                    final String groupID,
                                    final String artifactID) {

        final boolean validIfNull = topReactorPatterns.nullPatternImpliesValid();

        // Evaluate.
        return isMatch(topReactorPatterns.getParentGroupIdPattern(), validIfNull, parentGroupID)
                && isMatch(topReactorPatterns.getParentArtifactIdPattern(), validIfNull, parentArtifactID)
                && isMatch(topReactorPatterns.getGroupIdPattern(), validIfNull, groupID)
                && isMatch(topReactorPatterns.getArtifactIdPattern(), validIfNull, artifactID);
    }

    /**
     * Standard implementation, override in subclasses to supply custom functionality.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean isProjectParentPom(final String parentGroupID,
                                      final String parentArtifactID,
                                      final String groupID,
                                      final String artifactID) {

        final boolean validIfNull = topParentPatterns.nullPatternImpliesValid();

        // Evaluate.
        return isMatch(topParentPatterns.getParentGroupIdPattern(), validIfNull, parentGroupID)
                && isMatch(topParentPatterns.getParentArtifactIdPattern(), validIfNull, parentArtifactID)
                && isMatch(topParentPatterns.getGroupIdPattern(), validIfNull, groupID)
                && isMatch(topParentPatterns.getArtifactIdPattern(), validIfNull, artifactID);
    }

    //
    // Private helpers
    //

    private boolean isMatch(final Pattern pattern, final boolean validIfNullPattern, final String toMatch) {

        // Null pattern yields standard behaviour.
        if(pattern == null) {
            return validIfNullPattern;
        }

        // Perform standard pattern matching
        return pattern.matcher(toMatch).matches();
    }
}