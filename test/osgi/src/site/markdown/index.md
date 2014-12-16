# Nazgul Core: OSGi Test

The Nazgul Core: OSGi Test provides a set of classes that implement some of the
frequently occurring OSGi interfaces and sporting a (unit-)test-friendly internal
mechanics which can be set up as required in unit tests. Use this project when you
need to write unit tests for OSGi services. Include the Nazgul Core: OSGi Test
project into your OSGi-enabled project in the following manner
(note the **test** scope):

<pre class="brush: xml"><![CDATA[
        <dependency>
            <groupId>se.jguru.nazgul.test.osgi</groupId>
            <artifactId>nazgul-core-osgi-test</artifactId>
            <scope>test</scope>
        </dependency>
]]></pre>

### Why is this required?

Testing OSGi components can be quite taxing on the developer. On the one hand, OSGi
is a very POJO-inspired technology which means testing implementations in isolation
is trivial. On the other hand, the OSGi container is a highly asynchronous platform
for dependency injection, which makes unit testing very complex. The complexity of
unit testing OSGi applications is accentuated by the fact that the OSGi container
injects both implementation libraries (i.e. JARs) and services (i.e. Objects) in an
asynchronous manner.

If you have not developed OSGi applications, testing something developed around POJO
principles may sound like a trivial exercise. This is true only when creating tests
that do not rely on any OSGi mechanics such as events and dependency injection of
objects - but these things are what makes OSGi containers attractive runtime containers
and are very frequently used in OSGi applications.

An example illustrates the mocked OSGi integration:

### Example: OSGi events in a synchronous unit-test environment

The OSGi container directs events to listeners which are required to implement
various interfaces. This is an example of a BundleListener implementation for
unit test purposes:

<pre class="brush: java" title="Prelude: An OSGi BundleListener class"><![CDATA[

    public class TracingBundleListener implements BundleListener {

    public List<BundleEvent> callTrace = new ArrayList<BundleEvent>();
    public CountDownLatch onEventLatch;

    // Internal state
    private String id;

    /**
     * Creates a non-blocking TracingBundleListener instance.
     */
    public TracingBundleListener(final String id) {
        this(-1, id);
    }

    /**
     * Creates a TracingBundleListener instance holding a non-null onEventLatch with the given count.
     * The count of the onEventLatch is  decreased whenever the bundleChanged method is called.
     * For any value of countDownValue less than 1, no CountDownLatch will be created.
     *
     * @param countDownValue The countDown value of the created onEventLatch.
     * @see #onEventLatch
     */
    public TracingBundleListener(final int countDownValue, final String id) {

        this.id = id;
        if (countDownValue > 0) {
            onEventLatch = new CountDownLatch(countDownValue);
        }
    }

    /**
     * Receives notification that a bundle has had a lifecycle change.
     *
     * @param event The {@code BundleEvent}.
     */
    @Override
    public void bundleChanged(final BundleEvent event) {

        callTrace.add(event);

        if (onEventLatch != null) {
            onEventLatch.countDown();
        }
    }

    /**
     * @return The id of this TracingBundleListener.
     */
    public String getId() {
        return id;
    }
}
]]></pre>

The listener above is used in a test case to illustrate that the MockBundle sends and stores
events internally in the correct order an of the correct type. Note that the MockBundle class
("unitUnderTest") stems from the Nazgul Core: OSGi Test project:

<pre class="brush: java" title="OSGi-ified Unit Test using Nazgul Core: OSGi Test"><![CDATA[

    // Shared state
    private String osgiStringVersion = "1.2.3.SNAPSHOT";
    private MockBundle unitUnderTest;

    @Before
    public void setupSharedState() {
        unitUnderTest = new MockBundle(osgiStringVersion);
    }

    @Test
    public void validateListenerCallbacksOnStartStopLifecycle() throws BundleException {

        // Assemble
        final TracingBundleListener bundleListener = new TracingBundleListener("testId");
        unitUnderTest.getBundleContext().addBundleListener(bundleListener);

        // Act
        unitUnderTest.start();
        unitUnderTest.stop();

        // Assert
        final List<BundleEvent> callTrace = bundleListener.callTrace;
        Assert.assertEquals(2, callTrace.size());
        Assert.assertEquals(BundleEvent.STARTED, callTrace.get(0).getType());
        Assert.assertEquals(BundleEvent.STOPPED, callTrace.get(1).getType());
    }
]]></pre>
