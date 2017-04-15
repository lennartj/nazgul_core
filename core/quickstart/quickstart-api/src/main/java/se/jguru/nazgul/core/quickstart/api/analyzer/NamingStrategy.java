/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.quickstart.api.analyzer;

import org.apache.maven.model.Model;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.model.Name;

/**
 * Specification for how to validate and build Names for use within project structures.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface NamingStrategy {

    /**
     * Builds a Name from the supplied Maven Model.
     *
     * @param model A non-null Maven model.
     * @return A Name created from the supplied Model.
     */
    Name createName(Model model);

    /**
     * Retrieves the PomType for the supplied Model.
     *
     * @param model A non-null Maven model.
     * @return The PomType corresponding to the supplied Maven Model.
     * @throws java.lang.IllegalArgumentException if a PomType could not be found from the supplied model.
     *                                            This implies that the Model did not follow the required
     *                                            naming patterns as implied by this NamingStrategy.
     */
    PomType getPomType(Model model) throws IllegalArgumentException;

    /**
     * Validates the supplied name, throwing an Exception if the validation failed.
     *
     * @param aName   The Name to validate.
     * @param pomType The type of POM which should be validated against the supplied Name.
     * @throws java.lang.IllegalArgumentException if the name was invalid for the supplied PomType,
     *                                            according to this NamingStrategy.
     */
    void validate(Name aName, PomType pomType) throws IllegalArgumentException;

    /**
     * Indicates if folders within a project must use names on the form {@code projectPrefix-projectName-folderName},
     * or if they instead must use the format {@code projectName-folderName}.
     *
     * @return {@code true} if all folders are required to have names on the form
     * {@code projectPrefix-projectName-folderName}. If {@code false}, then folder names should
     * be on the form {@code projectName-folderName} instead.
     */
    boolean isPrefixRequiredOnAllFolders();
}
