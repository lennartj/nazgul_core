/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.blueprint;

import junit.framework.Assert;
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
