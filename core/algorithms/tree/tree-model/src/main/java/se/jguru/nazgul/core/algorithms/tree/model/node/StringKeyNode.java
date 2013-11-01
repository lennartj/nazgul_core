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
package se.jguru.nazgul.core.algorithms.tree.model.node;

import se.jguru.nazgul.core.algorithms.api.trees.node.MutableNode;
import se.jguru.nazgul.core.algorithms.api.trees.node.Node;
import se.jguru.nazgul.core.algorithms.api.trees.path.Path;
import se.jguru.nazgul.core.algorithms.tree.model.path.StringPath;

import java.io.Serializable;
import java.util.List;

/**
 * AbstractMutableNode implementation using Strings for keys.
 * This implementation uses StringPaths when creating a path.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringKeyNode<ValueType extends Serializable> extends AbstractLegacyMutableNode<String, ValueType> {

    /**
     * Compound constructor, creating a new StringKeyNode instance from the provided arguments.
     *
     * @param key      The key of this StringKeyNode instance.
     * @param data     The data of this StringKeyNode instance.
     * @param parent   The parent of this StringKeyNode instance.
     * @param children The children of this StringKeyNode instance. Can be {@code null}, in which
     *                 case a new ArrayList will be created and used for the children.
     */
    public StringKeyNode(final String key,
                         final ValueType data,
                         final MutableNode<String, ValueType> parent,
                         final List<Node<String, ValueType>> children) {

        // Delegate
        super(key, data, parent, children);
    }

    /**
     * Creats a new StringPath from the supplied key value.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    protected <X extends Path<String>> X makePath(final String key) {
        return (X) new StringPath(key);
    }
}
