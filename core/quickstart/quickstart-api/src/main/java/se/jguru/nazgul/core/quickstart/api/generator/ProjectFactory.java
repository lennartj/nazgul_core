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

import se.jguru.nazgul.core.quickstart.model.Project;

/**
 * Specification for how to generate a new project where no project exists.
 * A project should contain root resources such as required POMs and directories where maven projects
 * or other components should be located.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ProjectFactory {

    /**
     * Creates a new Project using the information found within the supplied projectDefinition.
     *
     * @param projectDefinition The entity defining the Project's values, and holding naming standards.
     * @return {@code true} if the project could be properly created, and false otherwise.
     */
    boolean createProject(final Project projectDefinition);
}
