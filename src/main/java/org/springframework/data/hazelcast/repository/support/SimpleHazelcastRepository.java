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
package org.springframework.data.hazelcast.repository.support;

import org.springframework.data.hazelcast.repository.HazelcastRepository;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.repository.core.EntityInformation;

import java.io.Serializable;

/**
 * <P>A concrete implementation to instantiate directly rather than allow
 * Spring to generate.
 * </P>
 *
 * @param <T>  The domain object
 * @param <ID> The key of the domain object
 * @author Neil Stevenson
 */
public class SimpleHazelcastRepository<T extends Serializable, ID extends Serializable>
        extends SimpleKeyValueRepository<T, ID>
        implements HazelcastRepository<T, ID> {

    public SimpleHazelcastRepository(EntityInformation<T, ID> metadata, KeyValueOperations operations) {
        super(metadata, operations);
    }

}
