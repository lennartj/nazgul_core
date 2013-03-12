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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper;

import se.jguru.nazgul.core.reflection.api.conversion.Converter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TrivialCharSequenceConverter {

    @Converter
    public JaxbAnnotatedTrivialCharSequence convert(final TrivialCharSequence trivialCharSequence) {
        return new JaxbAnnotatedTrivialCharSequence(trivialCharSequence);
    }

    @Converter(priority = Converter.DEFAULT_PRIORITY + 100)
    public TrivialCharSequence convertToTrivialCharSequence(final JaxbAnnotatedTrivialCharSequence transport) {
        return transport.getValue();
    }
}
