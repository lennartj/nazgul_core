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

package se.jguru.nazgul.core.cache.impl.hazelcast.grid;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DataSerializableAdapterTest {

    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullExternalizable() {

        // Assemble
        final DebugExternalizable externalizable = null;

        // Act & Assert
        new DataSerializableAdapter(externalizable);
    }

    @Test(expected = IOException.class)
    public void validateExceptionOnUnknownDataOutputType() throws IOException {

        // Assemble
        final String value = "testValue";
        final DebugExternalizable externalizable = new DebugExternalizable(value);
        final DataSerializableAdapter unitUnderTest = new DataSerializableAdapter(externalizable);

        final String filePath = getClass().getClassLoader().getResource("blackhole/placeholder.txt").getPath();
        final File parentDir = new File(filePath).getParentFile();
        final String filename = "validateExceptionOnUnknownDataOutputType_" + sdf.format(new Date()) + ".txt";
        final RandomAccessFile raf = new RandomAccessFile(new File(parentDir, filename), "rw");

        // Act & Assert
        unitUnderTest.writeData(raf);
        raf.close();
    }

    @Test(expected = IOException.class)
    public void validateExceptionOnUnknownDataInputType() throws IOException {

        // Assemble
        final String value = "testValue";
        final DebugExternalizable externalizable = new DebugExternalizable(value);
        final DataSerializableAdapter unitUnderTest = new DataSerializableAdapter(externalizable);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final String filePath = getClass().getClassLoader().getResource("blackhole/placeholder.txt").getPath();
        final File parentDir = new File(filePath).getParentFile();
        final String filename = "validateExceptionOnUnknownDataInputType_" + sdf.format(new Date()) + ".txt";
        final RandomAccessFile raf = new RandomAccessFile(new File(parentDir, filename), "rw");

        // Act & Assert
        final DataOutputStream dos = new DataOutputStream(baos);
        unitUnderTest.writeData(dos);
        dos.flush();
        dos.close();

        final DataSerializableAdapter resultHolder = new DataSerializableAdapter();
        resultHolder.readData(raf);
    }

    @Test
    public void validateNormalOperation() throws IOException {

        // Assemble
        final String value = "testValue";
        final DebugExternalizable externalizable = new DebugExternalizable(value);
        final DataSerializableAdapter unitUnderTest = new DataSerializableAdapter(externalizable);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Act
        final DataOutputStream dos = new DataOutputStream(baos);
        unitUnderTest.writeData(dos);
        dos.flush();
        dos.close();

        final DataSerializableAdapter resultHolder = new DataSerializableAdapter();
        resultHolder.readData(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));

        final Externalizable result = resultHolder.getWrappedExternalizable();

        // Assert
        Assert.assertEquals(DebugExternalizable.class, result.getClass());
        Assert.assertEquals(value, ((DebugExternalizable) result).getDaValue());
    }

    @Test
    public void validateNormalOperationUsingUnderlyingObjectOutput() throws IOException {

        // Assemble
        final String value = "testValue";
        final DebugExternalizable externalizable = new DebugExternalizable(value);
        final DataSerializableAdapter unitUnderTest = new DataSerializableAdapter(externalizable);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Act
        unitUnderTest.writeData(oos);
        oos.flush();
        oos.close();

        final DataSerializableAdapter resultHolder = new DataSerializableAdapter();
        resultHolder.readData(new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())));

        final Externalizable result = resultHolder.getWrappedExternalizable();

        // Assert
        Assert.assertEquals(DebugExternalizable.class, result.getClass());
        Assert.assertEquals(value, ((DebugExternalizable) result).getDaValue());
    }
}
