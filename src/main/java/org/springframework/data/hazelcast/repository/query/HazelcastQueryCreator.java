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
package org.springframework.data.hazelcast.repository.query;

import java.util.Iterator;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.IgnoreCaseType;
import org.springframework.data.repository.query.parser.Part.Type;
import org.springframework.data.repository.query.parser.PartTree;

import com.hazelcast.query.EntryObject;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.Predicates;

/**
 * @author Christoph Strobl
 */
public class HazelcastQueryCreator extends AbstractQueryCreator<KeyValueQuery<Predicate<?, ?>>, Predicate<?, ?>> {
    private final PredicateBuilder predicateBuilder;
    private final int limit;

    /**
     * Creates a new {@link HazelcastQueryCreator} for the given {@link PartTree}.
     *
     * @param tree must not be {@literal null}.
     */
    public HazelcastQueryCreator(PartTree tree) {
        super(tree);

        this.predicateBuilder = new PredicateBuilder();
        if (tree.isLimiting() && tree.getMaxResults() > 0) {
            this.limit = tree.getMaxResults();
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

        this.predicateBuilder = new PredicateBuilder();
        if (tree.isLimiting() && tree.getMaxResults() > 0) {
            this.limit = tree.getMaxResults();
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
        return this.from(predicateBuilder, part, (Iterator<Comparable<?>>) (Iterator) iterator);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.parser.AbstractQueryCreator
     *                          #and(org.springframework.data.repository.query.parser.Part, java.lang.Object, java.util.Iterator)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Predicate<?, ?> and(Part part, Predicate<?, ?> base, Iterator<Object> iterator) {
        return this.predicateBuilder.and(
                this.from(this.predicateBuilder, part, (Iterator<Comparable<?>>) (Iterator) iterator));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.parser.AbstractQueryCreator
     *                                                       #or(java.lang.Object, java.lang.Object)
     */
    @Override
    protected Predicate<?, ?> or(Predicate<?, ?> base, Predicate<?, ?> criteria) {
        return predicateBuilder.or(criteria);
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
     * TODO Not all types are currently implemented. Some will be easier than others.
     */
    private Predicate<?, ?> from(PredicateBuilder pb, Part part, Iterator<Comparable<?>> iterator) {

        String property = part.getProperty().toDotPath();
        Type type = part.getType();
        boolean ignoreCase = (part.shouldIgnoreCase() != IgnoreCaseType.NEVER);

        EntryObject entryObject = pb.getEntryObject();
        entryObject.get(property);

        switch (type) {

        case FALSE:
        case TRUE:
            return fromBooleanVariant(type, entryObject);

        case SIMPLE_PROPERTY:
            return fromEqualityVariant(type, ignoreCase, property, entryObject, iterator);

        case GREATER_THAN:
        case GREATER_THAN_EQUAL:
        case LESS_THAN:
        case LESS_THAN_EQUAL:
            return fromInequalityVariant(type, ignoreCase, entryObject, iterator);

        case LIKE:
            return fromLikeVariant(type, property, iterator);

        case IS_NOT_NULL:
        case IS_NULL:
            return fromNullVariant(type, entryObject);

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
            throw new InvalidDataAccessApiUsageException(
                String.format("Found invalid part '%s' in query", type));
        }

    }

    private Predicate<?, ?> fromBooleanVariant(Type type, EntryObject entryObject) {

        switch (type) {

        case TRUE:
            return entryObject.equal(true);
        case FALSE:
            return entryObject.equal(false);

        default:
            throw new InvalidDataAccessApiUsageException(
                String.format("Logic error for '%s' in query", type));
        }
    }

    private Predicate<?, ?> fromInequalityVariant(Type type, boolean ignoreCase,
            EntryObject entryObject, Iterator<Comparable<?>> iterator) {

        if (ignoreCase && type != Type.SIMPLE_PROPERTY) {
            throw new InvalidDataAccessApiUsageException(
                    String.format("Ignore case not supported for '%s'", type));
        }

        switch (type) {

        case GREATER_THAN:
            return entryObject.greaterThan(iterator.next());
        case GREATER_THAN_EQUAL:
            return entryObject.greaterEqual(iterator.next());
        case LESS_THAN:
            return entryObject.lessThan(iterator.next());
        case LESS_THAN_EQUAL:
            return entryObject.lessEqual(iterator.next());

        default:
            throw new InvalidDataAccessApiUsageException(
                String.format("Logic error for '%s' in query", type));
        }
    }

    private Predicate<?, ?> fromEqualityVariant(Type type, boolean ignoreCase,
            String property, EntryObject entryObject, Iterator<Comparable<?>> iterator) {

        switch (type) {

        case SIMPLE_PROPERTY:
            if (ignoreCase) {
                return Predicates.ilike(property, iterator.next().toString());
            } else {
                return entryObject.equal(iterator.next());
            }

        default:
            throw new InvalidDataAccessApiUsageException(
                String.format("Logic error for '%s' in query", type));
        }
    }

    private Predicate<?, ?> fromLikeVariant(Type type, String property, Iterator<Comparable<?>> iterator) {

        switch (type) {

        case LIKE:
            return Predicates.like(property, iterator.next().toString());

        default:
            throw new InvalidDataAccessApiUsageException(
                String.format("Logic error for '%s' in query", type));
        }
    }

    private Predicate<?, ?> fromNullVariant(Type type, EntryObject entryObject) {

        switch (type) {

        case IS_NULL:
            return entryObject.isNull();
        case IS_NOT_NULL:
            return entryObject.isNotNull();

        default:
            throw new InvalidDataAccessApiUsageException(
                String.format("Logic error for '%s' in query", type));
        }
    }

}
