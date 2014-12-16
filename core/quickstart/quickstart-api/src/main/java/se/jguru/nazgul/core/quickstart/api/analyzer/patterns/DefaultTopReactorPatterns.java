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
 * Default MavenModelPatterns implementation for a top-level reactor POM,
 * typically on the following form:
 * <p/>
 * <pre>
 *     &lt;parent>
 *          &lt;groupId>se.jguru.nazgul.tools.poms.external&lt;/groupId>
 *          &lt;artifactId>nazgul-tools-external-reactor-parent&lt;/artifactId>
 *     &lt;/parent>
 *
 *      &lt;groupId>com.acme.foobar&lt;/groupId>
 *      &lt;artifactId>foobar-reactor&lt;/artifactId>
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultTopReactorPatterns extends AbstractMavenModelPatterns {

    /**
     * Required pattern for artifactIDs within top-level reactor POMs.
     */
    public static final Pattern ARTIFACT_ID_PATTERN = Pattern.compile(".*-reactor", Pattern.CANON_EQ);

    /**
     * The pattern for parent's artifactIDs within top-level reactor POMs.
     */
    public static final Pattern PARENT_ARTIFACT_ID_PATTERN = Pattern.compile(
            "nazgul-tools-external-reactor-parent", Pattern.CANON_EQ);

    /**
     * The pattern for parent's groupIDs within top-level reactor POMs.
     */
    public static final Pattern PARENT_GROUP_ID_PATTERN = Pattern.compile(
            "se\\.jguru\\.nazgul\\.tools\\.poms\\.external", Pattern.CANON_EQ);

    /**
     * Default constructor, passing all defined patterns to its superclass.
     */
    public DefaultTopReactorPatterns() {
        super(true, null, ARTIFACT_ID_PATTERN, PARENT_GROUP_ID_PATTERN, PARENT_ARTIFACT_ID_PATTERN);
    }
}
