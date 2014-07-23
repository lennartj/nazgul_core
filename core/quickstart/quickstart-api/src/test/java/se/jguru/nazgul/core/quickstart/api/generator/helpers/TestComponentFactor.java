package se.jguru.nazgul.core.quickstart.api.generator.helpers;

import se.jguru.nazgul.core.quickstart.api.DefaultStructureNavigator;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.StructureNavigator;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestPomAnalyzer;
import se.jguru.nazgul.core.quickstart.api.generator.AbstractComponentFactory;
import se.jguru.nazgul.core.quickstart.model.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TestComponentFactor extends AbstractComponentFactory {

    // Shared state
    public List<String> callTrace;

    public TestComponentFactor(final NamingStrategy namingStrategy) {
        super(namingStrategy, new DefaultStructureNavigator(namingStrategy, new TestPomAnalyzer(namingStrategy)));

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
