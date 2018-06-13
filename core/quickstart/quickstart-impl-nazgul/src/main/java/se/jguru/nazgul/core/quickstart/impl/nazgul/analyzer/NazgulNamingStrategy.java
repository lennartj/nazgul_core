/*-
 * #%L
 * Nazgul Project: nazgul-core-quickstart-impl-nazgul
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.core.quickstart.impl.nazgul.analyzer;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.analyzer.AbstractNamingStrategy;
import se.jguru.nazgul.core.quickstart.model.Name;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Nazgul-style NamingStrategy implementation, based on the AbstractNamingStrategy.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulNamingStrategy extends AbstractNamingStrategy {

    /**
     * Pattern identifying a single word with ASCII-compatible, lowercase letters and optional digits.
     */
    public static final Pattern WORD_PATTERN = Pattern.compile("[a-z][a-z0-9]*", Pattern.CANON_EQ);

    /**
     * Pattern identifying several words with ASCII-compatible, lowercase letters and optional digits,
     * separated by '-' chars.
     */
    public static final Pattern WORDS_PATTERN = Pattern.compile("[a-z][a-z0-9]*([.-][a-z0-9]+)*", Pattern.CANON_EQ);

    /**
     * Nazgul prefix.
     */
    public static final String NAZGUL_PREFIX = "nazgul";

    // Internal state
    private String requiredPrefix;
    private Map<PomType, Pattern> validPatternMap;

    /**
     * Default constructor creating a NazgulNamingStrategy which requires that no prefixes should be placed
     * on folders within the project reactor, and that all artifactIDs should use the prefix {@code NAZGUL_PREFIX}.
     *
     * @see se.jguru.nazgul.core.quickstart.model.Name
     * @see se.jguru.nazgul.core.quickstart.model.Name#parse(String)
     */
    public NazgulNamingStrategy() {
        this(false, NAZGUL_PREFIX);
    }

    /**
     * Compound constructor creating a NazgulNamingStrategy wrapping the supplied data.
     *
     * @param prefixIsRequiredOnAllFolders if {@code true}, all folders within the project reactor
     *                                     are required to sport prefixes.
     * @param requiredArtifactIdPrefix     A non-empty string which must be used as the prefix of all valid Names.
     * @see se.jguru.nazgul.core.quickstart.model.Name
     * @see se.jguru.nazgul.core.quickstart.model.Name#parse(String)
     */
    public NazgulNamingStrategy(final boolean prefixIsRequiredOnAllFolders,
                                final String requiredArtifactIdPrefix) {
        super(prefixIsRequiredOnAllFolders);

        // Assign internal state
        Validate.notEmpty(requiredArtifactIdPrefix, "Cannot handle null or empty requiredPrefix argument.");
        this.requiredPrefix = requiredArtifactIdPrefix;

        validPatternMap = new TreeMap<>();
        validPatternMap.put(PomType.ROOT_REACTOR, Pattern.compile(AbstractNamingStrategy.REACTOR_SUFFIX));
        validPatternMap.put(PomType.REACTOR, Pattern.compile(
                WORDS_PATTERN + "-" + AbstractNamingStrategy.REACTOR_SUFFIX));
        validPatternMap.put(PomType.REACTOR, Pattern.compile(
                WORDS_PATTERN + "-" + AbstractNamingStrategy.REACTOR_SUFFIX));

        for (PomType current : Arrays.asList(PomType.PARENT, PomType.API_PARENT,
                PomType.MODEL_PARENT, PomType.WAR_PARENT)) {
            validPatternMap.put(current, Pattern.compile(current.name().toLowerCase().replace('_', '-')));
        }
        validPatternMap.put(PomType.OTHER_PARENT, Pattern.compile(WORDS_PATTERN
                + "-" + AbstractNamingStrategy.PARENT_SUFFIX));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Name toValidate, final PomType pomType) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(toValidate, "Cannot handle null Name argument.");
        Validate.notNull(pomType, "Cannot handle null PomType argument.");

        // Do we have a required prefix and separator?
        if (requiredPrefix != null) {
            if (toValidate.getPrefix() == null) {
                throw new IllegalArgumentException("The [" + getClass().getName() + "] NamingStrategy requires the "
                        + "prefix [" + requiredPrefix + "]");
            }
            Validate.matchesPattern(toValidate.getPrefix(), requiredPrefix);
        }
        Validate.matchesPattern(toValidate.getSeparator(), "-");

        // Check the type for compliance.
        final Pattern pattern = validPatternMap.get(pomType);
        if (!pattern.matcher(toValidate.getType()).matches()) {
            throw new IllegalArgumentException("Invalid Name [" + toValidate + "] for PomType [" + pomType + "].");
        }
    }
}
