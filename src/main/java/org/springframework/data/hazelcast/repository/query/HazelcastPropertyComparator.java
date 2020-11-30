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

import com.hazelcast.query.impl.getters.ReflectionHelper;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Comparator;
import java.util.Map.Entry;

/**
 * <p>
 * Implement a limited form of custom comparison between entries. The fields used for the comparison and the
 * ascending/descending can be specified at run time.
 * </P>
 *
 * @author Neil Stevenson
 */
public class HazelcastPropertyComparator
        implements Comparator<Entry<?, ?>>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final MethodHandle EXTRACT_VALUE_HAZELCAST_41 = resolveExtractValueHazelcast41();
    private static final MethodHandle EXTRACT_VALUE_HAZELCAST_403 = resolveExtractValueHazelcast403();


    private static MethodHandle resolveExtractValueHazelcast41() {
        try {
            return MethodHandles.lookup().findStatic(ReflectionHelper.class,
              "extractValue", MethodType.methodType(Object.class, Object.class, String.class, boolean.class));
        } catch (Throwable ex) {
            return null;
        }
    }

    private static MethodHandle resolveExtractValueHazelcast403() {
        try {
            return MethodHandles.lookup()
              .findStatic(ReflectionHelper.class,
                "extractValue", MethodType.methodType(Object.class, Object.class, String.class));
        } catch (Throwable ex) {
            return null;
        }
    }

    private final String attributeName;
    private final int direction;

    public HazelcastPropertyComparator(String attributeName, boolean ascending) {
        this.attributeName = attributeName;
        this.direction = (ascending ? 1 : -1);
    }

    /**
     * <p>
     * Use Hazelcast's {@code ReflectionHelper} to extract a field in an entry, and use this is in the comparison.
     * </P>
     *
     * @param o1 An entry in a map
     * @param o2 Another entry in the map
     * @return Comparison result
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public int compare(Entry<?, ?> o1, Entry<?, ?> o2) {

        try {

            Object o1Field;
            Object o2Field;

            if (EXTRACT_VALUE_HAZELCAST_41 == null && EXTRACT_VALUE_HAZELCAST_403 == null) {
                throw new IllegalStateException("Could not resolve a ReflectionHelper.extractValue method. Using a non-supported Hazelcast version");
            }

            try {
                if (EXTRACT_VALUE_HAZELCAST_403 != null) {
                    o1Field = EXTRACT_VALUE_HAZELCAST_403.invoke(o1.getValue(), this.attributeName);
                    o2Field = EXTRACT_VALUE_HAZELCAST_403.invoke(o2.getValue(), this.attributeName);
                } else {
                    o1Field = EXTRACT_VALUE_HAZELCAST_41.invoke(o1.getValue(), this.attributeName, true);
                    o2Field = EXTRACT_VALUE_HAZELCAST_41.invoke(o2.getValue(), this.attributeName, true);
                }
            } catch (Throwable throwable) {
                throw new IllegalStateException("Could not resolve a ReflectionHelper.extractValue method. Using a non-supported Hazelcast version", throwable);
            }

            if (o1Field == o2Field) {
                return 0;
            }
            if (o1Field == null) {
                return this.direction;
            }
            if (o2Field == null) {
                return -1 * this.direction;
            }
            if (o1Field instanceof Comparable && o2Field instanceof Comparable) {
                return this.direction * ((Comparable) o1Field).compareTo(o2Field);
            }

        } catch (Exception ex) {
            return 0;
        }

        return 0;
    }
}
