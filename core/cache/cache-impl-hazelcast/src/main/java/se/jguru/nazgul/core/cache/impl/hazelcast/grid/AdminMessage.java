/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
package se.jguru.nazgul.core.cache.impl.hazelcast.grid;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command object transmitted to perform various commands
 * within the AbstractHazelcastInstanceWrapper, such as removal of cache listeners.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class AdminMessage implements Serializable, DataSerializable {

    private static final long serialVersionUID = 88299913L;

    /**
     * A listing of all known commands.
     */
    public enum Command {

        /**
         * The command to remove a listener.
         */
        REMOVE_LISTENER,
        /**
         * The command to shutdown a particular cache instance.
         */
        SHUTDOWN_INSTANCE,
        /**
         * The command to reload the cache using the provided local file resource.
         */
        RELOAD_CACHE,

        /**
         * The command to create a shared cluster Instance from within the cluster,
         * as opposed to within a cache client.
         */
        CREATE_INCACHE_INSTANCE
    }

    /**
     * A listing of all supported in-cache types to be created.
     */
    public enum TypeDefinition {

        MAP,

        SET,

        COLLECTION,

        QUEUE,

        TOPIC
    }

    // State
    private Command command;
    private List<String> arguments = new ArrayList<String>();

    /**
     * Serializable-friendly constructor.
     */
    public AdminMessage() {
    }

    private AdminMessage(final Command command, final String[] values) {
        this.command = command;
        arguments.addAll(Arrays.asList(values));
    }

    /**
     * @return The command of this AdminMessage.
     */
    public Command getCommand() {
        return command;
    }

    /**
     * @return An unmodifiable list holding all provided arguments.
     */
    public List<String> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    /**
     * Creates a REMOVE_LISTENER message.
     *
     * @param listenerId         The cluster-unique ID of the CacheListener to remove.
     * @param distributeObjectID The cluster-unique ID of the distributedObject from
     *                           which to remove the cacheListenerID.
     * @return A correctly populated AdminMessage for a remove operation.
     */
    public static AdminMessage createRemoveListenerMessage(final String distributeObjectID, final String listenerId) {

        return new AdminMessage(AdminMessage.Command.REMOVE_LISTENER, new String[]{distributeObjectID, listenerId});
    }

    /**
     * Creates a shutdown instance message.
     *
     * @param memberID the cluster-unique ID of the Hazelcast member to shutdown.
     * @return A correctly populated AdminMessage for a member shutdown operation.
     */
    public static AdminMessage createShutdownInstanceMessage(final String memberID) {

        return new AdminMessage(AdminMessage.Command.SHUTDOWN_INSTANCE, new String[]{memberID});
    }

    /**
     * Creates a message requesting the creation of a distributed instance from within the cluster.
     *
     * @param typeDefinition The type definition, such as "IMap", "ISet"
     * @param uniqueID       The cluster-wide unique ID of the instance to create.
     * @return A correctly populated AdminMessage for an instance creation operation.
     */
    public static AdminMessage createMakeInCacheInstanceMessage(final TypeDefinition typeDefinition,
                                                                final String uniqueID) {
        return new AdminMessage(AdminMessage.Command.CREATE_INCACHE_INSTANCE,
                new String[]{typeDefinition.name(), uniqueID});
    }

    //
    // DataSerializable implementation, to improve Hazelcast performance.
    //

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeData(final ObjectDataOutput out) throws IOException {
        out.writeUTF(this.command.name());
        out.writeObject(this.arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readData(final ObjectDataInput in) throws IOException {
        this.command = Command.valueOf(in.readUTF());
        this.arguments = in.readObject();
    }
}
