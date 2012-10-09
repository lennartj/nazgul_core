/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper;

import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.AbstractXmlAdapterTypeConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FooConverter extends AbstractXmlAdapterTypeConverter<JaxbAnnotatedFoo, Foo> {

    // Internal state
    private FooXmlAdapter adapter;

    public FooConverter() {
        super(JaxbAnnotatedFoo.class, Foo.class);

        // Assign internal state
        adapter = new FooXmlAdapter();
    }

    /**
     * @return An XmlAdapter instance converting between the TransportType and OriginalType.
     *         Cannot be {@code null}.
     */
    @Override
    protected XmlAdapter<JaxbAnnotatedFoo, Foo> getAdapter() {
        return adapter;
    }

    class FooXmlAdapter extends XmlAdapter<JaxbAnnotatedFoo, Foo> {

        @Override
        public Foo unmarshal(final JaxbAnnotatedFoo v) throws Exception {
            return v == null ? null : v.getValue();
        }

        @Override
        public JaxbAnnotatedFoo marshal(final Foo v) throws Exception {
            return v == null ? null : new JaxbAnnotatedFoo(v);
        }
    }
}
