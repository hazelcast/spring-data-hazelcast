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
package org.springframework.data.hazelcast.repository;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * Default ordering for no-predefined-ordering use case: use Comparable, if not possible use no order (always 0).
 * @param <K> key type
 * @param <V> value type
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultOrderComparator<K, V> implements Comparator<Map.Entry<K, V>>, DataSerializable {

    @Override
    public int compare(Map.Entry<K, V> v1, Map.Entry<K, V> v2) {
        if (v1.getValue() instanceof Comparable v1c &&  v2.getValue() instanceof Comparable v2c) {
            return v1c.compareTo(v2c);
        }
        return 0;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
    }
}
