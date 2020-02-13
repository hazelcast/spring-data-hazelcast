/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.utils.repository.custom;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.data.hazelcast.repository.support.HazelcastRepositoryFactoryBean;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * <P>Factory bean for creating instances of {@link MyTitleRepository},
 * being {@link MovieRepository} and {@link SongRepository}.
 * </P>
 *
 * @param <T>  Repository type
 * @param <S>  Domain object type
 * @param <ID> Key of domain object type
 * @author Neil Stevenson
 */
public class MyTitleRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends HazelcastRepositoryFactoryBean<T, S, ID> {
    @Resource
    private HazelcastInstance hazelcastInstance;

    /*
     * Creates a new {@link MyTitleRepositoryFactoryBean} for the given repository interface.
     */
    public MyTitleRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    /* Create a specialised repository factory.
     *
     * (non-Javadoc)
     * @see org.springframework.data.hazelcast.repository.support.HazelcastRepositoryFactoryBean#createRepositoryFactory(org
     * .springframework.data.keyvalue.core.KeyValueOperations, java.lang.Class, java.lang.Class)
     */
    @Override
    protected MyTitleRepositoryFactory createRepositoryFactory(KeyValueOperations operations,
                                                               Class<? extends AbstractQueryCreator<?, ?>> queryCreator,
                                                               Class<? extends RepositoryQuery> repositoryQueryType) {

        return new MyTitleRepositoryFactory(operations, queryCreator, hazelcastInstance);
    }

}

