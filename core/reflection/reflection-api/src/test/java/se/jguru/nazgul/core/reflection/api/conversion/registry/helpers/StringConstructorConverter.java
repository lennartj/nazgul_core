/*
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
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

package se.jguru.nazgul.core.reflection.api.conversion.registry.helpers;

import se.jguru.nazgul.core.reflection.api.conversion.Converter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class StringConstructorConverter {

    // Interal state
    private String value;

    @Converter(priority = (Converter.DEFAULT_PRIORITY - 2))
    public StringConstructorConverter(final String aString) {
        this.value = aString;
    }

    @Converter
    public StringConstructorConverter(final String aString, final boolean aBoolean) {
        this.value = "" + aBoolean + "_" + aString;
    }

    public final String getValue() {
        return value;
    }
}
