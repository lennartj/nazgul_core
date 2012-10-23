/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model.common.converter;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.trees.TreeAlgorithms;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * JAXB-compliant transport type for an {@code EnumMap}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"enumType", "values"})
@XmlAccessorType(XmlAccessType.FIELD)
@Access(value = AccessType.FIELD)
public class JaxbAnnotatedEnumMap<E extends Enum<E>> implements Serializable {

    // Internal state
    private List values;

    @XmlAttribute(required = true)
    private Class<E> enumType;

    /**
     * JAXB/JPA-friendly constructor.
     * <strong>This is for framework use only.</strong>
     */
    public JaxbAnnotatedEnumMap() {
    }

    /**
     * Compound constructor, creating a JaxbAnnotatedEnumMap instance from
     * the provided EnumMap and Enum Type.
     *
     * @param value    The EnumMap value whose state should be retained/internally mapped.
     * @param enumType The Enum type used for keys within the provided EnumMap.
     */
    public JaxbAnnotatedEnumMap(final EnumMap<E, ?> value, final Class<E> enumType) {

        // Check sanity
        Validate.notNull(value, "Cannot handle null value argument.");
        Validate.notNull(enumType, "Cannot handle null enumType argument.");

        // Assign internal state
        this.enumType = enumType;
        values = new ArrayList();
        for (E current : value.keySet()) {
            values.add(value.get(current));
        }
    }

    /**
     * @return A re-created EnumMap holding the values supplied.
     */
    public EnumMap getEnumMap() {

        EnumMap toReturn = new EnumMap<E, Serializable>(enumType);
        final E[] enumValues = enumType.getEnumConstants();

        for (int i = 0; i < enumValues.length; i++) {
            Object currentValue = i < values.size() ? values.get(i) : null;
            if(currentValue != null) {
                toReturn.put(enumValues[i], currentValue);
            } else {
                toReturn.put(enumValues[i], null);
            }
        }

        // All done.
        return toReturn;
    }
}