/*
 * #%L
 * Nazgul Project: nazgul-core-tree-model
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
package se.jguru.nazgul.core.algorithms.tree.model.common.helpers;


import se.jguru.nazgul.core.algorithms.tree.model.MutableNode;
import se.jguru.nazgul.core.algorithms.tree.model.Node;
import se.jguru.nazgul.core.algorithms.tree.model.Path;
import se.jguru.nazgul.core.algorithms.tree.model.common.AbstractMutableNode;

import java.util.Arrays;
import java.util.List;

/**
 * A node relating String names to String process values.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ProcessNode extends AbstractMutableNode<String, String> {

    /**
     * {@inheritDoc}
     */
    public ProcessNode(final String key,
                       final String data,
                       final MutableNode<String, String> parent) {

        // Delegate
        this(key, data, parent, null);
    }


    /**
     * {@inheritDoc}
     */
    public ProcessNode(final String key,
                       final String data,
                       final MutableNode<String, String> parent,
                       final List<Node<String, String>> children) {

        // Delegate
        super(key, data, parent, children);
    }

    /**
     * Override this method to create (and use) a custom Path
     * implementation for this AbstractMutableNode instance.
     * The default implementation uses {@code AbstractListPath}.
     *
     * @param key The key to convert to a Path.
     * @param <X> The Path type (or subtype thereof, such as {@code SemanticPath}) returned.
     * @return The Path made from the provided key.
     */
    @Override
    protected <X extends Path<String>> X makePath(final String key) {
        return (X) ProcessStringPath.create(Arrays.asList(key));
    }
}
