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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.api.analyzer.AbstractNamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.model.Name;
import se.jguru.nazgul.core.quickstart.model.Project;

/**
 * Abstract utility implementations for all Factory classes.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractFactory {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractFactory.class.getName());

    // Internal state
    private NamingStrategy namingStrategy;

    /**
     * Creates a new AbstractFactory using the supplied NamingStrategy.
     *
     * @param namingStrategy The active NamingStrategy, used to validate supplied data.
     */
    protected AbstractFactory(final NamingStrategy namingStrategy) {
        Validate.notNull(namingStrategy, "Cannot handle null namingStrategy argument.");
        this.namingStrategy = namingStrategy;
    }

    /**
     * Synthesizes the directory name from the supplied Project and PomType.
     * The resulting name should adhere to the NamingStrategy's
     * {@code NamingStrategy.isPrefixRequiredOnAllFolders()} property.
     *
     * @param projectName   The name of the active project.
     * @param projectPrefix The optional prefix of the active project. Must be supplied (and non-empty) if
     *                      the NamingStrategy requires prefixes to be present on all folders' names.
     * @param aPomType      The PomType for which a directory name should be synthesized.
     * @return The name of the directory.
     */
    protected String getDirectoryName(final String projectName, final PomType aPomType, final String projectPrefix) {

        if (namingStrategy.isPrefixRequiredOnAllFolders() && (projectPrefix == null || projectPrefix.isEmpty())) {
            throw new IllegalArgumentException("NamingStrategy [" + namingStrategy.getClass().getSimpleName()
                    + "] requires that project prefix is required on all folders. Therefore, "
                    + "a nonempty projectPrefix argument must be supplied.");
        }

        // Reactor directories should only be called their respective component name.
        if(aPomType == PomType.ROOT_REACTOR || aPomType == PomType.REACTOR) {
            return namingStrategy.isPrefixRequiredOnAllFolders()
                    ? projectPrefix : "";
        }


        final String dirPrefix = (namingStrategy.isPrefixRequiredOnAllFolders()
                ? projectPrefix + Name.DEFAULT_SEPARATOR + projectName
                : projectName)
                + Name.DEFAULT_SEPARATOR;

        String dirSuffix = aPomType.name().toLowerCase().replace("_", "-");
        if (aPomType == PomType.OTHER_PARENT) {
            dirSuffix = AbstractNamingStrategy.PARENT_SUFFIX;
        }

        final String toReturn = dirPrefix + dirSuffix;
        if (log.isDebugEnabled()) {
            log.debug("PomType [" + aPomType + "] yielded name [" + toReturn + "]");
        }

        // All done.
        return toReturn;
    }

    /**
     * Retrieves the active NamingStrategy used by this AbstractFactory.
     *
     * @return the active NamingStrategy used by this AbstractFactory.
     */
    protected final NamingStrategy getNamingStrategy() {
        return this.namingStrategy;
    }

    /**
     * Acquires the data within the POM given by the supplied data.
     * The resulting String should be correctly formed data of the POM (i.e. fully tokenized if applicable).
     *
     * @param pomType         The type of POM whose data should be retrieved.
     * @param relativeDirPath The directory path (relative to the project root directory) of the POM to retrieve.
     * @param project         The active Project, from which tokens could be retrieved.
     * @return correctly formed data of the POM (i.e. fully tokenized if applicable).
     */
    protected abstract String getPom(final PomType pomType, final String relativeDirPath, final Project project);
}
