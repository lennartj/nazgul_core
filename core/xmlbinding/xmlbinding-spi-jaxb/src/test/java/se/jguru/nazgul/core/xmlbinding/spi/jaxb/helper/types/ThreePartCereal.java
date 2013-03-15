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
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement
@XmlType(namespace = "http://cereal",
        propOrder = {"baseIngredient", "flavourIngredient",
                "colourIngredient", "flavourPercentage", "colourPercentage"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ThreePartCereal {

    // Internal state
    @XmlElement(namespace = "http://cereal/ingredients/base", nillable = false)
    private String baseIngredient;

    @XmlElement(namespace = "http://cereal/ingredients/flavour", nillable = false, required = true)
    private String flavourIngredient;

    @XmlElement(namespace = "http://cereal/ingredients/colour", nillable = false)
    private String colourIngredient;

    @XmlAttribute(required = true)
    private int flavourPercentage;

    @XmlAttribute(required = true)
    private int colourPercentage;

    public ThreePartCereal() {
    }

    public ThreePartCereal(String baseIngredient,
                           String flavourIngredient,
                           String colourIngredient,
                           int flavourPercentage,
                           int colourPercentage) {

        this.baseIngredient = baseIngredient;
        this.flavourIngredient = flavourIngredient;
        this.colourIngredient = colourIngredient;
        this.flavourPercentage = flavourPercentage;
        this.colourPercentage = colourPercentage;
    }

    public String getBaseIngredient() {
        return baseIngredient;
    }

    public String getFlavourIngredient() {
        return flavourIngredient;
    }

    public String getColourIngredient() {
        return colourIngredient;
    }

    public int getFlavourPercentage() {
        return flavourPercentage;
    }

    public int getColourPercentage() {
        return colourPercentage;
    }

    public int getBasePercentage() {
        return 100 - flavourPercentage - colourPercentage;
    }
}
