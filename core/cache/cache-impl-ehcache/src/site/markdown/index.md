## Nazgul Core: Cache Implementation EhCache

The EhCache implementation adapts the EhCache caching product to the Nazgul Core: Cache API.
Note that the EhCache implementation implements only the (local) Cache interface, and not the
DistributedCache or DestinationProvider specifications. This implies that the EhCache implementation
works well for local caching but does not act in a distributed manner.

### EhCache configuration

For a full walkthrough of the EhCache configuration file, please refer to the online documentation
of the [EhCache project](http://ehcache.org/). However, the configuration specification (in the form
of an XSD) is bundled within this EhCache implementation as a resource. As an example, the
EhCache unit-test configuration might serve as an example of a configuration:

<pre class="brush: xml"><![CDATA[
<ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="ehcache.xsd">

    <!--
    The ehcache-failsafe.xml is a default configuration for ehcache, if an ehcache.xml is not configured.

    The diskStore element is optional. It must be configured if you have overflowToDisk or diskPersistent enabled
    for any cache. If it is not configured, a warning will be issues and java.io.tmpdir will be used.

    diskStore has only one attribute - "path". It is the path to the directory where .data and .index files will be created.

    If the path is a Java System Property it is replaced by its value in the
    running VM.

    The following properties are translated:
    * user.home - User's home directory
    * user.dir - User's current working directory
    * java.io.tmpdir - Default temp file path
    * ehcache.disk.store.dir - A system property you would normally specify on the command line
          e.g. java -Dehcache.disk.store.dir=/u01/myapp/diskdir ...

    Subdirectories can be specified below the property e.g. java.io.tmpdir/one

    <diskStore path="java.io.tmpdir"/>
    -->
    <diskStore path="${dir:uniqueSubTarget}"/>

    <!--
    Mandatory Default Cache configuration. These settings will be applied to caches
    created programmtically using CacheManager.add(String cacheName)
    -->
    <defaultCache
            maxElementsInMemory="10000"
            eternal="false"
            timeToIdleSeconds="120"
            timeToLiveSeconds="120"
            overflowToDisk="true"
            maxElementsOnDisk="10000000"
            diskPersistent="false"
            diskExpiryThreadIntervalSeconds="120"
            memoryStoreEvictionPolicy="LRU"
            transactionalMode="local"/>
</ehcache>
]]></pre>
