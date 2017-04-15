/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-impl-nazgul
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
package se.jguru.nazgul.core.quickstart.impl.nazgul.analyzer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.model.Name;

import java.io.File;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulNamingStrategyTest {

    // Shared state
    @SuppressWarnings("all")
    private File tmpTmpIoFileDir;
    private NazgulNamingStrategy unitUnderTest;

    @Before
    public void setupSharedState() {

        // Redirect the java.io.tmpdir
        tmpTmpIoFileDir = FileTestUtilities.createTmpDirectory(true);

        unitUnderTest = new NazgulNamingStrategy();
    }

    @After
    public void restoreSharedState() {
        FileTestUtilities.restoreOriginalTmpDirectory();
    }

    @Test
    public void validateNameStandard() {

        // Assemble
        final Name fooReactorParent = Name.parse("nazgul-foo-reactor");
        final Name fooPomsReactorParent = Name.parse("nazgul-foo-poms-reactor");

        final Name fooParent = Name.parse("nazgul-foo-parent");
        final Name fooApiParent = Name.parse("nazgul-foo-api-parent");
        final Name fooModelParent = Name.parse("nazgul-foo-model-parent");
        final Name fooWarParent = Name.parse("nazgul-foo-war-parent");
        final Name fooEarParent = Name.parse("nazgul-foo-ear-parent");

        // Act & Assert
        unitUnderTest.validate(fooReactorParent, PomType.ROOT_REACTOR);
        unitUnderTest.validate(fooPomsReactorParent, PomType.REACTOR);

        unitUnderTest.validate(fooParent, PomType.PARENT);
        unitUnderTest.validate(fooApiParent, PomType.API_PARENT);
        unitUnderTest.validate(fooModelParent, PomType.MODEL_PARENT);
        unitUnderTest.validate(fooWarParent, PomType.WAR_PARENT);
        unitUnderTest.validate(fooEarParent, PomType.OTHER_PARENT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNullPrefix() {

        // Act & Assert
        unitUnderTest.validate(Name.parse("foo-reactor"), PomType.ROOT_REACTOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnIncorrectPrefix() {

        // Act & Assert
        unitUnderTest.validate(Name.parse("some-nice-reactor"), PomType.ROOT_REACTOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnIncorrectSeparator() {

        // Act & Assert
        unitUnderTest.validate(Name.parse("nazgul#bar#reactor", "#"), PomType.ROOT_REACTOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnIncorrectType() {

        // Act & Assert
        unitUnderTest.validate(Name.parse("nazgul-foo-parent"), PomType.ROOT_REACTOR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnIncorrectSuffix() {

        // Act & Assert
        unitUnderTest.validate(Name.parse("nazgul-foo-something"), PomType.ROOT_REACTOR);
    }
}
