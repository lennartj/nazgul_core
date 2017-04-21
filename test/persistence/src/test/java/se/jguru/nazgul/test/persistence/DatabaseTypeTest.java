/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-test
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */


package se.jguru.nazgul.test.persistence;

import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.Validate;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DatabaseTypeTest {

    @Test
    public void validateH2DbType() {

        // Assemble
        final DatabaseType unitUnderTest = DatabaseType.H2;

        // Act
        final String fooJdbcURL = unitUnderTest.getUnitTestJdbcURL("foo", getTargetDirectory());
        final String databaseDialectClass = unitUnderTest.getDatabaseDialectClass();
        final IDataTypeFactory dataTypeFactory = unitUnderTest.getDataTypeFactory();

        // Assert
        Assert.assertTrue(fooJdbcURL.startsWith("jdbc:h2:" + getTargetDirectory().getPath()));
        Assert.assertEquals("org.apache.openjpa.jdbc.sql.H2Dictionary", databaseDialectClass);
        Assert.assertSame(H2DataTypeFactory.class, dataTypeFactory.getClass());
    }

    @Test
    public void validateHsqlDbType() {

        // Assemble
        final DatabaseType unitUnderTest = DatabaseType.HSQL;

        // Act
        final String fooJdbcURL = unitUnderTest.getUnitTestJdbcURL("foo", getTargetDirectory());
        final String databaseDialectClass = unitUnderTest.getDatabaseDialectClass();
        final IDataTypeFactory dataTypeFactory = unitUnderTest.getDataTypeFactory();

        // Assert
        Assert.assertEquals("jdbc:hsqldb:mem:foo;hsqldb.tx_level=serializable", fooJdbcURL);
        Assert.assertEquals("org.apache.openjpa.jdbc.sql.HSQLDictionary", databaseDialectClass);
        Assert.assertSame(HsqldbDataTypeFactory.class, dataTypeFactory.getClass());
    }

    @Test
    public void validatePostgresqlDbType() {

        // Assemble
        final DatabaseType unitUnderTest = DatabaseType.POSTGRESQL_LOCALHOST;

        // Act
        final String fooJdbcURL = unitUnderTest.getUnitTestJdbcURL("foo", getTargetDirectory());
        final String databaseDialectClass = unitUnderTest.getDatabaseDialectClass();
        final IDataTypeFactory dataTypeFactory = unitUnderTest.getDataTypeFactory();

        // Assert
        Assert.assertEquals("jdbc:postgresql:foo", fooJdbcURL);
        Assert.assertEquals("org.apache.openjpa.jdbc.sql.PostgresDictionary", databaseDialectClass);
        Assert.assertSame(PostgresqlDataTypeFactory.class, dataTypeFactory.getClass());
    }

    //
    // Private helpers
    //

    private File getTargetDirectory() {

        // Use CodeSource
        final URL location = getClass().getProtectionDomain().getCodeSource().getLocation();

        // Check sanity
        Validate.notNull(location, "CodeSource location not found for class [" + getClass().getSimpleName() + "]");

        // All done.
        return new File(location.getPath()).getParentFile();
    }
}
