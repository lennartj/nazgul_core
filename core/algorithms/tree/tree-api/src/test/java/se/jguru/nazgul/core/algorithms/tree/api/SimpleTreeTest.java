/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-tree-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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

package se.jguru.nazgul.core.algorithms.tree.api;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.tree.api.common.SimpleTree;
import se.jguru.nazgul.core.algorithms.tree.api.common.StringKeyNode;
import se.jguru.nazgul.core.algorithms.tree.api.helpers.ImmutableStringStringNode;
import se.jguru.nazgul.core.algorithms.tree.model.common.AbstractPath;
import se.jguru.nazgul.core.algorithms.tree.model.common.StringPath;

import java.util.ArrayList;
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

        final MutableNode<String, String> root1 = new StringKeyNode<String>(
                "root1Key", "root1Value", null, children);
        final MutableNode<String, String> root2 = new StringKeyNode<String>(
                "root2Key", "root2Value", null, null);


        final SimpleTree<String, String> unitUnderTest = new SimpleTree<String, String>(root1);

        // Act & Assert
        unitUnderTest.setRoot(root2);
    }

    @Test
    public void validateReassigningRoot() {

        // Assemble
        final MutableNode<String, String> root1 = new StringKeyNode<String>(
                "root1Key", "root1Value", null, null);
        final MutableNode<String, String> root2 = new StringKeyNode<String>(
                "root2Key", "root2Value", null, null);
        final MutableNode<String, String> child1 = new StringKeyNode<String>(
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
    }

    @Test
    public void validateAcquiringNodes() {

        // Assemble
        final MutableNode<String, String> root1 = new StringKeyNode<String>(
                "root1Key", "root1Value", null, null);
        final MutableNode<String, String> child1 = new StringKeyNode<String>(
                "child1Key", "child1Value", null, null);

        // Act
        final SimpleTree<String, String> unitUnderTest = new SimpleTree<String, String>(root1);
        final MutableNode<String, String> originalRoot = unitUnderTest.getRoot();
        originalRoot.addChild(child1);

        final AbstractPath<String> path1 = new StringPath("root2Key/child1Key");
        final AbstractPath<String> path2 = new StringPath("root1Key/child1Key");

        // Assert
        Assert.assertNull(unitUnderTest.get(path1));
        Assert.assertSame(child1, unitUnderTest.get(path2));
    }
}
