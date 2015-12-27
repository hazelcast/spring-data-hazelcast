/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hazelcast.repository;

import java.io.Serializable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

/**
 * <P>
 * Subtype {@link org.springframework.stereotype.Repository @Repository} for Hazelcast usage.
 * </P>
 * <P>
 * Although part of the rationale of the repository interface is to abstract the implementation, it is useful for type
 * checking to confirm the allowed type generics for the domain classes.
 * </P>
 * <P>
 * Note that {@link org.springframework.data.keyvalue.repository.KeyValueRepository KeyValueRepository} defines that the
 * {@code ID} class extends {@link Serializable}.
 * </P>
 *
 * @author Neil Stevenson
 * @param <T> The type of the domain value class
 * @param <ID> The type of the domain key class
 */
@NoRepositoryBean
public interface HazelcastRepository<T, ID extends Serializable> extends KeyValueRepository<T, ID> {}
