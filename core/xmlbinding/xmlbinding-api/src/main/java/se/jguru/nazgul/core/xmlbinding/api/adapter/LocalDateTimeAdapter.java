/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-api
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
package se.jguru.nazgul.core.xmlbinding.api.adapter;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * XML Adapter class to handle Java 8 {@link LocalDateTime} - which will convert to
 * and from Strings using the {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime unmarshal(final String transportForm) throws Exception {
        return transportForm == null ? null : LocalDateTime.parse(transportForm, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String marshal(final LocalDateTime objectForm) throws Exception {
        return objectForm == null ? null : DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(objectForm);
    }
}
