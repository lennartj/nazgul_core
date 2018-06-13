/*-
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
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

package se.jguru.nazgul.core.quickstart.api.generator.parser;

import org.junit.Assert;
import org.junit.Test;

import java.util.SortedMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PomTokenTest {

    @Test
    public void validateStandardUsage() {

        // Assemble

        // Act
        final SortedMap<String, String> map = PomToken.create()
                .addToken(PomToken.GROUPID, "myGroupId")
                .build();

        // Assert
        Assert.assertEquals(PomToken.values().length, map.size());
        for (PomToken current : PomToken.values()) {

            if (current == PomToken.GROUPID) {
                Assert.assertEquals("myGroupId", map.get(current.getToken()));
            } else {
                Assert.assertNull(map.get(current.getToken()));
            }
        }
    }
}
