package org.springframework.data.hazelcast.repository.query;

import com.hazelcast.query.Predicate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.domain.Sort;
import org.springframework.data.history.RevisionSort;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.Assert.*;

public class HazelcastSortAccessorTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test()
    public void test1()  throws Throwable  {
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Null handling not implemented:");
        KeyValueQuery<String> keyValueQuery0 = new KeyValueQuery<String>("^pg~]=P");
        Sort.Order[] sort_OrderArray0 = new Sort.Order[7];
        Sort.Order sort_Order0 = Sort.Order.asc("^pg~]=P");
        sort_OrderArray0[0] = sort_Order0;
        Sort.Order sort_Order1 = sort_Order0.nullsLast();
        sort_OrderArray0[1] = sort_Order1;
        Sort sort0 = Sort.by(sort_OrderArray0);
        keyValueQuery0.setSort(sort0);
        HazelcastSortAccessor hazelcastSortAccessor0 = new HazelcastSortAccessor();
        hazelcastSortAccessor0.resolve(keyValueQuery0);
    }

    @Test
    public void test2()  throws Throwable  {
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Ignore case not implemented");
        HazelcastSortAccessor hazelcastSortAccessor0 = new HazelcastSortAccessor();
        RevisionSort revisionSort0 = RevisionSort.asc();
        KeyValueQuery<Object> keyValueQuery0 = new KeyValueQuery<Object>((Sort) revisionSort0);
        Sort.Order[] sort_OrderArray0 = new Sort.Order[4];
        Sort.Order sort_Order0 = Sort.Order.asc("Cannot resolve nest mates of a latent type description: ");
        sort_OrderArray0[0] = sort_Order0;
        sort_OrderArray0[1] = sort_Order0;
        Sort.Order sort_Order1 = sort_Order0.ignoreCase();
        sort_OrderArray0[2] = sort_Order1;
        Sort sort0 = Sort.by(sort_OrderArray0);
        keyValueQuery0.setSort(sort0);
        hazelcastSortAccessor0.resolve(keyValueQuery0);
    }

    @Test
    public void test3()  throws Throwable  {
        HazelcastSortAccessor hazelcastSortAccessor0 = new HazelcastSortAccessor();
        RevisionSort revisionSort0 = RevisionSort.asc();
        KeyValueQuery<Object> keyValueQuery0 = new KeyValueQuery<Object>((Sort) revisionSort0);
        Comparator<Map.Entry<?, ?>> comparator0 = (Comparator<Map.Entry<?, ?>>)hazelcastSortAccessor0.resolve(keyValueQuery0);
        assertNotNull(comparator0);
    }

    @Test
    public void test4()  throws Throwable  {
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("Embedded fields not implemented");
        HazelcastSortAccessor hazelcastSortAccessor0 = new HazelcastSortAccessor();
        KeyValueQuery<Method> keyValueQuery0 = new KeyValueQuery<Method>();
        Sort.Direction sort_Direction0 = Sort.Direction.ASC;
        List<String> list0 = ResourceBundle.Control.FORMAT_PROPERTIES;
        Sort sort0 = new Sort(sort_Direction0, list0);
        KeyValueQuery<Method> keyValueQuery1 = keyValueQuery0.orderBy(sort0);
        hazelcastSortAccessor0.resolve(keyValueQuery1);
    }

    @Test
    public void test5()  throws Throwable  {
        HazelcastSortAccessor hazelcastSortAccessor0 = new HazelcastSortAccessor();
        KeyValueQuery<Object> keyValueQuery0 = new KeyValueQuery<Object>();
        Comparator<Map.Entry<?, ?>> comparator0 = (Comparator<Map.Entry<?, ?>>)hazelcastSortAccessor0.resolve(keyValueQuery0);
        assertNull(comparator0);
    }

    @Test
    public void test6()  throws Throwable  {
        HazelcastSortAccessor hazelcastSortAccessor0 = new HazelcastSortAccessor();
        Comparator<Map.Entry<?, ?>> comparator0 = (Comparator<Map.Entry<?, ?>>)hazelcastSortAccessor0.resolve((KeyValueQuery<?>) null);
        assertNull(comparator0);
    }

    @Test
    public void test7()  throws Throwable  {
        HazelcastSortAccessor hazelcastSortAccessor0 = new HazelcastSortAccessor();
        KeyValueQuery<Predicate<String, Foo>> query = new KeyValueQuery<>();
        List<String> propertyString = new LinkedList<>();
        propertyString.add("foo");
        propertyString.add("bar");
        query.setSort(new Sort(Sort.Direction.DESC, propertyString));
        Comparator<Map.Entry<?, ?>> comparator0 = (Comparator<Map.Entry<?, ?>>)hazelcastSortAccessor0.resolve((KeyValueQuery<?>) query);
        assertNotNull(comparator0);
        HazelcastSortAccessorTest.Foo testData1 = new HazelcastSortAccessorTest.Foo("zzz","def");
        HazelcastSortAccessorTest.Foo testData2 = new HazelcastSortAccessorTest.Foo("aaa","aaa");
        Map.Entry<String, HazelcastSortAccessorTest.Foo> entry1 = entryOf("testData1", testData1);
        Map.Entry<String, HazelcastSortAccessorTest.Foo> entry2 = entryOf("testData2", testData2);
        assertTrue(comparator0.compare(entry1, entry2) < 0);
        assertTrue(comparator0.compare(entry2, entry1) > 0);

        HazelcastSortAccessorTest.Foo testData3 = new HazelcastSortAccessorTest.Foo("aaa","aaa");
        HazelcastSortAccessorTest.Foo testData4 = new HazelcastSortAccessorTest.Foo("aaa","def");
        Map.Entry<String, HazelcastSortAccessorTest.Foo> entry3 = entryOf("testData3", testData3);
        Map.Entry<String, HazelcastSortAccessorTest.Foo> entry4 = entryOf("testData4", testData4);
        assertFalse(comparator0.compare(entry3, entry4) == 0);
        assertTrue((comparator0.compare(entry3, entry4) > 0));
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
            return ObjectUtils.nullSafeHashCode(new Object[] {this.foo,this.bar});
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
            return ObjectUtils.nullSafeEquals(this.foo, other.foo) && ObjectUtils.nullSafeEquals(this.bar,other.bar);
        }

    }

}