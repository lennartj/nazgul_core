/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.inheritance;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class InheritanceStructureTest {

    // Shared state
    private JaxbXmlBinder binder;
    private JaxbNamespacePrefixResolver namespacePrefixResolver;
    private Comparator<Platter> platterComparator = getComparator();

    private Platter dinnerPlatter;
    private Platter veganPlatter;

    private Sausage bratwurst;
    private Sausage falu;

    private Vegetable broccoli;
    private Vegetable peas;
    private Vegetable lettuce;

    @Before
    public void setupSharedState() {

        // Setup the namespace resolver
        namespacePrefixResolver = new JaxbNamespacePrefixResolver();
        namespacePrefixResolver.put(Platter.FOOD_NAMESPACE, "foodz");

        // Create the binder
        binder = new JaxbXmlBinder(namespacePrefixResolver);

        // Create some foods
        bratwurst = new Sausage("bratwurst", "Germany", "grillable");
        falu = new Sausage("falukorv", "Sweden", "inedible");

        broccoli = new Vegetable("broccoli", "Denmark", "greenery", false);
        peas = new Vegetable("peas", "Brazil", "greenery", false);
        lettuce = new Vegetable("lettuce", "Finland", "greenery", true);

        // Create some platters
        dinnerPlatter = new Platter("dinner", 250);
        dinnerPlatter.getFoods().addAll(Arrays.<AbstractFood>asList(bratwurst, broccoli, lettuce));

        veganPlatter = new Platter("lunch", 85);
        veganPlatter.getFoods().addAll(Arrays.<AbstractFood>asList(falu, broccoli, peas, lettuce));
    }

    @Test
    public void validateMarshallingAndUnmarshallingAbstractTypeCollectionWithoutXmlSeeAlso() {

        // Assemble

        // Act
        final String marshalled = binder.marshal(veganPlatter);
        final Platter resurrected = binder.unmarshalInstance(new StringReader(marshalled));

        // Assert
        Assert.assertEquals(0, platterComparator.compare(veganPlatter, resurrected));
        Assert.assertTrue(platterComparator.compare(dinnerPlatter, resurrected) != 0);
    }

    //
    // Private helpers
    //

    private Comparator<Platter> getComparator() {
        return new Comparator<Platter>() {
            @Override
            public int compare(final Platter first, final Platter second) {

                // Start by comparing the names of the platters
                int result = first.getName().compareTo(second.getName());

                if (result == 0) {

                    // Compare the platter prices
                    result = ((Integer) first.getPrice()).compareTo(second.getPrice());

                    if (result == 0) {

                        // Compare the number of AbstractFood instances on the platter
                        result = ((Integer) first.getFoods().size()).compareTo(second.getFoods().size());

                        if (result == 0) {

                            final int numFoods = first.getFoods().size();

                            // Compare each AbstractFood
                            for (int i = 0; i < numFoods; i++) {
                                result = first.getFoods().get(i).compareTo(second.getFoods().get(i));
                                if (result != 0) {
                                    break;
                                }
                            }
                        }
                    }
                }

                // All done.
                return result;
            }
        };
    }
}
