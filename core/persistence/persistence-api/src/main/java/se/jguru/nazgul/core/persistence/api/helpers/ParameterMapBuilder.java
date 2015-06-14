/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.jguru.nazgul.core.persistence.api.helpers;

import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility builder class intended to simplify creation of a NamedQuery parameter Map.
 * Typical Usage:<br/>
 * <pre>
 *     <code>
 *         // Create the ParameterMapBuilder
 *         Map&lt;String, Object&gt; parameters = ParameterMapBuilder
 *              .with("foo", "bar")
 *              .and("gnat", 42)
 *              .and("someData", 25.6d)
 *              .build();
 *     </code>
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class ParameterMapBuilder {

    // Internal state
    private Map<String, Object> namedJpaQueryParameters;

    /**
     * Private constructor in a utility class.
     *
     * @param name  The name of the first NamedQuery parameter.
     * @param value The value of the first NamedQuery parameter.
     */
    private ParameterMapBuilder(final String name, final Object value) {

        // Assign internal state
        namedJpaQueryParameters = new HashMap<String, Object>();
        namedJpaQueryParameters.put(name, value);
    }

    /**
     * Creates the ParameterMapBuilder with the first parameter having the
     * supplied name/key and value. Usage:<br/>
     * <pre>
     *     <code>
     *         // Create the ParameterMapBuilder
     *         Map&lt;String, Object&gt; parameters = ParameterMapBuilder
     *              .with("foo", "bar")
     *              .and("gnat", 42)
     *              .build();
     *     </code>
     * </pre>
     *
     * @param name  The name of the first parameter.
     * @param value The value of the first parameter.
     * @return The ParameterMapBuilder instance used to construct the Parameter map.
     */
    public static ParameterMapBuilder with(final String name, final Object value) {

        // Check sanity
        Validate.notNull(name, "Cannot handle null name argument.");
        Validate.notEmpty(name.trim(), "Cannot handle empty or whitespace name argument.");

        // All done.
        return new ParameterMapBuilder(name, value);
    }

    /**
     * Adds the supplied name/key to value parameter to this ParameterMapBuilder.
     * <p/>
     * Usage:<br/>
     * <pre>
     *     <code>
     *         // Create the ParameterMapBuilder
     *         Map&lt;String, Object&gt; parameters = ParameterMapBuilder
     *              .with("foo", "bar")
     *              .and("gnat", 42)
     *              .build();
     *     </code>
     * </pre>
     *
     * @param name  The name of the parameter to add.
     * @param value The value of the parameter to add.
     * @return This ParameterMapBuilder.
     */
    public ParameterMapBuilder and(final String name, final Object value) {

        // Check sanity
        Validate.notNull(name, "Cannot handle null name argument.");
        Validate.notEmpty(name.trim(), "Cannot handle empty or whitespace name argument.");

        // All done
        this.namedJpaQueryParameters.put(name, value);
        return this;
    }

    /**
     * Retrieves the Map holding name/key to value parameters from this ParameterMapBuilder.
     * <p/>
     * Usage:<br/>
     * <pre>
     *     <code>
     *         // Create the ParameterMapBuilder
     *         Map&lt;String, Object&gt; parameters = ParameterMapBuilder
     *              .with("foo", "bar")
     *              .and("gnat", 42)
     *              .build();
     *     </code>
     * </pre>
     *
     * @return the Map holding name/key to value parameters from this ParameterMapBuilder.
     */
    public Map<String, Object> build() {
        return this.namedJpaQueryParameters;
    }
}
