/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.resource.impl.resourcebundle.parser;

import se.jguru.nazgul.core.parser.api.agent.AbstractParserAgent;

/**
 * Static-only token replacement parser agent.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StaticReplacementParserAgent extends AbstractParserAgent {

    @Override
    protected String performDynamicReplacement(final String s) {
        throw new UnsupportedOperationException("No dynamic replacements within a StaticReplacementParserAgent.");
    }
}
