package se.jguru.nazgul.core.reflection.api.conversion.registry.helpers;

import se.jguru.nazgul.core.reflection.api.conversion.Converter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class StringConstructorConverter {

    // Interal state
    private String value;

    @Converter(priority = (Converter.DEFAULT_PRIORITY - 2))
    public StringConstructorConverter(final String aString) {
        this.value = aString;
    }

    @Converter
    public StringConstructorConverter(final String aString, final boolean aBoolean) {
        this.value = "" + aBoolean + "_" + aString;
    }

    public final String getValue() {
        return value;
    }
}
