package se.jguru.nazgul.core.reflection.api.conversion.registry.helpers;

import se.jguru.nazgul.core.reflection.api.conversion.Converter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class FakeConverter {

    @Converter
    public void tooFewArguments() {
        // Do nothing
    }

    @Converter
    public StringBuffer tooManyArguments(final String foo, final String bar) {
        return new StringBuffer(foo);
    }

    @Converter(conditionalConversionMethod = "nonExistentConversionMethod")
    public StringBuffer couldHaveBeenAConverterMethod(final String aString) {
        return new StringBuffer(aString);
    }
}
