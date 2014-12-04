## Nazgul Core: Cache Example

The Cache example project contains a set of unit tests containing copy-and-paste examples
demonstrating how to use the Cache API (and, of course, an injected implementation of choice).
For full examples, please refer to the unit test classes of the Cache example project.
Some typical examples are shown below.

## Example 1: Put and Get values in a Cache

Considering a cache to work similar to a java.util.Map, the following example illustrates
how to add a key/value to the cache and how to retrieve it.

<pre class="brush: java"><![CDATA[
    @Test
    public void useCase1_putAndGetValuesInCache() throws InterruptedException {

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


        // Assert
        Assert.assertEquals("bar", value);
    }
]]></pre>

If using a DistributedCache instead of a plain/non-distributed cache, the example
code is slightly changed:

<pre class="brush: java"><![CDATA[
    @Test
    public void useCase2_putAndGetValuesInDistributedMap() {

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
    }
]]></pre>