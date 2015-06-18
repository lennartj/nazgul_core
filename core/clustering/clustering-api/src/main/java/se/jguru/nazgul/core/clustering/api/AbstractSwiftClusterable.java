/*
 * #%L
 * Nazgul Project: nazgul-core-clustering-api
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
package se.jguru.nazgul.core.clustering.api;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Abstract clusterable implementation integrating Externalizable support, to
 * enable quicker or custom (de-)serialization.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractSwiftClusterable extends AbstractClusterable implements Externalizable {

    /**
     * {@inheritDoc}
     */
    protected AbstractSwiftClusterable(final IdGenerator idGenerator) {
        super(idGenerator);
    }

    /**
     * {@inheritDoc}
     */
    protected AbstractSwiftClusterable(final String clusterUniqueID) {
        super(clusterUniqueID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void writeExternal(final ObjectOutput out) throws IOException {

        // Start with the ID.
        out.writeUTF(getClusterId());

        // Delegate the rest.
        performWriteExternal(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

        // Read the ID.
        this.id = in.readUTF();

        // Delegate the rest
        performReadExternal(in);
    }

    /**
     * Externalizable write template delegation method, invoked from within writeExternal. You should write the internal
     * state of the Listener onto the ObjectOutput, adhering to the following pattern:
     * <pre>
     * <code>
     * class SomeIdentifiable extends AbstractSwiftIdentifiable
     * {
     *      // Internal state
     *      private int meaningOfLife = 42;
     *      private String name = "Lennart";
     *      ...
     *
     *      &#64;Override
     *      protected void performWriteExternal(ObjectOutput out) throws IOException
     *      {
     *          // Write your own state
     *          out.writeInt(meaningOfLife);
     *          out.writeUTF(name);
     *      }
     *
     *      &#64;Override
     *      protected void performReadExternal(ObjectInput in) throws IOException, ClassNotFoundException
     *      {
     *          // Then write your own state.
     *          // Use the same order as you wrote the properties in writeExternal.
     *          meaningOfLife = in.readInt();
     *          name = in.readUTF();
     *      }
     * }
     * </code>
     * </pre>
     *
     * @param out the stream to write the object to
     * @throws java.io.IOException Includes any I/O exception that may occur
     */
    protected abstract void performWriteExternal(final ObjectOutput out) throws IOException;

    /**
     * Externalizable read template delegation method, invoked from within writeExternal. You should read the internal
     * state of the Listener from the ObjectInput, adhering to the following pattern:
     * <pre>
     * <code>
     * class SomeIdentifiable extends AbstractSwiftIdentifiable
     * {
     *      // Internal state
     *      private int meaningOfLife = 42;
     *      private String name = "Lennart";
     *      ...
     *
     *      &#64;Override
     *      protected void performWriteExternal(ObjectOutput out) throws IOException
     *      {
     *          // Write your own state
     *          out.writeInt(meaningOfLife);
     *          out.writeUTF(name);
     *      }
     *
     *      &#64;Override
     *      protected void performReadExternal(ObjectInput in) throws IOException, ClassNotFoundException
     *      {
     *          // Write your own state.
     *          // Use the same order as you wrote the properties in writeExternal.
     *          meaningOfLife = in.readInt();
     *          name = in.readUTF();
     *      }
     * }
     * </code>
     * </pre>
     *
     * @param in the stream to read data from in order to restore the object
     * @throws java.io.IOException    if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being restored cannot be found.
     */
    protected abstract void performReadExternal(final ObjectInput in) throws IOException, ClassNotFoundException;
}
