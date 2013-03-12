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

package se.jguru.nazgul.test.messaging.hornetq;

import org.apache.commons.lang3.Validate;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.hornetq.jms.server.embedded.EmbeddedJMS;
import se.jguru.nazgul.test.messaging.MessageBroker;

import javax.jms.ConnectionFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * MessageBroker implementation for HornetQ.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
public class HornetQBroker implements MessageBroker {

    /**
     * Default Broker identity.
     */
    public static final String DEFAULT_BROKERNAME = "AbstractHornetQJmsTest_Broker";

    /**
     * Default configuration directory.
     */
    public static final String DEFAULT_CONFIGURATION_DIRECTORY = "hornetq/plainconfig";

    /**
     * Default name of the hornetq main configuration file.
     */
    public static final String HORNETQ_CONFIGURATION_FILENAME = "hornetq-configuration.xml";

    /**
     * Default name of the hornetq jms configuration file.
     */
    public static final String JMS_CONFIGURATION_FILENAME = "hornetq-jms.xml";

    /**
     * Default registry name of the hornetq jms ConfigurationFactory.
     */
    public static final String CONFIGURATION_FACTORY_ID = "UnitTestConnectionFactory";

    /**
     * Default name of the hornetq users/security configuration file.
     */
    public static final String SECURITY_CONFIGURATION_FILENAME = "hornetq-users.xml";

    /**
     * A map relating all pre-defined queue names/ids to their bindings (i.e. JNDI key).
     */
    public static final Map<String, String> PERSISTENT_QUEUES;

    static {

        PERSISTENT_QUEUES = new TreeMap<String, String>();
        PERSISTENT_QUEUES.put("clientRequestQueue", "/queue/client/outboundRequest");
        PERSISTENT_QUEUES.put("clientResponseQueue", "/queue/client/inboundResponse");
        PERSISTENT_QUEUES.put("serverRequestQueue", "/queue/server/inboundRequest");
        PERSISTENT_QUEUES.put("serverResponseQueue", "/queue/server/outboundResponse");
    }

    // Internal state
    private EmbeddedJMS jmsServer;
    private final String brokerName;

    /**
     * Creates a HornetQBroker wrapping the DEFAULT_BROKERNAME using DEFAULT_CONFIGURATION_DIRECTORY.
     */
    public HornetQBroker() {
        this(DEFAULT_BROKERNAME, DEFAULT_CONFIGURATION_DIRECTORY);
    }

    /**
     * Creates a new HornetQBroker instance using the supplied
     * brokerName and EmbeddedJMS instance.
     *
     * @param brokerName The brokerName to assign the the embedded HornetQServer instance.
     * @param configurationDirectory The path to the directory where the default hornetq configuration
     *                               files (named as per above) reside.
     */
    public HornetQBroker(final String brokerName, final String configurationDirectory) {

        // Check sanity
        Validate.notEmpty(brokerName, "Cannot handle null or empty brokerName argument.");
        Validate.notEmpty(configurationDirectory, "Cannot handle null or empty configurationDirectory argument.");

        // a) Create the HornetQ configuration
        final Configuration configuration = new ConfigurationImpl();
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);

        // b) Add a transport.
        //    Map it as an acceptor and a connector (using the same id).
        final String connectorId = "connector";
        final Set<TransportConfiguration> transportConfigurations = new HashSet<TransportConfiguration>();
        transportConfigurations.add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));

        configuration.setAcceptorConfigurations(transportConfigurations);
        configuration.getConnectorConfigurations().put(connectorId,
                new TransportConfiguration(NettyConnectorFactory.class.getName()));

        // c) Create the JMS configuration
        final JMSConfiguration jmsConfig = new JMSConfigurationImpl();

        final ConnectionFactoryConfigurationImpl cfConfig = new ConnectionFactoryConfigurationImpl(
                CONFIGURATION_FACTORY_ID,       // name
                false,                          // high-availability
                Arrays.asList(connectorId),     // connector names
                "/connection/" + CONFIGURATION_FACTORY_ID);     // binding
        jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

        final List<JMSQueueConfiguration> queueConfigurations = jmsConfig.getQueueConfigurations();
        for(String current : PERSISTENT_QUEUES.keySet()) {

            // Create a non-persistent queue without any selectors.
            queueConfigurations.add(
                    new JMSQueueConfigurationImpl(
                            current,    // name
                            null,       // selector
                            false,      // persistent
                            PERSISTENT_QUEUES.get(current)) // binding
            );
        }

        // d) Create the server and assign the broker name
        jmsServer = new EmbeddedJMS();
        jmsServer.setConfiguration(configuration);
        jmsServer.setJmsConfiguration(jmsConfig);

        this.brokerName = brokerName;
    }

    /**
     * @return The embedded HornetQ Jms server.
     */
    public final EmbeddedJMS getJmsServer() {
        return jmsServer;
    }

    /**
     * Starts the MessageBroker.
     *
     * @throws Exception if the broker could not be properly started.
     */
    @Override
    public void startBroker() throws Exception {
        jmsServer.start();
        jmsServer.getHornetQServer().setIdentity(brokerName);
    }

    /**
     * Stops the MessageBroker.
     *
     * @throws Exception if the broker could not be properly stopped.
     */
    @Override
    public void stopBroker() throws Exception {
        jmsServer.stop();
    }

    /**
     * @return a broker connection URI, suited for unit tests or integration tests as required.
     */
    @Override
    public String getMessageServerURI() {
        return "Irrelevant";
    }

    /**
     * Retrieves a fully configured ConnectionFactory from the wrapped MessageBroker.
     *
     * @param unused Unused; the ConnectionFactory is retrieved from the EmbeddedJMS using the
     *               CONFIGURATION_FACTORY_ID key.
     * @return a fully configured ConnectionFactory from the wrapped MessageBroker.
     */
    @Override
    public ConnectionFactory getConnectionFactory(final String unused) {
        return (ConnectionFactory) jmsServer.lookup("/connection/" + CONFIGURATION_FACTORY_ID);
    }

    /**
     * Retrieves the human-readable name of this MessageBroker.
     *
     * @return The human-readable name of this MessageBroker.
     */
    @Override
    public String getName() {
        return jmsServer.getHornetQServer().getIdentity();
    }
}
