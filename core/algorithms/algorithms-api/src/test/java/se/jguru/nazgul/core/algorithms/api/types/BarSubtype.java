/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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


package se.jguru.nazgul.core.algorithms.api.types;

import javax.xml.bind.annotation.XmlTransient;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public class BarSubtype extends FooSupertype implements Comparable<BarSubtype> {

    private String readOnlyValue, subtypeReadOnlyValue, subtypeReadWriteValue;

    public BarSubtype(final String readOnlyValue,
                      final String writeOnlyValue,
                      final String readWriteValue,
                      final int readWriteIntValue,
                      final String readOnlyValueValue,
                      final String subtypeReadOnlyValue,
                      final String subtypeReadWriteValue) {

        // Delegate
        super(readOnlyValue, writeOnlyValue, readWriteValue, readWriteIntValue);

        // Assign internal state
        this.readOnlyValue = readOnlyValueValue;
        this.subtypeReadOnlyValue = subtypeReadOnlyValue;
        this.subtypeReadWriteValue = subtypeReadWriteValue;
    }

    @Override
    public String getReadOnlyValue() {
        return readOnlyValue;
    }

    public String getSubtypeReadOnlyValue() {
        return subtypeReadOnlyValue;
    }

    public String getSubtypeReadWriteValue() {
        return subtypeReadWriteValue;
    }

    public void setSubtypeReadWriteValue(final String subtypeReadWriteValue) {
        this.subtypeReadWriteValue = subtypeReadWriteValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final BarSubtype that) {

        // Fail fast
        if(that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Silly implementation
        return readOnlyValue.compareTo(that.readOnlyValue);
    }
}
