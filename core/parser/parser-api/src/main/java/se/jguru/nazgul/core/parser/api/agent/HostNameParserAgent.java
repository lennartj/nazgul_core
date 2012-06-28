/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.parser.api.agent;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ParserAgent that handles dynamic subtitutions for DNS and InetAddres-related tokens.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HostNameParserAgent extends AbstractParserAgent {

    /**
     * Token key indicating that the replacement value should
     * be acquired using the local host's canonical name, i.e:
     * <code>InetAddress.getLocalHost().getCanonicalHostName()</code>.
     */
    public static final String HOST_CANONICAL_NAME = "host:canonicalName";

    /**
     * Token key indicating that the replacement value should
     * be acquired using the local host's canonical name, i.e:
     * <code>InetAddress.getLocalHost().getHostAddress()</code>.
     */
    public static final String HOST_ADDRESS = "host:address";

    /**
     * Token key indicating that the replacement value should
     * be acquired using the local host's canonical name, i.e:
     * <code>InetAddress.getLocalHost().getHostName()</code>.
     */
    public static final String HOST_NAME = "host:name";

    /**
     * Default constructor.
     */
    public HostNameParserAgent() {

        // Add the dynamic replacement tokens.
        dynamicTokens.add(HOST_CANONICAL_NAME);
        dynamicTokens.add(HOST_ADDRESS);
        dynamicTokens.add(HOST_NAME);
    }

    /**
     * Perform dynamic replacement for the provided token.
     *
     * @param token The token to dynamically replace.
     * @return The replacement value.
     */
    @Override
    protected String performDynamicReplacement(String token) {

        final InetAddress localhost;

        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Could not acquire localhost InetAddress.", e);
        }

        if (token.equals(HOST_CANONICAL_NAME)) {
            return localhost.getCanonicalHostName();
        }
        if (token.equals(HOST_ADDRESS)) {
            return localhost.getHostAddress();
        }
        if (token.equals(HOST_NAME)) {
            return localhost.getHostName();
        }

        throw new IllegalArgumentException("Cannot handle dynamic token [" + token + "].");
    }
}
