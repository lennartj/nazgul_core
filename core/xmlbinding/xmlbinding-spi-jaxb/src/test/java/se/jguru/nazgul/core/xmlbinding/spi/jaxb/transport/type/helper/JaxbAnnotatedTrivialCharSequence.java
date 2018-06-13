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

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"transportForm"})
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbAnnotatedTrivialCharSequence implements Serializable, Comparable {

    // Internal state
    @XmlTransient
    private TrivialCharSequence value;

    @XmlElement(nillable = false, required = true)
    private String transportForm;

    public JaxbAnnotatedTrivialCharSequence() {
    }

    public JaxbAnnotatedTrivialCharSequence(final TrivialCharSequence value) {

        // Assign internal state
        this.transportForm = value.toString();
    }

    public TrivialCharSequence getValue() {
        if (value == null) {
            value = new TrivialCharSequence(new StringBuffer(transportForm));
        }
        return value;
    }

    @Override
    public int compareTo(final Object o) {
        if (o instanceof JaxbAnnotatedTrivialCharSequence) {
            return getValue().toString().compareTo(((JaxbAnnotatedTrivialCharSequence) o).getValue().toString());
        } else if (o instanceof TrivialCharSequence) {
            return getValue().toString().compareTo(((TrivialCharSequence) o).toString());
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getValue().hashCode();
    }
}
