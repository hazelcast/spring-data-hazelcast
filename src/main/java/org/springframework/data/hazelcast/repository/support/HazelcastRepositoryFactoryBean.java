/*
 * Copyright 2014-2015 the original author or authors.
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

import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean;

/**
 * <P>Extend {@link KeyValueRepositoryFactoryBean} purely to be able to return
 * {@link HazelcastRepositoryFactory} from {@link #createRepositoryFactory()}
 * method.
 * </P>
 * <P>This is necessary as the default implementation does not implement querying
 * in a manner consistent with Hazelcast. More details of this are in
 * {@link HazelcastRepositoryFactory}.
 * </P>
 * <P>This class is only called as repositories are needed. Rather than try to
 * optimise called methods, allow to delegate to {@code super} to ease future changes.
 * </P>
 * <P>The end goal of this bean is for {@link HazelcastPartTreeQuery} to be used
 * for query preparation.
 * </P>
 *
 * @author Neil Stevenson
 *
 * @param <T>   Repository type, {@link HazelcastRepository}
 * @param <S>   Domain object class
 * @param <ID>  Domain object key, super expects {@link Serializable}
 */
public class HazelcastRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
    extends KeyValueRepositoryFactoryBean<T, S, ID> {

    private KeyValueOperations operations;
    private Class<? extends AbstractQueryCreator<?, ?>> queryCreator;

    /* Capture KeyValueOperations before passing to super.
     *
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean
     *                          #setKeyValueOperations(org.springframework.data.keyvalue.core.KeyValueOperations)
     */
    @Override
    public void setKeyValueOperations(KeyValueOperations operations) {
        this.operations = operations;
        super.setKeyValueOperations(this.operations);
    }

    /* Capture QueryCreator before passing to super.
     *
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean
     *                          #setQueryCreator(java.lang.Class)
     */
    @Override
    public void setQueryCreator(Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {
        this.queryCreator = queryCreator;
        super.setQueryCreator(queryCreator);
    }

    /**
     *<P>Return a {@link HazelcastRepositoryFactory} using the captured arguments.
     *</P>
     *<P>{@code super} would return {@link KeyValueRepositoryFactory} which in
     * turn builds {@link KeyValueRepository} instances, and these have a private
     * method that implement querying in a manner that does not fit with Hazelcast.
     * More details are in {@link HazelcastRepositoryFactory}.
     *</P>
     *
     * @return A {@link HazelcastRepositoryFactory} that creates
     *        {@link HazelcastRepository} instances.
     */
    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new HazelcastRepositoryFactory(this.operations, this.queryCreator);
    }

}
