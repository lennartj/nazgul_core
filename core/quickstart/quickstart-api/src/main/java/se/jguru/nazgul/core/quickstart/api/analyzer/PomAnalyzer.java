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
package se.jguru.nazgul.core.quickstart.api.analyzer;

import org.apache.maven.model.Model;
import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;
import se.jguru.nazgul.core.quickstart.api.PomType;

/**
 * <p>Specification for how to validate the content of POMs. In the Nazgul Framework structure, POMs have
 * distinct roles and - therefore - consists of separate patterns. This PomAnalyzer provides a top-level
 * specification for how a client can validate the content of a Maven POM. Such validation is required to
 * quickly detect incompatibilities in a desired multi-module Maven reactor structure - which, in turn, is
 * required to reduce entropy increase in big Maven projects.</p>
 * <p>Refer to <a href="http://maven.apache.org/pom.html">the Maven POM specification</a> which hosts a reference to
 * all parts of the POM.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface PomAnalyzer {

    /**
     * Validates the supplied Maven Root reactor POM Model or throws an exception indicating why the
     * supplied POM was invalid. The "Root reactor POM" is the topmost POM within the local reactor,
     * not counting a Parent POM which is not built within the reactor.
     * This method is a convenience specification, and should yield the equivalent of calling
     * {@code validate(aRootReactorPom, PomAnalyzer.PomType.ROOT_REACTOR, null)}
     *
     * @param aRootReactorPom A non-null maven Model from the topmost POM within the reactor (a.k.a. root reactor POM).
     * @throws InvalidStructureException if the supplied aRootReactorPom had invalid dependencies according
     *                                   to this PomAnalyzer.
     */
    void validateRootReactorPom(Model aRootReactorPom) throws InvalidStructureException;

    /**
     * Validates the supplied Maven topmost parent POM Model or throws an exception indicating why the
     * supplied POM was invalid. The "topmost parent POM" is the topmost POM within the local reactor,
     * used as Parent for other POMs. This differs from the root reactor pom in that parent poms are used
     * to define dependencies and plugins but not modules.
     * This method is a convenience specification, and should yield the equivalent of calling
     * {@code validate(topmostParentPom, PomAnalyzer.PomType.PARENT, null)}
     *
     * @param topmostParentPom A non-null maven Model from the topmost parent POM within the reactor.
     * @throws InvalidStructureException if the supplied aRootReactorPom had invalid dependencies according
     *                                   to this PomAnalyzer.
     */
    void validateTopmostParentPom(Model topmostParentPom) throws InvalidStructureException;

    /**
     * Validates the supplied Maven POM Model or throws an exception indicating why the supplied POM was invalid.
     *
     * @param aPOM         The POM model to validate.
     * @param parentOrNull The expected parent of aPOM, or {@code null} if it is irrelevant.
     * @param expectedType The expected type of POM to validate the supplied Model against.
     * @throws InvalidStructureException if the supplied aPOM was invalid according to this PomDependencyStrategy.
     */
    void validate(Model aPOM, PomType expectedType, Model parentOrNull) throws InvalidStructureException;
}
