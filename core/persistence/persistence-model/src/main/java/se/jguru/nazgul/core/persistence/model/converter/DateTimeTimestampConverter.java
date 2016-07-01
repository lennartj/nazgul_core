/*-
 * #%L
 * Nazgul Project: nazgul-core-persistence-model
 * %%
 * Copyright (C) 2010 - 2016 jGuru Europe AB
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

import org.apache.commons.lang3.Validate;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.sql.Timestamp;

/**
 * JPA AttributeConverter class to handle joda-time {@link DateTime}s - and convert them to
 * and from {@link Timestamp}s. Uses a pre-defined {@link Chronology} for the conversion, which can be
 * re-assigned by a call to {@link #setChronology(Chronology)}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@Converter(autoApply = true)
public class DateTimeTimestampConverter implements AttributeConverter<DateTime, Timestamp> {

    /**
     * The chronology used to convert TimeStamps into DateTimes.
     */
    static Chronology jodaCronology = ISOChronology.getInstance();

    /**
     * Re-assigns the joda-time {@link Chronology} with the supplied value.
     *
     * @param chronology A non-null {@link Chronology} instance used to convert {@link Timestamp} instances to
     *                   {@link DateTime}.
     */
    public static void setChronology(final Chronology chronology) {

        // Check sanity
        Validate.notNull(chronology, "Cannot handle null 'chronology' argument.");

        // Re-assign the chronology.
        jodaCronology = chronology;
    }

    /**
     * Retrieves the currently set Chronology, used to convert to joda-time Instants.
     *
     * @return the currently set Chronology, used to convert to joda-time Instants.
     */
    public static Chronology getJodaCronology() {
        return jodaCronology;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp convertToDatabaseColumn(final DateTime attribute) {
        return attribute == null ? null : new Timestamp(attribute.getMillis());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateTime convertToEntityAttribute(final Timestamp dbData) {
        return dbData == null ? null : new DateTime(dbData, jodaCronology);
    }
}
