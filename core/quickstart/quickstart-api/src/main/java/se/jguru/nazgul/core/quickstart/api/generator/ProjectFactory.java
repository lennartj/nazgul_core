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

import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.File;

/**
 * Specification for how to generate a new project structure in an empty or non-existent directory.
 * A project should contain root resources such as required POMs and directories where maven projects
 * or other components should be located.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ProjectFactory {

    /**
     * Creates a Project (definition) by harvesting names from the supplied name,
     * prefix and parent Artifact definitions.
     *
     * @param prefix        The project prefix. Optional, but recommended.
     * @param name          The project name.
     * @param reactorParent The parent of the root reactor POM.
     * @param parentParent  The parent of the topmost parent POM.
     * @return A Project instance for use with the {@code createProject} method.
     */
    Project createProjectDefinition(String prefix,
                                    String name,
                                    SimpleArtifact reactorParent,
                                    SimpleArtifact parentParent);

    /**
     * Creates a new Project using the information found within the supplied projectDefinition.
     *
     * @param projectParentDir          a directory in which the project should be created.
     * @param projectDefinition         The entity defining the Project's overall data, and holding naming standards.
     * @param packagePrefix             A non-null (but optionally empty) package prefix prepended to the groupId of the
     *                                  reactor parent POM. Typically the reverse DNS of the organisation creating the
     *                                  project, such as {@code se.jguru}. (For example, the groupId of the topmost
     *                                  reactor POM created within the projectParentDir is
     *                                  {@code [packagePrefix].[name]}).
     * @param reactorParentMavenVersion The non-empty Maven project version of reactor POMs within the project.
     *                                  Should be identical to the version of the reactor parent POM.
     * @param topmostParentMavenVersion The non-empty Maven project version of parent POMs within the project (and,
     *                                  normally, the version of all leaf projects within the Maven project reactor).
     *                                  Should be identical to the version of the parent parent POM.
     * @return {@code true} if the project could be properly created, and false otherwise.
     * @throws java.lang.IllegalArgumentException if projectParentDir was not an existing directory.
     * @throws java.lang.IllegalStateException    if the projectRootDirectory (typically either on the form
     *                                            {@code prefix-name} or {@code name}) could not be created
     *                                            under the projectParentDir.
     */
    boolean createProject(File projectParentDir,
                          Project projectDefinition,
                          String packagePrefix,
                          String reactorParentMavenVersion,
                          String topmostParentMavenVersion)
            throws IllegalArgumentException, IllegalStateException;
}
