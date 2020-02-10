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

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.hazelcast.repository.query.Query;
import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Hazelcast {@link QueryMethod} Implementation
 */
public class HazelcastQueryMethod
        extends QueryMethod {

    private final Method method;

    public HazelcastQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
        this.method = method;
    }

    public boolean hasAnnotatedQuery() {
        return StringUtils.hasText(getAnnotatedQuery());
    }

    String getAnnotatedQuery() {
        Query query = method.getAnnotation(Query.class);
        String queryString = (query != null ? (String) AnnotationUtils.getValue(query) : null);
        return (StringUtils.hasText(queryString) ? queryString : null);
    }

    String getKeySpace() {
        Class<?> clazz = getEntityInformation().getJavaType();
        KeySpace keySpace = clazz.getAnnotation(KeySpace.class);
        String queryString = (keySpace != null ? (String) AnnotationUtils.getValue(keySpace) : null);
        return (StringUtils.hasText(queryString) ? queryString : clazz.getName());

    }

}
