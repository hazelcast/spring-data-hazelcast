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
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.Type;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Iterator;

import static org.springframework.data.repository.query.parser.Part.Type.NOT_CONTAINING;
import static org.springframework.data.repository.query.parser.Part.Type.NOT_LIKE;

/**
 * @author Christoph Strobl
 * @author Neil Stevenson
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
     * @param tree       must not be {@literal null}.
     * @param parameters can be {@literal null}.
     */
    public HazelcastQueryCreator(PartTree tree, ParameterAccessor parameters) {
        super(tree, parameters);

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
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Predicate<?, ?> create(Part part, Iterator<Object> iterator) {
        return this.from(part, (Iterator<Comparable<?>>) (Iterator) iterator);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.parser.AbstractQueryCreator
     *                          #and(org.springframework.data.repository.query.parser.Part, java.lang.Object, java.util.Iterator)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
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
     */
    private Predicate<?, ?> from(Part part, Iterator<Comparable<?>> iterator) {
        String property = part.getProperty().toDotPath();
        Type type = part.getType();
        boolean ignoreCase = ifIgnoreCase(part);

        switch (type) {
            case AFTER:
            case GREATER_THAN:
            case GREATER_THAN_EQUAL:
            case BEFORE:
            case LESS_THAN:
            case LESS_THAN_EQUAL:
            case BETWEEN:
                return fromInequalityVariant(type, property, iterator);
            case IS_NULL:
            case IS_NOT_NULL:
                return fromNullVariant(type, property);
            case IN:
            case NOT_IN:
                return fromCollectionVariant(type, property, iterator);
            case CONTAINING:
            case NOT_CONTAINING:
            case STARTING_WITH:
            case ENDING_WITH:
            case LIKE:
            case NOT_LIKE:
                return fromLikeVariant(type, ignoreCase, property, iterator);
            case TRUE:
            case FALSE:
                return fromBooleanVariant(type, property);
            case SIMPLE_PROPERTY:
            case NEGATING_SIMPLE_PROPERTY:
                return fromEqualityVariant(type, ignoreCase, property, iterator);
            case REGEX:
                return Predicates.regex(property, iterator.next().toString());
            /* case EXISTS:
			 * case NEAR:
			 * case WITHIN:
			 */
            default:
                throw new InvalidDataAccessApiUsageException(String.format("Unsupported type '%s'", type));
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

    private Predicate<?, ?> fromCollectionVariant(Type type, String property, Iterator<Comparable<?>> iterator) {
        switch (type) {
            case IN:
                return Predicates.in(property, collectToArray(type, iterator));
            case NOT_IN:
                return Predicates.not(Predicates.in(property, collectToArray(type, iterator)));
            default:
                throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
        }
    }

    private Predicate<?, ?> fromInequalityVariant(Type type, String property, Iterator<Comparable<?>> iterator) {
        switch (type) {
            case AFTER:
            case GREATER_THAN:
                return Predicates.greaterThan(property, iterator.next());
            case GREATER_THAN_EQUAL:
                return Predicates.greaterEqual(property, iterator.next());
            case BEFORE:
            case LESS_THAN:
                return Predicates.lessThan(property, iterator.next());
            case LESS_THAN_EQUAL:
                return Predicates.lessEqual(property, iterator.next());
            case BETWEEN:
                Comparable<?> first = iterator.next();
                Comparable<?> second = iterator.next();
                return Predicates.between(property, first, second);
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

    private Predicate<?, ?> fromEqualityVariant(Type type, boolean ignoreCase, String property,
                                                Iterator<Comparable<?>> iterator) {
        switch (type) {
            case SIMPLE_PROPERTY:
                if(ignoreCase) {
                    return Predicates.ilike(property, iterator.next().toString());
                } else {
                    return Predicates.equal(property, iterator.next());
                }
            case NEGATING_SIMPLE_PROPERTY:
                if(ignoreCase) {
                    return Predicates.not(Predicates.ilike(property, iterator.next().toString()));
                } else {
                    return Predicates.notEqual(property, iterator.next());
                }
            default:
                throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
        }
    }

    private Predicate<?, ?> fromLikeVariant(Type type, boolean ignoreCase, String property,
                                            Iterator<Comparable<?>> iterator) {
        String likeExpression = iterator.next().toString();
        switch (type) {
            case CONTAINING:
            case NOT_CONTAINING:
                likeExpression = String.join("", "%", likeExpression, "%");
                break;
            case STARTING_WITH:
                likeExpression = String.join("", likeExpression, "%");
                break;
            case ENDING_WITH:
                likeExpression = String.join("", "%", likeExpression);
                break;
            case LIKE:
            case NOT_LIKE:
                break;
            default:
                throw new InvalidDataAccessApiUsageException(String.format("'%s' is not supported for LIKE style query",
                        type));
        }

        Predicate likePredicate = ignoreCase ? Predicates.ilike(property, likeExpression)
                : Predicates.like(property, likeExpression);
        return type.equals(NOT_LIKE) || type.equals(NOT_CONTAINING) ? Predicates.not(likePredicate) : likePredicate;
    }

    private boolean ifIgnoreCase(Part part) {
        switch (part.shouldIgnoreCase()) {
            case ALWAYS:
                Assert.state(canUpperCase(part.getProperty()),
                        String.format("Unable to ignore case of %s types, the property '%s' must reference a String",
                                part.getProperty().getType().getName(), part.getProperty().getSegment()));
                return true;
            case WHEN_POSSIBLE:
                if (canUpperCase(part.getProperty())) {
                    return true;
                }
                return false;
            case NEVER:
            default:
                return false;
        }
    }

    private boolean canUpperCase(PropertyPath path) {
        return String.class.equals(path.getType());
    }

    private boolean isCollection(Object item) {
        return Collection.class.isAssignableFrom(item.getClass());
    }

    private Comparable<?>[] collectToArray(Type type, Iterator<Comparable<?>> iterator) {
        Object item = iterator.next();
        Assert.state(isCollection(item), String.format("%s requires collection of values", type));
        Collection<Comparable<?>> itemcol = (Collection<Comparable<?>>) item;
        return itemcol.toArray(new Comparable<?>[0]);
    }
}
