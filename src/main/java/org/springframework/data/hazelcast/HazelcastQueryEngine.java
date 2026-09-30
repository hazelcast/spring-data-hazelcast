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

import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.impl.predicates.PagingPredicateImpl;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.hazelcast.repository.DefaultOrderComparator;
import org.springframework.data.hazelcast.repository.query.HazelcastCriteriaAccessor;
import org.springframework.data.hazelcast.repository.query.HazelcastSortAccessor;
import org.springframework.data.keyvalue.core.QueryEngine;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map.Entry;

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
        extends QueryEngine<HazelcastKeyValueAdapter<K, V>, Predicate<K, V>, Comparator<Entry<K, V>>> {


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
                                 final int rows, final @Nullable String keyspace) {

        final HazelcastKeyValueAdapter<K, V> adapter = getAdapter();
        Assert.notNull(adapter, "Adapter must not be 'null'.");

        Predicate<K, V> predicateToUse = criteria;

        Comparator<Entry<K, V>> sortToUse = sort;
        if (rows > 0) {
            if (sortToUse == null) {
                sortToUse = new DefaultOrderComparator<>();
            }
            PagingPredicate<K, V> pp = Predicates.pagingPredicate(predicateToUse, sortToUse, rows);
            long x = offset / rows;
            while (x > 0) {
                pp.nextPage();
                x--;
            }
            predicateToUse = pp;

        } else {
            if (sortToUse != null) {
                predicateToUse = new PagingPredicateImpl<>(predicateToUse, sortToUse, Integer.MAX_VALUE);
            }
        }

        if (predicateToUse == null) {
            return adapter.getMap(keyspace).values();
        } else {
            return adapter.getMap(keyspace).values((Predicate<Object, Object>) predicateToUse);
        }
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
        final HazelcastKeyValueAdapter<K, V> adapter = getAdapter();
        Assert.notNull(adapter, "Adapter must not be 'null'.");
        return adapter.getMap(keyspace).keySet((Predicate<Object, Object>) criteria).size();
    }

}
