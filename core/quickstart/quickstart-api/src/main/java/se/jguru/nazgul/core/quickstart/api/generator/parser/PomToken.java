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
package se.jguru.nazgul.core.quickstart.api.generator.parser;

import org.apache.commons.lang3.Validate;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Enumeration of a set of standardized tokens normally usable within a Maven POM.
 * All supplied tokens relate to typical properties used within POMs.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public enum PomToken {

    /**
     * The groupId for the active project.
     */
    GROUPID("groupId"),

    /**
     * The artifactId for the active project.
     */
    ARTIFACTID("artifactId"),

    /**
     * The maven version for the active project.
     */
    VERSION("mavenVersion"),

    /**
     * The relative path from the VCS project root to the active maven project.
     */
    RELATIVE_DIRPATH("relativeDirPath"),

    /**
     * The relative path from the VCS project root to the active maven project, with all '/' path
     * delimiters replaced with '.'.
     */
    RELATIVE_PACKAGE("relativePackage"),

    /**
     * The groupId of the parent to the active maven project.
     */
    PARENT_GROUPID("parentGroupId"),

    /**
     * The artifactId of the parent to the active maven project.
     */
    PARENT_ARTIFACTID("parentArtifactId"),

    /**
     * The maven version of the parent to the active maven project.
     */
    PARENT_VERSION("parentMavenVersion"),

    /**
     * The relative path between the active maven project and its parent POM.
     */
    PARENT_POM_RELATIVE_PATH("parentPomRelativePath"),

    /**
     * The fully populated modules XML element, usable only in REACTOR and ROOT_REACTOR poms.
     */
    MODULES("modules");

    // Internal state
    private String token;

    PomToken(final String token) {
        this.token = token;
    }

    /**
     * @return The standard token string to be used within the FactoryParserAgent.
     */
    public String getToken() {
        return token;
    }

    /**
     * Retrieves a SortedMap containing the {@code getToken()} values of each StandardToken as keys and null values.
     *
     * @return a SortedMap containing the {@code getToken()} values of each StandardToken as keys and null values.
     */
    public static MapBuilder create() {

        // All done.
        return new MapBuilder();
    }

    /**
     * Builder class to simplify creating a SortedMap whose keys contains the tokens of each StandardToken.
     * This Map can be used for token replacements in text template files.
     */
    static class MapBuilder {

        // State
        private SortedMap<String, String> map;

        MapBuilder() {
            map = new TreeMap<>();
            for (PomToken current : PomToken.values()) {
                map.put(current.getToken(), null);
            }
        }

        public SortedMap<String, String> build() {
            return map;
        }

        /**
         * Adds a token/value pair to the MapBuilder map, and returns this builder for
         * a standard build pattern.
         *
         * @param token The non-null StandardToken for which the token/value entry should be added.
         * @param value The non-null value to add.
         * @return The {@code map} object.
         */
        public MapBuilder addToken(final PomToken token, final String value) {

            // Check sanity
            Validate.notNull(token, "Cannot handle null token argument.");
            Validate.notNull(value, "Cannot handle null value argument.");

            // Add the token value pair, and return for chaining.
            map.put(token.getToken(), value);
            return this;
        }
    }
}
