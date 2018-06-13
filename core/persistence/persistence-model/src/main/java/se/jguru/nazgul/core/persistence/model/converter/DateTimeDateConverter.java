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

package se.jguru.nazgul.core.persistence.model.converter;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.sql.Date;

/**
 * JPA AttributeConverter class to handle joda-time {@link DateTime}s - and convert them to
 * and from {@link Date}s. Uses a pre-defined {@link Chronology} for the conversion, which can be
 * re-assigned by a call to {@link DateTimeTimestampConverter#setChronology(Chronology)}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@Converter(autoApply = true)
public class DateTimeDateConverter implements AttributeConverter<DateTime, Date> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Date convertToDatabaseColumn(final DateTime attribute) {
        return attribute == null ? null : new Date(attribute.getMillis());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateTime convertToEntityAttribute(final Date dbData) {
        return dbData == null ? null : new DateTime(dbData, DateTimeTimestampConverter.jodaCronology);
    }
}
