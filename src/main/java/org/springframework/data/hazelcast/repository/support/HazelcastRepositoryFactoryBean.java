/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hazelcast.repository.support;

import java.io.Serializable;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hazelcast.repository.config.Constants;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean;

/**
 * <P>
 * Extend {@link KeyValueRepositoryFactoryBean} purely to be able to return {@link HazelcastRepositoryFactory} from
 * {@link #createRepositoryFactory(KeyValueOperations, Class, Class)} method.
 * </P>
 * <P>
 * This is necessary as the default implementation does not implement querying in a manner consistent with Hazelcast.
 * More details of this are in {@link HazelcastRepositoryFactory}.
 * </P>
 * <P>
 * This class is only called as repositories are needed. Rather than try to optimise called methods, allow to delegate
 * to {@code super} to ease future changes.
 * </P>
 * <P>
 * The end goal of this bean is for {@link HazelcastPartTreeQuery} to be used for query preparation.
 * </P>
 *
 * @author Neil Stevenson
 * @param <T> Repository type, {@link HazelcastRepository}
 * @param <S> Domain object class
 * @param <ID> Domain object key, super expects {@link Serializable}
 */
public class HazelcastRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
		extends KeyValueRepositoryFactoryBean<T, S, ID> {

    @Autowired(required = false)
    private HazelcastInstance hazelcastInstance;

	/**
	 * <p>
	 * Default Spring Data KeyValue constructor {@link KeyValueRepositoryFactoryBean}
	 * Creates a new {@link HazelcastRepositoryFactoryBean} for the given repository interface.
	 * </P>
	 *
	 * @param repositoryInterface must not be {@literal null}.
	 */
	public HazelcastRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	/**
	 * <P>
	 * Return a {@link HazelcastRepositoryFactory}.
	 * </P>
	 * <P>
	 * {@code super} would return {@link KeyValueRepositoryFactory} which in turn builds {@link KeyValueRepository}
	 * instances, and these have a private method that implement querying in a manner that does not fit with Hazelcast.
	 * More details are in {@link HazelcastRepositoryFactory}.
	 * </P>
	 *
	 * @param KeyValueOperations
	 * @param Query Creator
	 * @param RepositoryQueryType, not used
	 * @return A {@link HazelcastRepositoryFactory} that creates {@link HazelcastRepository} instances.
	 */
	@Override
	protected KeyValueRepositoryFactory createRepositoryFactory(KeyValueOperations operations,
			Class<? extends AbstractQueryCreator<?, ?>> queryCreator, Class<? extends RepositoryQuery> repositoryQueryType) {
        HazelcastInstance notNullHazelcastInstance = hazelcastInstance == null ? HazelcastInstanceFactory
                .getOrCreateHazelcastInstance(new Config(Constants.HAZELCAST_INSTANCE_NAME)) : hazelcastInstance;
		return new HazelcastRepositoryFactory(operations, queryCreator, notNullHazelcastInstance);
	}

}
