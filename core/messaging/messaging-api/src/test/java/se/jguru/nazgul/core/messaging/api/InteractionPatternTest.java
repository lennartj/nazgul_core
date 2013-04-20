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

package se.jguru.nazgul.core.messaging.api;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class InteractionPatternTest {

    @Test
    public void validateOrdinalAndValues() {

        // Assemble
        final InteractionPattern[] values = InteractionPattern.values();
        final InteractionPattern[] expectedOrder = {
                InteractionPattern.FIRE_AND_FORGET,
                InteractionPattern.PSEUDO_SYNCHRONOUS,
                InteractionPattern.EVENT_CALLBACK};

        // Act & Assert
        Assert.assertEquals(expectedOrder.length, values.length);
        for(int i = 0; i < expectedOrder.length; i++) {
            Assert.assertSame(expectedOrder[i], values[i]);
        }
    }
}
