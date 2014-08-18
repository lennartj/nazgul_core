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

import java.io.File;
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
    public static RelativePathEnricher create(final PomType pomType, final Project project) {

        // Check sanity
        Validate.notNull(pomType, "Cannot handle null pomType argument.");
        Validate.notNull(project, "Cannot handle null project argument.");

        // Derive the project properties.
        final SortedMap<String, String> pomTokens = new TreeMap<>();
        pomTokens.put(POMTYPE_KEY, pomType.name());
        pomTokens.put(LOWERCASE_POMTYPE_KEY, pomType.name().toLowerCase().replace("_", "-"));

        // All done.
        return new RelativePathEnricher(project, pomType, pomTokens);
    }

    /**
     * Abstract specification for a BuilderStep class in the controlled Builder pattern.
     */
    abstract static class BuilderStep {

        // Internal state
        private boolean requiresProjectSuffix = false;
        protected Project project;
        protected PomType pomType;
        protected SortedMap<String, String> pomTokens;

        /**
         * Creates a new BuilderStep wrapping the supplied data. Each can be {@code null}.
         *
         * @param project   The Project of this BuilderStep.
         * @param pomType   The POM type of this BuilderStep.
         * @param pomTokens The pomTokens storage.
         */
        protected BuilderStep(final Project project,
                              final PomType pomType,
                              final SortedMap<String, String> pomTokens) {

            this.project = project;
            this.pomType = pomType;
            this.pomTokens = pomTokens;

            // Check sanity
            if (pomType != null) {

                for (SoftwareComponentPart current : SoftwareComponentPart.values()) {
                    if (pomType == current.getComponentPomType()) {
                        requiresProjectSuffix = current.isSuffixRequired();
                    }
                }
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
    }

    /**
     * Class which adds a non-null relativePath and PomTokens {@code PomToken.RELATIVE_DIRPATH}
     * and {@code PomToken.RELATIVE_PACKAGE}.
     */
    public static class RelativePathEnricher extends BuilderStep {

        /**
         * The pomToken key holding the name of the local component.
         */
        public static final String COMPONENT_NAME = "componentName";

        /**
         * The pomToken key holding the directory name of the local/active project.
         */
        public static final String LOCAL_PROJECT_DIRNAME = "localProjectDirname";

        RelativePathEnricher(final Project project, final PomType pomType, final SortedMap<String, String> pomTokens) {
            super(project, pomType, pomTokens);
        }

        /**
         * Adds the relative path of the active Maven project directory.
         *
         * @param projectRelativeDirectoryPath The non-empty relative directory path from the project VCS root to the
         *                                     active maven project which should be tokenized by the resulting
         *                                     TokenParser.
         * @return The next BuilderStep subtype in the builder chain.
         */
        public PrefixEnricher withRelativeDirectoryPath(final String projectRelativeDirectoryPath) {

            // Check sanity
            Validate.notEmpty(projectRelativeDirectoryPath,
                    "Cannot handle null or empty projectRelativeDirectoryPath argument.");

            // Add the PomTokens for relative directory path and -package
            //
            // Given a relPath of "services/finance/finance-api",
            // the relPackage should be "services.finance.api"
            //
            pomTokens.put(PomToken.RELATIVE_DIRPATH.getToken(), projectRelativeDirectoryPath);
            pomTokens.put(PomToken.RELATIVE_PACKAGE.getToken(), getRelativePackage(projectRelativeDirectoryPath));

            // All done.
            return new PrefixEnricher(project, pomType, pomTokens);
        }

        //
        // Private helpers
        //

        private String getRelativePackage(final String relativePath) {

            List<String> pathSegments = new ArrayList<>();

            final StringTokenizer tok = new StringTokenizer(relativePath, "/", false);
            while(tok.hasMoreTokens()) {
                pathSegments.add(tok.nextToken());
            }

            // Check sanity
            if(pomType.name().contains("COMPONENT")) {

                // A relative path of "services/finance/finance-api"
                // should yield the relative package "services.finance.api"

                // Do we have a last and second-to last pathSegment?
                if(pathSegments.size() >= 2) {

                    final String componentName = pathSegments.get(pathSegments.size() - 2);
                    pomTokens.put(COMPONENT_NAME, componentName);

                    final String partName = pathSegments.get(pathSegments.size() - 1);
                    pomTokens.put(LOCAL_PROJECT_DIRNAME, partName);

                    final String toSlice = componentName + Name.DEFAULT_SEPARATOR;

                    if(partName.startsWith(toSlice)) {

                        // Slice off the initial part of the last segment, and convert the rest to packages.
                        final String lastPackagePart = partName.substring(toSlice.length())
                                .replace(Name.DEFAULT_SEPARATOR, ".");

                        final StringBuilder builder = new StringBuilder();
                        for(int i = 0; i < pathSegments.size() - 1; i++) {
                            builder.append(pathSegments.get(i)).append(".");
                        }

                        // All done.
                        return builder.toString() + lastPackagePart;
                    }
                }
            }

            // TODO: Add the LOCAL_PROJECT_DIRNAME parameter.
            final String localProjectDirname = pathSegments.size() > 0
                    ? pathSegments.get(pathSegments.size() - 1)
                    : "";
            pomTokens.put(LOCAL_PROJECT_DIRNAME, localProjectDirname);

            // Fallback to default algorithm.
            return relativePath.replace("/", ".");
        }
    }

    public static class PrefixEnricher extends BuilderStep {

        static final String GROUPID_PREFIX = "groupIdPrefix";

        PrefixEnricher(final Project project, final PomType pomType, final SortedMap<String, String> pomTokens) {
            super(project, pomType, pomTokens);
        }

        /**
         * Adds prefix which should be added to the relative path to generate a groupId for the current project.
         *
         * @param projectGroupIdPrefix A non-null prefix string added to the relative path to generate a groupId for
         *                             the current project. Empty values are permitted, but null are not.
         * @return The next BuilderStep subtype in the builder chain.
         */
        public SuffixEnricher withProjectGroupIdPrefix(final String projectGroupIdPrefix) {

            // Check sanity
            Validate.notEmpty(projectGroupIdPrefix, "Cannot handle null or empty projectGroupIdPrefix argument.");
            validateRequiredKeyValuePair(PomToken.RELATIVE_PACKAGE.getToken());

            // Add the pomToken
            pomTokens.put(GROUPID_PREFIX, projectGroupIdPrefix);

            // All done.
            return new SuffixEnricher(project, pomType, pomTokens);
        }
    }

    /**
     * Builder step which can add a project suffix, for project types which requires it,
     * and no project suffix for project types that does not.
     */
    public static class SuffixEnricher extends BuilderStep {

        SuffixEnricher(final Project project, final PomType pomType, final SortedMap<String, String> pomTokens) {
            super(project, pomType, pomTokens);
        }

        /**
         * Adds a project suffix, which is used to calculate groupId and artifactId for the local
         * project for ComponentTypes where suffix is required. If the {@code isProjectSuffixRequired()} yields
         * {@code false}, the projectSuffix value is ignored (implying it may be null or empty in these cases).
         *
         * @param projectSuffix The suffix for the project. May be null or empty.
         * @return The next BuilderStep subtype in the builder chain.
         * @see se.jguru.nazgul.core.quickstart.api.generator.SoftwareComponentPart#isSuffixRequired()
         */
        public Builder withProjectSuffix(final String projectSuffix) {

            // Calculate the artifactId of the current project.
            final StringBuilder artifactIdBuilder = new StringBuilder();
            final String projectPrefix = project.getPrefix();
            if (projectPrefix != null && projectPrefix.length() > 0) {
                artifactIdBuilder.append(projectPrefix).append(Name.DEFAULT_SEPARATOR);
            }

            // Find the local Maven project name
            String localProjectName = pomTokens.get(RelativePathEnricher.LOCAL_PROJECT_DIRNAME);
            if(localProjectName == null) {
                validateRequiredKeyValuePair(RelativePathEnricher.LOCAL_PROJECT_DIRNAME);
            }
            artifactIdBuilder.append(localProjectName);

            if (projectSuffix != null && projectSuffix.length() > 0) {
                artifactIdBuilder.append(Name.DEFAULT_SEPARATOR).append(projectSuffix);
            }

            pomTokens.put(PomToken.ARTIFACTID.getToken(), artifactIdBuilder.toString());

            // Calculate the groupId of the current project.
            final String projectGroupIdPrefix = pomTokens.get(PrefixEnricher.GROUPID_PREFIX);
            final String relativeDirPath = pomTokens.get(PomToken.RELATIVE_DIRPATH.getToken());
            final StringBuilder groupIdBuilder = new StringBuilder();
            if (projectGroupIdPrefix != null) {
                groupIdBuilder.append(projectGroupIdPrefix.replace("/", "."));
                groupIdBuilder.append(".");
            }
            groupIdBuilder.append(relativeDirPath.replace(Name.DEFAULT_SEPARATOR, "/").replace("/", "."));

            // Should we add the projectSuffix?
            if (isProjectSuffixRequired()) {
                groupIdBuilder.append(projectSuffix);
            }

            pomTokens.put(PomToken.GROUPID.getToken(), groupIdBuilder.toString());

            // Add the relativePath to the parent directory
            final PomType parentPomType = getParentPomType();
            if (parentPomType != null) {
                appendRelativePathToParentPomDir(parentPomType);
            } else {
                log.warn("Could not find parent PomType for PomType [" + pomType
                        + "]. This implies that the [" + PomToken.PARENT_POM_RELATIVE_PATH.getToken()
                        + "] token cannot be set. Validate that your template does not require it, "
                        + "or set it manually.");
            }

            // All done.
            return new Builder(project, pomType, pomTokens);
        }

        /**
         * Adds a {@code null} project suffix, and delegates to calculate groupId and artifactId for
         * the local project for ComponentTypes where suffix is not used.
         *
         * @return The next BuilderStep subtype in the builder chain.
         * @see #withProjectSuffix(String)
         */
        public Builder withoutProjectSuffix() {

            // Check sanity
            Validate.isTrue(!isProjectSuffixRequired(), "Project type [" + pomType + "] requires a project suffix.");

            // Delegate
            return withProjectSuffix(null);
        }

        //
        // Private helpers
        //

        private PomType getParentPomType() {

            PomType toReturn = null;

            switch (pomType) {
                case REACTOR:
                    toReturn = PomType.REACTOR;
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
                    toReturn = PomType.PARENT;
                    break;

                default:
                    toReturn = null;
                    break;
            }

            // All done.
            return toReturn;
        }

        private void appendRelativePathToParentPomDir(final PomType parentPom) {

            // Check sanity
            Validate.notNull(parentPom, "Cannot handle null parentPom argument.");

            String relativePathToParentPom = null;
            if (parentPom == PomType.ROOT_REACTOR || parentPom == PomType.REACTOR || parentPom == PomType.PARENT) {
                relativePathToParentPom = "";
            } else {

                validateRequiredKeyValuePair(PomToken.RELATIVE_DIRPATH.getToken());

                //
                // #1) Find the directory name of the parent pom directory.
                //
                // It is normally on the form:
                // nazgul-foo-parent, or
                // nazgul-foo-api-parent
                //
                final String projectPrefix = project.getPrefix() == null
                        ? ""
                        : project.getPrefix() + Name.DEFAULT_SEPARATOR;
                final String parentProjectDirectoryName = projectPrefix + project.getName() + Name.DEFAULT_SEPARATOR
                        + parentPom.name().toLowerCase().replace("_", Name.DEFAULT_SEPARATOR);

                //
                // #2) Find the directory depth of the current MavenProject directory.
                //
                // Find the directory depth of the current MavenProject to the reactor parent project
                // (i.e. the VCS project root).
                //
                final String relativeDirPath = pomTokens.get(PomToken.RELATIVE_DIRPATH.getToken());
                final int depth = new StringTokenizer(relativeDirPath, "/").countTokens();

                final StringBuilder builder = new StringBuilder();
                for (int i = 0; i < depth; i++) {
                    builder.append("../");
                }
                builder.append("poms/").append(parentProjectDirectoryName);

                relativePathToParentPom = builder.toString();
            }

            // All done.
            pomTokens.put(PomToken.PARENT_POM_RELATIVE_PATH.getToken(), relativePathToParentPom);
        }
    }

    /*
    static class ModuleEnricher extends BuilderStep {

        // Internal state
        private SoftwareComponentPart currentPart;
        private List<String> modules;

        ModuleEnricher(final Project project, final PomType pomType, final SortedMap<String, String> pomTokens) {
            super(project, pomType, pomTokens);

            // Create internal state
            modules = new ArrayList<>();

            for (SoftwareComponentPart current : SoftwareComponentPart.values()) {
                if (pomType == current.getComponentPomType()) {
                    currentPart = current;
                    break;
                }
            }
        }

        public Builder withProjectRootDir(final File projectRootDir) {

            // Check sanity
            Validate.notNull(projectRootDir, "Cannot handle null projectRootDir argument.");
            if(projectRootDir != null) {

                // Add
                final Model pomModel = FileUtils.getPomModel(new File(parentDirectory, "pom.xml"));

                validateRequiredKeyValuePair(PomToken.ARTIFACTID.getToken());
                final String artifactId = pomTokens.get(PomToken.ARTIFACTID.getToken());

            } else {
                log.warn("Not adding the own Module to reactor POM, since a null parentDirectory was given.");
            }
        }
    }
    */

    /**
     * Builder class to simplify creating a SortedMap whose keys contains the tokens of each StandardToken.
     * This Map can be used for token replacements in text template files.
     */
    public static class Builder extends BuilderStep {

        // Internal state
        private File localProjectDirectory;

        Builder(final Project project, final PomType pomType, final SortedMap<String, String> pomTokens) {
            super(project, pomType, pomTokens);
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
