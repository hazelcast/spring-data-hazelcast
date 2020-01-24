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
package org.springframework.data.hazelcast.repository;

import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * <p>
 * Subtype {@link org.springframework.stereotype.Repository @Repository} for Hazelcast usage.
 * </P>
 * <p>
 * Although part of the rationale of the repository interface is to abstract the implementation, it is useful for type
 * checking to confirm the allowed type generics for the domain classes.
 * </P>
 * <p>
 * Note that {@link org.springframework.data.keyvalue.repository.KeyValueRepository KeyValueRepository} defines that the
 * {@code ID} class extends {@link Serializable}.
 * </P>
 *
 * @param <T>  The type of the domain value class
 * @param <ID> The type of the domain key class
 * @author Neil Stevenson
 */
@NoRepositoryBean
public interface HazelcastRepository<T extends Serializable, ID extends Serializable>
        extends KeyValueRepository<T, ID> {
}
