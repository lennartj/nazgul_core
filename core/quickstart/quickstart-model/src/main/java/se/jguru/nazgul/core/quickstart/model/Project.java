/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-model
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.quickstart.model;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Project artifact data holder, unencumbered by all the dependencies of the Maven core.
 * The Project class holds data used when creating a new project, as opposed to creating
 * a new software component (i.e. a set of collaborating Maven projects) within
 * an existing project.
 * In keeping with an orderly tradition, it is important to separate Maven build reactor
 * consistency and design from dependency management and plugin management. Build reactor
 * definition is done by maven POMs called "reactor parent" (whose role is simply to define
 * site generation and module plugins), and "parent parent" (whose role is to define dependencies
 * and plugins required to build delivery artifacts).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"name", "prefix", "reactorParent", "parentParent"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Project extends NazgulEntity implements Comparable<Project> {

    // Internal state
    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(required = false, nillable = true)
    private String prefix;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String name;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private SimpleArtifact reactorParent;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private SimpleArtifact parentParent;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Project() {
    }

    /**
     * Compound constructor creating a Project instance wrapping the supplied data.
     *
     * @param prefix        The project prefix, used as a prefix to the project name to form a unique
     *                      identifier. For example "nazgul" is the prefix for the project identifier
     *                      "nazgul-core".
     * @param name          The name of the project, such as "Entities". Should normally not contain
     *                      whitespace and is recommended to be a single word. For example,
     *                      "core" is the name for the project identifier "nazgul-core".
     * @param reactorParent The POM to be used as parent by the topmost reactor POM in the project. A
     *                      "reactor POM" is a pom.xml file that only defines the build order (and not
     *                      any dependencies or plugins for release-able artifacts such as JARs, WARs,
     *                      EARs or ZIPs).
     * @param parentParent  The POM to be used as parent by the topmost artifact parent POM in the project.
     *                      An "artifact parent POM" is a pom.xml file that defines dependencies and
     *                      profiles for artifacts released by the Maven reactor, but does not contain any
     *                      module definitions. Thus, a parent pom does not have any children.
     */
    public Project(final String prefix,
                   final String name,
                   final SimpleArtifact reactorParent,
                   final SimpleArtifact parentParent) {

        this.prefix = prefix;
        this.name = name;
        this.reactorParent = reactorParent;
        this.parentParent = parentParent;
    }

    /**
     * The project prefix, used as a prefix to the project name to form a unique identifier.
     * For example "nazgul" is the prefix for the project identifier "nazgul-core".
     *
     * @return The project prefix. May be null or empty.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * The name of the project, such as "Entities". Should normally not contain whitespace and is
     * recommended to be a single word. For example, "core" is the name for the project identifier "nazgul-core".
     *
     * @return The project name. Never null or empty.
     */
    public String getName() {
        return name;
    }

    /**
     * The POM to be used as parent by the topmost reactor POM in the project. A "reactor POM"
     * is a pom.xml file that only defines the build order (and not any dependencies or plugins
     * for release-able artifacts such as JARs, WARs, EARs or ZIPs).
     *
     * @return The GAV coordinates of the POM used as parent to the topmost reactor pom in the project.
     */
    public SimpleArtifact getReactorParent() {
        return reactorParent;
    }

    /**
     * The POM to be used as parent by the topmost artifact parent POM in the project. An
     * "artifact parent POM" is a pom.xml file that defines dependencies and profiles for
     * artifacts released by the Maven reactor, but does not contain any module definitions.
     * Thus, a parent pom does not have any immediate children projects.
     *
     * @return The GAV coordinates of the POM used as parent to the topmost parent pom in the project.
     */
    public SimpleArtifact getParentParent() {
        return parentParent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final String prefixOrEmpty = getPrefix() == null ? "[no prefix]" : getPrefix();
        return prefixOrEmpty + SimpleArtifact.DELIMITER + getName()
                + "\n ParentParent: " + getParentParent().toString()
                + "\n ReactorParent: " + getReactorParent().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        final int prefixOrEmpty = getPrefix() == null ? 0 : getPrefix().hashCode();
        return prefixOrEmpty + getName().hashCode() + getParentParent().hashCode() + getReactorParent().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        Validate.notNull(obj, "Cannot handle null obj argument.");
        return obj instanceof Project && hashCode() == obj.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Project project) {

        // Check sanity
        Validate.notNull(project, "Cannot handle null project argument.");
        if (project == this) {
            return 0;
        }

        // Delegate
        int toReturn = getName().compareTo(project.getName());
        if (toReturn == 0) {
            final String prefixOrEmpty = getPrefix() == null ? "[no prefix]" : getPrefix();
            final String thatPrefixOrEmpty = project.getPrefix() == null ? "[no prefix]" : project.getPrefix();
            toReturn = prefixOrEmpty.compareTo(thatPrefixOrEmpty);
        }
        if (toReturn == 0) {
            toReturn = getParentParent().compareTo(project.getParentParent());
        }
        if (toReturn == 0) {
            toReturn = getReactorParent().compareTo(project.getReactorParent());
        }
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(name, "name")
                .notNull(reactorParent, "reactorParent")
                .notNull(parentParent, "parentParent")
                .endExpressionAndValidate();
    }
}
