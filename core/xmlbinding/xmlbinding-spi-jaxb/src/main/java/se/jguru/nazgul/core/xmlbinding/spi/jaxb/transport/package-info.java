/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
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
/**
 * <p>The transport package contains classes which assist in marshalling Java object
 * graphs into XML. Specifically, the EntityTransporter class is the generic holder
 * used to convert un-marshallable types into marshallable types as well as containing
 * the type metadata for all classes transported. This is required in order to properly
 * unmarshal/resurrect all objects on the receiving side.</p>
 * <p>Moreover, this package-info file also holds schema name prefix definitions
 * for the well-known Nazgul namespaces.</p>
 *
 * @see se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter
 */
@XmlSchema(
        namespace = "http://www.jguru.se/nazgul/core",
        xmlns = {
                @XmlNs(prefix = "core", namespaceURI = "http://www.jguru.se/nazgul/core")
        }, elementFormDefault = XmlNsForm.UNQUALIFIED)
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
