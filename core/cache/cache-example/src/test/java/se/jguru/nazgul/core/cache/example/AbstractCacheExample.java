/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.example;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.extra.DirScannerProvisionOption;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.cache.api.Cache;
import se.jguru.nazgul.core.reflection.api.DependencyData;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Common functionality for cache examples.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@RunWith(JUnit4TestRunner.class)
public abstract class AbstractCacheExample {

    /**
     * GroupID for Hazelcast Cache implementation.
     */
    public static final String HAZELCAST_GROUPID = "se.jguru.nazgul.core.cache.impl.hazelcast";

    /**
     * GroupID for EhCache implementation.
     */
    public static final String EHCACHE_GROUPID = "se.jguru.nazgul.core.cache.impl.ehcache";


    /**
     * DependencyData for the current project.
     */
    protected final List<DependencyData> localDependencyData;
    protected DependencyData hazelcastDependencyData;
    protected DependencyData ehCacheDependencyData;

    @Inject
    private BundleContext ctx;

    protected AbstractCacheExample() {

        // Acquire local-project dependency data.
        localDependencyData = DependencyData.parseDefaultPlacedDependencyPropertiesFile();

        // Validate that the localDependencyData contains HazelcastCache and EhCache implementations.
        final List<DependencyData> hazelcast = CollectionAlgorithms.filter(localDependencyData,
                new Filter<DependencyData>() {
                    @Override
                    public boolean accept(final DependencyData candidate) {
                        return candidate.getGroupId().equals(HAZELCAST_GROUPID);
                    }
                });
        final List<DependencyData> ehCache = CollectionAlgorithms.filter(localDependencyData,
                new Filter<DependencyData>() {
                    @Override
                    public boolean accept(final DependencyData candidate) {
                        return candidate.getGroupId().equals(EHCACHE_GROUPID);
                    }
                });

        // Assign internal state
        this.hazelcastDependencyData = hazelcast.get(0);
        this.ehCacheDependencyData = ehCache.get(0);

        // Check sanity
        Assert.assertNotNull(this.hazelcastDependencyData);
        Assert.assertNotNull(this.ehCacheDependencyData);
    }

    /**
     * Retrieves the local BundleContext.
     *
     * @return the local BundleContext.
     */
    protected BundleContext getBundleContext() {
        return ctx;
    }

    /**
     * Convenience method to acquire the Cache from the unit test BundleContext.
     *
     * @return The BundleContext-bound Cache instance.
     */
    protected Cache<String> getCache() {
        final ServiceReference<Cache> serviceReference = getBundleContext().getServiceReference(Cache.class);
        return (Cache<String>) getBundleContext().getService(serviceReference);
    }

    /**
     * Supplies the server platform options to Pax configuration.
     *
     * @return The Server platform options.
     */
    public static Option[] getServerPlatform() {
        String ariesAssemblyDir = "${aries.assembly}/target";
        Option bootPackages = CoreOptions.bootDelegationPackages("javax.transaction", "javax.transaction.*");
        DirScannerProvisionOption unfiltered = CoreOptions.scanDir(ariesAssemblyDir);
        Option ariesAsembly = unfiltered.filter("*-*.jar");
        Option osgiFramework = CoreOptions.felix().version("3.5.0");
        return CoreOptions.options(bootPackages, ariesAsembly, CoreOptions.junitBundles(), osgiFramework);
    }

    protected Option[] getAllDependenciesOption() {

        List<Option> allOptions = new ArrayList<Option>();

        for(DependencyData current : localDependencyData) {

            MavenArtifactProvisionOption currentOption = CoreOptions.mavenBundle()
                    .groupId(current.getGroupId())
                    .artifactId(current.getArtifactId())
                    .version(current.getVersion());

            allOptions.add(currentOption);
        }

        Option[] toReturn = new Option[allOptions.size()];
        allOptions.toArray(toReturn);

        return toReturn;
    }
}
