/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.jguru.nazgul.core.quickstart.api;

/**
 * The types of POM normally available within a multi-module Maven project.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public enum PomType {

    /**
     * The root POM within the Maven build reactor.
     */
    ROOT_REACTOR,

    /**
     * A reactor POM within any directory other than the build reactor root.
     */
    REACTOR,

    /**
     * The root parent POM within the build reactor.
     * This normally has a parent outside of the build reactor.
     */
    PARENT,

    /**
     * The root parent POM of all API (and SPI) projects.
     * This project differs from the root parent POM in that is normally exports all classes for public use.
     */
    API_PARENT,

    /**
     * The root parent POM of all Model projects.
     * This project differs from the API parent POM in that it normally provides extended libraries and
     * facilities for unit testing Entities - typically libraries required for JPA or JAXB tests.
     */
    MODEL_PARENT,

    /**
     * The root parent POM of all Web Archive projects.
     * This project differs from the API parent POM in that it normally provides extended libraries and
     * facilities for testing WARs and web applications.
     */
    WAR_PARENT,

    /**
     * Any parent POM (other than already listed ones) within the reactor.
     */
    OTHER_PARENT,

    /**
     * The POM of a model project, part of a Software Component.
     */
    COMPONENT_MODEL,

    /**
     * The POM of an API project, part of a Software Component.
     */
    COMPONENT_API,

    /**
     * The POM of an SPI project, part of a Software Component.
     */
    COMPONENT_SPI,

    /**
     * The POM of an implementation project, part of a Software Component.
     */
    COMPONENT_IMPLEMENTATION,

    /**
     * The POM of a module test project, part of a Software Component.
     */
    COMPONENT_TEST,

    /**
     * Any other POM
     */
    OTHER
}
