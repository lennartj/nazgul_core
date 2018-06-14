/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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
package se.jguru.nazgul.core.algorithms.api.jmx;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.MemoryMXBean;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class JmxAlgorithmsTest {

    // Shared state
    private MBeanServer platformServer;

    @Before
    public void setupSharedState() {

        platformServer = JmxAlgorithms.getPlatformServer();

        Assert.assertNotNull(platformServer);
    }

    @After
    public void removeBoundMXBeans() {

        final String jmxDomain = FooBarMXBean.class.getPackage().getName();

        final Set<ObjectName> namesInDomain = JmxAlgorithms.getNamesInDomain(jmxDomain);
        if (namesInDomain.isEmpty()) {

            System.out.println("No JMX names found in domain [" + jmxDomain + "]. Not unbinding.");

        } else {

            for (ObjectName current : namesInDomain) {

                System.out.print("Unbinding [" + current + "] ... ");
                try {
                    JmxAlgorithms.getPlatformServer().unregisterMBean(current);
                    System.out.println("Done!");
                } catch (Exception e) {
                    System.out.println("Failed! Caused by:\n" + e);
                }
            }
        }
    }


    @Test
    public void validateGettingDomains() throws Exception {

        /*
java.util.logging:type=Logging
java.lang:type=OperatingSystem
java.lang:type=MemoryManager,name=Metaspace Manager
java.lang:type=MemoryPool,name=Metaspace
java.lang:type=MemoryPool,name=PS Old Gen
java.lang:type=ClassLoading
java.lang:type=Runtime
java.lang:type=GarbageCollector,name=PS Scavenge
java.lang:type=Threading
java.lang:type=MemoryManager,name=CodeCacheManager
java.lang:type=MemoryPool,name=PS Eden Space
java.lang:type=MemoryPool,name=Code Cache
java.lang:type=MemoryPool,name=Compressed Class Space
java.lang:type=MemoryPool,name=PS Survivor Space
java.lang:type=GarbageCollector,name=PS MarkSweep
java.lang:type=Memory
java.lang:type=Compilation
         */

        // Assemble
        final Set<ObjectInstance> memoryMBeanObjects = JmxAlgorithms.getPlatformServer()
                .queryMBeans(new ObjectName("*:type=Memory"), null);

        Assert.assertNotNull(memoryMBeanObjects);
        Assert.assertEquals(1, memoryMBeanObjects.size());
        final ObjectInstance memoryMxBean = memoryMBeanObjects.iterator().next();

        // Act
        final String mBeanInterfaceName = JmxAlgorithms.getMBeanInterfaceName(memoryMxBean.getObjectName());
        final MemoryMXBean mxBeanProxy = JmxAlgorithms.getMXBeanProxy(MemoryMXBean.class, memoryMxBean.getObjectName());

        // Assert
        Assert.assertEquals(MemoryMXBean.class.getName(), mBeanInterfaceName);
        Assert.assertTrue(MemoryMXBean.class.isAssignableFrom(mxBeanProxy.getClass()));

        /*
        for (aDomain in someDomains) {
            JmxAlgorithms.getMBeansInDomain(aDomain).map { it.objectName }.forEach { println(it) }
        }
        */
    }

    @Test
    public void validateSynthesizingObjectNames() throws Exception {

        // Assemble
        final Class<?> interfaceType = FooBarMXBean.class;
        final String expectedDomain = interfaceType.getPackage().getName();
        final SortedMap<String, String> extraProps = new TreeMap<>();
        extraProps.put("foo", "bar");

        // Act
        final ObjectName result = JmxAlgorithms.getNaturalObjectNameFor(interfaceType, extraProps);
        // println("Got result: " + result);

        // Assert
        Assert.assertEquals(expectedDomain, result.getDomain());
        Assert.assertEquals("se.jguru.nazgul.core.algorithms.api.jmx:jmxInterfaceType=FooBarMXBean,foo=bar",
                result.toString());
        Assert.assertEquals(interfaceType.getSimpleName(), result.getKeyProperty(JmxAlgorithms.JMX_INTERFACE_TYPE));
    }

    @Test
    public void validateGettingBoundMXBean() throws Exception {

        // Assemble
        final String newBar = "newBar";
        final FooBarImpl theImpl = new FooBarImpl();
        theImpl.setBar(newBar);

        // Act
        final ObjectInstance objectInstance = JmxAlgorithms.registerMXBean(FooBarMXBean.class, theImpl);

        final FooBarMXBean mxBean = JmxAlgorithms.getMXBeanProxy(
                FooBarMXBean.class, JmxAlgorithms.getNaturalObjectNameFor(FooBarMXBean.class));

        // Assert
        Assert.assertNotNull(mxBean);
        Assert.assertNotSame(mxBean, theImpl);
        Assert.assertEquals("newBar", mxBean.getBar());
        Assert.assertEquals("foo!", mxBean.getFoo());
    }

    @Test
    public void validateDifferentiatingBetweenBoundMXBeans() {

        // Assemble
        final String jmxDomain = FooBarMXBean.class.getPackage().getName();
        final String qualifierKey = "qualifier";

        final FooBarImpl impl1 = new FooBarImpl();
        impl1.setBar("impl1");
        final Map<String, String> impl1Properties = getSingleEntryMap(qualifierKey, "impl1");

        final FooBarImpl impl2 = new FooBarImpl();
        impl2.setBar("impl2");
        final Map<String, String> impl2Properties = getSingleEntryMap(qualifierKey, "impl2");

        JmxAlgorithms.registerMXBean(FooBarMXBean.class, impl1, impl1Properties);
        JmxAlgorithms.registerMXBean(FooBarMXBean.class, impl2, impl2Properties);

        // Act
        final Set<ObjectName> namesInDomain = JmxAlgorithms.getNamesInDomain(jmxDomain);
        final Set<ObjectInstance> mBeansInDomain = JmxAlgorithms.getMBeansInDomain(jmxDomain);
        final SortedMap<String, ObjectName> name2ObjectName = new TreeMap<>();
        final SortedMap<ObjectName, String> objectName2Bar = new TreeMap<>();

        namesInDomain.stream()
                .filter(Objects::nonNull)
                .forEach(current -> {

                    // Dig out the JMX Proxy
                    final String key = current.toString();
                    final FooBarMXBean currentProxy = JmxAlgorithms.getMXBeanProxy(FooBarMXBean.class, current);

                    // Filter out the corresponding ObjectInstance, and validate its ClassName.
                    final ObjectInstance directObjectInstance = mBeansInDomain.stream()
                            .filter(obj -> obj.getObjectName() == current)
                            .findFirst()
                            .orElse(null);
                    Assert.assertEquals(FooBarImpl.class.getName(), directObjectInstance.getClassName());

                    // Call a method within the Proxy, and stash the results
                    name2ObjectName.put(key, current);
                    objectName2Bar.put(current, currentProxy.getBar());
                });


        // Assert
        Assert.assertEquals(2, name2ObjectName.size());
        Assert.assertEquals(2, objectName2Bar.size());

        objectName2Bar.forEach((k, v) -> {

            final String qualifier = k.getKeyPropertyList().get(qualifierKey);
            Assert.assertNotNull(qualifier);
            Assert.assertEquals(qualifier, v);

            System.out.println("Domain: " + k.getDomain()
                    + ", Canonical Name: " + k.getCanonicalName()
                    + ", Key Property List: " + k.getKeyPropertyList());
        });
    }


    //
    // Private helpers
    //

    private Map<String, String> getSingleEntryMap(final String key, final String value) {

        final SortedMap<String, String> toReturn = new TreeMap<>();
        toReturn.put(key, value);
        return toReturn;
    }
}
