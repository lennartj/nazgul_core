/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */
package se.jguru.nazgul.core.cache.impl.hazelcast.clients;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.jguru.nazgul.core.cache.impl.hazelcast.LocalhostIpResolver;

import javax.inject.Inject;
import java.net.URL;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/config/spring/propertyHolder-applicationContext.xml")
public class SpringInjectedPropertyHolderTest {

    // Shared state
    @Inject
    private ApplicationContext context;

    private static final String localListeningIp = LocalhostIpResolver.getLocalHostAddress();
    private static final Integer localListeningPort = 5701;
    private static final String cluster = "testCluster";
    private static final String members = "testMembers";

    @BeforeClass
    public static void addRequiredSystemProperties() {

        System.setProperty(PropertyHolder.CACHE_CLUSTER_KEY, cluster);
        System.setProperty(PropertyHolder.CACHE_IP_KEY, localListeningIp);
        System.setProperty(PropertyHolder.CACHE_PORT_KEY, "" + localListeningPort);
        System.setProperty(PropertyHolder.CACHE_MEMBERS_KEY, members);

        // Load the Logback configuration
        final ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
        final URL logbackURL = ctxClassLoader.getResource("config/logging/logback-test.xml");

        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);

        try {
            configurator.doConfigure(logbackURL);
        } catch (JoranException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void removeRequiredSystemProperties() {

        System.clearProperty(PropertyHolder.CACHE_CLUSTER_KEY);
        System.clearProperty(PropertyHolder.CACHE_MEMBERS_KEY);
        System.clearProperty(PropertyHolder.CACHE_IP_KEY);
        System.clearProperty(PropertyHolder.CACHE_PORT_KEY);
    }

    @Test
    public void validateInjectedPropertyHolder() {

        // Assemble
        final PropertyHolder propertyHolder = context.getBean(PropertyHolder.class);
        final String injectedMembers = context.getBean("members", String.class);
        final String injectedCluster = context.getBean("cluster", String.class);
        final String injectedIp = context.getBean("listenerIp", String.class);
        final Integer injectedPort = context.getBean("listenerPort", Integer.class);

        // Act & Assert
        Assert.assertEquals(members, injectedMembers);
        Assert.assertEquals(cluster, propertyHolder.getCacheClusterId());
        Assert.assertEquals(cluster, injectedCluster);
        Assert.assertEquals(localListeningIp, injectedIp);
        Assert.assertEquals(localListeningPort, injectedPort);
    }

}

