/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
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
package se.jguru.nazgul.core.quickstart.api.generator.helpers;

import se.jguru.nazgul.core.quickstart.api.DefaultStructureNavigator;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestPomAnalyzer;
import se.jguru.nazgul.core.quickstart.api.generator.AbstractComponentFactory;
import se.jguru.nazgul.core.quickstart.model.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TestComponentFactory extends AbstractComponentFactory {

    // Shared state
    public boolean useDefaultPomTemplateImplementation = false;
    public List<String> callTrace;

    public TestComponentFactory(final NamingStrategy namingStrategy) {
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

        if(!useDefaultPomTemplateImplementation) {
            callTrace.add("[" + relativeDirPath + "] ==> [" + pomType + "]");
            return "pomData: [" + pomType + "]";
        } else {
            return super.getPom(pomType, relativeDirPath, project);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTemplateDirectoryPath(final PomType pomType) {
        return "testdata/" + super.getTemplateResource(pomType);
    }
}
