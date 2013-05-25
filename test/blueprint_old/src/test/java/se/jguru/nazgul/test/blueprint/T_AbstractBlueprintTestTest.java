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

package se.jguru.nazgul.test.blueprint;

import org.junit.Assert;
import org.apache.commons.lang3.ClassUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;
import se.jguru.nazgul.test.blueprint.types.Sudoku;

import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractBlueprintTestTest {

    @Ignore
    @Test
    public void validateBlueprintContainerConfiguration() throws Exception {

        // Assemble
        final MockBlueprintTest unitUnderTest = new MockBlueprintTest(true);

        // Act
        unitUnderTest.setupSharedState();

        for (ServiceReference current : unitUnderTest.registry.getServiceReferences(null, null)) {
            final Bundle currentBundle = current.getBundle();
            final Object theService = currentBundle.getBundleContext().getService(current);

            final List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(theService.getClass());
            final List<String> relevantTypes = CollectionAlgorithms.transform(
                    allInterfaces,
                    new Transformer<Class<?>, String>() {
                        @Override
                        public String transform(final Class<?> input) {
                            return input.getCanonicalName();
                        }
                    });

            System.out.println("Bundle [" + currentBundle.getSymbolicName() + " (" + currentBundle.getLocation()
                    + ")] exports service [" + theService.getClass().getSimpleName() + "] with types "
                    + relevantTypes);
        }

        // Assert
    }

    @Ignore
    @Test
    public void validateSynthesizingBlueprintComponentsInTestScope() throws Exception {

        // Assemble
        final MockBlueprintTest unitUnderTest = new MockBlueprintTest(true, "testdata/blueprint/sudokuConfig.xml");

        // Act
        unitUnderTest.setupSharedState();

        final Collection<ServiceReference<Sudoku>> sudokuRefs = unitUnderTest.registry
                .getBundleContext().getServiceReferences(Sudoku.class, null);

        // Assert
        Assert.assertEquals(2, sudokuRefs.size());

        for(ServiceReference<Sudoku> current : sudokuRefs) {

            final String difficulty = (String) current.getProperty("difficulty");
            Assert.assertNotNull(difficulty);

            final Sudoku aSudoku = (Sudoku) unitUnderTest.registry.getService(current);
            System.out.println("Got sudoku: " + aSudoku.getClass().getName() + " with supertypes "
                    + ClassUtils.getAllSuperclasses(aSudoku.getClass()));

            final boolean hard = "complex".equals(difficulty);
            Assert.assertEquals(hard, aSudoku.isHard());
        }
    }
}
