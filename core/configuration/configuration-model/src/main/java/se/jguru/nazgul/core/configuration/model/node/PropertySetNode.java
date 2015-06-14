/*
 * #%L
 * Nazgul Project: nazgul-core-configuration-model
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.jguru.nazgul.core.configuration.model.node;

import se.jguru.nazgul.core.algorithms.api.trees.node.MutableNode;
import se.jguru.nazgul.core.algorithms.api.trees.path.Path;
import se.jguru.nazgul.core.configuration.model.MutableProperty;

import java.io.Serializable;

/**
 * Specification for a MutableNode which holds a Set of MutableProperty objects,
 * implying the basic building block for configuration tree nodes.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface PropertySetNode<E extends Enum<E>, K extends Serializable & Comparable<K>, P extends Path<K>,
        V extends Serializable> extends MutableNode<Path<E>, MutableProperty<K, V>> {

    /**
     * Retrieves the location of this Property, which describes where in the tree of configuration
     * properties that this Property originates or resides. Location types provide the basis for
     * determining which Property should be used for reading values when a configuration change is
     * received.
     *
     * @return the location of this Property.
     */
    P getLocation();
}
