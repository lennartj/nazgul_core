/*
 * #%L
 * Nazgul Project: nazgul-core-blueprint-test
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
package se.jguru.nazgul.test.blueprint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BundleStateTest {

    // Shared state
    private Map<String, Integer> name2BundleStateMap;

    @Before
    public void setupSharedState() {

        name2BundleStateMap = new TreeMap<String, Integer>();
        name2BundleStateMap.put("UNINSTALLED", Bundle.UNINSTALLED);
        name2BundleStateMap.put("INSTALLED", Bundle.INSTALLED);
        name2BundleStateMap.put("RESOLVED", Bundle.RESOLVED);
        name2BundleStateMap.put("STARTING", Bundle.STARTING);
        name2BundleStateMap.put("STOPPING", Bundle.STOPPING);
        name2BundleStateMap.put("ACTIVE", Bundle.ACTIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnParsingUnknownBundleState() {

        // Act & Assert
        BundleState.convert(-3);
    }

    @Test
    public void validateConvertingBundleStateContantToEnum() {

        // Act
        for(Map.Entry<String, Integer> current : name2BundleStateMap.entrySet()) {

            final BundleState bundleState = BundleState.convert(current.getValue());
            Assert.assertEquals(current.getKey(), bundleState.name());
        }
    }

    @Test
    public void validateBundleStateData() throws Exception {

        // Act & Assert
        for (BundleState current : BundleState.values()) {

            Assert.assertEquals(current.name().toLowerCase(), current.toString());

            // Acquire the Bundle.state variable for the current BundleName.
            Field bundleField = Bundle.class.getField(current.name());
            int result = (Integer) bundleField.get(null);

            Assert.assertEquals(result, current.getOsgiBundleStateValue());
        }
    }
}
