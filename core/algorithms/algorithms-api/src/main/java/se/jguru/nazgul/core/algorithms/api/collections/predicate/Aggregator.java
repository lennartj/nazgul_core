/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

package se.jguru.nazgul.core.algorithms.api.collections.predicate;

/**
 * Generic type aggregator.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Aggregator<T, R> {

    /**
     * Performs aggregation of the candidate and the current (aggregate) result.
     *
     * @param candidate The candidate to be reduced / aggregated into the current value.
     * @param current   The current aggregate value. Note that this value is null on the first call to aggregate.
     * @return The result of the aggregate operation between the current result and the provided candidate.
     */
    R aggregate(R current, T candidate);
}
