/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-tree-model
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
package se.jguru.nazgul.core.algorithms.tree.model.node;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.trees.node.Node;
import se.jguru.nazgul.core.algorithms.api.trees.path.Path;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringNodeTest {

    @Test(expected = InternalStateValidationException.class)
    public void validateExceptionOnNullKey() {

        // Assemble
        final List<StringNode> children = new ArrayList<>();

        // Act & Assert
        new StringNode(null, "data", null, children);
    }

    @Test
    public void validateEmptyChildrenListOnConvenienceConstructor() {

        // Assemble

        // Act
        final StringNode unitUnderTest = new StringNode("key", "data", null);
        final String stringRepresentation = unitUnderTest.toString();

        // Assert
        Assert.assertNotNull("Received null children List", unitUnderTest.getChildren());
        Assert.assertEquals(0, unitUnderTest.getChildren().size());
        Assert.assertNotNull(stringRepresentation);
    }

    @Test
    public void vaidateComparisonAndEquality() {

        // Assemble
        final StringNode parent = new StringNode("parent", "data", null);
        final StringNode child1 = new StringNode("child1", "child1_data", parent);
        final StringNode child2 = new StringNode("child2", "child2_data", parent);
        final StringNode child2WithNullData = new StringNode("child2", null, parent);

        final StringNode child1WithChildren = new StringNode("child1", "child1_data", parent);
        new StringNode("grandChild", "grandChild_data", child1WithChildren);

        final List<Node<String, String>> firstChildren = child1.getChildren();
        final List<Node<String, String>> nextChildren = child1WithChildren.getChildren();

        // Act & Assert
        Assert.assertEquals(0, firstChildren.size());
        Assert.assertEquals(1, nextChildren.size());
        Assert.assertNotEquals(firstChildren.hashCode(), nextChildren.hashCode());

        Assert.assertEquals(0, parent.compareTo(parent));
        Assert.assertTrue(parent.equals(parent));
        Assert.assertFalse(parent.equals(child1));
        Assert.assertTrue(parent.compareTo(child1) != 0);
        Assert.assertFalse(child1.equals(child1WithChildren));
        Assert.assertFalse(child1.equals(child2));
        Assert.assertEquals(1, child1WithChildren.getChildren().size());
        Assert.assertFalse(child2WithNullData.compareTo(child2) == 0);
    }

    @Test
    public void validateAddingAndRemovingChildren() {

        // Assemble
        final StringNode unitUnderTest = new StringNode("key", "data", null);
        final StringNode child1 = new StringNode("child1", "child1_data", null);
        final StringNode child2 = new StringNode("child2", "child2_data", null);

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

        final StringNode unitUnderTest = new StringNode("key", "data", null);
        final StringNode child1 = new StringNode("child1", "child1_data", null);
        final StringNode child2 = new StringNode("child2", "child2_data", null);

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
        final StringNode unitUnderTest = new StringNode("key", "data", null);
        final List<StringNode> children = new ArrayList<>();
        final StringNode child = new StringNode("child", "childData", unitUnderTest, children);

        // Act
        unitUnderTest.removeChild(child);

        // Assert
        Assert.assertEquals(0, unitUnderTest.getChildren().size());
    }


    @Test
    public void validateAcquiringPath() {

        // Assemble
        final StringNode unitUnderTest = new StringNode("key", "data", null);
        final StringNode child1 = new StringNode("child1", "child1_data", null);
        final StringNode child2 = new StringNode("child2", "child2_data", null);

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

        Assert.assertEquals("key", rootPath.toString());
        Assert.assertEquals("key/child1", child1Path.toString());
        Assert.assertEquals("key/child1/child2", child2Path.toString());
    }
}
