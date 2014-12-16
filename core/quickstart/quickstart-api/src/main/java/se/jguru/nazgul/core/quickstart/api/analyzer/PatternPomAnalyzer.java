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
package se.jguru.nazgul.core.quickstart.api.analyzer;

import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;
import se.jguru.nazgul.core.quickstart.api.analyzer.patterns.DefaultProjectParentPatterns;
import se.jguru.nazgul.core.quickstart.api.analyzer.patterns.DefaultTopReactorPatterns;
import se.jguru.nazgul.core.quickstart.api.analyzer.patterns.MavenModelPatterns;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * PomAnalyzer implementation which uses a set of regular expressions to validate
 * that the topmost reactor and parent POMs are valid.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PatternPomAnalyzer extends AbstractPomAnalyzer {

    // Internal state
    private MavenModelPatterns parentPomPatterns;
    private MavenModelPatterns topReactorPomPatterns;

    /**
     * Convenience constructor creating a new PatternPomAnalyzer using the supplied NamingStrategy and
     * using the DefaultTopReactorPatterns and DefaultProjectParentPatterns MavenModelPatterns
     * implementations.
     *
     * @param namingStrategy A non-null NamingStrategy used to validate Models.
     */
    public PatternPomAnalyzer(final NamingStrategy namingStrategy) {
        this(namingStrategy, new DefaultProjectParentPatterns(), new DefaultTopReactorPatterns());
    }

    /**
     * Creates a new PatternPomAnalyzer instance wrapping the supplied NamingStrategy and MavenModelPatterns.
     *
     * @param namingStrategy        A non-null NamingStrategy used to validate Models.
     * @param parentPomPatterns     A non-null MavenModelPatterns definition for identifying the topmost Parent POM
     *                              within a multi-module reactor.
     * @param topReactorPomPatterns A non-null MavenModelPatterns definition for identifying the top Reactor POM
     *                              within a multi-module reactor.
     */
    public PatternPomAnalyzer(final NamingStrategy namingStrategy,
                              final MavenModelPatterns parentPomPatterns,
                              final MavenModelPatterns topReactorPomPatterns) {
        super(namingStrategy);

        Validate.notNull(parentPomPatterns, "Cannot handle null parentPomPatterns argument.");
        Validate.notNull(topReactorPomPatterns, "Cannot handle null topReactorPomPatterns argument.");

        this.parentPomPatterns = parentPomPatterns;
        this.topReactorPomPatterns = topReactorPomPatterns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCustomRootReactorPomValidation(final Model rootReactorModel)
            throws InvalidStructureException {
        validatePomPatterns(rootReactorModel, topReactorPomPatterns);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCustomTopmostParentPomValidation(final Model topmostParentModel)
            throws InvalidStructureException {
        validatePomPatterns(topmostParentModel, parentPomPatterns);
    }

    //
    // Private helpers
    //

    private void validatePomPatterns(final Model model, final MavenModelPatterns patterns) {

        final Parent parent = model.getParent();
        final String groupId = model.getGroupId();
        final String artifactId = model.getArtifactId();
        String suffix = "for POM (" + groupId + "/" + artifactId;

        if (parent == null) {
            throw new InvalidStructureException("Missing Parent definition " + suffix + ")");
        }

        final String effectiveVersion = model.getVersion() == null
                ? parent.getVersion()
                : model.getVersion();
        suffix = suffix + "/" + effectiveVersion + ")";

        final boolean isNullValid = patterns.nullPatternImpliesValid();

        final boolean groupIdMatch = isMatch(patterns.getGroupIdPattern(), isNullValid, groupId);
        final boolean artifactIdMatch = isMatch(patterns.getArtifactIdPattern(), isNullValid, artifactId);
        final boolean parentGroupIdMatch = isMatch(
                patterns.getParentGroupIdPattern(), isNullValid, parent.getGroupId());
        final boolean parentArtifactIdMatch = isMatch(
                patterns.getParentArtifactIdPattern(), isNullValid, parent.getArtifactId());

        final List<String> errors = new ArrayList<>();
        if (!parentGroupIdMatch) {
            errors.add("Incorrect parent groupId [" + parent.getGroupId() + "]");
        }
        if (!parentArtifactIdMatch) {
            errors.add("Incorrect parent artifactId [" + parent.getArtifactId() + "]");
        }
        if (!groupIdMatch) {
            errors.add("Incorrect groupId [" + groupId + "]");
        }
        if (!artifactIdMatch) {
            errors.add("Incorrect artifactId [" + artifactId + "]");
        }

        if (errors.size() > 0) {
            StringBuilder builder = new StringBuilder("Invalid structure detected for POM ("
                    + model.getGroupId() + "/" + model.getArtifactId() + "/" + effectiveVersion + "): ");
            for (int i = 0; i < errors.size(); i++) {
                builder.append("\n " + (i + 1) + ": " + errors.get(i));
            }
            throw new InvalidStructureException(builder.toString());
        }
    }

    private boolean isMatch(final Pattern aPattern, final boolean nullImpliesValid, final String toMatch) {

        // Should we compare nulls?
        if(aPattern == null || toMatch == null) {
            return nullImpliesValid;
        }

        return aPattern.matcher(toMatch).matches();
    }
}
