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
package se.jguru.nazgul.core.quickstart.api.generator.parser;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.parser.api.agent.AbstractParserAgent;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Parser agent which performs token substitutions using data within a Project.
 * Readable JavaBean getters are accessible using expressions such as <code>${project:reactorParent.groupId}</code>,
 * or simple properties such <code>${project:name}</code>.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FactoryParserAgent extends AbstractParserAgent {

    /**
     * Prefix indicating that a token replacement value should
     * be acquired from Project data using <code>project.getSomeProperty()</code>,
     * given that the token is <code>${project:someProperty}</code>.
     */
    public static final String PROJECT_PREFIX = "project:";

    /**
     * Delimiter between property names in expressions.
     * For example, the property accessor <code>${project:reactorParent.groupId}</code> uses the separator (".")
     * to indicate that the getGroupId() method should be called in the object returned by the getReactorParent()
     * method in the Project class.
     */
    public static final String PROPERTY_DELIMITER = ".";

    private static final Comparator<Class<?>> CLASSNAME_COMPARATOR = new Comparator<Class<?>>() {
        @Override
        public int compare(final Class<?> o1, final Class<?> o2) {

            final String lhsName = o1 == null ? "" : o1.getName();
            final String rhsName = o2 == null ? "" : o2.getName();

            return lhsName.compareTo(rhsName);
        }
    };

    // Internal state
    private Project project;
    private Map<Class<?>, Map<String, Method>> typedPropertyReadMethods;

    /**
     * Convenience constructor creating a FactoryParserAgent wrapping the supplied project object,
     * from which readable JavaBean getters are accessible using expressions such as
     * <code>${project:reactorParent.groupId}</code>. No extra static tokens are supplied.
     *
     * @param project The Project from which this FactoryParserAgent should read token data.
     */
    public FactoryParserAgent(final Project project) {
        this(project, null);
    }

    /**
     * Compound constructor creating a FactoryParserAgent wrapping the supplied project object,
     * from which readable JavaBean getters are accessible using expressions such as
     * <code>${proj:reactorParent.groupId}</code>.
     *
     * @param project      The Project from which this FactoryParserAgent should read token data.
     * @param staticTokens An optional Map containing static tokens for substitution.
     */
    public FactoryParserAgent(final Project project, final Map<String, String> staticTokens) {

        // Check sanity
        Validate.notNull(project, "Cannot handle null project argument.");

        // Assign internal state
        this.project = project;
        this.typedPropertyReadMethods = new TreeMap<>(CLASSNAME_COMPARATOR);

        mapJavaBeanPropertyGetters(typedPropertyReadMethods, Project.class);
        mapJavaBeanPropertyGetters(typedPropertyReadMethods, SimpleArtifact.class);

        if (staticTokens != null) {
            for (Map.Entry<String, String> current : staticTokens.entrySet()) {
                addStaticReplacement(current.getKey(), current.getValue());
            }
        }

        // Add the standard dynamic replacement tokens.
        dynamicTokens.add(PROJECT_PREFIX + ".*");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String performDynamicReplacement(final String token) {

        // Is this a Project property?
        if (token.startsWith(PROJECT_PREFIX)) {
            String key = token.substring(PROJECT_PREFIX.length());
            return "" + getProjectData(key);
        }

        // Unknown token. Complain.
        throw new IllegalArgumentException("Cannot handle dynamic token [" + token + "].");
    }

    /**
     * Adds an entry between the supplied Class and a Map relating readable JavaBean property names
     * to their respective getter methods.
     *
     * @param toPopulate The structure to populate with the JavaBean name 2 getter methods for the supplied class.
     * @param clazz      The class which should be introspected.
     */
    public static void mapJavaBeanPropertyGetters(final Map<Class<?>, Map<String, Method>> toPopulate,
                                                  final Class<?> clazz) {

        // Check sanity
        Validate.notNull(clazz, "Cannot handle null clazz argument.");
        Validate.notNull(toPopulate, "Cannot handle null toPopulate argument.");

        // Map all JavaBean getter methods to their respective property names.
        Map<String, Method> name2GetterMap = toPopulate.get(clazz);
        if (name2GetterMap == null) {
            name2GetterMap = new TreeMap<>();
            toPopulate.put(clazz, name2GetterMap);
        }

        try {
            for (PropertyDescriptor current : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {

                String name = current.getName();
                Method getter = current.getReadMethod();
                if (getter != null && name != null) {
                    name2GetterMap.put(name, getter);
                }
            }
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException("Could not introspect class [" + clazz.getName() + "]", e);
        }
    }

    //
    // Private helpers
    //

    private Object getProjectData(final String propertyName) {

        StringTokenizer tok = new StringTokenizer(propertyName, PROPERTY_DELIMITER, false);

        Object currentResult = null;
        for (Object current = this.project; tok.hasMoreTokens(); current = currentResult) {

            final Class<?> currentClass = current.getClass();
            final Map<String, Method> getterName2GetterMap = typedPropertyReadMethods.get(currentClass);
            Validate.notNull(getterName2GetterMap, "Class [" + currentClass.getName()
                    + "] was not mapped WRT JavaBean getter methods. Mapped classes: "
                    + typedPropertyReadMethods.keySet());

            final String readablePropertyName = tok.nextToken();
            final Method getter = getterName2GetterMap.get(readablePropertyName);
            Validate.notNull(getter, "Class [" + currentClass.getName() + "] has no readable property named ["
                    + readablePropertyName + "]. Mapped property names are: " + getterName2GetterMap.keySet());

            // Invoke the getter, note the result.
            try {
                currentResult = getter.invoke(current);
            } catch (Exception e) {
                throw new IllegalArgumentException("JavaBean property [" + readablePropertyName
                        + "] getter method call failed in class [" + currentClass.getName() + "]", e);
            }
        }

        // All done.
        return currentResult;
    }
}
