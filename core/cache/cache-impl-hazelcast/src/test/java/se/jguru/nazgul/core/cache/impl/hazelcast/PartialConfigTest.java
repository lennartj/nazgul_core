/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.config.Config;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.impl.hazelcast.clients.HazelcastCacheMember;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PartialConfigTest {

    private Logger logger = LoggerFactory.getLogger(PartialConfigTest.class);


    @Test
    public void validateOKReadingOfPartialConfiguration() {

        // Assemble
        final String partialConfigPath = "config/hazelcast/PartialConfig.xml";

        // Act
        final Config result = HazelcastCacheMember.readConfigFile(partialConfigPath);

        // Assert
        logger.debug("Got: " + result.toString());
    }
}
