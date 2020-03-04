package org.springframework.data.hazelcast.repository.query;

import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.Map;

public class HazelcastSortAccessorTest {

    @Test
    public void returnsNonNullComparator() {
        KeyValueQuery<?> query = new KeyValueQuery<>();
        Sort sort = new Sort(Sort.Direction.ASC, "property1", "property2");
        query.setSort(sort);
        HazelcastSortAccessor hazelcastSortAccessor = new HazelcastSortAccessor();
        Comparator<Map.Entry<?, ?>> comparator = hazelcastSortAccessor.resolve(query);
        Assert.notNull(comparator, "Non null Comparator should be return");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEmptyProperties() {
        KeyValueQuery<?> query = new KeyValueQuery<>();
        Sort sort = new Sort(Sort.Direction.ASC, ".");
        query.setSort(sort);
        HazelcastSortAccessor hazelcastSortAccessor = new HazelcastSortAccessor();
        hazelcastSortAccessor.resolve(query);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNonNativeNullHandling() {
        KeyValueQuery<?> query = new KeyValueQuery<>();
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "property", Sort.NullHandling.NULLS_LAST);
        Sort sort = Sort.by(order);
        query.setSort(sort);
        HazelcastSortAccessor hazelcastSortAccessor = new HazelcastSortAccessor();
        hazelcastSortAccessor.resolve(query);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIgnoreOrderCase() {
        KeyValueQuery<?> query = new KeyValueQuery<>();
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "property", Sort.NullHandling.NULLS_LAST);
        order = order.ignoreCase();
        Sort sort = Sort.by(order);
        query.setSort(sort);
        HazelcastSortAccessor hazelcastSortAccessor = new HazelcastSortAccessor();
        hazelcastSortAccessor.resolve(query);
    }

    @Test
    public void testEmptyQueryAndUnsortedSort() {
        KeyValueQuery<?> query = null;
        HazelcastSortAccessor hazelcastSortAccessor = new HazelcastSortAccessor();
        Comparator<Map.Entry<?, ?>> comparator = hazelcastSortAccessor.resolve(query);
        Assert.isNull(comparator, "should be null in case of null query object");

        query = new KeyValueQuery<>();
        query.setSort(Sort.unsorted());
        hazelcastSortAccessor.resolve(query);
        Assert.isNull(comparator, "should be null in case of unsorted object");
    }
}
