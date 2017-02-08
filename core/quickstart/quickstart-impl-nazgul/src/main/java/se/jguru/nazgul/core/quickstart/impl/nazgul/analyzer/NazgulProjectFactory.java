/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-impl-nazgul
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.jguru.nazgul.core.quickstart.impl.nazgul.analyzer;

import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.generator.AbstractProjectFactory;

import java.net.URL;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulProjectFactory extends AbstractProjectFactory {

    public NazgulProjectFactory() {
        this(NazgulQuickstartUtils.getNazgulNamingStrategy());
    }

    /**
     * Creates a new AbstractProjectFactory wrapping the supplied data.
     *
     * @param namingStrategy The active NamingStrategy, used to validate given Project data.
     */
    public NazgulProjectFactory(final NamingStrategy namingStrategy) {
        super(namingStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected URL getTemplateResourceURL(final String templateResourcePath) {
        return NazgulQuickstartUtils.getTemplateResourceURL(templateResourcePath);
    }
}