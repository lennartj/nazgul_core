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
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockProjectNamingStrategy extends AbstractPatternBasedProjectNamingStrategy {

    public MockProjectNamingStrategy(final String projectNamePattern,
                                     final String projectPrefixPattern) {
        super(projectNamePattern, projectPrefixPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createRootDirectoryNameFrom(final String validProjectName) {
        return "RootDirectoryName_" + validProjectName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createTopLevelPackageFrom(final String reverseOrganisationDNS) {
        return "TopLevelPackage_" + reverseOrganisationDNS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProjectName(final boolean isReactorData,
                                    final String parentGroupID,
                                    final String parentArtifactID,
                                    final String groupID,
                                    final String artifactID) {

        return artifactID.substring(0, artifactID.indexOf("-"));
    }
}
