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
package se.jguru.nazgul.core.configuration.model;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Implementation of a mutable AbstractProperty, providing value mutability.
 * Note that this implementation does not check the validity of the value assigned to
 * this AbstractMutableProperty through the use of the {@code #setValue} method.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractMutableProperty<K extends Serializable & Comparable<K>, V extends Serializable>
        extends AbstractProperty<K, V> implements MutableProperty<K, V> {

    /**
     * JAXB / JPA-friendly constructor.<br/>
     * <strong>Note!</strong> For framework use only.
     */
    public AbstractMutableProperty() {
    }

    /**
     * Creates a new AbstractMutableProperty which can hold values of the supplied valueTypeClass.
     *
     * @param valueTypeClass The class (V) of values held by this AbstractMutableProperty.
     *                       The valueTypeClass argument cannot be {@code null}.
     */
    public AbstractMutableProperty(final Class<V> valueTypeClass) {
        super(valueTypeClass);
    }
}
