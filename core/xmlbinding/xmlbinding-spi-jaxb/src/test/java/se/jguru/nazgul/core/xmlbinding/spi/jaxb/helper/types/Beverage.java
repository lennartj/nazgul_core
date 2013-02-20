package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "http://some/good/beverage", propOrder = {"name"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Beverage {

    private String name;

    public Beverage() {
    }

    public Beverage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
