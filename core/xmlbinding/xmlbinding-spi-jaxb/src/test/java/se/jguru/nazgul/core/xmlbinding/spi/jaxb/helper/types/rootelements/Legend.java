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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.rootelements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement
@XmlType(namespace = "http://www.jguru.se/legends", propOrder = {"name", "info"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Legend {

    @XmlElement(required = true)
    private String name;

    //
    // The lax="true" attribute makes the payload object be unmarshalled
    // as a JAXBElement, rather than DOM nodes. This is generally what is
    // desired, although it requires two (non-obvious) things:
    //
    // a) The property must be annotated with @XmlAnyElement(lax = true)
    // b) The class of the object marshalled within the @XmlAnyElement must
    //    be annotated with @XmlRootElement(name = "...", namespace = "...")
    //
    @XmlAnyElement(lax = true)
    private Object info;

    /**
     * JAXB-friendly constructor
     */
    public Legend() {
    }

    public Legend(final String name, final Object info) {
        this.name = name;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Object getInfo() {
        return info;
    }

    public void setInfo(final Object info) {
        this.info = info;
    }
}
