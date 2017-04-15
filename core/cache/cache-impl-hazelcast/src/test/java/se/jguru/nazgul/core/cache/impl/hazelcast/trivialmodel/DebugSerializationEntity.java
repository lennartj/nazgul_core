/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */

package se.jguru.nazgul.core.cache.impl.hazelcast.trivialmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DebugSerializationEntity implements Serializable {

    // Internal state
    public String name;
    public Collection<String> friends;

    public DebugSerializationEntity(String name, Collection<String> friends) {
        this.name = name;
        this.friends = friends;
    }

    @Override
    public boolean equals(Object obj) {
        DebugSerializationEntity that = (DebugSerializationEntity) obj;
        return (new EqualsBuilder()).append(name, that.name).isEquals();
    }

    @Override
    public int hashCode() {
        return (new HashCodeBuilder()).append(name).hashCode();
    }

    @Override
    public String toString() {
        return "(" + name + ": " + friends + ") ";
    }
}
