## Nazgul Core: Cache API

The Nazgul Core: Cache API provides specifications for how to work with caches in a
common manner - irrespective if the cache implementations are local (i.e. standalone) 
or distributed (i.e. networked with or without load balancing and failover). 
The Nazgul Core Cache API provides interactions for the most common approaches to 
caching in a simple and useable manner, as opposed to the Java Cache API which provides
a more complex specification which caters for all caching use cases.
 
There are 3 types of cache:

1. **Cache**. The Cache interface defines the operations common to all types of cache, such 
   as putting and getting values, adding and removing listeners and how to specify operations
   which should be executed within transactions. All of these operations and types are given in
   an implementation-neutral way, inspired by the way that JDBC is a vendor-neutral database
   interaction specification.
2. **DistributedCache**. The DistributedCache interface adds operations to create, acquire and
   manage listeners to distributed objects. Frequently, distributed cache implementations consist
   of several cache servers which communicate autonomously with each other, and creating a 
   distributed object (typically a Collection) requires slightly more information than creating
   a non-distributed object. Also, it is typically possible to add and remove listeners directly to
   distributed objects to be made aware if - say - an element is added to a distributed List.
3. **DestinationProvider**. Some DistributedCache implementations provide an asynchronous way to 
   send messages between cache servers, typically to run methods at each cache server instance.
   The DestinationProvider specification unlocks some of these mechanics in a simple and 
   vendor-neutral way.
   
The typical uses of the caches are illustrated within the `nazgul-core-cache-example` project,
and also in the code snippets below.

### Using a Cache

A Cache can be considered to work as a standard `java.util.Map`:

<pre class="brush: java"><![CDATA[
        // Acquire the cache.
        final Cache<String> cache = getCache();

        // 1: Put a value in the cache.
        cache.put("foo", "bar");

        // 2: Simulate some work in the system
        Thread.sleep(200);

        // 3: Get the value from the cache.
        //
        //    NOTE: You will get the same result even if you
        //    execute this call in a DistributedCache instance
        //    within the same group running in another JVM.
        //
        final String value = (String) cache.get("foo");
]]></pre> 

### Using a DistributedCache

The only difference between a plain and a distributed cache is the way
that (distributed) collections are managed:
 
<pre class="brush: java"><![CDATA[
        // Acquire the cache.
        final DistributedCache<String> cache = getCache();

        // 1: Get or create a distributed Map, and put a key/value pair in it.
        final String distMapID = "aClusterUniqueIdForTheDistributedMap";
        Map<String, String> distMap = cache.getDistributedMap(distMapID);
        distMap.put("foo", "bar");

        // 2: Get the value from the distributed Map.
        //    Note that this can be done in another class
        //    or even on another JVM, as long as that JVM
        //    is part of the same cluster as this one.
        //
        //    ... and, of course, that we use the same ID
        //    string to acquire the Map ...
        Map<String, String> theSameDistMapInAnotherJVM = cache.getDistributedMap(distMapID);
        final String value = theSameDistMapInAnotherJVM.get("foo");

        // 3: Remove the key/value pair from the distributed Map.
        Map<String, String> theSameDistMapInYetAnotherJVM = cache.getDistributedMap(distMapID);
        final String removedValue = theSameDistMapInYetAnotherJVM.remove("foo");

        // Assert
        Assert.assertEquals("bar", value);
        Assert.assertEquals("bar", removedValue);
]]></pre> 

### Using a DestinationProvider

DestinationProviders can send messages to one another using LightweightTopics. 
Each such LightweightTopic
