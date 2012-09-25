/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.trees.helpers.StringStringNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ListMutableNodeTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullKey() {

        // Assemble
        final List<Node<String, String>> children = new ArrayList<Node<String, String>>();

        // Act & Assert
        new StringStringNode(null, "data", null, children);
    }

    @Test
    public void validateEmptyChildrenListOnNullChildrenArgument() {

        // Assemble

        // Act
        final StringStringNode unitUnderTest = new StringStringNode("key", "data", null, null);

        // Assert
        Assert.assertNotNull("Received null children List", unitUnderTest.getChildren());
        Assert.assertEquals(0, unitUnderTest.getChildren().size());
        Assert.assertNotNull(unitUnderTest.toString());
    }

    @Test
    public void validateAddingAndRemovingChildren() {

        // Assemble
        final StringStringNode unitUnderTest = new StringStringNode("key", "data", null, null);
        final StringStringNode child1 = new StringStringNode("child1", "child1_data", null, null);
        final StringStringNode child2 = new StringStringNode("child2", "child2_data", null, null);

        // Act & Assert #1
        Assert.assertEquals(0, unitUnderTest.getChildren().size());

        unitUnderTest.addChild(child1);
        unitUnderTest.addChild(child2);

        Assert.assertSame(unitUnderTest, child1.getParent());
        Assert.assertSame(unitUnderTest, child2.getParent());
        Assert.assertSame(child1, unitUnderTest.getChildren().get(0));
        Assert.assertSame(child2, unitUnderTest.getChildren().get(1));

        // Act & Assert #2
        Assert.assertEquals(2, unitUnderTest.getChildren().size());

        unitUnderTest.removeChild(child1);
        unitUnderTest.removeChild(child2);

        Assert.assertNull(child1.getParent());
        Assert.assertNull(child2.getParent());
        Assert.assertEquals(0, unitUnderTest.getChildren().size());
    }

    @Test
    public void validateRemovingChildrenUsingFiltering() {

        // Assemble
        final Filter<Node<String, String>> removeChildrenFilter = new Filter<Node<String, String>>() {
            @Override
            public boolean accept(final Node<String, String> candidate) {
                return candidate.getKey().equals("child2");
            }
        };

        final StringStringNode unitUnderTest = new StringStringNode("key", "data", null, null);
        final StringStringNode child1 = new StringStringNode("child1", "child1_data", null, null);
        final StringStringNode child2 = new StringStringNode("child2", "child2_data", null, null);

        // Act & Assert #1
        Assert.assertEquals(0, unitUnderTest.getChildren().size());

        unitUnderTest.addChild(child1);
        unitUnderTest.addChild(child2);

        Assert.assertSame(unitUnderTest, child1.getParent());
        Assert.assertSame(unitUnderTest, child2.getParent());
        Assert.assertSame(child1, unitUnderTest.getChildren().get(0));
        Assert.assertSame(child2, unitUnderTest.getChildren().get(1));

        // Act & Assert #2
        Assert.assertEquals(2, unitUnderTest.getChildren().size());

        unitUnderTest.removeChildren(removeChildrenFilter);

        Assert.assertSame(unitUnderTest, child1.getParent());
        Assert.assertNull(child2.getParent());
        Assert.assertEquals(1, unitUnderTest.getChildren().size());
        Assert.assertEquals(child1, unitUnderTest.getChildren().get(0));
    }

    @Test
    public void validateNoExceptionOnRemovingNonExistentChild() {

        // Assemble
        final StringStringNode unitUnderTest = new StringStringNode("key", "data", null, null);
        final List<Node<String, String>> children = new ArrayList<Node<String, String>>();
        final StringStringNode child = new StringStringNode("child", "childData", unitUnderTest, children);

        // Act
        unitUnderTest.removeChild(child);

        // Assert
        Assert.assertEquals(0, unitUnderTest.getChildren().size());
    }


    @Test
    public void validateAcquiringPath() {

        // Assemble
        final StringStringNode unitUnderTest = new StringStringNode("key", "data", null, null);
        final StringStringNode child1 = new StringStringNode("child1", "child1_data", null, null);
        final StringStringNode child2 = new StringStringNode("child2", "child2_data", null, null);

        unitUnderTest.addChild(child1);
        child1.addChild(child2);

        // Act
        final Path<String> rootPath = unitUnderTest.getPath();
        final Path<String> child1Path = child1.getPath();
        final Path<String> child2Path = child2.getPath();

        // Assert
        Assert.assertEquals(1, rootPath.size());
        Assert.assertEquals("key", rootPath.get(0));
        Assert.assertEquals("key", rootPath.iterator().next());

        Assert.assertEquals(2, child1Path.size());
        Assert.assertEquals("key", child1Path.get(0));
        Assert.assertEquals("child1", child1Path.get(1));

        Assert.assertEquals(3, child2Path.size());
        Assert.assertEquals("key", child2Path.get(0));
        Assert.assertEquals("child1", child2Path.get(1));
        Assert.assertEquals("child2", child2Path.get(2));

        Assert.assertEquals("{ key }", rootPath.toString());
        Assert.assertEquals("{ key/child1 }", child1Path.toString());
        Assert.assertEquals("{ key/child1/child2 }", child2Path.toString());
    }
}
