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
package org.springframework.data.hazelcast.repository.query;

import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.IgnoreCaseType;
import org.springframework.data.repository.query.parser.Part.Type;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Iterator;

/**
 * @author Christoph Strobl
 * @author Neil Stevenson
 * @author Viacheslav Petriaiev
 */
public class HazelcastQueryCreator extends AbstractQueryCreator<KeyValueQuery<Predicate<?, ?>>, Predicate<?, ?>> {
	private final int limit;

	/**
	 * Creates a new {@link HazelcastQueryCreator} for the given {@link PartTree}.
	 *
	 * @param tree must not be {@literal null}.
	 */
	public HazelcastQueryCreator(PartTree tree) {
		super(tree);

        final Integer maxResults = tree.getMaxResults();
		if (tree.isLimiting() && maxResults != null && maxResults > 0) {
			this.limit = maxResults;
		} else {
			this.limit = 0;
		}
	}

	/**
	 * Creates a new {@link HazelcastQueryCreator} for the given {@link PartTree} and {@link ParameterAccessor}. The
	 * latter is used to hand actual parameter values into the callback methods as well as to apply dynamic sorting via a
	 * {@link Sort} parameter.
	 *
	 * @param tree must not be {@literal null}.
	 * @param parameters can be {@literal null}.
	 */
	public HazelcastQueryCreator(PartTree tree, ParameterAccessor parameters) {
		super(tree, parameters);

		final Integer maxResults = tree.getMaxResults();
		if (tree.isLimiting() && maxResults != null && maxResults > 0) {
			this.limit = maxResults;
		} else {
			this.limit = 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.parser.AbstractQueryCreator
	 *                          #create(org.springframework.data.repository.query.parser.Part, java.util.Iterator)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected Predicate<?, ?> create(Part part, Iterator<Object> iterator) {
		return this.from(part, (Iterator<Comparable<?>>) (Iterator) iterator);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.parser.AbstractQueryCreator
	 *                          #and(org.springframework.data.repository.query.parser.Part, java.lang.Object, java.util.Iterator)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected Predicate<?, ?> and(Part part, Predicate<?, ?> base, Iterator<Object> iterator) {
		Predicate<?, ?> criteria = this.from(part, (Iterator<Comparable<?>>) (Iterator) iterator);
		return Predicates.and(base, criteria);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.parser.AbstractQueryCreator
	 *                                                       #or(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected Predicate<?, ?> or(Predicate<?, ?> base, Predicate<?, ?> criteria) {
		return Predicates.or(base, criteria);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.parser.AbstractQueryCreator
	 *                                                       #complete(java.lang.Object, org.springframework.data.domain.Sort)
	 */
	@Override
	protected KeyValueQuery<Predicate<?, ?>> complete(Predicate<?, ?> criteria, Sort sort) {

		KeyValueQuery<Predicate<?, ?>> keyValueQuery;

		if (this.limit == 0) {
			keyValueQuery = new KeyValueQuery<Predicate<?, ?>>(criteria);
		} else {
			keyValueQuery = new KeyValueQuery<Predicate<?, ?>>(new PagingPredicate(criteria, this.limit));
		}

		if (sort != null) {
			keyValueQuery.setSort(sort);
		}
		return keyValueQuery;
	}

	/* Map query types to Hazelcast predicates. Use multiple methods to separate into
	 * logical groups, easing testing and for possible recursion.
	 *
	 * Use Predicate in favour over PredicateBuilder as the latter cannot support
	 * the former being embedded in the chain.
	 *
	 * TODO Not all types are currently implemented. Some will be easier than others.
	 */
	private Predicate<?, ?> from(Part part, Iterator<Comparable<?>> iterator) {

		String property = part.getProperty().toDotPath();
		Type type = part.getType();
		boolean ignoreCase = (part.shouldIgnoreCase() != IgnoreCaseType.NEVER);

		switch (type) {

			case FALSE:
			case TRUE:
				return fromBooleanVariant(type, property);

			case SIMPLE_PROPERTY:
				return fromEqualityVariant(type, ignoreCase, property, iterator);

			case GREATER_THAN:
			case GREATER_THAN_EQUAL:
			case LESS_THAN:
			case LESS_THAN_EQUAL:
				return fromInequalityVariant(type, ignoreCase, property, iterator);

			case LIKE:
				return fromLikeVariant(type, property, iterator);

			case IS_NOT_NULL:
			case IS_NULL:
				return fromNullVariant(type, property);

			/* case AFTER:
			 * case BEFORE:
			 * case BETWEEN:
			 * case CONTAINING:
			 * case ENDING_WITH:
			 * case EXISTS:
			 * case IN:
			 * case NEAR:
			 * case NEGATING_SIMPLE_PROPERTY:
			 * case NOT_CONTAINING:
			 * case NOT_IN:
			 * case NOT_LIKE:
			 * case REGEX:
			 * case STARTING_WITH:
			 * case WITHIN:
			 */
			default:
				throw new InvalidDataAccessApiUsageException(String.format("Found invalid part '%s' in query", type));
		}

	}

	private Predicate<?, ?> fromBooleanVariant(Type type, String property) {

		switch (type) {

			case TRUE:
				return Predicates.equal(property, true);
			case FALSE:
				return Predicates.equal(property, false);

			default:
				throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
		}
	}

	private Predicate<?, ?> fromInequalityVariant(Type type, boolean ignoreCase, String property,
			Iterator<Comparable<?>> iterator) {

		if (ignoreCase && type != Type.SIMPLE_PROPERTY) {
			throw new InvalidDataAccessApiUsageException(String.format("Ignore case not supported for '%s'", type));
		}

		switch (type) {

			case GREATER_THAN:
				return Predicates.greaterThan(property, iterator.next());
			case GREATER_THAN_EQUAL:
				return Predicates.greaterEqual(property, iterator.next());
			case LESS_THAN:
				return Predicates.lessThan(property, iterator.next());
			case LESS_THAN_EQUAL:
				return Predicates.lessEqual(property, iterator.next());

			default:
				throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
		}
	}

	private Predicate<?, ?> fromEqualityVariant(Type type, boolean ignoreCase, String property,
			Iterator<Comparable<?>> iterator) {

		switch (type) {

			case SIMPLE_PROPERTY:
				if (ignoreCase) {
					return Predicates.ilike(property, iterator.next().toString());
				} else {
					return Predicates.equal(property, iterator.next());
				}

			default:
				throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
		}
	}

	private Predicate<?, ?> fromLikeVariant(Type type, String property, Iterator<Comparable<?>> iterator) {

		switch (type) {

			case LIKE:
				return Predicates.like(property, iterator.next().toString());

			default:
				throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
		}
	}

	private Predicate<?, ?> fromNullVariant(Type type, String property) {

		switch (type) {

			case IS_NULL:
				return Predicates.equal(property, null);
			case IS_NOT_NULL:
				return Predicates.notEqual(property, null);

			default:
				throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
		}
	}

}
