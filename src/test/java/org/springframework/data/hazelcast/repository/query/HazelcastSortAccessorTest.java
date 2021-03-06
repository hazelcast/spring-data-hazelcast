package org.springframework.data.hazelcast.repository.query;

import com.hazelcast.query.Predicate;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.history.RevisionSort;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.*;

import static org.junit.Assert.*;

public class HazelcastSortAccessorTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testResolvingNonNativeNullHandlingOrderIsUnsupported() {
        // given
        KeyValueQuery<String> keyValueQuery = new KeyValueQuery<>("^pg~]=P");
        Sort.Order sortOrder1 = Sort.Order.asc("^pg~]=P");
        Sort.Order sortOrder2 = sortOrder1.nullsLast();
        keyValueQuery.setSort(Sort.by(sortOrder1, sortOrder2));

        HazelcastSortAccessor hazelcastSortAccessor = new HazelcastSortAccessor();
        // when
        hazelcastSortAccessor.resolve(keyValueQuery);
        // Then expect UnsupportedOperationException
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testResolvingIgnoreCaseOrderIsUnsupported() {
        // given
        HazelcastSortAccessor hazelcastSortAccessor = new HazelcastSortAccessor();
        KeyValueQuery<Object> keyValueQuery = new KeyValueQuery<>(RevisionSort.asc());
        Sort.Order sortOrderA = Sort.Order.asc("Cannot resolve nest mates of a latent type description: ");
        Sort.Order sortOrderB = sortOrderA.ignoreCase();
        keyValueQuery.setSort(Sort.by(sortOrderA, sortOrderB));

        // when
        hazelcastSortAccessor.resolve(keyValueQuery);
        // Then expect UnsupportedOperationException
    }

    @Test
    public void testResolvingNonEmptyKeyValueQueryReturnsNonNullComparator() {
        //given
        KeyValueQuery<Object> keyValueQuery = new KeyValueQuery<>(RevisionSort.asc());

        //when
        HazelcastSortAccessor hazelcastSortAccessor = new HazelcastSortAccessor();
        Comparator<Map.Entry<?, ?>> comparator = hazelcastSortAccessor.resolve(keyValueQuery);

        //then
        assertNotNull(comparator);
    }

    @Test
    public void testResolvingEmptyKeyValueQueryReturnsNullComparator() {
        //given
        KeyValueQuery<Object> keyValueQuery = new KeyValueQuery<>();

        //when
        HazelcastSortAccessor hazelcastSortAccessor = new HazelcastSortAccessor();
        Comparator<Map.Entry<?, ?>> comparator = hazelcastSortAccessor.resolve(keyValueQuery);

        //then
        assertNull(comparator);
    }

    @Test
    public void testResolvingNullKeyValueQueryReturnsNullComparator() {
        //given //when
        HazelcastSortAccessor hazelcastSortAccessor = new HazelcastSortAccessor();
        Comparator<Map.Entry<?, ?>> comparator = hazelcastSortAccessor.resolve(null);
        //then
        assertNull(comparator);
    }

    @Test
    public void testResolvingMultipleSortOrdersReturnsCompositeComparator() {
        //given
        KeyValueQuery<Predicate<String, Foo>> query = new KeyValueQuery<>();
        Sort.Order fooOrder = new Sort.Order(Sort.Direction.DESC, "foo");
        Sort.Order barOrder = new Sort.Order(Sort.Direction.DESC, "bar");

        query.setSort(Sort.by(fooOrder, barOrder));

        HazelcastSortAccessorTest.Foo testData1 = new HazelcastSortAccessorTest.Foo("zzz", "def");
        HazelcastSortAccessorTest.Foo testData2 = new HazelcastSortAccessorTest.Foo("aaa", "aaa");
        Map.Entry<String, HazelcastSortAccessorTest.Foo> entry1 = entryOf("testData1", testData1);
        Map.Entry<String, HazelcastSortAccessorTest.Foo> entry2 = entryOf("testData2", testData2);

        HazelcastSortAccessorTest.Foo testData3 = new HazelcastSortAccessorTest.Foo("aaa", "aaa");
        HazelcastSortAccessorTest.Foo testData4 = new HazelcastSortAccessorTest.Foo("aaa", "def");
        Map.Entry<String, HazelcastSortAccessorTest.Foo> entry3 = entryOf("testData3", testData3);
        Map.Entry<String, HazelcastSortAccessorTest.Foo> entry4 = entryOf("testData4", testData4);

        //when
        HazelcastSortAccessor hazelcastSortAccessor = new HazelcastSortAccessor();
        Comparator<Map.Entry<?, ?>> comparator = hazelcastSortAccessor.resolve(query);

        //then
        assertNotNull(comparator);
        assertTrue(comparator.compare(entry1, entry2) < 0);
        assertTrue(comparator.compare(entry2, entry1) > 0);
        assertFalse(comparator.compare(entry3, entry4) == 0);
        assertTrue((comparator.compare(entry3, entry4) > 0));
    }

    private static <K, V> Map.Entry<K, V> entryOf(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    static class Foo
            implements Serializable {

        String foo;
        String bar;

        public Foo(String foo, String bar) {
            this.foo = foo;
            this.bar = bar;
        }

        @Override
        public int hashCode() {
            return ObjectUtils.nullSafeHashCode(new Object[]{this.foo, this.bar});
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof HazelcastSortAccessorTest.Foo)) {
                return false;
            }
            HazelcastSortAccessorTest.Foo other = (HazelcastSortAccessorTest.Foo) obj;
            return ObjectUtils.nullSafeEquals(this.foo, other.foo) && ObjectUtils.nullSafeEquals(this.bar, other.bar);
        }

    }

}
