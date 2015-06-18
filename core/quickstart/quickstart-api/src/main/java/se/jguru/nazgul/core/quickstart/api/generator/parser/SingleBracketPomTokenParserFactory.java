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
package se.jguru.nazgul.core.quickstart.api.generator.parser;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.parser.api.DefaultTokenParser;
import se.jguru.nazgul.core.parser.api.SingleBracketTokenDefinitions;
import se.jguru.nazgul.core.parser.api.TokenParser;
import se.jguru.nazgul.core.parser.api.agent.DefaultParserAgent;
import se.jguru.nazgul.core.parser.api.agent.HostNameParserAgent;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.generator.SoftwareComponentPart;
import se.jguru.nazgul.core.quickstart.model.Name;
import se.jguru.nazgul.core.quickstart.model.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Builder class to simplify creating a TokenParser tailored for maven POM and project data token substitution.
 * The returned TokenParsers will use {@code SingleBracketTokenDefinitions} for token definitions, to avoid confusing
 * the parser tokens with maven's default variable form. (I.e. it will use tokens on the form [token] to
 * avoid clashing with ${token}).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class SingleBracketPomTokenParserFactory {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(SingleBracketPomTokenParserFactory.class.getName());

    /**
     * The key for the token holding the PomType verbatim.
     */
    public static final String POMTYPE_KEY = "POMTYPE";

    /**
     * The key for the token holding the {@code pomType.name().toLowerCase().replace("_", "-")} value.
     */
    public static final String LOWERCASE_POMTYPE_KEY = "pomtype";

    /**
     * The name of the directory containing all parent POM projects.
     */
    public static final String POMS_DIRECTORY_NAME = "poms";

    /*
     * Hide factory class constructors.
     */
    private SingleBracketPomTokenParserFactory() {
        // Do nothing
    }

    /**
     * Factory builder method entry method, creating the internal Builder and assigning the supplied tokens.
     *
     * @param pomType The POM type for which this SingleBracketPomTokenParserFactory should create a TokenParser.
     * @param project The non-null Project used by this SingleBracketPomTokenParserFactory to derive tokens for the
     *                created compound TokenParser.
     * @return The Builder used to create the TokenParser.
     */
    public static DirectoryPrefixEnricher create(final PomType pomType,
                                                 final Project project) {

        // Check sanity
        Validate.notNull(pomType, "Cannot handle null pomType argument.");
        Validate.notNull(project, "Cannot handle null project argument.");

        // Derive the project properties.
        final SortedMap<String, String> pomTokens = new TreeMap<>();
        pomTokens.put(POMTYPE_KEY, pomType.name());
        pomTokens.put(LOWERCASE_POMTYPE_KEY, pomType.name().toLowerCase().replace("_", "-"));

        // All done.
        return new DirectoryPrefixEnricher(project, pomType, pomTokens, false);
    }

    /**
     * Abstract specification for a BuilderStep class in the controlled Builder pattern.
     */
    abstract static class BuilderStep {

        // Internal state
        private boolean parentPomType;
        private boolean requiresProjectSuffix = false;
        private boolean addProjectNameAsDirectoryPrefix;
        protected Project project;
        protected PomType pomType;
        protected SortedMap<String, String> pomTokens;

        /**
         * Creates a new BuilderStep wrapping the supplied data. Each can be {@code null}.
         *
         * @param project                         The Project of this BuilderStep.
         * @param pomType                         The POM type of this BuilderStep.
         * @param pomTokens                       The pomTokens storage.
         * @param addProjectNameAsDirectoryPrefix set to {@code true} if directory names and artifactIDs should
         *                                        add/prepend the project name as prefix
         *                                        (i.e. yield {@code project.getName()-} prepended to directory names).
         */
        protected BuilderStep(final Project project,
                              final PomType pomType,
                              final SortedMap<String, String> pomTokens,
                              final boolean addProjectNameAsDirectoryPrefix) {

            this.project = project;
            this.pomType = pomType;
            this.pomTokens = pomTokens;
            this.addProjectNameAsDirectoryPrefix = addProjectNameAsDirectoryPrefix;

            // Check sanity
            if (pomType != null) {

                for (SoftwareComponentPart current : SoftwareComponentPart.values()) {
                    if (pomType == current.getComponentPomType()) {
                        requiresProjectSuffix = current.isSuffixRequired();
                    }
                }

                parentPomType = pomType.name().contains("PARENT");
            }
        }

        /**
         * Validates that the given key is present within the pomTokens,
         * and that it has a non-null value.
         *
         * @param key The token key.
         */
        protected void validateRequiredKeyValuePair(final String key) {

            // Check sanity
            Validate.isTrue(pomTokens.containsKey(key), "Required key [" + key + "] not present in tokenMap.");
            Validate.notEmpty(pomTokens.get(key),
                    "Cannot handle null or empty value for key [" + key + "] in tokenMap.");
        }

        /**
         * @return {@code} true if this Builder should have a project suffix, due to the PomType.
         */
        protected final boolean isProjectSuffixRequired() {
            return requiresProjectSuffix;
        }

        /**
         * @return {@code true} if the PomType of this BuilderStep is a Parent POM (as opposed to a Component POM).
         */
        protected final boolean isParentPomType() {
            return parentPomType;
        }

        /**
         * @return {@code true} if this BuilderStep should add prefixes with the Project's name
         * to local project directory names. This means that SoftwareComponent directories should be
         * given names on the form {@code foo-finance-api} instead of {@code finance-api},
         * given that the project's name is {@code foo} and the SoftwareComponent's {@code finance}.
         */
        protected boolean isProjectNameAddedToDirectories() {
            return addProjectNameAsDirectoryPrefix;
        }

        /**
         * Converts the supplied path to a List of segments, assuming that the path uses '/' chars to separate
         * directory names. Returning a List containing all directory names (i.e. path parts)
         * in their order of occurrence.
         *
         * @param path The path to tokenize, assumed to use '/' as separator chars.
         * @return A List whose elements contain the directory names within the supplied path.
         */
        protected List<String> tokenize(final String path) {

            final List<String> toReturn = new ArrayList<>();
            if (path != null) {
                final StringTokenizer tok = new StringTokenizer(path, "/", false);
                while (tok.hasMoreTokens()) {
                    toReturn.add(tok.nextToken());
                }
            }
            return toReturn;
        }
    }

    /**
     * Class which defines whether or not the name of the Project should be added as a prefix to
     * SoftwareComponent directories when their names are synthesized. Given a SoftwareComponent
     * {@code configuration} in a Project with the name {@code foo}, its API project directory name is
     * <ul>
     * <li>{@code configuration-api} if this DirectoryPrefixEnricher is given a false argument
     * (or the method {@code withoutProjectNameAsDirectoryPrefix()} is called.</li>
     * <li>{@code foo-configuration-api} if this DirectoryPrefixEnricher is given a true argument
     * (or the method {@code useProjectNameAsDirectoryPrefix()} is called.</li>
     * </ul>
     */
    public static class DirectoryPrefixEnricher extends BuilderStep {

        public DirectoryPrefixEnricher(final Project project,
                                       final PomType pomType,
                                       final SortedMap<String, String> pomTokens,
                                       final boolean addProjectNameAsDirectoryPrefix) {
            super(project, pomType, pomTokens, addProjectNameAsDirectoryPrefix);
        }

        /**
         * Indicate if the Project's name should be prepended as a prefix to software component directory names.
         * Given a SoftwareComponent {@code configuration} in a Project with the name {@code foo},
         * its API project directory name is
         * <ul>
         * <li>{@code configuration-api} if this method is called with the argument {@code false}.</li>
         * <li>{@code foo-configuration-api} if this method is called with the argument {@code true}.</li>
         * </ul>
         *
         * @param yes true to indicate that the project name should be added as a prefix to SoftwareComponent
         *            directories, and false otherwise.
         * @return The next BuilderStep subtype in the builder chain.
         */
        public RelativePathEnricher prependProjectNameAsDirectoryPrefix(final boolean yes) {
            return new RelativePathEnricher(project, pomType, pomTokens, yes);
        }

        /**
         * Convenience method calling {@code return prependProjectNameAsDirectoryPrefix(true);}
         *
         * @return The next BuilderStep subtype in the builder chain.
         * @see #prependProjectNameAsDirectoryPrefix(boolean)
         */
        public RelativePathEnricher useProjectNameAsDirectoryPrefix() {
            return prependProjectNameAsDirectoryPrefix(true);
        }

        /**
         * Convenience method calling {@code return prependProjectNameAsDirectoryPrefix(false);}
         *
         * @return The next BuilderStep subtype in the builder chain.
         * @see #prependProjectNameAsDirectoryPrefix(boolean)
         */
        public RelativePathEnricher withoutProjectNameAsDirectoryPrefix() {
            return prependProjectNameAsDirectoryPrefix(false);
        }
    }

    /**
     * Class which adds the relative path to a SoftwareComponent, as well as temporary pomTokens
     * {@code SOFTWARE_COMPONENT_RELATIVE_PATH} and {@code SOFTWARE_COMPONENT_NAME}.
     * Adds the relative path to the reactor of the SoftwareComponent in which the active Maven project
     * which should be tokenized by the resulting TokenParser resides. The reactor for a SoftwareComponentPart project
     * is its parent directory. (I.e. for the {@code finance-api} project residing within the SoftwareComponent
     * {@code finance}, the relative path should be {@code services/finance} assuming that we would like the
     * finance SoftwareComponent to reside in the {@code services} VCS path).
     */
    public static class RelativePathEnricher extends BuilderStep {

        /**
         * The pomTokens key holding the relative path to the SoftwareComponent containing the active Maven project.
         */
        public static final String SOFTWARE_COMPONENT_RELATIVE_PATH = "softwareComponentRelativePath";

        /**
         * The pomTokens key holding the relative name of the SoftwareComponent containing the active Maven project.
         */
        public static final String SOFTWARE_COMPONENT_NAME = "softwareComponentName";

        /**
         * The string name of the PomType for the parent of the active Maven project.
         * (I.e. {@code parentPomType.name()}).
         */
        public static final String PARENT_POMTYPE = "parent_pomType";

        RelativePathEnricher(final Project project,
                             final PomType pomType,
                             final SortedMap<String, String> pomTokens,
                             final boolean addProjectNameAsDirectoryPrefix) {
            super(project, pomType, pomTokens, addProjectNameAsDirectoryPrefix);
        }

        /**
         * Parent projects are not part of a SoftwareComponent; therefore one should call this method instead of
         * the {@code inSoftwareComponentWithRelativePath()} method.
         *
         * @return The next BuilderStep subtype in the builder chain.
         */
        public PrefixEnricher isParentProject() {

            // Check sanity
            Validate.isTrue(isParentPomType(),
                    "isParentProject should only be called for PARENT PomTypes.");

            // Since all parent poms are located in the [projName]/poms/[xyz-]projName-[something-]parent
            // directory, we really need not define the software component path at all.
            //
            // pomTokens.put(SOFTWARE_COMPONENT_RELATIVE_PATH, "poms");

            // Add the PomType of the parent, which is non-null for PomTypes other than PomType.PARENT.
            final PomType parentPomType = getParentPomType(2);
            if (parentPomType != null) {
                pomTokens.put(PARENT_POMTYPE, parentPomType.name());

                // Add the relative path to the parent POM directory
                final String parentPomDirName = (isProjectNameAddedToDirectories() || isParentPomType()
                        ? project.getName() + Name.DEFAULT_SEPARATOR
                        : "") + parentPomType.name().toLowerCase().replace("_", Name.DEFAULT_SEPARATOR);
                pomTokens.put(PomToken.PARENT_POM_RELATIVE_PATH.getToken(), "../" + parentPomDirName);
            } else {
                // This should happen if pomType is PomType.PARENT
                pomTokens.put(PomToken.PARENT_POM_RELATIVE_PATH.getToken(), "");
            }

            // All Done.
            return new PrefixEnricher(project, pomType, pomTokens, isProjectNameAddedToDirectories());
        }

        /**
         * <p>Adds the relative path to the reactor of the SoftwareComponent in which the active Maven project which
         * should be tokenized by the resulting TokenParser resides. The reactor for a SoftwareComponentPart project
         * is its parent directory. (I.e. for the {@code finance-api} project residing within the SoftwareComponent
         * {@code finance}, the relative path should be {@code services/finance} assuming that we would like the
         * finance SoftwareComponent to reside in the {@code services} VCS path).
         * This implies that the directory name of the project itself (such as {@code finance-api}) should not be
         * provided in the componentRelativePath.</p>
         * <p>Example:</p>
         * <pre>
         *     <code>
         *         // Assume that we want to create the TokenParser for the project
         *         //
         *         // finance-api
         *         //
         *         // Then the componentRelativePath provided should be "services/finance"
         *         // and not "services/finance/finance-api"
         *         //
         *         final TokenParser tokenParser = SingleBracketPomTokenParserFactory
         *         .create(PomType.COMPONENT_API, acmeFooProject, false)
         *         .inSoftwareComponentWithRelativePath("services/finance")
         *         .withProjectGroupIdPrefix("org.acme", false)
         *         .withoutProjectSuffix()
         *         .build();
         *     </code>
         * </pre>
         *
         * @param relativePathToSoftwareComponent Cannot be null or empty. The relative directory path from the project VCS
         *                                        root to the <strong>component directory</strong> containing the Maven
         *                                        project which should be tokenized by the resulting TokenParser.
         * @return The next BuilderStep subtype in the builder chain.
         */
        public PrefixEnricher inSoftwareComponentWithRelativePath(final String relativePathToSoftwareComponent) {

            // Delegate if required
            if (isParentPomType()) {
                return isParentProject();
            }

            final String invalidRelativePathMsg = "Cannot handle null or empty "
                    + "projectRelativeDirectoryPath argument for non-RootReactor projects.";
            final List<String> pathSegments = tokenize(relativePathToSoftwareComponent);

            // We have a non-parent project, so we might need to calculate the relative path.
            switch (pomType) {

                case ROOT_REACTOR:
                    pomTokens.put(SOFTWARE_COMPONENT_RELATIVE_PATH, "");
                    // pomTokens.put(PomToken.PARENT_POM_RELATIVE_PATH.getToken(), "");
                    // pomTokens.put(SOFTWARE_COMPONENT_NAME, null);
                    break;

                case REACTOR:
                    Validate.notEmpty(relativePathToSoftwareComponent, invalidRelativePathMsg);
                    final String pathSegmentSeparator = "/";
                    String paddedRelativePath = relativePathToSoftwareComponent.endsWith(pathSegmentSeparator)
                            ? relativePathToSoftwareComponent.substring(0,
                            relativePathToSoftwareComponent.lastIndexOf(pathSegmentSeparator))
                            : relativePathToSoftwareComponent;
                    pomTokens.put(SOFTWARE_COMPONENT_RELATIVE_PATH, paddedRelativePath);
                    pomTokens.put(SOFTWARE_COMPONENT_NAME, pathSegments.get(pathSegments.size() - 1));

                    PomType parentPomType = getParentPomType(pathSegments.size());
                    pomTokens.put(PARENT_POMTYPE, parentPomType.name());
                    pomTokens.put(PomToken.PARENT_POM_RELATIVE_PATH.getToken(), "");
                    break;

                default:
                    Validate.notEmpty(relativePathToSoftwareComponent, invalidRelativePathMsg);
                    pomTokens.put(SOFTWARE_COMPONENT_RELATIVE_PATH, relativePathToSoftwareComponent);
                    pomTokens.put(SOFTWARE_COMPONENT_NAME, pathSegments.get(pathSegments.size() - 1));

                    parentPomType = getParentPomType(pathSegments.size());
                    final String relative = getRelativePathToParentPom(relativePathToSoftwareComponent, parentPomType);
                    pomTokens.put(PARENT_POMTYPE, parentPomType.name());
                    pomTokens.put(PomToken.PARENT_POM_RELATIVE_PATH.getToken(), relative);
                    break;
            }

            // All Done.
            return new PrefixEnricher(project, pomType, pomTokens, isProjectNameAddedToDirectories());
        }

        //
        // Private helpers
        //

        private String getRelativePathToParentPom(final String relativePathToSoftwareComponent,
                                                  final PomType parentPomType) {

            final List<String> tokenizedPath = tokenize(relativePathToSoftwareComponent);
            final int numCdsUpwards = pomType == PomType.REACTOR
                    ? tokenizedPath.size()
                    : tokenizedPath.size() + 1;

            final StringBuilder relativePathBuilder = new StringBuilder();
            for (int i = 0; i < numCdsUpwards; i++) {
                relativePathBuilder.append("../");
            }
            relativePathBuilder.append(POMS_DIRECTORY_NAME + "/");

            // Always prepend the project name to parent POM dirs (i.e. in the "poms/" directory).
            final String parentPomDirName = project.getName() + Name.DEFAULT_SEPARATOR
                    + parentPomType.name().replace("_", Name.DEFAULT_SEPARATOR).toLowerCase();

            relativePathBuilder.append(parentPomDirName);
            return relativePathBuilder.toString();
        }

        private PomType getParentPomType(final int numSegmentsInRelativePathToProjectRoot) {

            PomType toReturn = null;

            switch (pomType) {
                case REACTOR:
                    toReturn = numSegmentsInRelativePathToProjectRoot < 2
                            ? PomType.ROOT_REACTOR
                            : PomType.REACTOR;
                    break;

                case COMPONENT_MODEL:
                    toReturn = PomType.MODEL_PARENT;
                    break;

                case MODEL_PARENT:
                case COMPONENT_SPI:
                case COMPONENT_API:
                    toReturn = PomType.API_PARENT;
                    break;

                case API_PARENT:
                case WAR_PARENT:
                case COMPONENT_TEST:
                case COMPONENT_IMPLEMENTATION:
                case OTHER_PARENT:
                    toReturn = PomType.PARENT;
                    break;

                default:

                    if (log.isWarnEnabled()) {
                        log.warn("Could not determine parent PomType for pomType [" + pomType + "].");
                    }
                    toReturn = null;
                    break;
            }

            // All done.
            return toReturn;
        }
    }

    /**
     * BuilderStep which adds the prefix of Maven GroupIDs for a relative path within a directory.
     * A "groupIdPrefix" is normally identical to a reverse DNS of the project's organisation,
     * similar to {@code se.jguru} or {@code org.acme}. Note that the groupIdPrefix should <strong>not</strong>
     * include the project name (i.e. {@code project.getName()}), as it will be automatically appended by this builder.
     * Given a SoftwareComponent with the relative path {@code services/finance}, the groupIdPrefix
     * {@code se.jguru} and the project name {@code foo}, the full groupId of the finance service
     * SoftwareComponent can be constructed as {@code se.jguru.foo.services.finance}.
     * This, in turn, implies that the API project in the finance SoftwareComponent will retrieve the groupId
     * {@code se.jguru.foo.services.finance.api}.
     */
    public static class PrefixEnricher extends BuilderStep {

        /**
         * The property containing the {@code groupIdPrefix}, a.k.a. reverse organisation DNS,
         * which should be prepended to the groupId and package properties.
         * Given a SoftwareComponent with the relative path {@code services/finance}, the groupIdPrefix
         * {@code se.jguru} and the project name {@code foo}, the full groupId of the finance service
         * SoftwareComponent can be constructed as {@code se.jguru.foo.services.finance}.
         * This, in turn, implies that the API project in the finance SoftwareComponent will retrieve the groupId
         * {@code se.jguru.foo.services.finance.api}.
         */
        public static final String REVERSE_DNS_OF_ORGANISATION = "reverseOrganisationDns";

        PrefixEnricher(final Project project,
                       final PomType pomType,
                       final SortedMap<String, String> pomTokens,
                       final boolean addProjectNameAsDirectoryPrefix) {
            super(project, pomType, pomTokens, addProjectNameAsDirectoryPrefix);
        }

        /**
         * Adds prefix which should be prepended to the relative path to generate a groupId for the current project.
         * A "projectGroupIdPrefix" is normally identical to a reverse DNS of the project's organisation,
         * similar to {@code se.jguru} or {@code org.acme}. Note that the groupIdPrefix should <strong>not</strong>
         * include the project name (i.e. {@code project.getName()}), as it will be automatically appended by this
         * builder.
         *
         * @param projectGroupIdPrefix A non-null prefix string added to the relative path to generate a groupId for
         *                             the current project. Empty values are permitted, but null are not. Normally
         *                             identical to a reverse DNS of the project's organisation, similar to
         *                             {@code se.jguru} or {@code org.acme}. Note that the groupIdPrefix should
         *                             <strong>not</strong> include the project name (i.e. {@code project.getName()}),
         *                             as it will be automatically appended by this builder.
         * @return The next BuilderStep subtype in the builder chain.
         */
        public SuffixEnricher withProjectGroupIdPrefix(final String projectGroupIdPrefix) {

            // Check sanity
            Validate.notEmpty(projectGroupIdPrefix, "Cannot handle null or empty projectGroupIdPrefix argument.");

            final PomType parentPomType = pomTokens.get(RelativePathEnricher.PARENT_POMTYPE) == null
                    ? null
                    : PomType.valueOf(pomTokens.get(RelativePathEnricher.PARENT_POMTYPE));

            // Add the relevant pomTokens
            pomTokens.put(REVERSE_DNS_OF_ORGANISATION, projectGroupIdPrefix);
            pomTokens.put(PomToken.PARENT_GROUPID.getToken(), getParentGroupId(projectGroupIdPrefix, parentPomType));
            pomTokens.put(PomToken.PARENT_ARTIFACTID.getToken(), getParentArtifactId(parentPomType));

            // All done.
            return new SuffixEnricher(project, pomType, pomTokens, isProjectNameAddedToDirectories());
        }

        //
        // Private helpers
        //

        private String getParentGroupId(final String projectGroupIdPrefix, final PomType parentPomType) {

            final String delimiter = ".";
            final String projectPrefix = project.getPrefix() != null ? project.getPrefix() + delimiter : "";
            StringBuilder groupIdBuilder = new StringBuilder(
                    projectGroupIdPrefix + delimiter + projectPrefix + project.getName() + delimiter);

            // Calculate the Maven groupId for the parent POM
            switch (pomType) {
                case ROOT_REACTOR:
                    groupIdBuilder = new StringBuilder(project.getReactorParent().getGroupId());
                    break;

                case PARENT:
                    groupIdBuilder = new StringBuilder(project.getParentParent().getGroupId());
                    break;

                case REACTOR:

                    // Find the relative path of the parent reactor only for the case that the
                    // parent is not
                    //
                    // relativePath: osgi.launcher --> parent GID: [se.jguru.nazgul.core].osgi
                    if (parentPomType != PomType.ROOT_REACTOR) {
                        final String relPath = pomTokens.get(RelativePathEnricher.SOFTWARE_COMPONENT_RELATIVE_PATH);
                        final List<String> segments = tokenize(relPath);

                        Validate.isTrue(segments.size() > 0, "For a reactor POM not immediately below the root "
                                + "reactor, the number of path segments should be at least 1. (Got: "
                                + segments.size() + ")");

                        for (int i = 0; i < segments.size() - 1; i++) {
                            groupIdBuilder.append(segments.get(i));
                            if (i < segments.size() - 2) {
                                groupIdBuilder.append(delimiter);
                            }
                        }
                    } else {

                        // Remove the trailing '.'
                        groupIdBuilder.deleteCharAt(groupIdBuilder.length() - 1);
                    }
                    break;

                default:

                    // Check sanity
                    Validate.notNull(parentPomType, "Parent PomType should not be null for pomType [" + pomType + "]");
                    final String projectPrefixSegment = project.getPrefix() != null
                            ? project.getPrefix() + Name.DEFAULT_SEPARATOR
                            : "";
                    groupIdBuilder.append(POMS_DIRECTORY_NAME)
                            .append(delimiter)
                            .append(projectPrefixSegment)
                            .append(project.getName())
                            .append(Name.DEFAULT_SEPARATOR)
                            .append(parentPomType.name().replace("_", Name.DEFAULT_SEPARATOR).toLowerCase());
                    break;
            }

            // All done.
            return groupIdBuilder.toString();
        }

        private String getParentArtifactId(final PomType parentPomType) {

            StringBuilder artifactIdBuilder = new StringBuilder();
            final String prefix = project.getPrefix() != null ? project.getPrefix() + Name.DEFAULT_SEPARATOR : "";

            // Parent artifactIDs should be on the form
            //
            // [prefix-][name]-[parentPomType (transformed)]
            artifactIdBuilder.append(prefix).append(project.getName()).append(Name.DEFAULT_SEPARATOR);

            // Calculate the Maven groupId for the parent POM
            switch (pomType) {
                case ROOT_REACTOR:
                    artifactIdBuilder = new StringBuilder(project.getReactorParent().getArtifactId());
                    break;

                case PARENT:
                    artifactIdBuilder = new StringBuilder(project.getParentParent().getArtifactId());
                    break;

                case REACTOR:
                    //
                    // The following reactor G/A:
                    // <groupId>se.jguru.nazgul.core.osgi.launcher</groupId>
                    // <artifactId>nazgul-core-osgi-launcher-reactor</artifactId>
                    //
                    // ... yields the parent G/A as shown:
                    // <groupId>se.jguru.nazgul.core.osgi</groupId>
                    // <artifactId>nazgul-core-osgi-reactor</artifactId>
                    //
                    if (parentPomType != PomType.ROOT_REACTOR) {

                        final String relPath = pomTokens.get(RelativePathEnricher.SOFTWARE_COMPONENT_RELATIVE_PATH);
                        final List<String> segments = tokenize(relPath);

                        Validate.isTrue(segments.size() > 0, "For a reactor POM not immediately below the root "
                                + "reactor, the number of path segments should be at least 1. (Got: "
                                + segments.size() + ")");

                        for (int i = 0; i < segments.size() - 1; i++) {
                            artifactIdBuilder.append(segments.get(i)).append(Name.DEFAULT_SEPARATOR);
                        }
                    }

                    artifactIdBuilder.append("reactor");
                    break;

                default:
                    // The parent artifactId should be on the form
                    // [prefix-][name-][parentPomType (adjusted)]
                    // nazgul-core-parent
                    artifactIdBuilder.append(parentPomType.name().replace("_", Name.DEFAULT_SEPARATOR).toLowerCase());
                    break;
            }

            // All done
            return artifactIdBuilder.toString();
        }

        /*
            // RelativeParentPomPath: Add the relativePath to the parent POM directory
            final PomType parentPomType = pomTokens.get(RelativePathEnricher.PARENT_POMTYPE) == null
                    ? null
                    : PomType.valueOf(pomTokens.get(RelativePathEnricher.PARENT_POMTYPE));
            if (parentPomType != null) {

                String relativePathToParentPomDir = getRelativePathToParentPomDir(
                        parentPomType, parentDirSegments, dirPrefix);
                if (isParentPomType()) {

                    relativePathToParentPomDir = "../"
                            + (isProjectNameAddedToDirectories() ? project.getName() + Name.DEFAULT_SEPARATOR : "")
                            + parentPomType.name().toLowerCase().replace("_", Name.DEFAULT_SEPARATOR);
                }
                pomTokens.put(PomToken.PARENT_POM_RELATIVE_PATH.getToken(), relativePathToParentPomDir);

                // Add the parent POM groupId and artifactId coordinates
                switch (pomType) {

                    case ROOT_REACTOR:
                        final SimpleArtifact reactorParent = project.getReactorParent();
                        pomTokens.put(PomToken.PARENT_GROUPID.getToken(), reactorParent.getGroupId());
                        pomTokens.put(PomToken.PARENT_ARTIFACTID.getToken(), reactorParent.getArtifactId());
                        break;

                    case PARENT:
                        final SimpleArtifact parentParent = project.getParentParent();
                        pomTokens.put(PomToken.PARENT_GROUPID.getToken(), parentParent.getGroupId());
                        pomTokens.put(PomToken.PARENT_ARTIFACTID.getToken(), parentParent.getArtifactId());
                        break;

                    case API_PARENT:
                    case MODEL_PARENT:
                    case WAR_PARENT:
                    case OTHER_PARENT:
                    case OTHER:

                        //
                        // Expected form:
                        // acme-foo-api-parent, that is
                        // ([projectPrefix]-)[projectName]-[parentPomType]
                        //
                        final String parentArtifactId = artifactPrefix + project.getName() + Name.DEFAULT_SEPARATOR
                                + parentPomType.name().toLowerCase().replace("_", "-");

                        //
                        // Expected form:
                        // org.acme.foo.poms.foo-api-parent, that is
                        // [projectGroupIdPrefix].[projectName].poms.[projectName]-[parentPomType]
                        //
                        final String parentGroupId = projectGroupIdPrefix + "." + project.getName() + ".poms."
                                + project.getName() + Name.DEFAULT_SEPARATOR
                                + parentPomType.name().toLowerCase().replace("_", "-");

                        pomTokens.put(PomToken.PARENT_ARTIFACTID.getToken(), parentArtifactId);
                        pomTokens.put(PomToken.PARENT_GROUPID.getToken(), parentGroupId);
                        break;

                    case REACTOR:
                        // Typical reactor-to-immediate_parent_reactor structure:
                        */
                        /*
                            <parent>
                                <groupId>se.jguru.nazgul.core.osgi</groupId>
                                <artifactId>nazgul-core-osgi-reactor</artifactId>
                                <version>1.6.1-SNAPSHOT</version>
                            </parent>

                            <groupId>se.jguru.nazgul.core.osgi.launcher</groupId>
                            <artifactId>nazgul-core-osgi-launcher-reactor</artifactId>
                        */
                        /*
                        //
                        // GroupID:     as activeProject's groupID, minus the last segment.
                        // ArtifactID:  ([projectPrefix]-)[projectName]-[parentDirRelativePathAsRawArtifactId]-reactor
                        //
                        final StringBuilder parentGroupIdBuilder = new StringBuilder(projectGroupIdPrefix);
                        final int numParentPathSegments = parentDirSegments.size() == 0
                                ? 0
                                : parentDirSegments.size() - 1;
                        parentGroupIdBuilder.append(".").append(project.getName());
                        for (int i = 0; i < numParentPathSegments; i++) {
                            parentGroupIdBuilder.append(".").append(parentDirSegments.get(i));
                        }

                        final StringBuilder parentArtifactIdBuilder = new StringBuilder();
                        if (nonEmptyProjectPrefix) {
                            parentArtifactIdBuilder.append(project.getPrefix()).append("-");
                        }
                        parentArtifactIdBuilder.append(project.getName()).append("-");
                        for (int i = 0; i < numParentPathSegments; i++) {
                            parentArtifactIdBuilder.append(parentDirSegments.get(i)).append("-");
                        }
                        parentArtifactIdBuilder.append("reactor");

                        // We assume that all reactors will have the same version as the root reactor
                        pomTokens.put(PomToken.PARENT_GROUPID.getToken(), parentGroupIdBuilder.toString());
                        pomTokens.put(PomToken.PARENT_ARTIFACTID.getToken(), parentArtifactIdBuilder.toString());
                        break;

                    default:
                        // No idea how to treat this...
                        log.warn("PomType [" + pomType + "] is not handled by the [" + getClass().getSimpleName()
                                + "] in respect to parent groupID (" + PomToken.PARENT_GROUPID.getToken() + ") and "
                                + "artifactID (" + PomToken.PARENT_ARTIFACTID.getToken() + ") tokens.\n"
                                + "This should not happen; please notify the developers ... nicely.");
                        break;

                }
            } else {
                log.warn("Could not find parent PomType for PomType [" + pomType
                        + "]. This implies that the [" + PomToken.PARENT_POM_RELATIVE_PATH.getToken()
                        + "] token cannot be set. Validate that your template does not require it, "
                        + "or set it manually.");
            }
            */
    }

    /**
     * Builder step which can add a project suffix, for project types which requires it
     * (and no project suffix for project types that requires none).
     */
    public static class SuffixEnricher extends BuilderStep {

        SuffixEnricher(final Project project,
                       final PomType pomType,
                       final SortedMap<String, String> pomTokens,
                       final boolean addProjectNameAsDirectoryPrefix) {
            super(project, pomType, pomTokens, addProjectNameAsDirectoryPrefix);
        }

        /**
         * Adds a project suffix, which is used to calculate groupId and artifactId for the local
         * project for ComponentTypes where suffix is required. If the {@code isProjectSuffixRequired()} yields
         * {@code false}, the projectSuffix value is ignored (implying it may be null or empty in these cases).
         *
         * @param projectSuffix The suffix for the project. May be null or empty, depending on the active projectType.
         * @return The next BuilderStep subtype in the builder chain.
         * @see se.jguru.nazgul.core.quickstart.api.generator.SoftwareComponentPart#isSuffixRequired()
         */
        public MavenVersionEnricher withProjectSuffix(final String projectSuffix) {

            // Get some token parts
            final String localProjectDirName = getProjectDirectoryName(projectSuffix);
            final String relativePackage = getRelativePackage(projectSuffix);

            String pathToParentDir = pomTokens.get(RelativePathEnricher.SOFTWARE_COMPONENT_RELATIVE_PATH);
            if (pathToParentDir == null && isParentPomType()) {
                pathToParentDir = POMS_DIRECTORY_NAME;
            }
            final String relativeDirPath = (pomType == PomType.REACTOR || pomType == PomType.ROOT_REACTOR)
                    ? pathToParentDir
                    : pathToParentDir + "/" + localProjectDirName;

            pomTokens.put(PomToken.RELATIVE_DIRPATH.getToken(), relativeDirPath);
            pomTokens.put(PomToken.RELATIVE_PACKAGE.getToken(), relativePackage);

            // Now find the groupId and artifactId for the active project.
            final boolean nonEmptyProjectPrefix = project.getPrefix() != null && project.getPrefix().length() > 0;
            final String artifactPrefix = nonEmptyProjectPrefix ? project.getPrefix() + Name.DEFAULT_SEPARATOR : "";

            // ArtifactID: Calculate for the current project.
            //
            // Using the localProjectDirName, add the project name as a prefix if required.
            // DirName: "finance-api"  --> ArtifactID: "acme-foo-finance-api"
            // DirName: "foo-finance-api"  --> ArtifactID: "acme-foo-finance-api"
            //
            final String slice = artifactPrefix + project.getName() + Name.DEFAULT_SEPARATOR;
            String rawProjectDirName = localProjectDirName.startsWith(slice)
                    ? localProjectDirName.substring(slice.length())
                    : localProjectDirName;
            String artifactId = artifactPrefix + project.getName() + Name.DEFAULT_SEPARATOR + rawProjectDirName;

            if (pomType == PomType.REACTOR) {
                artifactId = artifactId + "-reactor";
            } else if (pomType == PomType.ROOT_REACTOR) {
                artifactId = artifactPrefix + project.getName() + Name.DEFAULT_SEPARATOR + "reactor";
            }
            pomTokens.put(PomToken.ARTIFACTID.getToken(), artifactId);

            //
            // GroupID: Calculate for the current project, on the form:
            //
            // Component Project [xyz]:
            //   [reverseDns].[[projectPrefix].][projectName].[relativePathToComponentParent].[componentName].[type],
            //   i.e:
            //
            //   Reverse DNS:    org.acme
            //   Project Prefix: gnat
            //   Project Name:   foo
            //   Relative Path:  messaging
            //
            //   a) Project type MODEL           --> groupID: org.acme.gnat.foo.messaging.model
            //   b) Project type REACTOR         --> groupID: org.acme.gnat.foo.messaging
            //   c) Project type SPI (jpa)       --> groupID: org.acme.gnat.foo.messaging.spi.jpa
            //   d) Project type IMPL (pojo)     --> groupID: org.acme.gnat.foo.messaging.impl.pojo
            //
            final String groupId = pomTokens.get(PrefixEnricher.REVERSE_DNS_OF_ORGANISATION) + "."
                    + (nonEmptyProjectPrefix ? project.getPrefix() + "." : "")
                    + project.getName()
                    + ("".equals(relativePackage) ? "" : "." + relativePackage);
            pomTokens.put(PomToken.GROUPID.getToken(), groupId);

            // All done.
            return new MavenVersionEnricher(project, pomType, pomTokens, isProjectNameAddedToDirectories());
        }

        /**
         * Adds a {@code null} project suffix, and delegates to calculate groupId and artifactId for
         * the local project for ComponentTypes where suffix is not used.
         *
         * @return The next BuilderStep subtype in the builder chain.
         * @see #withProjectSuffix(String)
         */
        public MavenVersionEnricher withoutProjectSuffix() {

            // Check sanity
            Validate.isTrue(!isProjectSuffixRequired(), "Project type [" + pomType + "] requires a project suffix.");

            // Delegate
            return withProjectSuffix(null);
        }

        //
        // Private helpers
        //

        /**
         * Retrieves the directory name for the active Maven project.
         *
         * @param projectSuffix The suffix for the project. May be null or empty, depending on the active projectType.
         * @return The name of the directory for the active Maven project. Never null or empty.
         */
        @SuppressWarnings("all")
        private String getProjectDirectoryName(final String projectSuffix) {

            // Check sanity
            if (pomType == PomType.ROOT_REACTOR) {
                // This is a special case
                return project.getName();
            }

            final boolean addProjectSuffixToDirName = isProjectSuffixRequired()
                    && projectSuffix != null
                    && !projectSuffix.isEmpty();

            final StringBuilder dirNameBuilder = new StringBuilder();
            if (isProjectNameAddedToDirectories()) {

                //
                // In this case, the project dirName should be prepended with the
                // project prefix (if it exists) and name, i.e:
                //
                // a) foo-gnat-messaging-model, or
                // b) gnat-messaging-model (if no prefix is defined within the project).
                //
                if (project.getPrefix() != null && !"".equals(project.getPrefix())) {
                    dirNameBuilder.append(project.getPrefix()).append(Name.DEFAULT_SEPARATOR);
                }
                dirNameBuilder.append(project.getName()).append(Name.DEFAULT_SEPARATOR);
            }

            // For parent POMs, we should simply add the PomType as the name ...
            if (!isParentPomType()) {

                //
                // The SoftwareComponent's name should not be null or empty for non-parent projects.
                //
                final String softwareComponentName = pomTokens.get(RelativePathEnricher.SOFTWARE_COMPONENT_NAME);
                Validate.notEmpty(softwareComponentName, "SoftwareComponentName was null or empty for non-parent "
                        + "pomType [" + pomType + "]. Cannot calculate project directory name. "
                        + "This should not happen.");
                dirNameBuilder.append(softwareComponentName);
            }

            // Append the project type to the directory name, minding that reactor POMs are a special case.
            // Reactor project reside within the software component directory and simply ensure that all
            // SoftwareComponentPart projects are included within the build reactor.
            final String lcPomType = pomType.name().toLowerCase().replace("_", Name.DEFAULT_SEPARATOR);
            if (pomType == PomType.REACTOR) {

                // The reactor POM in the poms reactor is not part of a software component.
                if (POMS_DIRECTORY_NAME.equals(pomTokens.get(RelativePathEnricher.SOFTWARE_COMPONENT_NAME))) {
                    return POMS_DIRECTORY_NAME;
                }

                // Standard component reactor; don't append anything.
                // dirNameBuilder.append(Name.DEFAULT_SEPARATOR).append(lcPomType);
            } else if (isParentPomType()) {
                dirNameBuilder.append(lcPomType);
            } else {

                // This is a component PomType. Remove the "component-" part of the lcPomType.
                final int stripOffLength = ("component" + Name.DEFAULT_SEPARATOR).length();
                if (lcPomType.length() < stripOffLength) {
                    log.error("Got stripOffLength: [" + stripOffLength + "] and lcPomType.length(): ["
                            + lcPomType.length() + "] for lcPomType [" + lcPomType + "]");
                    throw new IllegalStateException("This should not happen...");
                }

                dirNameBuilder.append(Name.DEFAULT_SEPARATOR).append(lcPomType.substring(stripOffLength));
            }

            if (addProjectSuffixToDirName) {
                dirNameBuilder.append(Name.DEFAULT_SEPARATOR).append(projectSuffix);
            }

            // All done - but shorten the name of the directory somewhat.
            return dirNameBuilder
                    .toString()
                    .replace("implementation", "impl");
        }

        private String getRelativePackage(final String localProjectSuffix) {

            final String prefix = project.getPrefix() != null ? project.getPrefix() + Name.DEFAULT_SEPARATOR : "";
            final StringBuilder relativePackageBuilder = new StringBuilder();

            if (isParentPomType()) {

                // The relative package should be something like
                // poms.[project.prefix-][project.name]-[pomType],
                //
                // given that the pomType is made lowercase.
                relativePackageBuilder
                        .append(POMS_DIRECTORY_NAME)
                        .append(".")
                        .append(prefix)
                        .append(project.getName())
                        .append(Name.DEFAULT_SEPARATOR)
                        .append(pomType.toString().toLowerCase().replace("_", Name.DEFAULT_SEPARATOR));
            } else if (pomType == PomType.ROOT_REACTOR) {
                return "";
            } else {

                // The first part of the relative package should be
                //
                // [relativePathToParent (converted)].[softwareComponent]
                final String relativePathToSoftwareComponent = pomTokens.get(
                        RelativePathEnricher.SOFTWARE_COMPONENT_RELATIVE_PATH);
                relativePackageBuilder.append(relativePathToSoftwareComponent.replace("/", "."));

                if (pomType != PomType.REACTOR) {

                    // A relative path of:
                    // 1) "services/finance/finance-api", or
                    // 2) "services/finance/foo-finance-api"
                    //
                    // ... should yield relative package:
                    // "services.finance.api"
                    //
                    // ... where the last package segment is retrieved from the PomType.

                    final String lcPomTypeName = pomType.name().toLowerCase();
                    final String pomTypePackage = lcPomTypeName.substring(lcPomTypeName.lastIndexOf("_") + 1);
                    relativePackageBuilder.append(".").append(pomTypePackage);

                    // Append the project suffix, if required
                    if (isProjectSuffixRequired()) {
                        relativePackageBuilder.append(".").append(localProjectSuffix);
                    }
                }
            }

            // All done.
            return relativePackageBuilder.toString();
        }
    }

    /**
     * Either of two Maven versions are generally required to tokenize the &lt;parent&gt;
     * element within a POM. For {@code PomType.REACTOR} and {@code PomType.ROOT_REACTOR} projects,
     * the {@code reactorPomMavenVersion} is used, and for other projects the {@code topParentMavenVersion}
     * version is used.
     */
    public static class MavenVersionEnricher extends BuilderStep {

        MavenVersionEnricher(final Project project,
                             final PomType pomType,
                             final SortedMap<String, String> pomTokens,
                             final boolean addProjectNameAsDirectoryPrefix) {
            super(project, pomType, pomTokens, addProjectNameAsDirectoryPrefix);
        }

        /**
         * Assigns the two Maven versions used to create POM Tokens within this Builder. For {@code PomType.REACTOR}
         * and {@code PomType.ROOT_REACTOR} projects, the {@code reactorPomMavenVersion} is used, and for other
         * projects the {@code topParentMavenVersion} version is used.
         * This BuilderStep provides the possibility to use different Maven versions for the parent POM and the
         * current POM, which is required in root/top reactor and root/top parent poms. For example, a typical
         * tokenization for a root reactor template POM uses the following structure:
         * <pre>
         *     <code>
         *             &lt;parent&gt;
         *                 &lt;groupId&gt;[parentGroupId]&lt;/groupId&gt;
         *                 &lt;artifactId&gt;[parentArtifactId]&lt;/artifactId&gt;
         *                 <strong>&lt;version&gt;[project:reactorParent.mavenVersion]&lt;/version&gt;</strong>
         *                 &lt;relativePath&gt;[parentPomRelativePath]&lt;/relativePath&gt;
         *             &lt;/parent&gt;
         *
         *
         *             &lt;groupId&gt;[groupId]&lt;/groupId&gt;
         *             &lt;artifactId&gt;[artifactId]&lt;/artifactId&gt;
         *             <strong>&lt;version&gt;[mavenVersion]&lt;/version&gt;</strong>
         *     </code>
         * </pre>
         *
         * @param reactorPomMavenVersion The version of the reactor POMs. Note that this is <strong>not</strong>
         *                               identical to {@code project.getReactorParent().getMavenVersion()},
         *                               since the reactor parent's maven version is the version of the
         *                               <strong>parent</strong> element for the root/topmost reactor POM in the
         *                               project.
         * @param topParentMavenVersion  The version of the
         * @return The next BuilderStep subtype in the builder chain.
         */
        public Builder withMavenVersions(final String reactorPomMavenVersion,
                                         final String topParentMavenVersion) {

            // Check sanity
            Validate.notEmpty(reactorPomMavenVersion, "Cannot handle null or empty reactorPomMavenVersion argument.");
            Validate.notEmpty(topParentMavenVersion, "Cannot handle null or empty topParentMavenVersion argument.");

            // Use the correct parent Maven version

            switch (pomType) {
                case ROOT_REACTOR:
                    pomTokens.put(PomToken.PARENT_VERSION.getToken(), project.getReactorParent().getMavenVersion());
                    pomTokens.put(PomToken.VERSION.getToken(), reactorPomMavenVersion);
                    break;

                case PARENT:
                    pomTokens.put(PomToken.PARENT_VERSION.getToken(), project.getParentParent().getMavenVersion());
                    pomTokens.put(PomToken.VERSION.getToken(), topParentMavenVersion);
                    break;

                case REACTOR:
                    pomTokens.put(PomToken.PARENT_VERSION.getToken(), reactorPomMavenVersion);
                    pomTokens.put(PomToken.VERSION.getToken(), reactorPomMavenVersion);
                    break;

                default:
                    pomTokens.put(PomToken.PARENT_VERSION.getToken(), topParentMavenVersion);
                    pomTokens.put(PomToken.VERSION.getToken(), topParentMavenVersion);
                    break;
            }

            // All done.
            return new Builder(project, pomType, pomTokens, isProjectNameAddedToDirectories());
        }
    }

    /**
     * Builder class to simplify creating a SortedMap whose keys contains the tokens of each StandardToken.
     * This Map can be used for token replacements in text template files.
     */
    public static class Builder extends BuilderStep {

        Builder(final Project project,
                final PomType pomType,
                final SortedMap<String, String> pomTokens,
                final boolean addProjectNameAsDirectoryPrefix) {
            super(project, pomType, pomTokens, addProjectNameAsDirectoryPrefix);
        }

        /**
         * Standard additive token addition method.
         *
         * @param pomToken The non-null PomToken for which to add a value.
         * @param value    The non-empty value to add.
         * @return This Builder object, for call chaining.
         */
        public Builder addPomToken(final PomToken pomToken, final String value) {

            // Check sanity
            Validate.notNull(pomToken, "Cannot handle null pomToken argument.");

            // Delegate and return
            addToken(pomToken.getToken(), value);
            return this;
        }

        /**
         * Adds an arbitrary key/value pair to this SingleBracketPomTokenParserFactory.
         *
         * @param key   The non-empty token key.
         * @param value The non-empty value to add.
         * @return This Builder object, for call chaining.
         */
        public Builder addToken(final String key, final String value) {

            // Check sanity
            Validate.notNull(key, "Cannot handle null key argument.");
            Validate.notEmpty(value, "Cannot handle null or empty value argument.");

            // Add the pomToken
            pomTokens.put(key, value);
            return this;
        }

        /**
         * @return A fully set-up TokenParser sporting 3 ParserAgents (i.e. DefaultParserAgent,
         * HostNameParserAgent and a FactoryParserAgent).
         */
        public TokenParser build() {

            // Add the ParserAgents and return.
            final TokenParser toReturn = new DefaultTokenParser();

            toReturn.addAgent(new DefaultParserAgent());
            toReturn.addAgent(new HostNameParserAgent());
            toReturn.addAgent(new FactoryParserAgent(project, pomTokens));

            // All done.
            toReturn.initialize(new SingleBracketTokenDefinitions());
            return toReturn;
        }
    }
}
