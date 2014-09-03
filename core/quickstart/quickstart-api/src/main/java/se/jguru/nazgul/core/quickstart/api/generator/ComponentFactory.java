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
package se.jguru.nazgul.core.quickstart.api.generator;

import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;

import java.io.File;
import java.util.SortedMap;

/**
 * Specification for how to create a new Software Component, which consists of several collaborating Maven
 * projects within the same reactor.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ComponentFactory {

    /**
     * Creates a new SoftwareComponent containing the supplied parts in the given componentDirectory.
     *
     * @param componentDirectory An non-existent or empty directory where a SoftwareComponent should be
     *                           created. The directory name will be identical to the SoftwareComponent name,
     *                           and must therefore be compliant with the NamingStrategy used.
     * @param parts2SuffixMap    A non-empty Map relating SoftwareComponentParts to be created within the
     *                           SoftwareComponent to their respective suffix. A non-empty value is only required
     *                           within the Map if {@code SoftwareComponentPart.isSuffixRequired()} yields {@code true}.
     * @throws InvalidStructureException If {@code componentDirectory} already contained a POM,
     *                                   or was not compliant with the NamingStrategy chosen. Also thrown if the parts
     *                                   list is empty or holds an Implementation SoftwareComponentPart but neither
     *                                   API nor SPI SoftwareComponentParts.
     */
    void createSoftwareComponent(File componentDirectory,
                                 SortedMap<SoftwareComponentPart, String> parts2SuffixMap)
            throws InvalidStructureException;

    /**
     * Adds a SoftwareComponentPart to the SoftwareComponent found within the supplied componentDirectory.
     *
     * @param componentDirectory A directory containing an existing SoftwareComponent
     *                           (i.e. containing the reactor POM of the SoftwareComponent).
     * @param toAdd              The SoftwareComponentPart to add to the supplied SoftwareComponent.
     * @param suffix             The suffix of the SoftwareComponent's type. Ignored unless
     *                           {@code toAdd.isSuffixRequired()} yields {@code true}.
     * @throws InvalidStructureException if the supplied componentDirectory did not contain a SoftwareComponent,
     *                                   or if the toAdd SoftwareComponentPart was already present within
     *                                   the {@code componentDirectory} directory.
     * @see SoftwareComponentPart#isSuffixRequired()
     */
    void addSoftwareComponentPart(File componentDirectory,
                                  SoftwareComponentPart toAdd,
                                  String suffix)
            throws InvalidStructureException;
}
