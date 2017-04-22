Nazgul Core: Algoritms API
=============================

The Algoritms API project contains implementations of functional-style, reusable algorithms
which can be imported and used within any project. Most of the algorithms are divided into 2
categories, manifested by 2 classes:

1. **Collection algorithms**. The CollectionAlgorithms class contains a set of static methods
   that simplifies working with Collections. These algorithms are functional in nature, and based
   on the JavaSE 8+ Collections and Streams APIs.

2. **Tree algorithms**. The TreeAlgorithms class contains algorithms that simplify working with
   data, organised within a Tree structure. The javax.swing package contains a partial
   implementation of a Tree model, but the Tree definitions within the Algorithms API project
   provides a fuller/richer set of operations than the swing implementation.
   
## Example: Network algorithm usage
   
Some frequently occurring operations on network addresses appear un-obvious at first, but are in reality not all that
complex when you dig into the richness of the JavaSDK. However, most developers hesitate when charged with - say -   
finding the non-loopback, non-link-local IPv4 InetAddress of the computer your program is running on.
   
The NetworkAlgorithms class define a slew of algorithms, filters and suppliers solving common problems related to 
Network problems. Most stream API building blocks are defined as constants within the NetworkAlgorithms class.
The example Predicate definition below solves the problem of identifying all InetAddress objects which are IPv4, not 
a Loopback address and not a link-local IPv4 address:

<pre class="brush: java"><![CDATA[
    /**
     * Predicate identifying non-null IPv4, non-loopback InetAddress objects that are not LinkLocal addresses.
     */
    public static final Predicate<InetAddress> PUBLIC_IPV4_FILTER = candidate ->
            IPV4_FILTER.test(candidate)
                    && !LOOPBACK_FILTER.test(candidate)
                    && !candidate.isLinkLocalAddress();
]]></pre>
                    
Combine the algorithm building blocks to form elegant solutions to potentially complex problems, exemplified by one 
of the existing methods to retrieve publicly available IPv4 addresses:
 
<pre class="brush: java"><![CDATA[
    /**
     * Convenience method to find all public (i.e. non-loopback, non-linklocal) IPv4 addresses.
     *
     * @return A sorted set holding all Inet4Address objects which are neither loopback nor link-local
     * for all network interfaces on the local computer.
     * @see #INETADDRESS_COMPARATOR
     * @see #GET_INETADDRESSES
     * @see #PUBLIC_IPV4_FILTER
     */
    @NotNull
    public static SortedSet<Inet4Address> getPublicIPv4Addresses() {

        // Create the return value with the
        final SortedSet<Inet4Address> toReturn = new TreeSet<>(INETADDRESS_COMPARATOR);

        // Use the PUBLIC_IPV4_FILTER to
        NetworkAlgorithms.getAllNetworkInterfaces(null)
                .stream()
                .map(NetworkAlgorithms.GET_INETADDRESSES)
                .forEach(c -> c.stream()
                        .filter(NetworkAlgorithms.PUBLIC_IPV4_FILTER)
                        .map(addr -> (Inet4Address) addr)
                        .forEach(toReturn::add));

        // All Done.
        return toReturn;
    }
]]></pre> 

## Example: TypeAlgorithms usage

We frequently need to use reflection on classes to perform operations which rely on getting information about 
capabilities in run-time, rather than at compile time. Such operations typically include Dependency Injection ehich 
form the basis for the CDI standard, the Spring Framework's core, the Plexus Framework, the Nazgûl Core: Reflection API 
and others.
 
Typically, most reflection-related algorithms extract some information about the type - and these are conveniently 
collected into a TypeAlgorithms utility class and a TypeInformation reflection information holder.   

<pre class="brush: java"><![CDATA[
    
    // Get TypeInformation 
    final TypeInformation typeInfo = new TypeInformation(SomeClass.class);
    
    // Now extract reflective information from the typeInfo
    // There are lots of different methods here; refer to the documentation and test cases.
    final SortedSet<Class<?>> allImplementedInterfaces = typeInfo.getAllInterfaces();
    final SortedMap<String, Method> property2GetterMethodMap = typeInfo.getJavaBeanGetterMethods()
    final SortedMap<String, Method> property2SetterMethodMap = typeInfo.getJavaBeanSetterMethods()
    ...
]]></pre>

A use of this is exemplified by a Predicate (implemented in the nazgul-core-reflection-api project), and
which finds all Interfaces (which are also Classes, remember?) matching a certain Predicate (if supplied).
One simple way to dig out all Interfaces is to use the TypeInformation class as shown:

<pre class="brush: java"><![CDATA[
    /**
     * Acquires all interfaces from the provided class which matches the provided selector's acceptance criteria.
     *
     * @param clazz    The class from which to derive all appropriate interfaces.
     * @param selector an optional (i.e. nullable) Predicate. If provided, the Predicate is used to
     *                 filter all interfaces implemented by the supplied Class.
     * @return All interfaces implemented by the provided class, and which
     * matched the supplied selector's acceptance criteria.
     */
    @NotNull
    public static SortedSet<Class<?>> getInterfaces(@NotNull final Class<?> clazz,
                                                    final Predicate<Class<?>> selector) {

        // Check sanity
        Validate.notNull(clazz, "clazz");

        // Extract all interfaces
        final TypeInformation typeInfo = new TypeInformation(clazz);
        final SortedSet<Class<?>> allInterfaces = typeInfo.getAllInterfaces();

        if (log.isDebugEnabled()) {
            log.debug(clazz.getName() + " implements " + allInterfaces.size() + " interfaces: "
                    + allInterfaces.stream()
                    .map(Class::getName)
                    .sorted()
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<none>"));
        }

        // Delegate
        return selector == null
                ? allInterfaces
                : allInterfaces.stream().filter(selector).collect(TypeAlgorithms.SORTED_CLASSNAME_COLLECTOR);
    }
]]></pre>

