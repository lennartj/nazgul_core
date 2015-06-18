/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.model.Name;

/**
 * Abstract NamingStrategy implementation which contains some standard method
 * implementations usable by all NamingStrategy subtypes.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractNamingStrategy implements NamingStrategy {

    /**
     * Standard suffix for Reactor POM artifactIds.
     */
    public static final String REACTOR_SUFFIX = "reactor";

    /**
     * Standard suffix for Parent POM artifactIds.
     */
    public static final String PARENT_SUFFIX = "parent";


    // Internal state
    private boolean prefixIsRequiredOnAllFolders;

    /**
     * Creates a new AbstractNamingStrategy wrapping the supplied data.
     *
     * @param prefixIsRequiredOnAllFolders {@code true} if all folders are required to have names on the form
     *                                     {@code projectPrefix-projectName-folderName}. If {@code false}, then folder
     *                                     names should be on the form {@code projectName-folderName} instead.
     */
    protected AbstractNamingStrategy(final boolean prefixIsRequiredOnAllFolders) {
        this.prefixIsRequiredOnAllFolders = prefixIsRequiredOnAllFolders;
    }

    /**
     * The default implementation in AbstractNamingStrategy simply creates a Name
     * by parsing the artifactId of the supplied model. (I.e. {@code return Name.parse(model.getArtifactId());}).
     * Override this method if you need other ways to synthesize a Name from the supplied Model.
     */
    @Override
    public Name createName(final Model model) {

        Validate.notNull(model, "Cannot handle null model argument.");
        return Name.parse(model.getArtifactId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PomType getPomType(final Model model) throws IllegalArgumentException {

        Validate.notNull(model, "Cannot handle null model argument.");
        final Name name = createName(model);
        final Parent parent = model.getParent();
        final String effectiveVersion = model.getVersion() == null ? parent.getVersion() : model.getVersion();
        PomType toReturn = null;

        if (name.getType().toLowerCase().endsWith(REACTOR_SUFFIX)) {
            toReturn = resolvePomTypeForReactorPom(model);
        }

        outer:
        if (toReturn == null) {
            for (PomType current : PomType.values()) {
                final String expectedSuffix = current.name().toLowerCase().replace("_", name.getSeparator());
                if (!expectedSuffix.contains(REACTOR_SUFFIX) && name.getType().equalsIgnoreCase(expectedSuffix)) {
                    toReturn = current;
                    break outer;
                }
            }

            if (isOtherParent(model)) {
                toReturn = PomType.OTHER_PARENT;
            }
        }

        if (toReturn == null) {
            throw new IllegalArgumentException("Could not define PomType for POM (" + model.getGroupId() + "/"
                    + model.getArtifactId() + "/" + effectiveVersion + ")");
        }

        // All done.
        validate(name, toReturn);
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrefixRequiredOnAllFolders() {
        return prefixIsRequiredOnAllFolders;
    }

    //
    // Private helpers
    //

    /**
     * Retrieves the PomType for a reactor POM with the supplied Model and Parent.
     * The AbstractNamingStrategy implementation assumes that the ROOT_REACTOR POM uses a parent outside
     * of its reactor and all other REACTOR POMs do not. This implies that the groupId of the
     * supplied parent can be used to determine if the Model is crafted from a ROOT_REACTOR POM,
     * according to the following algorithm:
     * {@code model.getGroupId().startsWith(parent.getGroupId())}
     * Override this method to provide custom logic if your ROOT_REACTOR and REACTOR POMs
     * are crafted in another way.
     *
     * @param model The (non-null) Maven Model of the reactor POM whose PomType should be created.
     * @return the PomType for a reactor POM with the supplied Model and Parent.
     */
    protected PomType resolvePomTypeForReactorPom(final Model model) {

        // Check sanity
        final Parent parent = model.getParent();
        Validate.notNull(parent, "POM (" + model.getGroupId() + "/" + model.getArtifactId() + "/"
                + model.getVersion() + ") must have a Parent definition.");

        // All done.
        return model.getGroupId().startsWith(parent.getGroupId())
                ? PomType.REACTOR
                : PomType.ROOT_REACTOR;
    }

    /**
     * Determines if the supplied model is a parent POM (but of an explicitly undefined type) or not.
     * The AbstractNamingStrategy implementation assumes that all parent POMs use a naming strategy that
     * implies that the last segment of the groupId for each parent POM is the end part of its corresponding
     * artifactId. For example:
     * <pre>
     *     <code>
     *         &lt;groupId&gt;se.jguru.nazgul.foo.poms.<strong>foo-api-parent</strong>&lt;/groupId&gt;
     *         &lt;artifactId&gt;nazgul-<strong>foo-api-parent</strong>&lt;/artifactId&gt;
     *     </code>
     * </pre>
     * Override this method to provide custom logic if your parent POMs are crafted in another way.
     *
     * @param model The Maven Model of a parent POM.
     * @return {@code true} if the supplied Model is to be considered a Parent POM.
     */
    protected boolean isOtherParent(final Model model) {

        // Find the last segment of the groupId.
        final String groupId = model.getGroupId();
        final int lastGroupIdSegmentIndex = Math.max(0, groupId.lastIndexOf(".") + 1);
        final String lastGroupIdSegment = groupId.substring(lastGroupIdSegmentIndex);

        // All done.
        return model.getArtifactId().endsWith(lastGroupIdSegment);
    }
}
