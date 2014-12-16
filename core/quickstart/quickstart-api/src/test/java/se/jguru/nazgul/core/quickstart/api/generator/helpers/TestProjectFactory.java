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
package se.jguru.nazgul.core.quickstart.api.generator.helpers;

import se.jguru.nazgul.core.quickstart.api.DefaultStructureNavigator;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.StructureNavigator;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestNamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestPomAnalyzer;
import se.jguru.nazgul.core.quickstart.api.generator.AbstractProjectFactory;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TestProjectFactory extends AbstractProjectFactory {

    // Shared state
    public StructureNavigator navigator;
    public List<String> callTrace;
    public String testdataSubDir = "test";

    public TestProjectFactory(final NamingStrategy namingStrategy) {
        super(namingStrategy);

        // Assign internal state
        navigator = new DefaultStructureNavigator(new TestNamingStrategy(), new TestPomAnalyzer());
        callTrace = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected URL getTemplateResourceURL(final String templateResourcePath) {

        callTrace.add("getTemplateResourceURL(" + templateResourcePath + ")");

        // Ensure that we check the testdata/directory.
        final String enrichedPath = "testdata/templates/" + testdataSubDir + "/" + templateResourcePath;
        return Thread.currentThread().getContextClassLoader().getResource(enrichedPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createProject(final File projectParentDir,
                                 final Project project,
                                 final String packagePrefix,
                                 final String reactorParentMavenVersion,
                                 final String topmostParentMavenVersion)
            throws IllegalArgumentException, IllegalStateException {

        callTrace.add("createProject");
        return super.createProject(projectParentDir,
                project,
                packagePrefix,
                reactorParentMavenVersion,
                topmostParentMavenVersion);
    }

    @Override
    public Project createProjectDefinition(final String prefix,
                                           final String name,
                                           final SimpleArtifact reactorParent,
                                           final SimpleArtifact parentParent) {
        callTrace.add("createProjectDefinition(" + prefix + ", " + name + ", "
                + reactorParent + ", " + "" + parentParent + ")");
        return super.createProjectDefinition(prefix, name, reactorParent, parentParent);
    }

    @Override
    public String getSkeletonPomDirectoryName(final String projectName, final PomType aPomType, final String projectPrefix) {
        callTrace.add("getSkeletonPomDirectoryName(" + projectName + ", " + aPomType + ", " + projectPrefix + ")");
        return super.getSkeletonPomDirectoryName(projectName, aPomType, projectPrefix);
    }

    @Override
    protected String getTemplateResource(final String templateResourcePath) {
        callTrace.add("getTemplateResource(" + templateResourcePath + ")");
        return super.getTemplateResource(templateResourcePath);
    }

    @Override
    protected FileFilter getShouldTokenizeFilter() {
        callTrace.add("getShouldTokenizeFilter");
        return super.getShouldTokenizeFilter();
    }
}
