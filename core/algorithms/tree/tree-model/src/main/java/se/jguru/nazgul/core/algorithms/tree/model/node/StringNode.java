/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-tree-model
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

import se.jguru.nazgul.core.algorithms.api.trees.node.Node;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the AbstractStringKeyNode which uses Strings as values.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"data", "children"})
@XmlAccessorType(XmlAccessType.FIELD)
public class StringNode extends AbstractStringKeyNode<String> {

    // Internal state

    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(nillable = true, required = false)
    private String data;

    @XmlElementWrapper(name = "children", nillable = true, required = false)
    @XmlElement(name = "child")
    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    /*@JoinTable(name = "nodes_children",
            joinColumns = @JoinColumn(name = "node_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "child_id", referencedColumnName = "id")) */
    private List<StringNode> children;

    /**
     * JPA/JAXB-friendly constructor.
     */
    public StringNode() {
    }

    /**
     * Convenience constructor creating a StringNode wrapping the supplied
     * data and sporting an empty List of children.
     *
     * @param key    The nonempty key of this StringNode.
     * @param data   The value of this StringNode. May be null.
     * @param parent The parent StringNode of this one. May be null only if this is a Root node.
     */
    public StringNode(final String key,
                      final String data,
                      final StringNode parent) {
        this(key, data, parent, new ArrayList<StringNode>());
    }

    /**
     * Compound constructor creating a StringNode wrapping the supplied data.
     *
     * @param key      The nonempty key of this StringNode.
     * @param data     The value of this StringNode. May be null.
     * @param parent   The parent StringNode of this one. May be null only if this is a Root node.
     * @param children The children of this StringNode. May be empty but not null.
     */
    public StringNode(final String key,
                      final String data,
                      final StringNode parent,
                      final List<StringNode> children) {

        super(key, parent);

        // Assign internal state
        this.data = data;
        this.children = children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Node<String, String>> List<X> getChildren() {
        return (List<X>) children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getData() {
        return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Node<String, String> that) {

        int toReturn = super.compareTo(that);
        if (toReturn == 0) {
            if (getData() != null) {
                toReturn = getData().compareTo(that.getData());
            } else if (that.getData() != null) {
                toReturn = 1;
            }
        }

        if (toReturn == 0) {
            // Simply check the number of children;
            // Don't perform a recursive analysis.
            toReturn = getChildren().size() - that.getChildren().size();
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // First, delegate.
        super.validateEntityState();

        // The children set may be empty but not null.
        InternalStateValidationException.create()
                .notNull(children, "children")
                .endExpressionAndValidate();
    }
}
