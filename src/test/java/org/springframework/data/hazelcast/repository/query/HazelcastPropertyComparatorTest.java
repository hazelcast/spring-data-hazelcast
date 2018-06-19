package org.springframework.data.hazelcast.repository.query;

import org.junit.Test;

import java.util.AbstractMap;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;

public class HazelcastPropertyComparatorTest {

    private final HazelcastPropertyComparator propertyComparator = new HazelcastPropertyComparator("value", true);

    @Test
    public void testNaturalOrder() {
        TestData testData1 = new TestData(1);
        TestData testData2 = new TestData(2);
        Entry<String, TestData> entry1 = entryOf("testData1", testData1);
        Entry<String, TestData> entry2 = entryOf("testData2", testData2);
        assertEquals(-1, propertyComparator.compare(entry1, entry2));
        assertEquals(1, propertyComparator.compare(entry2, entry1));
    }

    @Test
    public void compareTwoNullProperties() {
        TestData testData1 = new TestData(null);
        TestData testData2 = new TestData(null);
        Entry<String, TestData> nullField1 = entryOf("testData1", testData1);
        Entry<String, TestData> nullField2 = entryOf("testData2", testData2);
        assertEquals(0, propertyComparator.compare(nullField1, nullField2));
        assertEquals(0, propertyComparator.compare(nullField2, nullField1));
    }

    @Test
    public void compareNullPropertyWithNonNullProperty() {
        TestData testData1 = new TestData(null);
        TestData testData2 = new TestData(2);
        Entry<String, TestData> nullField = entryOf("testData1", testData1);
        Entry<String, TestData> notNullField = entryOf("testData2", testData2);
        assertEquals(1, propertyComparator.compare(nullField, notNullField));
        assertEquals(-1, propertyComparator.compare(notNullField, nullField));
    }

    @Test
    public void compareNotComparableProperties() {
        TestData testData1 = new TestData(new TestData(1));
        TestData testData2 = new TestData(2);
        TestData testData3 = new TestData(new TestData(3));
        Entry<String, TestData> entry1NotComparable = entryOf("testData1", testData1);
        Entry<String, TestData> entry2Comparable = entryOf("testData2", testData2);
        Entry<String, TestData> entry3NotComparable = entryOf("testData3", testData3);
        assertEquals(0, propertyComparator.compare(entry1NotComparable, entry2Comparable));
        assertEquals(0, propertyComparator.compare(entry2Comparable, entry1NotComparable));
        assertEquals(0, propertyComparator.compare(entry1NotComparable, entry3NotComparable));
    }

    private static <K, V> Entry<K, V> entryOf(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    private static class TestData {
        private final Object value;

        public TestData(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

}