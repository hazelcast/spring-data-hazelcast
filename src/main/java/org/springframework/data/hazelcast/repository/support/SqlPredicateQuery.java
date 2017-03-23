package org.springframework.data.hazelcast.repository.support;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;
import org.springframework.data.hazelcast.repository.config.Constants;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

public class SqlPredicateQuery implements RepositoryQuery {


    private final HazelcastQueryMethod queryMethod;
    private final String keySpace;

    public SqlPredicateQuery(HazelcastQueryMethod queryMethod) {
        this.queryMethod = queryMethod;
        this.keySpace = queryMethod.getKeySpace();
    }

    @Override
    public Object execute(Object[] parameters) {

        String queryString = queryMethod.getAnnotatedQuery();

        // TODO check and replace parameters

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
