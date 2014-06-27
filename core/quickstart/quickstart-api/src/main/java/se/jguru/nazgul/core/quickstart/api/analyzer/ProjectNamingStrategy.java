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
package se.jguru.nazgul.core.quickstart.api.analyzer;

/**
 * Specification for how to validate project names and build relevant
 * project-structure names or paths.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ProjectNamingStrategy {

    /**
     * Checks if the supplied name is a valid project name.
     *
     * @param name The name to validate.
     * @return {@code true} if the supplied [project] name is valid.
     */
    boolean isValidProjectName(String name);

    /**
     * Retrieves the root directory name for the supplied project name.
     *
     * @param projectName The project name to use.
     * @return The name of the root directory for the given project name.
     * @throws IllegalArgumentException if the supplied projectName argument was invalid.
     * @see #isValidProjectName(String)
     */
    String getRootDirectoryName(String projectName) throws IllegalArgumentException;

    /**
     * Checks if the supplied projectPrefix is valid.
     *
     * @param projectPrefix The suggested project prefix.
     * @return {@code true} if the supplied projectPrefix is valid.
     */
    boolean isValidProjectPrefix(String projectPrefix);

    /**
     * Retrieves the top-level package for the supplied reverseOrganisationDNS (which, in itself,
     * must be a valid java package, such as "com.acme" or "org.foobar").
     *
     * @param reverseOrganisationDNS The reverse organisation DNS name which must be a valid java package,
     *                               such as "com.acme" or "org.foobar".
     * @return The top level package for the given reverseOrganisationDNS.
     * @throws IllegalArgumentException if the supplied reverseOrganisationDNS was invalid.
     * @see #isValidProjectPrefix(String)
     */
    String getTopLevelPackage(String reverseOrganisationDNS) throws IllegalArgumentException;

    /**
     * Checks if the combination of groupId/artifactId as well as parent groupId/artifactId indicates
     * a root reactor POM.
     *
     * @param parentGroupID    The parent groupId, as found within a POM.
     * @param parentArtifactID The parent artifactId, as found within a POM.
     * @param groupID          The groupId of a POM.
     * @param artifactID       The artifactId of a POM.
     * @return {@code true} if the supplied data indicates a root reactor pom.
     */
    boolean isRootReactorPom(String parentGroupID, String parentArtifactID, String groupID, String artifactID);

    /**
     * Checks if the combination of groupId/artifactId as well as parent groupId/artifactId indicates
     * a project parent POM.
     *
     * @param parentGroupID    The parent groupId, as found within a POM.
     * @param parentArtifactID The parent artifactId, as found within a POM.
     * @param groupID          The groupId of a POM.
     * @param artifactID       The artifactId of a POM.
     * @return {@code true} if the supplied data indicates a project parent pom.
     */
    boolean isProjectParentPom(String parentGroupID, String parentArtifactID, String groupID, String artifactID);

    /**
     * Retrieves the project name, if the supplied data yields {@code true} for a call to either
     * {@code isRootReactorPom} or {@code isProjectParentPom}.
     *
     * @param parentGroupID    The parent groupId, as found within a POM.
     * @param parentArtifactID The parent artifactId, as found within a POM.
     * @param groupID          The groupId of a POM.
     * @param artifactID       The artifactId of a POM.
     * @return The project name, if the supplied data is for a root reactor POM or a project parent POM.
     */
    String getProjectName(String parentGroupID, String parentArtifactID, String groupID, String artifactID);
}
