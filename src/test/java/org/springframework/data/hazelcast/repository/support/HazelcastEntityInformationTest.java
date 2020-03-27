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
package org.springframework.data.hazelcast.repository.support;

import com.hazelcast.core.Hazelcast;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.hazelcast.HazelcastUtils;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentEntity;
import test.utils.domain.NoIdEntity;

public class HazelcastEntityInformationTest {

    private KeyValueOperations operations;

    @Before
    public void setUp() {
        this.operations = new KeyValueTemplate(HazelcastUtils.preconfiguredHazelcastKeyValueAdapter());
    }

    @Test(expected = MappingException.class)
    public void throwsMappingExceptionWhenNoIdPropertyPresent() {
        PersistentEntity<?, ?> persistentEntity = operations.getMappingContext().getPersistentEntity(NoIdEntity.class);
        new HazelcastEntityInformation<>(persistentEntity);
    }

    @AfterClass
    public static void close() {
        Hazelcast.shutdownAll();
    }
}