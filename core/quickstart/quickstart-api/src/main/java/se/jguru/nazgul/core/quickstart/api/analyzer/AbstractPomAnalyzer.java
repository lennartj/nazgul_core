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
package se.jguru.nazgul.core.quickstart.api.analyzer;

import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.model.Name;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract PomAnalyzer implementation, providing convenience methods for validating various
 * properties of Maven POMs. Override this for a concrete PomAnalyzer class which can be applied
 * along with a concrete NamingStrategy to analyze and validate the content of POMs for
 * compliance with a given strategy.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractPomAnalyzer implements PomAnalyzer {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractPomAnalyzer.class.getName());

    // Internal state
    private NamingStrategy namingStrategy;

    /**
     * Creates an AbstractPomAnalyzer using the supplied NamingStrategy to create Names from Models and
     * to validate the well-formedness of POMs.
     *
     * @param namingStrategy A non-null NamingStrategy used to validate Models.
     */
    protected AbstractPomAnalyzer(final NamingStrategy namingStrategy) {

        Validate.notNull(namingStrategy, "Cannot handle null namingStrategy argument.");
        this.namingStrategy = namingStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public final void validateRootReactorPom(final Model aRootReactorPom) throws InvalidStructureException {

        // Validate that the supplied Model complies with the naming strategy.
        final Name name = namingStrategy.createName(aRootReactorPom);
        try {
            namingStrategy.validate(name, PomType.ROOT_REACTOR);
        } catch (IllegalArgumentException e) {
            final StringBuilder builder = getErrorMessageBuilder("Incompatible POM name for strategy ["
                    + namingStrategy.getClass().getSimpleName() + "]: " + e.getMessage(), aRootReactorPom);
            throw new InvalidStructureException(builder.toString());
        }

        // Delegate
        performCustomRootReactorPomValidation(aRootReactorPom);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public final void validateTopmostParentPom(final Model topmostParentPom) throws InvalidStructureException {

        // Validate that the supplied Model complies with the naming strategy.
        final Name name = namingStrategy.createName(topmostParentPom);
        try {
            namingStrategy.validate(name, PomType.PARENT);
        } catch (IllegalArgumentException e) {
            final StringBuilder builder = getErrorMessageBuilder("Incompatible POM name for strategy ["
                    + namingStrategy.getClass().getSimpleName() + "]: " + e.getMessage(), topmostParentPom);
            throw new InvalidStructureException(builder.toString());
        }

        // Parent POMs should not define Modules
        verifyNoModules(topmostParentPom);

        // Delegate
        performCustomTopmostParentPomValidation(topmostParentPom);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public final void validate(final Model aPOM, final PomType expectedType, final Model parentOrNull) throws
            InvalidStructureException {

        // Check sanity
        Validate.notNull(aPOM, "Cannot handle null aPOM argument.");
        Validate.notNull(expectedType, "Cannot handle null expectedType argument.");

        // Validate that the supplied Model complies with the naming strategy.
        final Name name = namingStrategy.createName(aPOM);
        try {
            namingStrategy.validate(name, expectedType);
        } catch (IllegalArgumentException e) {
            final StringBuilder builder = getErrorMessageBuilder("Incompatible POM name for strategy ["
                    + namingStrategy.getClass().getSimpleName() + "]: " + e.getMessage(), aPOM);
            throw new InvalidStructureException(builder.toString());
        }

        switch (expectedType) {
            case ROOT_REACTOR:
                validateRootReactorPom(aPOM);
                break;

            case PARENT:
                // Delegate
                validateTopmostParentPom(aPOM);
                break;

            case API_PARENT:
            case MODEL_PARENT:
            case WAR_PARENT:
            case OTHER_PARENT:

                // Check sanity
                Validate.notNull(parentOrNull, "Cannot handle null parentOrNull argument for expected type ["
                        + expectedType + "].");

                // Delegate
                validateParentPom(aPOM, expectedType, parentOrNull);
                break;

            case REACTOR:

                // Check sanity
                Validate.notNull(parentOrNull, "Cannot handle null parentOrNull argument for expected type ["
                        + expectedType + "].");

                validateReactorPom(aPOM, parentOrNull);
                break;

            default:
                throw new UnsupportedOperationException("Cannot handle expectedType [" + expectedType + "]");
        }
    }

    /**
     * Verifies that the supplied toValidate Model does not contain any defined Modules, and
     * that the supplied nonNullParent has the same GAV coordinates as the supplied toValidate POM.
     *
     * @param toValidate   The POM model to validate.
     * @param expectedType The expected type of POM to validate the supplied Model against.
     * @param parentOrNull A Model which holds the expected Maven GAV coordinates for the aPOM's Parent POM.
     * @throws InvalidStructureException
     */
    protected void validateParentPom(final Model toValidate, final PomType expectedType, final Model parentOrNull)
            throws InvalidStructureException {

        // Parent POMs should not define Modules
        verifyNoModules(toValidate);

        // Is the supplied parentOrNull correct?
        if (parentOrNull != null) {
            verifyParentPom(toValidate, parentOrNull);
        }
    }

    /**
     * Verifies that the supplied toValidate Model does not contain any defined Modules, and
     * that the supplied nonNullParent has the same GAV coordinates as the supplied toValidate POM.
     *
     * @param toValidate    The POM model to validate.
     * @param nonNullParent A Model which holds the expected Maven GAV coordinates for the aPOM's Parent POM.
     * @throws InvalidStructureException
     */
    protected void validateReactorPom(final Model toValidate, final Model nonNullParent)
            throws InvalidStructureException {

        // Is the supplied nonNullParent correct?
        verifyParentPom(toValidate, nonNullParent);
    }

    /**
     * Override this method to perform custom validation of the Maven Model from the root reactor POM.
     * The NamingStrategy has already been used to validate maven GAV coordinates for the supplied root reactor
     * when this method is invoked.
     *
     * @param rootReactorModel the root rootReactorModel which should be validated in a custom way.
     * @throws InvalidStructureException if the supplied aRootReactorModel was invalid.
     */
    protected void performCustomRootReactorPomValidation(final Model rootReactorModel)
            throws InvalidStructureException {
        // Do nothing.
    }

    /**
     * Override this method to perform custom validation of the Maven Model from the topmost parent POM.
     * The NamingStrategy has already been used to validate maven GAV coordinates for the supplied topmost
     * parent POM when this method is invoked.
     *
     * @param topmostParentModel the root rootReactorModel which should be validated in a custom way.
     * @throws InvalidStructureException if the supplied topmostParentModel was invalid.
     */
    protected void performCustomTopmostParentPomValidation(final Model topmostParentModel)
            throws InvalidStructureException {
        // Do nothing.
    }

    //
    // Private helpers
    //

    /**
     * Verifies that the supplied toValidate Model has identical groupId, artifactId and version
     * of the supplied requiredParent Model.
     *
     * @param toVerify       The Model to verify.
     * @param requiredParent The Maven Model containing data of the required Parent for the supplied toVerify Model.
     * @throws InvalidStructureException if the GAV coordinates of the supplied toVerify Model did not match those of
     *                                   the supplied requiredParent Module.
     */
    protected final void verifyParentPom(final Model toVerify, final Model requiredParent)
            throws InvalidStructureException {

        final List<String> errors = new ArrayList<>();

        //
        // Note that the version of the toValidate project might be null
        // (in case the version is inherited from the parent, and therefore matching).
        //
        final Parent parentToValidate = toVerify.getParent();
        final boolean versionMatches = toVerify.getVersion() == null
                || requiredParent.getVersion().equals(parentToValidate.getVersion());
        if (!versionMatches) {
            errors.add("Required version mismatch (Required: " + requiredParent.getVersion() + ", Actual: "
                    + parentToValidate.getVersion() + ")");
        }
        if (!requiredParent.getGroupId().equals(parentToValidate.getGroupId())) {
            errors.add("Required groupId mismatch (Required: " + requiredParent.getGroupId() + ", Actual: "
                    + parentToValidate.getGroupId() + ")");
        }
        if (!requiredParent.getArtifactId().equals(parentToValidate.getArtifactId())) {
            errors.add("Required artifactId mismatch (Required: " + requiredParent.getArtifactId() + ", Actual: "
                    + parentToValidate.getArtifactId() + ")");
        }

        if (errors.size() > 0) {

            final StringBuilder builder = getErrorMessageBuilder("Incorrect parent relationship", toVerify);
            for (int i = 0; i < errors.size(); i++) {
                builder.append("\n " + (i + 1) + ") ").append(errors.get(i));
            }

            // All done.
            throw new InvalidStructureException(builder.toString());
        }
    }

    /**
     * Verifies that the supplied Model does not have any defined Modules.
     * This is a requirement for all Parent POMs, since they should be the direct parents of
     * artifact/leaf projects within a multi-module reactor.
     *
     * @param toVerify The Model to verify.
     * @throws InvalidStructureException if the supplied Model contained Module definitions.
     */
    protected void verifyNoModules(final Model toVerify) throws InvalidStructureException {
        final List<String> modules = toVerify.getModules();
        if (modules != null && modules.size() > 0) {

            final StringBuilder builder = getErrorMessageBuilder("POM should not contain Modules", toVerify);
            throw new InvalidStructureException(builder.toString());
        }
    }

    /**
     * Retrieves the expected suffix for an artifactID of a project Model with the supplied type.
     *
     * @param type The PomType of a project.
     * @return the expected suffix for an artifactID of a project Model with the supplied type, or {@code null}
     * if none could be provided. (This is the case for a OTHER_PARENT only).
     */
    protected String getExpectedArtifactIdSuffix(final PomType type) {

        // Check sanity
        Validate.notNull(type, "Cannot handle null type argument.");

        String toReturn = null;

        switch (type) {
            case ROOT_REACTOR:
            case REACTOR:
                toReturn = AbstractNamingStrategy.REACTOR_SUFFIX;
                break;

            case PARENT:
            case API_PARENT:
            case MODEL_PARENT:
            case WAR_PARENT:
                toReturn = type.name().toLowerCase().replace('_', '-');
                break;

            case OTHER_PARENT:
                toReturn = AbstractNamingStrategy.PARENT_SUFFIX;
                break;

            default:
                log.warn("PomType [" + type + "] not registered.");
                break;
        }

        // All done.
        return toReturn;
    }

    private StringBuilder getErrorMessageBuilder(final String mainError, final Model model) {

        // Check sanity
        Validate.notEmpty(mainError, "Cannot handle null or empty mainError argument.");
        Validate.notNull(model, "Cannot handle null model argument.");

        final String effectiveVersion = model.getVersion() == null
                ? model.getParent().getVersion()
                : model.getVersion();

        return new StringBuilder(mainError + " for POM (" + model.getGroupId() + "/" + model.getArtifactId()
                + "/" + effectiveVersion + "): ");
    }
}
