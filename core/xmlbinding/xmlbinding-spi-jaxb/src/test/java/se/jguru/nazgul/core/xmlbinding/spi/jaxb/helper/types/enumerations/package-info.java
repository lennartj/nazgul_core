/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
@XmlSchema(
        namespace = "http://some.other.namespace",
        xmlns = {
                @XmlNs(prefix = "someOther", namespaceURI = "http://some.other.namespace")
        },
        elementFormDefault = XmlNsForm.UNQUALIFIED) package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.enumerations;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;


