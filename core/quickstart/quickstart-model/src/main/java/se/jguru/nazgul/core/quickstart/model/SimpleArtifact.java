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
 * Maven artifact data holder, unencumbered by all the dependencies of the Maven core.
 * This class does not contain all the mechanics of the Maven core, implying that the
 * user must take care not to inject insane values (for groupId, artifactId, type etc.).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"groupId", "artifactId", "mavenVersion"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SimpleArtifact extends NazgulEntity implements Comparable<SimpleArtifact> {

    /**
     * String delimiter for SimpleArtifact data.
     */
    public static final String DELIMITER = "/";

    // Internal state
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String groupId;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String artifactId;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
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
    public int compareTo(final SimpleArtifact art) {

        // Check sanity
        Validate.notNull(art, "Cannot handle null art argument.");
        if (this == art) {
            return 0;
        }

        // Delegate
        int toReturn = getGroupId().compareTo(art.getGroupId());
        if (toReturn == 0) {
            toReturn = getArtifactId().compareTo(art.getArtifactId());
        }
        if (toReturn == 0) {
            toReturn = getMavenVersion().compareTo(art.getMavenVersion());
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getGroupId() + DELIMITER + getArtifactId() + DELIMITER + getMavenVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getGroupId().hashCode()
                + getArtifactId().hashCode()
                + getMavenVersion().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        Validate.notNull(obj, "Cannot handle null obj argument.");
        if (obj instanceof SimpleArtifact) {

            final SimpleArtifact that = (SimpleArtifact) obj;
            return getGroupId().equals(that.getGroupId())
                    && getArtifactId().equals(that.getArtifactId())
                    && getMavenVersion().equals(that.getMavenVersion());
        }

        // All done.
        return false;
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
