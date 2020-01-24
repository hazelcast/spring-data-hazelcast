/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package test.utils.repository.custom;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.data.hazelcast.repository.support.HazelcastRepositoryFactory;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;

import java.io.Serializable;

/**
 * <P>Factory for creating instances of {@link MyTitleRepository},
 * being {@link MovieRepository} and {@link SongRepository}.
 * </P>
 *
 * @author Neil Stevenson
 */
public class MyTitleRepositoryFactory
        extends HazelcastRepositoryFactory {

    private KeyValueOperations keyValueOperations;

    /* Delegate creation to super, but capture the KeyValueOperations
     * object for later use.
     */
    public MyTitleRepositoryFactory(KeyValueOperations keyValueOperations,
                                    Class<? extends AbstractQueryCreator<?, ?>> queryCreator,
                                    HazelcastInstance hazelcastInstance) {
        super(keyValueOperations, queryCreator, hazelcastInstance);
        this.keyValueOperations = keyValueOperations;
    }

    /* Create an implementation using captured KeyValueOperations
     * and the domain class metadata.
     *
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory#getTargetRepository(org
     * .springframework.data.repository.core.RepositoryInformation)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object getTargetRepository(RepositoryInformation repositoryInformation) {
        EntityInformation<?, Serializable> entityInformation = getEntityInformation(repositoryInformation.getDomainType());
        return new MyTitleRepositoryImpl(entityInformation, this.keyValueOperations);
    }

    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return MyTitleRepository.class;
    }

}
