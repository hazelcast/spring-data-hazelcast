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
package org.springframework.data.hazelcast.repository.config;

/**
 * Constants used in this project.
 *
 * @deprecated Don't use these constants, since it's not guaranteed that a HazelcastInstace with the name {@literal
 * HAZELCAST_INSTANCE_NAME} is running.
 */
@Deprecated
public final class Constants {

    /**
     * Hazelcast Instance Name used to gurantee only one Hazelcast Instance inside the JVM
     */
    public static final String HAZELCAST_INSTANCE_NAME = "spring-data-hazelcast-instance";

    private Constants() {
    }
}
