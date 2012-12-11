/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model.common;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.tree.model.MutableNode;
import se.jguru.nazgul.core.algorithms.tree.model.Node;
import se.jguru.nazgul.core.algorithms.tree.model.common.helpers.ImmutableStringStringNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class SimpleTreeTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullRoot() {

        // Act & Assert
        new SimpleTree<String, String>(null);
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnMovingNonMutableNodeChildren() {

        // Assemble
        final Node<String, String> child1 = new ImmutableStringStringNode("child1Key", "child1Value", null, null);
        final List<Node<String, String>> children = new ArrayList<Node<String, String>>();
        children.add(child1);

        final MutableNode<String, String> root1 = new ListMutableNode<String, String>(
                "root1Key", "root1Value", null, children);
        final MutableNode<String, String> root2 = new ListMutableNode<String, String>(
                "root2Key", "root2Value", null, null);


        final SimpleTree<String, String> unitUnderTest = new SimpleTree<String, String>(root1);

        // Act & Assert
        unitUnderTest.setRoot(root2);
    }

    @Test
    public void validateReassigningRoot() {

        // Assemble
        final MutableNode<String, String> root1 = new ListMutableNode<String, String>(
                "root1Key", "root1Value", null, null);
        final MutableNode<String, String> root2 = new ListMutableNode<String, String>(
                "root2Key", "root2Value", null, null);
        final MutableNode<String, String> child1 = new ListMutableNode<String, String>(
                "child1Key", "child1Value", null, null);

        // Act & Assert #1
        final SimpleTree<String, String> unitUnderTest = new SimpleTree<String, String>(root1);
        final MutableNode<String, String> originalRoot = unitUnderTest.getRoot();
        originalRoot.addChild(child1);

        Assert.assertSame(root1, originalRoot);
        Assert.assertSame(child1, originalRoot.getChildren().get(0));
        Assert.assertSame(root1, child1.getParent());

        // Act & Assert #2
        final MutableNode<String, String> oldRoot = unitUnderTest.setRoot(root2);

        Assert.assertSame(root1, oldRoot);
        Assert.assertEquals(0, oldRoot.getChildren().size());
        Assert.assertEquals(1, root2.getChildren().size());
        Assert.assertSame(child1, unitUnderTest.getRoot().getChildren().get(0));

        // Act & Assert #3

    }

    @Test
    public void validateAcquiringNodes() {

        // Assemble
        final MutableNode<String, String> root1 = new ListMutableNode<String, String>(
                "root1Key", "root1Value", null, null);
        final MutableNode<String, String> child1 = new ListMutableNode<String, String>(
                "child1Key", "child1Value", null, null);

        // Act
        final SimpleTree<String, String> unitUnderTest = new SimpleTree<String, String>(root1);
        final MutableNode<String, String> originalRoot = unitUnderTest.getRoot();
        originalRoot.addChild(child1);

        final ListPath<String> path1 = new ListPath<String>(Arrays.asList("root2Key", "child1Key"));
        final ListPath<String> path2 = new ListPath<String>(Arrays.asList("root1Key", "child1Key"));

        // Assert
        Assert.assertNull(unitUnderTest.get(path1));
        Assert.assertSame(child1, unitUnderTest.get(path2));
    }
}
