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

import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.analyzer.AbstractNamingStrategy;
import se.jguru.nazgul.core.quickstart.model.Name;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TestNamingStrategy extends AbstractNamingStrategy {

    // Shared state
    public boolean throwException = false;

    public TestNamingStrategy() {
        this(false);
    }

    public TestNamingStrategy(final boolean prefixIsRequiredOnAllFolders) {
        super(prefixIsRequiredOnAllFolders);
    }

    @Override
    public void validate(final Name aName, final PomType pomType) throws IllegalArgumentException {

        if (throwException) {
            throw new IllegalArgumentException("Instructed to throw an Exception.");
        }
    }
}
