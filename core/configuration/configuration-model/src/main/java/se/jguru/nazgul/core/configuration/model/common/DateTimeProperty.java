/*
 * #%L
 * Nazgul Project: nazgul-core-configuration-model
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
package se.jguru.nazgul.core.configuration.model.common;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import se.jguru.nazgul.core.configuration.model.AbstractStringKeyedMutableProperty;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Calendar;

/**
 * AbstractStringKeyedMutableProperty implementation using DateTimes as values.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"value"})
@XmlAccessorType(XmlAccessType.FIELD)
public class DateTimeProperty extends AbstractStringKeyedMutableProperty<DateTime> {

    // Internal state
    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(required = false, nillable = true)
    private Calendar value;

    /**
     * JAXB / JPA-friendly constructor.<br/>
     * <strong>Note!</strong> For framework use only.
     */
    public DateTimeProperty() {
    }

    /**
     * Creates a new DateTimeProperty from the supplied key and value data.
     *
     * @param key   The key of this DateTimeProperty. Cannot be null.
     * @param value The DateTimeProperty value. Can be null.
     */
    public DateTimeProperty(final String key, final DateTime value) {

        // Delegate
        super(key, DateTime.class);

        // Assign internal state
        setValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setValue(final DateTime value) {
        this.value = value == null ? null : value.toGregorianCalendar();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateTime getValue() {
        return value == null ? null : new DateTime(value);
    }

    /**
     * Equality comparison definition that compares key and value.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object that) {

        // Check sanity
        if(that == null || !that.getClass().equals(DateTimeProperty.class)) {
            return false;
        }
        if(this == that) {
            return true;
        }

        // Delegate
        final DateTimeProperty thatProperty = (DateTimeProperty) that;
        final DateTimeComparator comparator = DateTimeComparator.getInstance();

        boolean valuesAreEqual = getValue() == null
                ? thatProperty.getValue() == null
                : comparator.compare(getValue(), thatProperty.getValue()) == 0;

        return getKey().equals(thatProperty.getKey()) && valuesAreEqual;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
