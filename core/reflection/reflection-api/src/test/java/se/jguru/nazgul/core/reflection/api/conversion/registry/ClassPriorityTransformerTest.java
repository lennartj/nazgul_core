package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ClassPriorityTransformerTest {

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnUnrelatedClasses() {

        // Act & Assert
        ClassPriorityTransformer.getRelationDifference(String.class, Set.class);
    }

    @Test
    public void validateRelationDifference() {

        // Assemble

        // Act & Assert
        Assert.assertEquals(0, ClassPriorityTransformer.getRelationDifference(Set.class, Set.class));
        Assert.assertEquals(-1, ClassPriorityTransformer.getRelationDifference(AbstractSet.class, HashSet.class));
        Assert.assertEquals(1, ClassPriorityTransformer.getRelationDifference(HashSet.class, AbstractSet.class));
        Assert.assertEquals(-2, ClassPriorityTransformer.getRelationDifference(Object.class, Float.class));
        Assert.assertEquals(2, ClassPriorityTransformer.getRelationDifference(Integer.class, Object.class));
        Assert.assertEquals(-2, ClassPriorityTransformer.getRelationDifference(Collection.class, HashSet.class));
    }

    @Test
    public void validatePriorities() {

        // Assemble
        final List<Class<?>> classes = Arrays.asList(Object.class, Collection.class, HashSet.class);

        // Act
        final Map<Integer, Class<?>> collectionResult = CollectionAlgorithms.map(classes,
                new ClassPriorityTransformer(Collection.class));
        final Map<Integer, Class<?>> setResult = CollectionAlgorithms.map(classes,
                new ClassPriorityTransformer(Set.class));

        // Assert
        System.out.println("Coll: " + collectionResult);
        System.out.println("Set: " + setResult);

    }
}
