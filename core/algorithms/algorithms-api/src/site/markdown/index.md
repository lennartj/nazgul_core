Nazgul Core: Algoritms API
=============================

The Algoritms API project contains implementations of functional-style, reusable algorithms
which can be imported and used within any project. Most of the algorithms are divided into 2
categories, manifested by 2 classes:

1. **Collection algorithms**. The CollectionAlgorithms class contains a set of static methods
   that simplifies working with Collections. These algorithms are functional in nature, but
   can be used with any JDK 1.5+ collections.

2. **Tree algorithms**. The TreeAlgorithms class contains algorithms that simplify working with
   data, organised within a Tree structure. The javax.swing package contains a partial
   implementation of a Tree model, but the Tree definitions within the Algorithms API project
   provides a fuller/richer set of operations than the swing implementation.

## Collection algorithm usage examples

Usage of all algorithms is found within the test cases of the Algorithms API project,
but a sample is shown below. Frequent tasks, such as obtaining Class objects from Strings
containing class names, can be stashed as public static final Transformers to simplify
processing of collections drastically.

<pre class="brush: java"><![CDATA[
        // Create a Filter which acts on Collections containing Strings
        final Filter<String> filter = new Filter<String>() {
            @Override
            public boolean accept(final String candidate) {
                return !candidate.equals("43");
            }
        };

        // Filter the "source" collection, returning a new Collection
        // with the entries accepted by the Filter.
        final List<String> result = CollectionAlgorithms.filter(source, filter);
]]></pre>




