/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-inmemory
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
/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.inmemory;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NamedSequenceThreadFactoryTest {

    @Test
    public void validateThreadNameGeneration() {

        // Assemble
        final NamedSequenceThreadFactory unitUnderTest = new NamedSequenceThreadFactory("foo");
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Do nothing.
            }
        };

        // Act
        final Thread result0 = unitUnderTest.newThread(runnable);
        final Thread result1 = unitUnderTest.newThread(runnable);

        // Assert
        Assert.assertNotNull(result0);
        Assert.assertNotNull(result1);

        Assert.assertEquals("foo-0", result0.getName());
        Assert.assertEquals("foo-1", result1.getName());
    }

    @Test
    public void validateThreadSequenceRollover() {

        // Assemble
        final NamedSequenceThreadFactory unitUnderTest = new NamedSequenceThreadFactory("bar", 2);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Do nothing.
            }
        };

        // Act
        final Thread result0 = unitUnderTest.newThread(runnable);
        final Thread result1 = unitUnderTest.newThread(runnable);
        final Thread result2 = unitUnderTest.newThread(runnable);

        // Assert
        Assert.assertNotNull(result0);
        Assert.assertNotNull(result1);
        Assert.assertNotNull(result2);

        Assert.assertEquals("bar-0", result0.getName());
        Assert.assertEquals("bar-1", result1.getName());
        Assert.assertEquals("bar-0", result2.getName());
    }
}
