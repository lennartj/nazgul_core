/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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
package se.jguru.nazgul.core.quickstart.api.generator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestNamingStrategy;
import se.jguru.nazgul.core.quickstart.api.generator.helpers.TestComponentFactory;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.File;
import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractComponentFactoryTest {

    // Shared state
    private SortedMap<SoftwareComponentPart, String> parts2SuffixMap;
    private ComponentFactory componentFactory;
    private NamingStrategy namingStrategy;
    private File testDataDir;
    private File factoryRootDir;
    private SimpleArtifact reactorParent = new SimpleArtifact(
            "se.jguru.nazgul.tools.poms.external",
            "nazgul-tools-external-reactor-parent",
            "4.0.0");
    private SimpleArtifact parentParent = new SimpleArtifact(
            "se.jguru.nazgul.core.poms.core-parent",
            "nazgul-core-parent",
            "1.6.1");

    @Before
    public void setupSharedState() {

        namingStrategy = new TestNamingStrategy(false);
        final URL testdata = getClass().getClassLoader().getResource("testdata");
        testDataDir = new File(testdata.getPath());
        factoryRootDir = new File(testDataDir, "componentFactoryRoot");

        Assert.assertTrue(testDataDir.exists() && testDataDir.isDirectory());

        if (!factoryRootDir.exists()) {
            factoryRootDir = FileUtils.makeDirectory(testDataDir, "componentFactoryRoot");
        }

        parts2SuffixMap = new TreeMap<>();
        parts2SuffixMap.put(SoftwareComponentPart.MODEL, null);
        parts2SuffixMap.put(SoftwareComponentPart.API, null);
        parts2SuffixMap.put(SoftwareComponentPart.SPI, "foobar");
        parts2SuffixMap.put(SoftwareComponentPart.IMPLEMENTATION, "blah");

        componentFactory = new TestComponentFactory(namingStrategy);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullComponentDirectory() {

        // Act & Assert
        componentFactory.createSoftwareComponent(null, parts2SuffixMap);
    }

    @Test(expected = InvalidStructureException.class)
    public void validateExceptionOnInconsistentPartsMap() {

        // Assemble
        final File compDirectory = FileUtils.makeDirectory(factoryRootDir, "incorrectComponent");
        parts2SuffixMap.remove(SoftwareComponentPart.API);
        parts2SuffixMap.remove(SoftwareComponentPart.SPI);

        // Act & Assert
        componentFactory.createSoftwareComponent(compDirectory, parts2SuffixMap);
    }
}
