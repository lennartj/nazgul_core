/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.ClassInformationHolder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Abstract supertype of JAXB-annotated transport types, implying that all subtypes
 * should implement ClassInformationHolder and Serializable.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractJaxbAnnotatedTransportType<T>
        implements ClassInformationHolder, Serializable, Comparable {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(AbstractJaxbAnnotatedTransportType.class);

    /**
     * Transport types require a serialVersionUID.
     */
    public static final long serialVersionUID = 7085076030001L;

    // Internal state
    @XmlTransient
    protected T value;

    @XmlTransient
    protected SortedSet<String> classInformation;

    /**
     * JAXB-friendly constructor.
     * <strong>This is for framework use only.</strong>
     */
    public AbstractJaxbAnnotatedTransportType() {

        // Create classinfo holder
        TreeSet<String> classinfo = new TreeSet<String>();

        // Get the class of the generic parameter
        TypeToken<T> parameterTypeToken = new TypeToken<T>(getClass()) {
        };

        final Type type = parameterTypeToken.getType();

        if (type instanceof Class) {

            // Simply cast the type, and use it.
            Class<T> transportTypeClass = (Class<T>) type;
            classinfo.add(transportTypeClass.getName());

        } else if (type instanceof TypeVariable) {

            // Populate recursively
            populate(classinfo, (TypeVariable) type);

        } else {

            // Complain.
            log.error("Could not acquire class information for [" + type + "]. Extract manually.");
        }

        // Create internal state
        classInformation = Collections.unmodifiableSortedSet(classinfo);
    }

    /**
     * Compound default constructor, wrapping the provided value within itself.
     *
     * @param value The original type value to wrap.
     */
    public AbstractJaxbAnnotatedTransportType(T value) {
        this();

        // Check sanity
        Validate.notNull(value, "Cannot handle null value argument.");

        // Assign internal state
        this.value = value;
    }

    /**
     * Compound constructor, assigning the given classInformation (i.e. class names on
     * string form) to the classInformation member - wrapped into an unmodifiable SortedSet
     * instance.
     *
     * @param classInformation The classInformation data, holding strings received by calls
     *                         to {@code Class.getName()}.
     */
    protected AbstractJaxbAnnotatedTransportType(final SortedSet<String> classInformation) {

        // Do *not* delegate to the default constructor of this type.
        super();

        // Delegate
        setClassInformation(classInformation);
    }

    /**
     * {@inheritDoc}
     */
    public final SortedSet<String> getClassInformation() {
        return classInformation;
    }

    /**
     * Assigns the class information (in the form of {@code Class.getName()} for each
     * transport-wrapped class) within this AbstractJaxbAnnotatedTransportType instance.
     *
     * @param classInformation The names of all classes which should be transport-wrapped within
     *                         this AbstractJaxbAnnotatedTransportType instance. The data should
     *                         be strings received by calls to {@code Class.getName()} for each
     *                         transport-wrapped class.
     */
    protected final void setClassInformation(final SortedSet<String> classInformation) {

        // Check sanity
        Validate.notEmpty(classInformation, "Cannot handle null or empty classInformation argument.");

        SortedSet<String> classinfo = new TreeSet<String>();
        for (String current : classInformation) {

            Validate.notEmpty(current, "Given classInformation argument cannot contain null or empty values.");
            classinfo.add(current);
        }

        // Assign internal state.
        this.classInformation = Collections.unmodifiableSortedSet(classinfo);
    }

    /**
     * @return The contained value.
     */
    public T getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    //
    // Private helpers
    //

    private static void populate(SortedSet<String> typeNames, final TypeVariable typeVariable) {

        // Extract the generic declaration information from the TypeVariable
        GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
        if(genericDeclaration instanceof Class) {

            // TODO: Extract and assign converted types to the typeNames (i.e. the JaxbAnnotatedX types)
            EntityTransporter.getRegistry().getPackagingTransportTypeConverter()
            typeNames.add(((Class) genericDeclaration).getName());
        }

        // Extract the types of the generics.
        for (Type current : typeVariable.getBounds()) {
            if (current instanceof Class) {

                // TODO: Extract and assign converted types to the typeNames (i.e. the JaxbAnnotatedX types)
                // Simply cast the type, and use it.
                typeNames.add(((Class) current).getName());

            } else if(current instanceof TypeVariable) {

                // Decend
                populate(typeNames, (TypeVariable) current);
            }
        }
    }
}
