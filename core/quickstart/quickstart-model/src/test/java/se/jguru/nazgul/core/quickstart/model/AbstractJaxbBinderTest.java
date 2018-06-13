/*-
 * #%L
 * Nazgul Project: nazgul-core-quickstart-model
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

package se.jguru.nazgul.core.quickstart.model;

import org.junit.Before;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractJaxbBinderTest extends AbstractStandardizedTimezoneTest {

    // Shared state
    protected JaxbXmlBinder binder;
    protected JaxbNamespacePrefixResolver namespacePrefixResolver;

    @Before
    public void setupSharedState() {

        namespacePrefixResolver = new JaxbNamespacePrefixResolver();
        binder = new JaxbXmlBinder(namespacePrefixResolver);
    }
}
