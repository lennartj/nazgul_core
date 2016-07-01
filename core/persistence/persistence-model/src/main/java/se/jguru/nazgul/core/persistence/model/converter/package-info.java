/**
 * <p>Package containing JPA AttributeConverter implementations which makes Java8 time classes and JodaTime DateTime
 * types first class citizens. This implies that entity classes can use either of these hierarchies of classes as
 * internal state properties, as long as the <code>persistence.xml</code> file contains entries on any of the
 * converter entries shown below. It is advisable to include only one of the set of converters, as shown below.</p>
 * <h2>Java8 Time Classes</h2>
 * <p>The converters for Java8 time classes can be included in your persistence context with the following entries:</p>
 * <pre>
 *     <code>
 * &lt;class&gt;se.jguru.nazgul.core.persistence.model.converter.LocalDateAttributeConverter&lt;/class&gt;
 * &lt;class&gt;se.jguru.nazgul.core.persistence.model.converter.LocalDateTimeAttributeConverter&lt;/class&gt;
 * &lt;class&gt;se.jguru.nazgul.core.persistence.model.converter.LocalTimeAttributeConverter&lt;/class&gt;
 *      </code>
 * </pre>
 * <h2>JodaTime Classes</h2>
 * <p>The converters for joda-time classes can be included in your persistence context with the following entries:</p>
 * <pre>
 *     <code>
 * &lt;class&gt;se.jguru.nazgul.core.persistence.model.converter.DateTimeDateConverter&lt;/class&gt;
 * &lt;class&gt;se.jguru.nazgul.core.persistence.model.converter.DateTimeTimestampConverter&lt;/class&gt;
 *      </code>
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see javax.xml.bind.annotation.XmlRootElement
 */
@XmlSchema(
        xmlns = {
                @XmlNs(prefix = "core", namespaceURI = "http://www.jguru.se/nazgul/core"),
                @XmlNs(prefix = "xs", namespaceURI = "http://www.w3.org/2001/XMLSchema"),
                @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"),
                @XmlNs(prefix = "vc", namespaceURI = "http://www.w3.org/2007/XMLSchema-versioning")
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
package se.jguru.nazgul.core.persistence.model.converter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
