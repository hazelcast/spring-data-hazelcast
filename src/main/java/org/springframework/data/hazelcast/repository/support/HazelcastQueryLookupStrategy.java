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

import java.lang.reflect.Method;
import org.springframework.data.hazelcast.repository.query.HazelcastPartTreeQuery;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.util.Assert;

/**
 * <P>
 * Ensures {@link HazelcastPartTreeQuery} is used for query preparation rather than {@link KeyValuePartTreeQuery} or
 * other alternatives.
 * </P>
 *
 * @author Neil Stevenson
 */
public class HazelcastQueryLookupStrategy implements QueryLookupStrategy {

	private EvaluationContextProvider evaluationContextProvider;
	private KeyValueOperations keyValueOperations;
	private Class<? extends AbstractQueryCreator<?, ?>> queryCreator;

	/**
	 * <P>
	 * Required constructor, capturing arguments for use in {@link #resolveQuery}.
	 * </P>
	 * <P>
	 * Assertions copied from {@link KayValueRepositoryFactory.KeyValueQUeryLookupStrategy} which this class essentially
	 * duplicates.
	 * </P>
	 *
	 * @param key Not used
	 * @param evaluationContextProvider For evaluation of query expressions
	 * @param keyValueOperations Bean to use for Key/Value operations on Hazelcast repos
	 * @param queryCreator Likely to be {@link HazelcastQueryCreator}
	 */
	public HazelcastQueryLookupStrategy(QueryLookupStrategy.Key key, EvaluationContextProvider evaluationContextProvider,
			KeyValueOperations keyValueOperations, Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {

		Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");
		Assert.notNull(keyValueOperations, "KeyValueOperations must not be null!");
		Assert.notNull(queryCreator, "Query creator type must not be null!");

		this.evaluationContextProvider = evaluationContextProvider;
		this.keyValueOperations = keyValueOperations;
		this.queryCreator = queryCreator;
	}

	/**
	 * <P>
	 * Use {@link HazelcastPartTreeQuery} for resolving queries against Hazelcast repositories.
	 * </P>
	 *
	 * @param Method, the query method
	 * @param RepositoryMetadata, not used
	 * @param ProjectionFactory, not used
	 * @param NamedQueries, not used
	 * @return A mechanism for querying Hazelcast repositories
	 */
	public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, 
			ProjectionFactory projectionFactory, NamedQueries namedQueries) {

		HazelcastQueryMethod queryMethod = new HazelcastQueryMethod(method, metadata, projectionFactory);

		if (queryMethod.hasAnnotatedQuery()) {
			return new StringBasedHazelcastRepositoryQuery(queryMethod);

		}

		return new HazelcastPartTreeQuery(queryMethod, evaluationContextProvider, this.keyValueOperations,
				this.queryCreator);
	}
	
}
