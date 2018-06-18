package org.springframework.data.hazelcast.repository.query;

import org.junit.Test;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class HazelcastPropertyComparatorTest {

    static class TestData {
        String first;
        String second;
        Integer third;

        public TestData(String first, String second, Integer third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }

        public Integer getThird() {
            return third;
        }
    }

    @Test
    public void compareTwoNullProperties() {
        HazelcastPropertyComparator thirdFieldComparator = new HazelcastPropertyComparator("third", true);
        TestData testData1 = new TestData("Test", null, null);
        TestData testData2 = new TestData("Test", null, null);
        Entry<String, TestData> nullField1 = entryOf("testData1", testData1);
        Entry<String, TestData> nullField2 = entryOf("testData2", testData2);
        assertEquals(0, thirdFieldComparator.compare(nullField1, nullField2));
        assertEquals(0, thirdFieldComparator.compare(nullField2, nullField1));
    }

    @Test
    public void compareNullPropertyWithNonNullProperty() {
        HazelcastPropertyComparator secondFieldComparator = new HazelcastPropertyComparator("second", true);
        TestData testData1 = new TestData("Test", null, 1);
        TestData testData2 = new TestData("Test", "Case", 2);
        Entry<String, TestData> nullField = entryOf("second", testData1);
        Entry<String, TestData> notNullField = entryOf("second", testData2);
        assertEquals(1, secondFieldComparator.compare(nullField, notNullField));
        assertEquals(-1, secondFieldComparator.compare(notNullField, nullField));
    }

    @Test
    public void compareOnMultipleProperties() {
        Comparator<Entry<String,TestData>> comparator = comparatorForFields(true, "first", "second", "third");
        TestData testData0 = new TestData("Test", null, 0);
        TestData testData1 = new TestData("Test", "Case", 1);
        TestData testData2 = new TestData("Test", "Case", 2);
        Entry<String, TestData> testData0Entry = entryOf("testData0", testData0);
        Entry<String, TestData> testData1Entry = entryOf("testData1", testData1);
        Entry<String, TestData> testData2Entry = entryOf("testData2", testData2);
        // td0 > td2 > td2 > td1 => td0 > td1
        assertEquals(1, comparator.compare(testData0Entry, testData2Entry));
        assertEquals(1, comparator.compare(testData2Entry, testData1Entry));
        assertEquals(1, comparator.compare(testData0Entry, testData1Entry));

    }

    private static <K,V> Entry<K,V> entryOf(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    private static <K,V> Comparator<Entry<K,V>> comparatorForFields(boolean asc, String...fields) {
        List<HazelcastPropertyComparator> comparators = Stream.of(fields).map(field -> new HazelcastPropertyComparator(field, asc)).collect(toList());
        return (o1, o2) ->  comparators
                .stream()
                .map(c -> c.compare(o1, o2))
                .filter(i -> i != 0)
                .findFirst()
                .orElse(0);

    }
}