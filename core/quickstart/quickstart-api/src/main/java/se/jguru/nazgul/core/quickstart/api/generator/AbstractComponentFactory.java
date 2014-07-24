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

import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Model;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;
import se.jguru.nazgul.core.quickstart.api.StructureNavigator;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.model.Name;
import se.jguru.nazgul.core.quickstart.model.Project;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Abstract ComponentFactory implementation sporting utility methods and
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractComponentFactory extends AbstractFactory implements ComponentFactory {

    // Internal state
    private StructureNavigator navigator;

    /**
     * Creates a new AbstractComponentFactory wrapping the supplied NamingStrategy.
     *
     * @param structureNavigator A StructureNavigator used to navigate the project in which this
     *                           AbstractComponentFactory should create Software Components or SoftwareComponentParts.
     * @param namingStrategy     The active NamingStrategy, used to validate supplied data.
     */
    protected AbstractComponentFactory(final NamingStrategy namingStrategy,
                                       final StructureNavigator structureNavigator) {
        super(namingStrategy);

        // Assign internal state
        Validate.notNull(structureNavigator, "Cannot handle null structureNavigator argument.");
        this.navigator = structureNavigator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createSoftwareComponent(final File componentDirectory,
                                        final SortedMap<SoftwareComponentPart, String> parts2SuffixMap)
            throws InvalidStructureException {

        // Check sanity
        Validate.notNull(componentDirectory, "Cannot handle null componentDirectory argument.");
        Validate.notNull(parts2SuffixMap, "Cannot handle null parts2SuffixMap argument.");

        // Check sanity
        final Set<SoftwareComponentPart> parts = parts2SuffixMap.keySet();
        if (parts.contains(SoftwareComponentPart.IMPLEMENTATION)
                && !(parts.contains(SoftwareComponentPart.API) || parts.contains(SoftwareComponentPart.SPI))) {
            throw new InvalidStructureException("Software components containing an Implementation project should"
                    + "contain either an API or SPI project as well.");
        }
        for (Map.Entry<SoftwareComponentPart, String> current : parts2SuffixMap.entrySet()) {
            final String suffix = current.getValue();
            if (current.getKey().isSuffixRequired()) {
                Validate.notEmpty(suffix, "A suffix is required for SoftwareComponentPart [" + current.getKey().name()
                        + "], but none was given.");
            }
        }

        // Create the software component's directory.
        final File rootDir = navigator.getProjectRootDirectory(componentDirectory);
        final String relativePath = navigator.getRelativePath(componentDirectory, false);

        File componentDir = componentDirectory;
        if (!FileUtils.exists(componentDir, true)) {
            componentDir = FileUtils.makeDirectory(rootDir, relativePath);
        }
        Validate.isTrue(FileUtils.exists(componentDir, true), "Could not create directory ["
                + FileUtils.getCanonicalPath(componentDir) + "]");

        // Create each software component part project.
        for (Map.Entry<SoftwareComponentPart, String> current : parts2SuffixMap.entrySet()) {
            addSoftwareComponentPart(componentDir, current.getKey(), current.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSoftwareComponentPart(final File componentDirectory,
                                         final SoftwareComponentPart toAdd,
                                         final String suffix) throws InvalidStructureException {

        // Check sanity
        Validate.notNull(componentDirectory, "Cannot handle null componentDirectory argument.");
        Validate.notNull(toAdd, "Cannot handle null toAdd argument.");
        if (toAdd.isSuffixRequired()) {
            Validate.notEmpty(suffix,
                    "Cannot handle null or empty suffix argument for SoftwareComponentPart [" + toAdd + "].");
        }
        Validate.isTrue(componentDirectory.exists() && componentDirectory.isDirectory(),
                "Software Component Directory [" + FileUtils.getCanonicalPath(componentDirectory)
                        + "] was not an existing directory.");

        // Proceed to create the structures as instructed.
        final File rootDir = navigator.getProjectRootDirectory(componentDirectory);
        final String relativePath = navigator.getRelativePath(componentDirectory, false);
        final Model rootReactorPomModel = FileUtils.getPomModel(new File(rootDir, "pom.xml"));
        final Name rootReactorName = getNamingStrategy().createName(rootReactorPomModel);

        final File parentPomDir = navigator.getParentPomDirectory(componentDirectory);
        final Model parentPomModel = FileUtils.getPomModel(new File(parentPomDir, "pom.xml"));

        File componentDir = componentDirectory;
        if (!FileUtils.exists(componentDir, true)) {
            componentDir = FileUtils.makeDirectory(rootDir, relativePath);
        }

        // Create the part directory
        final String prefix = getNamingStrategy().isPrefixRequiredOnAllFolders() ? rootReactorName.getPrefix() : "";
        final Name partName = toAdd.createName(prefix, toAdd.getType(), suffix);
        final File partDir = FileUtils.makeDirectory(componentDir, partName.toString());

        // Synthesize the Project data.
        final Project projectData = new Project(
                rootReactorName.getPrefix(),
                rootReactorName.getName(),
                FileUtils.getSimpleArtifact(rootReactorPomModel),
                FileUtils.getSimpleArtifact(parentPomModel));

        // Create the part POM, and write it appropriately.
        final String partPomData = getPom(toAdd.getComponentPomType(),
                relativePath + StructureNavigator.DIRECTORY_SEPARATOR + partDir.getName(),
                projectData);
        FileUtils.writeFile(new File(partDir, "pom.xml"), partPomData);
    }

    protected void addStandardProjectStructure(final File projectDirectory) {

        // Create the following structure.
    }
}
