/*-
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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
import se.jguru.nazgul.core.quickstart.api.analyzer.PatternPomAnalyzer;

import java.lang.reflect.Field;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TestPatternPomAnalyzer extends PatternPomAnalyzer {

    // Shared state
    public TestPatternPomAnalyzer() {
        super(new TestNamingStrategy());
    }

    public NamingStrategy getNamingStrategy() {

        try {
            final Field namingStrategyField = AbstractPomAnalyzer.class.getDeclaredField("namingStrategy");
            namingStrategyField.setAccessible(true);

            return (NamingStrategy) namingStrategyField.get(this);
        } catch (Exception e) {
            throw new IllegalStateException("Could not acquire the NamingStrategy field.", e);
        }
    }
}
