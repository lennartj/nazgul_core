package se.jguru.nazgul.core.quickstart.model;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ProjectTest extends AbstractStandardizedTimezoneTest {

    // Shared state
    private JaxbXmlBinder binder;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupSharedState() {
        binder = new JaxbXmlBinder();
    }

    @Test
    public void validateMarshalling() {

        // Assemble
        final SimpleArtifact reactorParent = new SimpleArtifact(
                "reactorGroupId", "reactorArtifactId", "reactorMavenVersion");
        final SimpleArtifact parentParent = new SimpleArtifact("groupId", "artifactId", "mavenVersion");
        final Project unitUnderTest = new Project("prefix", "name", "reactorName", reactorParent, parentParent);

        final String expected = XmlTestUtils.readFully("testdata/project.xml");

        // Act
        final String result = binder.marshal(unitUnderTest);

        // Assert
        XmlTestUtils.compareXmlIgnoringWhitespace(expected, result);
    }
}
