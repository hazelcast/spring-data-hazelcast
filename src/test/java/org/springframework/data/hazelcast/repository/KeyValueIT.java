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

import org.junit.Test;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import test.utils.domain.Person;

import javax.annotation.Resource;

/**
 * <p>
 * Downcast a {@link HazelcastRepository} into a {@link KeyValueRepository} to test any Key-Value additions to
 * {@link PagingAndSortingRepository}. At the moment, there are none so this is empty.
 * </P>
 *
 * @author Neil Stevenson
 */
public class KeyValueIT {

    // PersonRepository is really a HazelcastRepository
    @Resource
    private KeyValueRepository<Person, String> personRepository;

    /* No tests required, see class comments above.
     */
    @Test
    public void no_op() {
        ;
    }
}
