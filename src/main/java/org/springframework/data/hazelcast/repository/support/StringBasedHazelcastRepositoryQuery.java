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

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;
import org.springframework.data.hazelcast.repository.config.Constants;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

/**
 * {@link RepositoryQuery} using String based {@link SqlPredicate} to query Hazelcast Cluster.
 */
public class StringBasedHazelcastRepositoryQuery implements RepositoryQuery {


    private final HazelcastQueryMethod queryMethod;
    private final String keySpace;

    public StringBasedHazelcastRepositoryQuery(HazelcastQueryMethod queryMethod) {
        this.queryMethod = queryMethod;
        this.keySpace = queryMethod.getKeySpace();
    }

    @Override
    public Object execute(Object[] parameters) {

        String queryStringTemplate = queryMethod.getAnnotatedQuery();

        String queryString = String.format(queryStringTemplate, parameters);

        SqlPredicate sqlPredicate = new SqlPredicate(queryString);

        return getMap(keySpace).values(sqlPredicate);
    }

    private IMap getMap(String keySpace) {
        return Hazelcast.getHazelcastInstanceByName(Constants.HAZELCAST_INSTANCE_NAME).getMap(keySpace);
    }

    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }
}
