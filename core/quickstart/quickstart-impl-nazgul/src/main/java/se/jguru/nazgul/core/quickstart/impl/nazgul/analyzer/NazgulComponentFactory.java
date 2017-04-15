/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-impl-nazgul
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
package se.jguru.nazgul.core.quickstart.impl.nazgul.analyzer;

import se.jguru.nazgul.core.quickstart.api.DefaultStructureNavigator;
import se.jguru.nazgul.core.quickstart.api.StructureNavigator;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.PatternPomAnalyzer;
import se.jguru.nazgul.core.quickstart.api.generator.AbstractComponentFactory;

import java.net.URL;

/**
 * Nazgul-flavoured ComponentFactory implementation, which uses the {@code nazgul/templates} directory as the
 * root storage directory for all templates.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulComponentFactory extends AbstractComponentFactory {

    /**
     * Creates a new NazgulComponentFactory wrapping the supplied NamingStrategy and StructureNavigator.
     *
     * @param structureNavigator A StructureNavigator used to navigate the project in which this
     *                           NazgulComponentFactory should create Software Components or SoftwareComponentParts.
     * @param namingStrategy     The active NamingStrategy, used to validate supplied data.
     */
    public NazgulComponentFactory(final NamingStrategy namingStrategy, final StructureNavigator structureNavigator) {
        super(namingStrategy, structureNavigator);
    }

    /**
     * Convenience constructor, creating a new NazgulComponentFactory wrapping the supplied NamingStrategy.
     */
    public NazgulComponentFactory() {
        this(NazgulQuickstartUtils.getNazgulNamingStrategy(),
                new DefaultStructureNavigator(NazgulQuickstartUtils.getNazgulNamingStrategy(),
                        new PatternPomAnalyzer(NazgulQuickstartUtils.getNazgulNamingStrategy())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected URL getTemplateResourceURL(final String templateResourcePath) {
        return NazgulQuickstartUtils.getTemplateResourceURL(templateResourcePath);
    }
}
