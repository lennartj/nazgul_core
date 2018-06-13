/*-
 * #%L
 * Nazgul Project: nazgul-core-messaging-test
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.test.messaging.artemis;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.registry.MapBindingRegistry;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.activemq.artemis.spi.core.naming.BindingRegistry;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.test.messaging.MessageBroker;

import javax.jms.ConnectionFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * MessageBroker implementation for Artemis.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ArtemisBroker implements MessageBroker {

    /**
     * Default Broker identity.
     */
    public static final String DEFAULT_BROKERNAME = "AbstractArtemisJmsTest_Broker";

    /**
     * Default configuration directory.
     */
    public static final String DEFAULT_CONFIGURATION_DIRECTORY = "artemis/plainconfig";

    /**
     * Default registry name of the hornetq jms ConfigurationFactory.
     */
    public static final String CONFIGURATION_FACTORY_ID = "UnitTestConnectionFactory";

    /**
     * A map relating all pre-defined queue names/ids to their bindings (i.e. JNDI key).
     */
    public static final Map<String, String> UNIT_TEST_QUEUES;

    static {

        UNIT_TEST_QUEUES = new TreeMap<>();
        UNIT_TEST_QUEUES.put("clientRequestQueue", "/queue/client/outboundRequest");
        UNIT_TEST_QUEUES.put("clientResponseQueue", "/queue/client/inboundResponse");
        UNIT_TEST_QUEUES.put("serverRequestQueue", "/queue/server/inboundRequest");
        UNIT_TEST_QUEUES.put("serverResponseQueue", "/queue/server/outboundResponse");
    }

    // Internal state
    private EmbeddedJMS jmsServer;
    private BindingRegistry registry;
    private String brokerName;

    /**
     * Creates an ArtemisBroker wrapping the DEFAULT_BROKERNAME using DEFAULT_CONFIGURATION_DIRECTORY.
     */
    public ArtemisBroker() {
        this(DEFAULT_BROKERNAME, DEFAULT_CONFIGURATION_DIRECTORY);
    }

    /**
     * Creates a new ArtemisBroker instance using the supplied brokerName and configurationDirectory instance.
     *
     * @param brokerName             The brokerName to assign the the embedded HornetQServer instance.
     * @param configurationDirectory The path to the directory where the default hornetq configuration
     *                               files (named as per above) reside.
     */
    @SuppressWarnings("PMD")
    public ArtemisBroker(@NotNull @Size(min = 1) final String brokerName,
                         @NotNull @Size(min = 1) final String configurationDirectory) {

        // Check sanity
        Validate.notEmpty(brokerName, "brokerName");
        Validate.notEmpty(configurationDirectory, "configurationDirectory");

        // a) Create the Artemis configuration
        this.registry = new MapBindingRegistry();
        final Configuration configuration = new ConfigurationImpl();
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);
        configuration.setJournalDirectory(
                getTargetDirectory().getAbsolutePath() + File.separatorChar + configurationDirectory);

        // b) Add a transport.
        //    Map it as an acceptor and a connector (using the same id).
        final String connectorId = "connector";
        final Set<TransportConfiguration> transportConfigurations = new HashSet<>();
        transportConfigurations.add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));

        configuration.setAcceptorConfigurations(transportConfigurations);
        configuration.getConnectorConfigurations().put(connectorId,
                new TransportConfiguration(NettyConnectorFactory.class.getName()));

        // c) Create the JMS configuration
        final JMSConfiguration jmsConfig = new JMSConfigurationImpl();

        jmsConfig.getConnectionFactoryConfigurations().add(
                new ConnectionFactoryConfigurationImpl()
                        .setName(CONFIGURATION_FACTORY_ID)
                        .setHA(false)
                        .setConnectorNames(connectorId)
                        .setBindings("/connection/" + CONFIGURATION_FACTORY_ID));

        final List<JMSQueueConfiguration> queueConfigurations = jmsConfig.getQueueConfigurations();
        for (Map.Entry<String, String> current : UNIT_TEST_QUEUES.entrySet()) {

            // Create a non-persistent queue without any selectors.
            queueConfigurations.add(
                    new JMSQueueConfigurationImpl()
                            .setName(current.getKey())
                            .setSelector(null)
                            .setDurable(false)
                            .setBindings(current.getValue()));
        }

        // d) Create the server and assign the broker name
        jmsServer = new EmbeddedJMS();
        jmsServer.setRegistry(this.registry);
        jmsServer.setConfiguration(configuration);
        jmsServer.setJmsConfiguration(jmsConfig);

        this.brokerName = brokerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startBroker() throws Exception {
        
        jmsServer.start();
        jmsServer.getJMSServerManager().getActiveMQServer().setIdentity(brokerName);
    }

    /**
     * @return The embedded Artemis Jms server.
     */
    public final EmbeddedJMS getJmsServer() {
        return jmsServer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopBroker() throws Exception {
        jmsServer.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessageServerURI() {
        return "Irrelevant";
    }

    /**
     * @return Retrieves the non-null BindingRegistry within the Artemis server.
     */
    public BindingRegistry getRegistry() {
        return registry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionFactory getConnectionFactory(final String configuration) {
        return (ConnectionFactory) jmsServer.lookup("/connection/" + CONFIGURATION_FACTORY_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return brokerName;
    }

    /**
     * Retrieves a File to the target directory.
     *
     * @return the project target directory path, wrapped in a File object.
     */
    protected File getTargetDirectory() {

        // Use CodeSource
        final URL location = getClass().getProtectionDomain().getCodeSource().getLocation();

        // Check sanity
        if(location == null) {
            throw new NullPointerException("CodeSource location not found for class ["
                    + getClass().getSimpleName() + "]");
        }

        // All done.
        return new File(location.getPath()).getParentFile();
    }
}
