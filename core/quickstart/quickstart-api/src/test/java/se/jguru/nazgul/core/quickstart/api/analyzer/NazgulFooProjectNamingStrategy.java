/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.quickstart.api.analyzer;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulFooProjectNamingStrategy extends AbstractPatternBasedProjectNamingStrategy {

    public static final Pattern PROJECT_NAME_PATTERN = Pattern.compile(
            "[a-z][a-z0-9]*(-[a-z0-9]+)?", Pattern.CANON_EQ);

    public static final Pattern PROJECT_PREFIX_PATTERN = Pattern.compile(
            "[a-z][a-z0-9]*([.-][a-z0-9]+)*", Pattern.CANON_EQ);

    public NazgulFooProjectNamingStrategy() {
        super(PROJECT_NAME_PATTERN.pattern(), PROJECT_PREFIX_PATTERN.pattern());
    }

    /**
     * {@code}
     */
    @Override
    protected String createRootDirectoryNameFrom(final String validProjectName) {

        if(validProjectName.contains(".")) {
            return validProjectName.substring(validProjectName.lastIndexOf(".") + 1);
        }

        return validProjectName;
    }

    /**
     * {@code}
     */
    @Override
    protected String createTopLevelPackageFrom(final String reverseOrganisationDNS) {
        return reverseOrganisationDNS;
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
        return "fooBars";
    }
}
