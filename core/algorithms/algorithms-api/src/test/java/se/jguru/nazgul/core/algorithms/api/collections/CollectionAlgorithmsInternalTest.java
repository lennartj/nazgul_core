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

package se.jguru.nazgul.core.algorithms.api.collections;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CollectionAlgorithmsInternalTest {

    @Test
    public void validateCloningEmptyCollectionsFromASourceType() {

        // Act
        final LinkedList linkedList = cloneEmptyFromType(LinkedList.class);
        final HashSet hashSet = cloneEmptyFromType(HashSet.class);
        final TreeSet treeSet = cloneEmptyFromType(TreeSet.class);
        final ArrayList arrayList = cloneEmptyFromType(ArrayList.class);

        final List aList = cloneEmptyFromType(List.class);
        final Set aSet = cloneEmptyFromType(Set.class);

        // Assert
        Assert.assertNotNull(linkedList);
        Assert.assertNotNull(hashSet);
        Assert.assertNotNull(treeSet);
        Assert.assertNotNull(arrayList);

        Assert.assertNotNull(aList);
        Assert.assertTrue(aList instanceof ArrayList);
        Assert.assertNotNull(aSet);
        Assert.assertTrue(aSet instanceof TreeSet);
    }

    @Test
    public void validateDefaultFallbackForAbstractTypes() {

        // Act
        final AbstractList abstractList = cloneEmptyFromType(AbstractList.class);
        final AbstractSet abstractSet = cloneEmptyFromType(AbstractSet.class);

        // Assert
        Assert.assertNotNull(abstractList);
        Assert.assertEquals(ArrayList.class, abstractList.getClass());
        Assert.assertNotNull(abstractSet);
        Assert.assertEquals(TreeSet.class, abstractSet.getClass());
    }

    @Test(expected = ClassCastException.class)
    public void validateStrangeCollectionTypeBehaviour() {

        // Assemble
        class CollectionWithoutDefaultConstructor extends AbstractList<String> {

            CollectionWithoutDefaultConstructor(int size) {
                super();
            }

            @Override
            public String get(int index) {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }
        }

        // Act & Assert #1
        final Object instance = cloneEmptyFromType(CollectionWithoutDefaultConstructor.class);
        Assert.assertEquals(ArrayList.class, instance.getClass());

        // Act & Assert #2
        final CollectionWithoutDefaultConstructor willNotWork
                = cloneEmptyFromType(CollectionWithoutDefaultConstructor.class);
    }

    private <T, C extends Collection<T>> C cloneEmptyFromType(final Class<C> type) {

        try {
            Method toInvoke = CollectionAlgorithms.class.getDeclaredMethod("cloneEmptyFromType", new Class<?>[]{Class.class});
            toInvoke.setAccessible(true);

            return (C) toInvoke.invoke(null, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not invoke method", e);
        }
    }
}
