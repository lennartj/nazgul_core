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




package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.rootelements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(name = "age", namespace = "http://www.jguru.se/legends")
@XmlType(/*name = "age", namespace = "http://www.jguru.se/legends", */propOrder = {"century", "approximate"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Age {

    // Internal state
    @XmlElement(required = true, nillable = false)
    private int century;

    @XmlAttribute(required = true)
    private boolean approximate;

    /**
     * JAXB-friendly constructor.
     */
    public Age() {
    }

    public Age(final boolean approximate, final int century) {
        this.approximate = approximate;
        this.century = century;
    }

    public int getCentury() {
        return century;
    }

    public boolean isApproximate() {
        return approximate;
    }
}
