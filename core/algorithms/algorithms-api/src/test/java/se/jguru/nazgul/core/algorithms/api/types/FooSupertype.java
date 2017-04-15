/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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

package se.jguru.nazgul.core.algorithms.api.types;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings("all")
@Resource
public class FooSupertype implements Serializable {

    private String readOnlyValue, writeOnlyValue, readWriteValue;
    private int readWriteIntValue;

    public FooSupertype(final String readOnlyValue,
                        final String writeOnlyValue,
                        final String readWriteValue,
                        final int readWriteIntValue) {
        this.readOnlyValue = readOnlyValue;
        this.writeOnlyValue = writeOnlyValue;
        this.readWriteValue = readWriteValue;
        this.readWriteIntValue = readWriteIntValue;
    }

    public int getReadWriteIntValue() {
        return readWriteIntValue;
    }

    public void setReadWriteIntValue(final int readWriteIntValue) {
        this.readWriteIntValue = readWriteIntValue;
    }

    public String getReadOnlyValue() {
        return readOnlyValue;
    }

    public String getReadWriteValue() {
        return readWriteValue;
    }

    public void setWriteOnlyValue(final String writeOnlyValue) {
        this.writeOnlyValue = writeOnlyValue;
    }

    public void setReadWriteValue(final String readWriteValue) {
        this.readWriteValue = readWriteValue;
    }
}
