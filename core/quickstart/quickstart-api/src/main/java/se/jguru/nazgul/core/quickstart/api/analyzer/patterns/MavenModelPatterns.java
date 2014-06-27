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
 * Holder specification for Patterns intended to validate or invalidate data
 * retrieved from a Maven POM.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface MavenModelPatterns {

    /**
     * @return {@code true} if a null Pattern should imply that any value is valid.
     */
    boolean nullPatternImpliesValid();

    /**
     * Retrieves the Pattern which must be matched to imply a valid groupId.
     *
     * @return the Java RegExp Pattern which must be matched to imply a valid groupId.
     */
    Pattern getGroupIdPattern();

    /**
     * Retrieves the Pattern which must be matched to imply a valid artifactId.
     *
     * @return the Java RegExp Pattern which must be matched to imply a valid artifactId.
     */
    Pattern getArtifactIdPattern();

    /**
     * Retrieves the Pattern which must be matched to imply a valid parent groupId
     * (i.e. the groupId of the Parent of the actual Maven Model).
     *
     * @return the Java RegExp Pattern which must be matched to imply a valid parent groupId
     *         (i.e. the groupId of the Parent of the actual Maven Model).
     */
    Pattern getParentGroupIdPattern();

    /**
     * Retrieves the Pattern which must be matched to imply a valid parent artifactId
     * (i.e. the artifactId of the Parent of the actual Maven Model).
     *
     * @return the Java RegExp Pattern which must be matched to imply a valid parent artifactId
     *         (i.e. the artifactId of the Parent of the actual Maven Model).
     */
    Pattern getParentArtifactIdPattern();
}
