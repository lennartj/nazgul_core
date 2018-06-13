/*-
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
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

package se.jguru.nazgul.core.quickstart.api.generator;

import se.jguru.nazgul.core.quickstart.api.PomType;

/**
 * Definition of all known SoftwareComponent parts, implying the different
 * Maven projects which together create a SoftwareComponent.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public enum SoftwareComponentPart {

    /**
     * The model SoftwareComponentPart.
     */
    MODEL("model", false, PomType.COMPONENT_MODEL),

    /**
     * The API SoftwareComponentPart.
     */
    API("api", false, PomType.COMPONENT_API),

    /**
     * The optional SPI SoftwareComponentPart. Requires a suffix.
     */
    SPI("spi", true, PomType.COMPONENT_SPI),

    /**
     * The optional Implementation SoftwareComponentPart. Requires a suffix.
     */
    IMPLEMENTATION("impl", true, PomType.COMPONENT_IMPLEMENTATION);

    // Internal state
    private PomType componentPomType;
    private String type;
    private boolean requiresSuffix;

    SoftwareComponentPart(final String type,
                          final boolean requiresSuffix,
                          final PomType componentPomType) {
        this.type = type;
        this.requiresSuffix = requiresSuffix;
        this.componentPomType = componentPomType;
    }

    /**
     * @return The type segment of a Name, constructed to match this SoftwareComponentPart.
     */
    public String getType() {
        return type;
    }

    /**
     * @return The standard component PomType for this SoftwareComponentPart.
     */
    public PomType getComponentPomType() {
        return componentPomType;
    }

    /**
     * @return {@code true} if this SoftwareComponentPart requires a suffix, and {@code false} otherwise.
     * A suffix is used in creating a Name (typically for Implementation and SPI projects) where multiple
     * implementations can occur. Example: "nazgul-core-quickstart-impl-<strong>nazgul</strong>"
     * or "acme-configuration-spi-<strong>jdbc</strong>".
     */
    public boolean isSuffixRequired() {
        return requiresSuffix;
    }
}
