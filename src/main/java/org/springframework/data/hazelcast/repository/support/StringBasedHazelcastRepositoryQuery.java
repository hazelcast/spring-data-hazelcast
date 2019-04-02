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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * {@link RepositoryQuery} using String based {@link SqlPredicate} to query Hazelcast Cluster.
 */
public class StringBasedHazelcastRepositoryQuery
        implements RepositoryQuery {

    private final HazelcastQueryMethod queryMethod;
    private final String keySpace;
    private final HazelcastInstance hazelcastInstance;

    public StringBasedHazelcastRepositoryQuery(HazelcastQueryMethod queryMethod, HazelcastInstance hazelcastInstance) {
        this.queryMethod = queryMethod;
        this.keySpace = queryMethod.getKeySpace();
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public Object execute(Object[] parameters) {
        String queryStringTemplate = queryMethod.getAnnotatedQuery();
        String queryString = String.format(queryStringTemplate, formatParameters(parameters));
        SqlPredicate sqlPredicate = new SqlPredicate(queryString);
        return getMap(keySpace).values(sqlPredicate);
    }

    private Object[] formatParameters(Object[] parameters) {
        Object[] result = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] instanceof Collection) {
                result[i] = formatCollection((Collection) parameters[i]);
            } else {
                result[i] = parameters[i];
            }
        }
        return result;
    }

    private static String formatCollection(Collection<?> collection) {
        return String.format("(%s)", collection.stream().map(Object::toString).collect(Collectors.joining(",")));
    }

    private IMap getMap(String keySpace) {
        return hazelcastInstance.getMap(keySpace);
    }

    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }
}
