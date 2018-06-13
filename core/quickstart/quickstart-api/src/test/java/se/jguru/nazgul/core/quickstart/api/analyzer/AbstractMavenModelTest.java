/*-
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
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

package se.jguru.nazgul.core.quickstart.api.analyzer;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.junit.Assert;

import java.io.File;
import java.io.FileReader;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractMavenModelTest {

    // Shared state
    protected MavenXpp3Reader pomReader;
    protected File testDataDirectory;

    protected AbstractMavenModelTest() {
        pomReader = new MavenXpp3Reader();

        final ClassLoader classLoader = getClass().getClassLoader();
        testDataDirectory = new File(classLoader.getResource("testdata").getPath());
        Assert.assertTrue(testDataDirectory.exists() && testDataDirectory.isDirectory());
    }

    protected final Model getPomModel(final File aPomFile) {
        String canonicalPath = null;
        try {
            canonicalPath = aPomFile.getCanonicalPath();
            return pomReader.read(new FileReader(aPomFile));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read POM file [" + canonicalPath + "]", e);
        }
    }

    protected final File getTestDataFile(final String relativePath) {
        return new File(testDataDirectory, relativePath);
    }
}
