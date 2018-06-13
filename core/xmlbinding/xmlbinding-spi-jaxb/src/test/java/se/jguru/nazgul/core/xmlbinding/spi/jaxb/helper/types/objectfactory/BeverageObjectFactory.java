/*-
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
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

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.objectfactory;

import org.apache.commons.lang3.Validate;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRegistry
public class BeverageObjectFactory {

    public static final String NAMESPACE_URI = "http://www.jguru.se/beverages";
    public static final String PREFIX = "beverages";

    @XmlElementDecl(name = "beer", namespace = NAMESPACE_URI, scope = Beverages.class)
    public JAXBElement<Beer> createBeer(final Beer aBeer) {

        // Check sanity
        Validate.notNull(aBeer, "Cannot handle null 'aBeer' argument.");

        // All done.
        return new JAXBElement<Beer>(getQName("beer"), Beer.class, aBeer);
    }

    @XmlElementDecl(name = "soda", namespace = NAMESPACE_URI, scope = Beverages.class)
    public JAXBElement<Soda> createSoda(final Soda aSoda) {

        // Check sanity
        Validate.notNull(aSoda, "Cannot handle null 'aSoda' argument.");

        // All done.
        return new JAXBElement<Soda>(getQName("soda"), Soda.class, aSoda);
    }

    //
    // Private helpers
    //

    private QName getQName(final String localPart) {
        return new QName(NAMESPACE_URI, localPart, PREFIX);
    }
}
