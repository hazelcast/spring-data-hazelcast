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
        String queryString = String.format(queryStringTemplate, parameters);
        SqlPredicate sqlPredicate = new SqlPredicate(queryString);
        return getMap(keySpace).values(sqlPredicate);
    }

    private IMap getMap(String keySpace) {
        return hazelcastInstance.getMap(keySpace);
    }

    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }
}
