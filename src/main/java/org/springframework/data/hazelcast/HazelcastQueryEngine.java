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
package org.springframework.data.hazelcast;

import com.hazelcast.map.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.impl.predicates.PagingPredicateImpl;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.hazelcast.repository.query.HazelcastCriteriaAccessor;
import org.springframework.data.hazelcast.repository.query.HazelcastSortAccessor;
import org.springframework.data.keyvalue.core.QueryEngine;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * <p>
 * Implementation of {@code findBy*()} and {@code countBy*{}} queries.
 * </P>
 *
 * @param <K> key type
 * @param <V> value type
 * @author Christoph Strobl
 * @author Neil Stevenson
 * @author Viacheslav Petriaiev
 */
public class HazelcastQueryEngine<K, V>
        extends QueryEngine<HazelcastKeyValueAdapter, Predicate<K, V>, Comparator<Entry<K, V>>> {

    public HazelcastQueryEngine() {
        super(new HazelcastCriteriaAccessor<>(), new HazelcastSortAccessor<>());
    }

    /**
     * <p>
     * Construct the final query predicate for Hazelcast to execute, from the base query plus any paging and sorting.
     * </P>
     * <p>
     * Variations here allow the base query predicate to be omitted, sorting to be omitted, and paging to be omitted.
     * </P>
     *
     * @param criteria Search criteria, null means match everything
     * @param sort     Possibly null collation
     * @param offset   Start point of returned page, -1 if not used
     * @param rows     Size of page, -1 if not used
     * @param keyspace The map name
     * @return Results from Hazelcast
     */
    @Override
    @NonNull
    public Collection<?> execute(final @Nullable Predicate<K, V> criteria,
                                 final @Nullable Comparator<Entry<K, V>> sort,
                                 final long offset,
                                 final int rows,
                                 final @Nullable String keyspace) {
        return execute(criteria, sort, offset, rows, keyspace, false);
    }

    /**
     * <p>
     * Same as {@link #execute(Predicate, Comparator, long, int, String)}, but knows the entity type. A page without
     * a sort order over entities that are not {@link Comparable} is read by key, so the entities do not have to be
     * comparable and the members need no class from this module.
     * </P>
     */
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public <T> Collection<T> execute(final @Nullable Predicate<K, V> criteria,
                                     final @Nullable Comparator<Entry<K, V>> sort,
                                     final long offset,
                                     final int rows,
                                     final @Nullable String keyspace,
                                     final @Nullable Class<T> type) {
        boolean pageByKey = type != null && !Comparable.class.isAssignableFrom(type);
        return (Collection<T>) execute(criteria, sort, offset, rows, keyspace, pageByKey);
    }

    private Collection<?> execute(final @Nullable Predicate<K, V> criteria,
                                  final @Nullable Comparator<Entry<K, V>> sort,
                                  final long offset,
                                  final int rows,
                                  final @Nullable String keyspace,
                                  final boolean pageByKey) {

        final HazelcastKeyValueAdapter adapter = getAdapter();
        Assert.notNull(adapter, "Adapter must not be 'null'.");

        Predicate<K, V> predicateToUse = criteria;

        if (rows > 0) {
            PagingPredicate<K, V> pp = Predicates.pagingPredicate(predicateToUse, sort, rows);
            long x = offset / rows;
            while (x > 0) {
                pp.nextPage();
                x--;
            }
            if (sort == null && pageByKey) {
                // Without a comparator Hazelcast orders a values() page by the values, which then
                // must be Comparable. A keySet() page is ordered by the keys instead.
                return valuesOrderedByKey(adapter.getMap(keyspace), pp);
            }
            predicateToUse = pp;

        } else {
            if (sort != null) {
                predicateToUse = new PagingPredicateImpl<>(predicateToUse, sort, Integer.MAX_VALUE);
            }
        }

        if (predicateToUse == null) {
            return adapter.getMap(keyspace).values();
        } else {
            //noinspection unchecked
            return adapter.getMap(keyspace).values((Predicate<Object, Object>) predicateToUse);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<Object> valuesOrderedByKey(IMap<Object, Object> map, PagingPredicate page) {
        Set<Object> keys = map.keySet(page);
        Map<Object, Object> values = map.getAll(keys);
        List<Object> result = new ArrayList<>(keys.size());
        for (Object key : keys) {
            Object value = values.get(key);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * <p>
     * Execute {@code countBy*()} queries against a Hazelcast map.
     * </P>
     *
     * @param criteria Predicate to use, not null
     * @param keyspace The map name
     * @return Results from Hazelcast
     */
    @Override
    @SuppressWarnings("unchecked")
    public long count(final Predicate<K, V> criteria, final @Nullable String keyspace) {
        final HazelcastKeyValueAdapter adapter = getAdapter();
        Assert.notNull(adapter, "Adapter must not be 'null'.");
        return adapter.getMap(keyspace).keySet((Predicate<Object, Object>) criteria).size();
    }

}
