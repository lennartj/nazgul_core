/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-model
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
package se.jguru.nazgul.core.quickstart.model;

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
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"name", "reactorName",
        "prefix", "reactorParent", "parentParent"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Project extends NazgulEntity {

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
    private String reactorName;

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
     * @param prefix        The project prefix, used as a prefix to the project name to form a unique identifier.
     *                      For example "nazgul" is the prefix for the project identifier "nazgul-core".
     * @param name          The name of the project, such as "Entities". Should normally not contain whitespace and is
     *                      recommended to be a single word. For example, "core" is the name for the project
     *                      identifier "nazgul-core".
     * @param reactorName   The name of the reactor of a project. This is used for topmost folder name and
     *                      affects Maven site documentation.
     * @param reactorParent The POM to be used as parent by the topmost reactor POM in the project. A "reactor POM"
     *                      is a pom.xml file that only defines the build order (and not any dependencies or plugins
     *                      for release-able artifacts such as JARs, WARs, EARs or ZIPs).
     * @param parentParent  The POM to be used as parent by the topmost artifact parent POM in the project. An
     *                      "artifact parent POM" is a pom.xml file that defines dependencies and profiles for
     *                      artifacts released by the Maven reactor, but does not contain any module definitions.
     *                      Thus, a parent pom does not have any children.
     */
    public Project(final String prefix,
                   final String name,
                   final String reactorName,
                   final SimpleArtifact reactorParent,
                   final SimpleArtifact parentParent) {

        this.prefix = prefix;
        this.name = name;
        this.reactorName = reactorName;
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
     * The name of the reactor of a project. This is used for topmost folder name and affects Maven site documentation.
     *
     * @return The name of the reactor of a project. Never null or empty.
     */
    public String getReactorName() {
        return reactorName;
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
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(name, "name")
                .notNullOrEmpty(reactorName, "reactorName")
                .notNull(reactorParent, "reactorParent")
                .notNull(parentParent, "parentParent")
                .endExpressionAndValidate();
    }
}
