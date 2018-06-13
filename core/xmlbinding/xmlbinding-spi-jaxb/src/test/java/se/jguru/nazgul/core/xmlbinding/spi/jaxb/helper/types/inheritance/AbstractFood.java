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

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.inheritance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Platter.FOOD_NAMESPACE, propOrder = {"name"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractFood implements Comparable<AbstractFood> {

    // Internal state
    @XmlElement(nillable = false, required = true)
    private String name;

    @XmlAttribute(required = true)
    private String origin;

    public AbstractFood() {
    }

    public AbstractFood(String name, String origin) {
        this.name = name;
        this.origin = origin;
    }

    public String getName() {
        return name;
    }

    public String getOrigin() {
        return origin;
    }

    abstract public String getCategory();

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final AbstractFood that) {

        //
        // Compare in the following order
        //
        // a) Name
        // b) Origin
        // c) Category
        //
        int result = this.name.compareTo(that.name);
        if (result == 0) {
            result = this.origin.compareTo(that.origin);

            if (result == 0) {
                result = this.getCategory().compareTo(that.getCategory());
            }
        }

        // All done.
        return result;
    }
}
