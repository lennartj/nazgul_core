/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-event-api
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

package se.jguru.nazgul.core.algorithms.event.api.consumer;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractEventConsumerTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullId() {

        // Assemble
        final String incorrectNull = null;

        // Act
        new MockEventConsumer(incorrectNull);
    }

    @Test
    public void validateIdentityHandling() {

        // Assemble
        final String id = "FooBar!";
        final MockEventConsumer unitUnderTest = new MockEventConsumer(id);

        // Act & Assert
        Assert.assertEquals(id, unitUnderTest.getClusterId());
    }
}
