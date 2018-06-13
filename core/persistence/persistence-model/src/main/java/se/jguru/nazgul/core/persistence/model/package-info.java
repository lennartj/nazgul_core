/*-
 * #%L
 * Nazgul Project: nazgul-core-persistence-model
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

/**
 * Package containing abstract implementations normally serving as superclasses to define entities.
 * The {@link se.jguru.nazgul.core.persistence.model.NazgulEntity} class provides an encapsulation of
 * required JPA properties, permitting the user to
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see javax.xml.bind.annotation.XmlRootElement
 */
@XmlSchema(
        xmlns = {
                @XmlNs(prefix = "core", namespaceURI = "http://www.jguru.se/nazgul/core"),
                @XmlNs(prefix = "xs", namespaceURI = "http://www.w3.org/2001/XMLSchema"),
                @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"),
                @XmlNs(prefix = "vc", namespaceURI = "http://www.w3.org/2007/XMLSchema-versioning")
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
package se.jguru.nazgul.core.persistence.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
