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
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Maven artifact data holder, unencumbered by all the dependencies of the Maven core.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"groupId", "artifactId", "mavenVersion"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SimpleArtifact extends NazgulEntity {

    // Internal state
    private String groupId;
    private String artifactId;
    private String mavenVersion;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public SimpleArtifact() {
    }

    /**
     * Compound constructor creating a SimpleArtifact, wrapping ths supplied data.
     *
     * @param groupId      The Maven groupId. Cannot be null or empty.
     * @param artifactId   The Maven ArtifactId. Cannot be null or empty.
     * @param mavenVersion The Maven version. Cannot be null or empty.
     */
    public SimpleArtifact(final String groupId, final String artifactId, final String mavenVersion) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.mavenVersion = mavenVersion;
    }

    /**
     * @return The Maven groupId. Never null or empty.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return The Maven artifactId. Never null or empty.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return The Maven version. Never null or empty.
     */
    public String getMavenVersion() {
        return mavenVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(groupId, "groupId")
                .notNullOrEmpty(artifactId, "artifactId")
                .notNullOrEmpty(mavenVersion, "mavenVersion")
                .endExpressionAndValidate();
    }
}
