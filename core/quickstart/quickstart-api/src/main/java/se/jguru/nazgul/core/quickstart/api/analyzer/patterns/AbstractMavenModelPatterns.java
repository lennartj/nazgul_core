/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
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
package se.jguru.nazgul.core.quickstart.api.analyzer.patterns;

import java.util.regex.Pattern;

/**
 * Abstract/generic holder implementation of the MavenModelPatterns specification.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractMavenModelPatterns implements MavenModelPatterns {

    // Internal state
    private boolean nullPatternsImpliedValidity;
    private Pattern groupIdPattern;
    private Pattern artifactIdPattern;
    private Pattern parentGroupIdPattern;
    private Pattern parentArtifactIdPattern;

    /**
     * Creates a new AbstractMavenModelPatterns instance wrapping the supplied data.
     * Any Pattern may be {@code null}.
     *
     * @param nullPatternsImpliedValidity {@code true} if a null Pattern should imply that any value is valid.
     * @param groupIdPattern              the Java RegExp Pattern which must be matched to imply a valid groupId.
     * @param artifactIdPattern           the Java RegExp Pattern which must be matched to imply a valid artifactId.
     * @param parentGroupIdPattern        the Java RegExp Pattern which must be matched to imply a valid parent groupId
     *                                    (i.e. the groupId of the Parent of the actual Maven Model).
     * @param parentArtifactIdPattern     the Java RegExp Pattern which must be matched to imply a valid parent
     *                                    artifactId (i.e. the artifactId of the Parent of the actual Maven Model).
     */
    protected AbstractMavenModelPatterns(final boolean nullPatternsImpliedValidity,
                                         final Pattern groupIdPattern,
                                         final Pattern artifactIdPattern,
                                         final Pattern parentGroupIdPattern,
                                         final Pattern parentArtifactIdPattern) {

        this.nullPatternsImpliedValidity = nullPatternsImpliedValidity;
        this.groupIdPattern = groupIdPattern;
        this.artifactIdPattern = artifactIdPattern;
        this.parentGroupIdPattern = parentGroupIdPattern;
        this.parentArtifactIdPattern = parentArtifactIdPattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean nullPatternImpliesValid() {
        return nullPatternsImpliedValidity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pattern getGroupIdPattern() {
        return groupIdPattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pattern getArtifactIdPattern() {
        return artifactIdPattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pattern getParentGroupIdPattern() {
        return parentGroupIdPattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pattern getParentArtifactIdPattern() {
        return parentArtifactIdPattern;
    }
}
