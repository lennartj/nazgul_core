/*-
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-api
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
 * Package holding entity classes defining Organisations in the context of the mithlond reactor.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlSchema(
        xmlns = {
                @XmlNs(prefix = "core", namespaceURI = XmlBinder.CORE_NAMESPACE),
                @XmlNs(prefix = "xs", namespaceURI = "http://www.w3.org/2001/XMLSchema"),
                @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"),
                @XmlNs(prefix = "vc", namespaceURI = "http://www.w3.org/2007/XMLSchema-versioning")
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlJavaTypeAdapters({
        @XmlJavaTypeAdapter(type = LocalDate.class, value = LocalDateAdapter.class),
        @XmlJavaTypeAdapter(type = LocalTime.class, value = LocalTimeAdapter.class),
        @XmlJavaTypeAdapter(type = LocalDateTime.class, value = LocalDateTimeAdapter.class),
        @XmlJavaTypeAdapter(type = ZonedDateTime.class, value = ZonedDateTimeAdapter.class),
        @XmlJavaTypeAdapter(type = TimeZone.class, value = TimeZoneAdapter.class)
})
package se.jguru.nazgul.core.xmlbinding.api.adapter;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.TimeZone;
