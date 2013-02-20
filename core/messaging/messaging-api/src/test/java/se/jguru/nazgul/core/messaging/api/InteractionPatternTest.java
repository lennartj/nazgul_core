package se.jguru.nazgul.core.messaging.api;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class InteractionPatternTest {

    @Test
    public void validateOrdinalAndValues() {

        // Assemble
        final InteractionPattern[] values = InteractionPattern.values();
        final InteractionPattern[] expectedOrder = {
                InteractionPattern.FIRE_AND_FORGET,
                InteractionPattern.PSEUDO_SYNCHRONOUS,
                InteractionPattern.EVENT_CALLBACK};

        // Act & Assert
        Assert.assertEquals(expectedOrder.length, values.length);
        for(int i = 0; i < expectedOrder.length; i++) {
            Assert.assertSame(expectedOrder[i], values[i]);
        }
    }
}
