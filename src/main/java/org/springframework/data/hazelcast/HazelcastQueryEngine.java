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
package org.springframework.data.hazelcast;

import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.predicates.PagingPredicateImpl;
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
 * @author Christoph Strobl
 * @author Neil Stevenson
 * @author Viacheslav Petriaiev
 */
public class HazelcastQueryEngine
        extends QueryEngine<HazelcastKeyValueAdapter, Predicate<?, ?>, Comparator<Entry<?, ?>>> {

    public HazelcastQueryEngine() {
        super(new HazelcastCriteriaAccessor(), new HazelcastSortAccessor());
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
    public Collection<?> execute(final Predicate<?, ?> criteria, final Comparator<Entry<?, ?>> sort, final long offset,
                                 final int rows, final String keyspace) {

        final HazelcastKeyValueAdapter adapter = getAdapter();
        Assert.notNull(adapter, "Adapter must not be 'null'.");

        Predicate<?, ?> predicateToUse = criteria;
        @SuppressWarnings({"unchecked", "rawtypes"}) Comparator<Entry> sortToUse = ((Comparator<Entry>) (Comparator) sort);

        if (rows > 0) {
            PagingPredicate pp = new PagingPredicateImpl(predicateToUse, sortToUse, rows);
            long x = offset / rows;
            while (x > 0) {
                pp.nextPage();
                x--;
            }
            predicateToUse = pp;

        } else {
            if (sortToUse != null) {
                predicateToUse = new PagingPredicateImpl(predicateToUse, sortToUse, Integer.MAX_VALUE);
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
    public long count(final Predicate<?, ?> criteria, final String keyspace) {
        final HazelcastKeyValueAdapter adapter = getAdapter();
        Assert.notNull(adapter, "Adapter must not be 'null'.");
        return adapter.getMap(keyspace).keySet((Predicate<Object, Object>) criteria).size();
    }

}
