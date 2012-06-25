package se.jguru.nazgul.core.reflection.api.annotation;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@TestTypeMarkerAnnotation
public interface AnnotatedSpecification {

    String getValue();

    int getAge();

    void setAge(int age);
}
