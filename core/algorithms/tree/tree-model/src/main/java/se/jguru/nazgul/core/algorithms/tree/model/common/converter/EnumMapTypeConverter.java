/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model.common.converter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Iterator;

/**
 * XmlAdapter converting between the transport type JaxbAnnotatedEnumMap and the EnumMap original type.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnumMapTypeConverter<E extends Enum<E>, KeyType extends Serializable & Comparable<KeyType>>
        extends XmlAdapter<JaxbAnnotatedEnumMap, EnumMap<E, KeyType>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumMap<E, KeyType> unmarshal(final JaxbAnnotatedEnumMap value) throws Exception {

        EnumMap<E, KeyType> toReturn = null;
        if(value != null) {
            toReturn = value.getEnumMap();
        }

        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JaxbAnnotatedEnumMap marshal(final EnumMap<E, KeyType> value) throws Exception {

        JaxbAnnotatedEnumMap toReturn = null;

        if(value != null) {
            final Iterator<E> it = value.keySet().iterator();
            if(it.hasNext()){
                toReturn = new JaxbAnnotatedEnumMap(value, it.next().getDeclaringClass());
            }
        }

        return toReturn;
    }
}
