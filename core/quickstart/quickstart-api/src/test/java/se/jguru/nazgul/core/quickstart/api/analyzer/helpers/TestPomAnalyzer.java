/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
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
package se.jguru.nazgul.core.quickstart.api.analyzer.helpers;

import se.jguru.nazgul.core.quickstart.api.analyzer.AbstractPomAnalyzer;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TestPomAnalyzer extends AbstractPomAnalyzer {

    // Internal state
    private NamingStrategy namingStrategy;

    public TestPomAnalyzer() {
        this(new TestNamingStrategy());
    }

    public TestPomAnalyzer(final NamingStrategy namingStrategy) {
        super(namingStrategy);

        this.namingStrategy = namingStrategy;
    }

    public NamingStrategy getNamingStrategy() {
        return this.namingStrategy;
    }
}
