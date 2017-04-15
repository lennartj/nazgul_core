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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.enumerations;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"adjustment", "text"})
@XmlAccessorType(XmlAccessType.FIELD)
public class TextSection {

    // Internal state
    private Adjustment adjustment;
    private String text;

    /**
     * JAXB-friendly constructor
     */
    public TextSection() {
    }

    /**
     * Compound constructor.
     *
     * @param adjustment The text adjustment.
     * @param text       The text.
     */
    public TextSection(final Adjustment adjustment, final String text) {
        this.adjustment = adjustment;
        this.text = text;
    }

    public Adjustment getAdjustment() {
        return adjustment;
    }

    public String getText() {
        return text;
    }
}
