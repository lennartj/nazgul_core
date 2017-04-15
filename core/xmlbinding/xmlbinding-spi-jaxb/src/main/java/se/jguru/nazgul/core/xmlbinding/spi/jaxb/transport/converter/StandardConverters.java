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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import org.joda.time.DateTime;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedCollection;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedDateTime;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull;

import java.util.Collection;

/**
 * Standard converter method holder type.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings("rawtypes")
public class StandardConverters {

    /**
     * Null check method.
     *
     * @param obj The object to check.
     * @return The
     */
    public boolean isNull(final Object obj) {
        return obj == null;
    }

    /**
     * Converter for null values only, given a priority to be checked before the default converters.
     *
     * @param obj The null value to be converted.
     * @return the JaxbAnnotatedNull [singleton] instance.
     */
    @Converter(acceptsNullValues = true, conditionalConversionMethod = "isNull",
            priority = (Converter.DEFAULT_PRIORITY - 10))
    public JaxbAnnotatedNull packageNull(final Object obj) {
        return JaxbAnnotatedNull.getInstance();
    }

    /**
     * Resurrection method for JaxbAnnotatedNull instances.
     *
     * @param obj The JaxbAnnotatedNull to resurrect back to a null value.
     * @return always null.
     */
    @Converter
    public Object resurrectNull(final JaxbAnnotatedNull obj) {
        return null;
    }

    /**
     * Converts DateTimes to JaxbAnnotatedDateTime instances.
     *
     * @param obj The object to package.
     * @return The resulting JaxbAnnotatedDateTime instance.
     */
    @Converter
    public JaxbAnnotatedDateTime packageDateTime(final DateTime obj) {
        return new JaxbAnnotatedDateTime(obj);
    }

    /**
     * Resurrects DateTimes from JaxbAnnotatedDateTime instances.
     *
     * @param obj The object to resurrect.
     * @return The resurrected DateTime.
     */
    @Converter
    public DateTime resurrectDateTime(final JaxbAnnotatedDateTime obj) {
        return obj.getValue();
    }

    /**
     * Converts Collection subtypes to JaxbAnnotatedCollection instances.
     *
     * @param obj The object to package.
     * @param <T> The collection (sub)type.
     * @return The resulting JaxbAnnotatedCollection instane.
     */
    @Converter
    public <T extends Collection> JaxbAnnotatedCollection<T> packageCollection(final T obj) {
        return new JaxbAnnotatedCollection<T>(obj);
    }

    /**
     * Resurrects Collection subtypes from JaxbAnnotatedCollection instances.
     *
     * @param obj The object to resurrect.
     * @param <T> The collection (sub)type.
     * @return A clone of the OriginalType's instance.
     */
    @Converter
    public <T extends Collection> T reviveAfterTransport(final JaxbAnnotatedCollection<T> obj) {
        return obj.getValue();
    }
}
