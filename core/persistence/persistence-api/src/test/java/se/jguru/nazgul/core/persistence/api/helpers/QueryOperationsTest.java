/*-
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.core.persistence.api.helpers;

import org.easymock.EasyMock;
import org.junit.Test;
import se.jguru.nazgul.core.persistence.api.PersistenceOperationException;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class QueryOperationsTest {

    @Test
    public void validateNoExceptionOnNullParameters() {

        // Assemble
        final Query irrelevantQuery = null;
        final Object[] nullArguments = null;

        // Act & Assert
        QueryOperations.setParameters(irrelevantQuery, nullArguments);
    }

    @Test(expected = PersistenceOperationException.class)
    public void validateValidationExceptionIsWrappedInPersistenceException() {

        // Assemble
        final Class<MockNazgulEntity> mockNazgulEntityClass = MockNazgulEntity.class;
        final Integer mockPrimaryKey = 42;
        final EntityManager mockEntityManager = EasyMock.createMock(EntityManager.class);
        final MockNazgulEntity mockNazgulEntity = new MockNazgulEntity("foobar!");
        mockNazgulEntity.throwValidationException = true;

        EasyMock.expect(mockEntityManager.find(mockNazgulEntityClass, mockPrimaryKey)).andReturn(mockNazgulEntity);
        EasyMock.replay(mockEntityManager);

        // Act & Assert
        QueryOperations.findByPrimaryKey(mockNazgulEntityClass, mockEntityManager, mockPrimaryKey);
    }
}
