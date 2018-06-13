/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-event-api
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


package se.jguru.nazgul.core.algorithms.event.api.consumer;

import se.jguru.nazgul.core.clustering.api.IdGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockEventConsumer extends AbstractEventConsumer<MockEventConsumer> {

    public List<String> callTrace = new ArrayList<String>();

    public MockEventConsumer(final String id) {
        super(id);
    }

    /**
     * {@inheritDoc}
     */
    public MockEventConsumer(IdGenerator idGenerator) {
        super(idGenerator);
    }

    public void consume(final MockEvent event) {
        callTrace.add("consume [" + event.getSource() + "]");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final MockEventConsumer that) {
        return that == null ? -1 : getClusterId().compareTo(that.getClusterId());
    }
}
