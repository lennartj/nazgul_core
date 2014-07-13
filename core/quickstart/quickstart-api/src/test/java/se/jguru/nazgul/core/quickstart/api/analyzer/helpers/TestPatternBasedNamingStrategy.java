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
package se.jguru.nazgul.core.quickstart.api.analyzer.helpers;

import se.jguru.nazgul.core.quickstart.api.analyzer.AbstractPatternBasedProjectNamingStrategy;

import java.util.StringTokenizer;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TestPatternBasedNamingStrategy extends AbstractPatternBasedProjectNamingStrategy {

    @Override
    protected String createRootDirectoryNameFrom(final String validProjectName) {
        return validProjectName;
    }

    @Override
    protected String createTopLevelPackageFrom(final String reverseOrganisationDNS) {
        return reverseOrganisationDNS;
    }

    @Override
    protected String getProjectName(final boolean isReactorData,
                                    final String parentGroupID,
                                    final String parentArtifactID,
                                    final String groupID,
                                    final String artifactID) {

        // Project name "foo" should be returned ...
        //
        // ... from root reactor pom data:
        // <groupId>se.jguru.nazgul.foo</groupId>
        // <artifactId>nazgul-foo-reactor</artifactId>
        //
        // ... from topmost parent pom data:
        // <groupId>se.jguru.nazgul.foo.poms.foo-parent</groupId>
        // <artifactId>nazgul-foo-parent</artifactId>

        final String suffix = isReactorData ? "reactor" : "parent";

        if(!artifactID.endsWith(suffix)) {
            throw new IllegalArgumentException("Project name could not be determined from [" + artifactID + "]");
        }

        String lastWord = "";
        StringTokenizer tok = new StringTokenizer(artifactID, "-", false);
        while(tok.hasMoreTokens()) {
            String current = tok.nextToken();
            if(current.equalsIgnoreCase(suffix)) {
                break;
            }
            lastWord = current;
        }

        return lastWord;
    }

    @Override
    protected String getProjectPrefix(final boolean isReactorData,
                                      final String parentGroupID,
                                      final String parentArtifactID,
                                      final String groupID,
                                      final String artifactID) {

        // Project prefix "nazgul" should be returned ...
        //
        // ... from root reactor pom data:
        // <groupId>se.jguru.nazgul.foo</groupId>
        // <artifactId>nazgul-foo-reactor</artifactId>
        //
        // ... from topmost parent pom data:
        // <groupId>se.jguru.nazgul.foo.poms.foo-parent</groupId>
        // <artifactId>nazgul-foo-parent</artifactId>

        final String suffix = isReactorData ? "reactor" : "parent";

        if(!artifactID.endsWith(suffix) || !artifactID.contains("-")) {
            throw new IllegalArgumentException("Project name could not be determined from [" + artifactID + "]");
        }

        return artifactID.substring(0, artifactID.indexOf("-"));
    }
}
