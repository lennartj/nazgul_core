package se.jguru.nazgul.core.quickstart.api.generator.helpers;

import se.jguru.nazgul.core.quickstart.api.DefaultStructureNavigator;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.StructureNavigator;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestNamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestPomAnalyzer;
import se.jguru.nazgul.core.quickstart.api.generator.AbstractProjectFactory;
import se.jguru.nazgul.core.quickstart.model.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TestProjectFactory extends AbstractProjectFactory {

    // Shared state
    public StructureNavigator navigator;
    public List<String> callTrace;

    public TestProjectFactory(final NamingStrategy namingStrategy) {
        super(namingStrategy);

        // Assign internal state
        navigator = new DefaultStructureNavigator(new TestNamingStrategy(), new TestPomAnalyzer());
        callTrace = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPom(final PomType pomType,
                            final String relativeDirPath,
                            final Project project) {

        callTrace.add("[" + relativeDirPath + "] ==> [" + pomType + "]");
        return "pomData: [" + pomType + "]";
    }
}
