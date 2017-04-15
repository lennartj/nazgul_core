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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.marshal;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"anInt", "aNonAnnotatedMap",
        "aTransientStringSet", "aString", "anXmlTransientStringList", "stringWithXmlElementAnnotation"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassWithPrimitivesAndCollections {

    // Internal state
    private String aString = "Foo";

    @XmlAttribute(required = false)
    private int anInt = 42;

    private transient Set<String> aTransientStringSet;

    private Map<String, Object> aNonAnnotatedMap;

    @XmlElement(required = true, nillable = false)
    private String stringWithXmlElementAnnotation;

    @XmlTransient
    private List<String> anXmlTransientStringList;

    public ClassWithPrimitivesAndCollections() {
    }

    public ClassWithPrimitivesAndCollections(final String aString,
                                             final int anInt,
                                             final Set<String> aTransientStringSet,
                                             final Map<String, Object> aNonAnnotatedMap,
                                             final String stringWithXmlElementAnnotation,
                                             final List<String> anXmlTransientStringList) {
        this.aString = aString;
        this.anInt = anInt;
        this.aTransientStringSet = aTransientStringSet;
        this.aNonAnnotatedMap = aNonAnnotatedMap;
        this.stringWithXmlElementAnnotation = stringWithXmlElementAnnotation;
        this.anXmlTransientStringList = anXmlTransientStringList;
    }

    public String getaString() {
        return aString;
    }

    public int getAnInt() {
        return anInt;
    }

    public Set<String> getaTransientStringSet() {
        return aTransientStringSet;
    }

    public Map<String, Object> getaNonAnnotatedMap() {
        return aNonAnnotatedMap;
    }

    public String getStringWithXmlElementAnnotation() {
        return stringWithXmlElementAnnotation;
    }

    public List<String> getAnXmlTransientStringList() {
        return anXmlTransientStringList;
    }
}
