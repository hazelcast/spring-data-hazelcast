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
/**
 * <P>
 * {@link HazelcastPartTreeQuery} is a required modification to that provided by Spring-Data-Keyvalue (class
 * {@link KeyValuePartTreeQuery}) to implement queries correctly for Hazelcast.
 * </P>
 * <P>
 * As this is embedded, need to provide {@link HazelcastRepositoryFactoryBean} to create
 * {@link HazelcastRepositoryFactory} which provide a {@link HazelcastQueryLookupStrategy}. There largely extend their
 * Spring-Data-KeyValue counterparts, copying where necessary, and overriding where possible.
 * </P>
 */
package org.springframework.data.hazelcast.repository.support;
