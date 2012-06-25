package se.jguru.nazgul.core.reflection.api.annotation;

import java.io.Serializable;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AnnotatedImplementation implements AnnotatedSpecification, Serializable {

    // Internal state
    private String value;
    private int age;

    public AnnotatedImplementation(final String value, final int age) {
        this.value = value;
        this.age = age;
    }

    @Override
    @TestMethodMarkerAnnotation
    public String getValue() {
        return value;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public void setAge(int age) {
        this.age = age;
    }

    @TestMethodMarkerAnnotation
    protected String getProtectedValue() {
        return value;
    }
}
