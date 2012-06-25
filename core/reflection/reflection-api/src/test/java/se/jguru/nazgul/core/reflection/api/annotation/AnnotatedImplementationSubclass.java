package se.jguru.nazgul.core.reflection.api.annotation;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AnnotatedImplementationSubclass extends AnnotatedImplementation {

    @TestFieldMarkerAnnotation
    private String aPrivateMarkedValue;

    @TestFieldMarkerAnnotation(permit = false)
    private String aPrivateMarkedNotPermittedValue = "fooBar!";

    public AnnotatedImplementationSubclass(String value, int age) {
        super(value, age);
        aPrivateMarkedValue = "meaningOfAnnotationLife";
    }

    @TestMethodMarkerAnnotation
    public String getValueInSubclass() {
        return aPrivateMarkedValue;
    }
}
