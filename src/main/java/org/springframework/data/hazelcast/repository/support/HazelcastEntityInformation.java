/*
 * Copyright 2014-2016 the original author or authors.
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
package org.springframework.data.hazelcast.repository.support;

import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.support.PersistentEntityInformation;

/**
 * {@link EntityInformation} implementation checks for {@link Id} field eagarly to prevent NPE if not found.
 *
 * @author Gokhan Oner
 */
class HazelcastEntityInformation<T, ID> extends PersistentEntityInformation<T, ID> {

    /**
     * @param entity must not be {@literal null}.
     */
    HazelcastEntityInformation(PersistentEntity<T, ?> entity) {
        super(entity);
        if (!entity.hasIdProperty()) {
            throw new MappingException(
                    String.format("Entity %s requires a field annotated with %s",
                            entity.getName(), Id.class.getName()));
        }
    }
}
