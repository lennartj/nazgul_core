/*
 * #%L
 * Nazgul Project: nazgul-core-clustering-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.clustering.api;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockIdGenerator implements IdGenerator {

    public boolean idAvailable = true;
    public List<String> callTrace = new ArrayList<String>();
    public static AtomicInteger counter = new AtomicInteger(0);

    // Internal state
    private String prefix;
    private int index;

    public MockIdGenerator(final String prefix) {
        Validate.notEmpty(prefix, "Cannot handle null or empty prefix.");
        this.prefix = prefix;
        index = counter.incrementAndGet();
    }

    /**
     * @return {@code true} if this IdGenerator can deliver an identifier
     *         at the time of this method being called, and {@code false}
     *         otherwise.
     */
    @Override
    public boolean isIdentifierAvailable() {
        return idAvailable;
    }

    /**
     * @return A (cluster-)unique identifier for each call.
     */
    @Override
    public String getIdentifier() {
        final String toReturn = prefix + "_" + index;
        callTrace.add("getIdentifier: " + toReturn);
        return toReturn;
    }
}
