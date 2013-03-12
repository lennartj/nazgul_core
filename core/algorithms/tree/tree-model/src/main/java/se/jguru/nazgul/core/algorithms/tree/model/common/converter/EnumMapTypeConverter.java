/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
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
        if (value != null) {
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

        if (value != null) {
            final Iterator<E> it = value.keySet().iterator();
            if (it.hasNext()) {
                toReturn = new JaxbAnnotatedEnumMap(value, it.next().getDeclaringClass());
            }
        }

        return toReturn;
    }
}
