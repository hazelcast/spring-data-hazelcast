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
package org.springframework.data.hazelcast.repository.query;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.impl.predicates.PagingPredicateImpl;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyPath;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
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
 * @author Viacheslav Petriaiev
 */
public class HazelcastQueryCreator
        extends AbstractQueryCreator<KeyValueQuery<Predicate<?, ?>>, Predicate<?, ?>> {
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
     * @param tree       must not be {@literal null}.
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Predicate<?, ?> create(Part part, Iterator<Object> iterator) {
        return from(part, (Iterator) iterator);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.parser.AbstractQueryCreator
     *                          #and(org.springframework.data.repository.query.parser.Part, java.lang.Object, java.util.Iterator)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Predicate<?, ?> and(Part part, Predicate<?, ?> base, Iterator<Object> iterator) {
        Predicate<?, ?> criteria = from(part, (Iterator) iterator);
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
            keyValueQuery = new KeyValueQuery<>(criteria);
        } else {
            keyValueQuery = new KeyValueQuery<>(new PagingPredicateImpl<>(criteria, this.limit));
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
    private static Predicate<?, ?> from(Part part, Iterator<Comparable<?>> iterator) {
        String property = part.getProperty().toDotPath();
        Type type = part.getType();
        boolean ignoreCase = ifIgnoreCase(part);

        switch (type) {
            case AFTER, GREATER_THAN, GREATER_THAN_EQUAL, BEFORE, LESS_THAN, LESS_THAN_EQUAL, BETWEEN:
                return fromInequalityVariant(type, property, iterator);
            case IS_NULL, IS_NOT_NULL:
                return fromNullVariant(type, property);
            case IN, NOT_IN:
                return fromCollectionVariant(type, property, iterator);
            case CONTAINING, NOT_CONTAINING, STARTING_WITH, ENDING_WITH, LIKE, NOT_LIKE:
                return fromLikeVariant(type, ignoreCase, property, iterator);
            case TRUE, FALSE:
                return fromBooleanVariant(type, property);
            case SIMPLE_PROPERTY, NEGATING_SIMPLE_PROPERTY:
                return fromEqualityVariant(type, ignoreCase, property, iterator);
            case REGEX:
                return Predicates.regex(property, iterator.next().toString());
            case IS_EMPTY, IS_NOT_EMPTY:
                return fromEmptyVariant(type, property);
            /* case EXISTS:*/
            case NEAR, WITHIN:
                return fromGeoVariant(type, property, iterator);

            default:
                throw new InvalidDataAccessApiUsageException(String.format("Unsupported type '%s'", type));
        }
    }

    private static Predicate<?, ?> fromBooleanVariant(Type type, String property) {
        switch (type) {
            case TRUE:
                return Predicates.equal(property, true);
            case FALSE:
                return Predicates.equal(property, false);
            default:
                throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
        }
    }

    private static Predicate<?, ?> fromCollectionVariant(Type type, String property, Iterator<Comparable<?>> iterator) {
        switch (type) {
            case IN:
                return Predicates.in(property, collectToArray(type, iterator));
            case NOT_IN:
                return Predicates.not(Predicates.in(property, collectToArray(type, iterator)));
            default:
                throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
        }
    }

    private static Predicate<?, ?> fromInequalityVariant(Type type, String property, Iterator<Comparable<?>> iterator) {
        switch (type) {
            case AFTER, GREATER_THAN:
                return Predicates.greaterThan(property, iterator.next());
            case GREATER_THAN_EQUAL:
                return Predicates.greaterEqual(property, iterator.next());
            case BEFORE, LESS_THAN:
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

    private static Predicate<?, ?> fromNullVariant(Type type, String property) {
        switch (type) {
            case IS_NULL:
                return Predicates.equal(property, null);
            case IS_NOT_NULL:
                return Predicates.notEqual(property, null);

            default:
                throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
        }
    }

    private static Predicate<?, ?> fromEqualityVariant(Type type, boolean ignoreCase, String property,
                                                Iterator<Comparable<?>> iterator) {
        switch (type) {
            case SIMPLE_PROPERTY:
                if (ignoreCase) {
                    return Predicates.ilike(property, iterator.next().toString());
                } else {
                    return Predicates.equal(property, iterator.next());
                }
            case NEGATING_SIMPLE_PROPERTY:
                if (ignoreCase) {
                    return Predicates.not(Predicates.ilike(property, iterator.next().toString()));
                } else {
                    return Predicates.notEqual(property, iterator.next());
                }
            default:
                throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
        }
    }

    private static Predicate<?, ?> fromLikeVariant(Type type, boolean ignoreCase, String property, Iterator<Comparable<?>> iterator) {
        String likeExpression = iterator.next().toString();
        switch (type) {
            case CONTAINING, NOT_CONTAINING:
                likeExpression = String.join("", "%", likeExpression, "%");
                break;
            case STARTING_WITH:
                likeExpression = String.join("", likeExpression, "%");
                break;
            case ENDING_WITH:
                likeExpression = String.join("", "%", likeExpression);
                break;
            case LIKE, NOT_LIKE:
                break;
            default:
                throw new InvalidDataAccessApiUsageException(String.format("'%s' is not supported for LIKE style query", type));
        }

        Predicate<?, ?> likePredicate = ignoreCase ? Predicates.ilike(property, likeExpression) : Predicates
                .like(property, likeExpression);
        return type.equals(NOT_LIKE) || type.equals(NOT_CONTAINING) ? Predicates.not(likePredicate) : likePredicate;
    }

    private static boolean ifIgnoreCase(Part part) {
        switch (part.shouldIgnoreCase()) {
            case ALWAYS:
                Assert.state(canUpperCase(part.getProperty()),
                        String.format("Unable to ignore case of %s types, the property '%s' must reference a String",
                                part.getProperty().getType().getName(), part.getProperty().getSegment()));
                return true;
            case WHEN_POSSIBLE:
                return canUpperCase(part.getProperty());
            case NEVER:
            default:
                return false;
        }
    }

    private static boolean canUpperCase(PropertyPath path) {
        return String.class.equals(path.getType());
    }

    private static boolean isCollection(Object item) {
        return Collection.class.isAssignableFrom(item.getClass());
    }

    private static Comparable<?>[] collectToArray(Type type, Iterator<Comparable<?>> iterator) {
        Object item = iterator.next();
        Assert.state(isCollection(item), String.format("%s requires collection of values", type));
        Collection<Comparable<?>> itemcol = (Collection<Comparable<?>>) item;
        return itemcol.toArray(new Comparable<?>[0]);
    }

    private static Predicate<?, ?> fromEmptyVariant(Type type, String property) {
        switch (type) {
            case IS_EMPTY:
                return Predicates.equal(property, "");
            case IS_NOT_EMPTY:
                return Predicates.notEqual(property, "");

            default:
                throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
        }
    }

    private static Predicate<?, ?> fromGeoVariant(Type type, String property, Iterator<Comparable<?>> iterator) {
        final Object item = iterator.next();
        Point point;
        Distance distance;
        if (item instanceof Point p) {
            point = p;
            if (!iterator.hasNext()) {
                throw new InvalidDataAccessApiUsageException(
                        "Expected to find distance value for geo query. Are you missing a parameter?");
            }

            Object distObject = iterator.next();
            if (distObject instanceof Distance d) {
                distance = d;
            } else if (distObject instanceof Number n) {
                distance = new Distance(n.doubleValue(), Metrics.KILOMETERS);
            } else {
                throw new InvalidDataAccessApiUsageException(String
                        .format("Expected to find Distance or Numeric value for geo query but was %s.",
                                distObject.getClass()));
            }
        } else if (item instanceof Circle c) {
            point = c.getCenter();
            distance = c.getRadius();
        } else {
            throw new InvalidDataAccessApiUsageException(
                    String.format("Expected to find a Circle or Point/Distance for geo query but was %s.", item.getClass()));
        }

        switch (type) {
            case WITHIN, NEAR:
                return new GeoPredicate<>(property, point, distance);

            default:
                throw new InvalidDataAccessApiUsageException(String.format("Logic error for '%s' in query", type));
        }
    }
}
