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
package org.springframework.data.hazelcast.repository.query;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.keyvalue.core.SortAccessor;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.util.comparator.CompoundComparator;

import java.util.Comparator;
import java.util.Map.Entry;

/**
 * <p>
 * Implements sorting for Hazelcast repository queries.
 * </P>
 * <p>
 * Although {@code SpelPropertyComparator} would do most of the work, this is not serializable so cannot work in a
 * cluster. Also, do not wish to assume anything other than Hazelcast classes are available on remote nodes.
 * </P>
 *
 * @author Neil Stevenson
 */
public class HazelcastSortAccessor
        implements SortAccessor<Comparator<Entry<?, ?>>> {

    /**
     * <p>
     * Sort on a sequence of fields, possibly none.
     * </P>
     *
     * @param query If not null, will contain one of more {@link Sort.Order} objects.
     * @return A sequence of comparators or {@code null}
     */
    public Comparator<Entry<?, ?>> resolve(KeyValueQuery<?> query) {

        if (query == null || query.getSort() == Sort.unsorted()) {
            return null;
        }

        CompoundComparator<Entry<?, ?>> compoundComparator = new CompoundComparator<>();

        for (Order order : query.getSort()) {

            if (order.getProperty().indexOf('.') > -1) {
                throw new UnsupportedOperationException("Embedded fields not implemented: " + order);
            }

            if (order.isIgnoreCase()) {
                throw new UnsupportedOperationException("Ignore case not implemented: " + order);
            }

            if (NullHandling.NATIVE != order.getNullHandling()) {
                throw new UnsupportedOperationException("Null handling not implemented: " + order);
            }

            HazelcastPropertyComparator hazelcastPropertyComparator = new HazelcastPropertyComparator(order.getProperty(),
                    order.isAscending());

            compoundComparator.addComparator(hazelcastPropertyComparator);
        }

        return compoundComparator;
    }

}
