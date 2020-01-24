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
package org.springframework.data.hazelcast.repository.support;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.query.SpelQueryCreator;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * <p>
 * Hazelcast version of {@link KeyValueRepositoryFactory}, a factory to build {@link HazelcastRepository} instances.
 * </P>
 * <p>
 * The purpose of extending is to ensure that the {@link #getQueryLookupStrategy} method returns a
 * {@link HazelcastQueryLookupStrategy} rather than the default.
 * </P>
 * <p>
 * The end goal of this bean is for {@link HazelcastPartTreeQuery} to be used for query preparation.
 * </P>
 *
 * @author Neil Stevenson
 */
public class HazelcastRepositoryFactory
        extends KeyValueRepositoryFactory {

    private static final Class<SpelQueryCreator> DEFAULT_QUERY_CREATOR = SpelQueryCreator.class;

    private final KeyValueOperations keyValueOperations;
    private final Class<? extends AbstractQueryCreator<?, ?>> queryCreator;
    private final HazelcastInstance hazelcastInstance;

    /* Mirror functionality of super, to ensure private
     * fields are set.
     */
    public HazelcastRepositoryFactory(KeyValueOperations keyValueOperations, HazelcastInstance hazelcastInstance) {
        this(keyValueOperations, DEFAULT_QUERY_CREATOR, hazelcastInstance);
    }

    /* Capture KeyValueOperations and QueryCreator objects after passing to super.
     */
    public HazelcastRepositoryFactory(KeyValueOperations keyValueOperations,
                                      Class<? extends AbstractQueryCreator<?, ?>> queryCreator,
                                      HazelcastInstance hazelcastInstance) {

        super(keyValueOperations, queryCreator);

        this.keyValueOperations = keyValueOperations;
        this.queryCreator = queryCreator;
        this.hazelcastInstance = hazelcastInstance;
    }

    /**
     * <p>
     * Ensure the mechanism for query evaluation is Hazelcast specific, as the original
     * {@link KeyValueRepositoryFactory.KeyValueQueryLookupStrategy} does not function correctly for Hazelcast.
     * </P>
     */
    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key,
                                                            QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(new HazelcastQueryLookupStrategy(key, evaluationContextProvider, keyValueOperations, queryCreator,
                hazelcastInstance));
    }

    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        PersistentEntity<T, ?> entity = (PersistentEntity<T, ?>) keyValueOperations.getMappingContext()
                                                                                   .getPersistentEntity(domainClass);
        Assert.notNull(entity, "Entity must not be 'null'.");
        return new HazelcastEntityInformation<>(entity);
    }
}
