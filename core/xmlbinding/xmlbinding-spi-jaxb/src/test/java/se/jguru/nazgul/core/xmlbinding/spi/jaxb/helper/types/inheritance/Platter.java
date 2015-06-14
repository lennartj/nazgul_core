/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.inheritance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Platter.FOOD_NAMESPACE, propOrder = {"name", "price", "foods"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Platter {

    public static final String FOOD_NAMESPACE = "http://silly.food.structure";

    // Internal state
    @XmlElement(nillable = false, required = true)
    private String name;

    @XmlAttribute(required = true)
    private int price;

    @XmlElementWrapper(name = "foods")
    @XmlElement(name = "food")
    private final List<AbstractFood> foods = new ArrayList<AbstractFood>();

    public Platter() {
    }

    public Platter(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public List<AbstractFood> getFoods() {
        return foods;
    }

    public int getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }
}
