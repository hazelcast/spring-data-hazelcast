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
package org.springframework.data.hazelcast.repository.query;

import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicate;
import org.springframework.data.keyvalue.core.CriteriaAccessor;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;

/**
 * <P>
 * Provide a mechanism to convert the abstract query into the direct implementation in Hazelcast.
 * </P>
 *
 * @author Neil Stevenson
 * @author Viacheslav Petriaiev
 */
public class HazelcastCriteriaAccessor implements CriteriaAccessor<Predicate<?, ?>> {

	/**
	 * @param A query in Spring form
	 * @return The same in Hazelcast form
	 */
	public Predicate<?, ?> resolve(KeyValueQuery<?> query) {

		if (query == null || query.getCriteria() == null) {
			return null;
		}

		if (query.getCriteria() instanceof PagingPredicate) {
			PagingPredicate pagingPredicate = (PagingPredicate) query.getCriteria();
			query.limit(pagingPredicate.getPageSize());
			return pagingPredicate.getPredicate();
		}

		if (query.getCriteria() instanceof Predicate) {
			return (Predicate<?, ?>) query.getCriteria();
		}

		throw new UnsupportedOperationException(query.toString());
	}

}
