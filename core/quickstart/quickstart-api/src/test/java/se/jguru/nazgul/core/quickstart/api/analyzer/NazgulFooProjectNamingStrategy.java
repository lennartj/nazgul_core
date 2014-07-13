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
/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.quickstart.api.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.api.StructureNavigator;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulFooProjectNamingStrategy extends AbstractPatternBasedProjectNamingStrategy {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(NazgulFooProjectNamingStrategy.class.getName());

    public static final String REACTOR_SUFFIX = "-reactor";
    public static final String PARENT_SUFFIX = "-parent";

    public static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("[a-z][a-z0-9]*", Pattern.CANON_EQ);
            // "[a-z][a-z0-9]*(-[a-z0-9]+)?", Pattern.CANON_EQ);

    public static final Pattern PROJECT_PREFIX_PATTERN = Pattern.compile("[a-z][a-z0-9]*", Pattern.CANON_EQ);
            // "[a-z][a-z0-9]*([.-][a-z0-9]+)*", Pattern.CANON_EQ);

    public NazgulFooProjectNamingStrategy() {
        super(PROJECT_NAME_PATTERN.pattern(), PROJECT_PREFIX_PATTERN.pattern());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createTopLevelPackageFrom(final String reverseOrganisationDNS) {
        return reverseOrganisationDNS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createRootDirectoryNameFrom(final String validProjectName) {

        if(validProjectName.contains(StructureNavigator.PACKAGE_SEPARATOR)) {
            return validProjectName.substring(validProjectName.lastIndexOf(StructureNavigator.PACKAGE_SEPARATOR) + 1);
        }

        return validProjectName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProjectPrefix(final boolean isReactorData,
                                      final String parentGroupID,
                                      final String parentArtifactID,
                                      final String groupID,
                                      final String artifactID) {

        final String toReturn = artifactID.substring(0, artifactID.indexOf("-"));

        log.info("isReactorData [" + isReactorData + "], parentGroupID [" + parentGroupID
                + "], parentArtifactID [" + parentArtifactID + "], groupID [" + groupID
                + "], artifactID [" + artifactID + "] ==> [" + toReturn + "]");
        return toReturn;
    }

    /**
     * {@code}
     */
    @Override
    protected String getProjectName(final boolean isReactorData,
                                    final String parentGroupID,
                                    final String parentArtifactID,
                                    final String groupID,
                                    final String artifactID) {

        /*
        <parent>
        <groupId>se.jguru.nazgul.tools.poms.external</groupId>
        <artifactId>nazgul-tools-external-reactor-parent</artifactId>
        <version>4.0.0</version>
        <relativePath />
    </parent>

    <groupId>se.jguru.nazgul.core</groupId>
    <artifactId>nazgul-core-reactor</artifactId>
         */

        final String projectPrefix = getProjectPrefix(isReactorData,
                parentGroupID, parentArtifactID,
                groupID, artifactID);

        final int prefixEndIndex = artifactID.lastIndexOf(projectPrefix) + projectPrefix.length() + 1;

        final String toReturn = isReactorData
                ? artifactID.substring(prefixEndIndex, artifactID.lastIndexOf(REACTOR_SUFFIX))
                : artifactID.substring(prefixEndIndex, artifactID.lastIndexOf(PARENT_SUFFIX));

        log.info("isReactorData [" + isReactorData + "], parentGroupID [" + parentGroupID
                + "], parentArtifactID [" + parentArtifactID + "], groupID [" + groupID
                + "], artifactID [" + artifactID + "] ==> [" + toReturn + "]");

        return toReturn;
    }
}
