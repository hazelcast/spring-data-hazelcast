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

import com.hazelcast.query.impl.getters.ReflectionHelper;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;

/**
 * <P>
 * Implement a limited form of custom comparison between entries. The fields used for the comparison and the
 * ascending/descending can be specified at run time.
 * </P>
 *
 * @author Neil Stevenson
 */
public class HazelcastPropertyComparator implements Comparator<Entry<?, ?>>, Serializable {
	private static final long serialVersionUID = 1L;

	private String attributeName;
	private int direction;

	public HazelcastPropertyComparator(String attributeName, boolean ascending) {
		this.attributeName = attributeName;
		this.direction = (ascending ? 1 : -1);
	}

	/**
	 * <P>
	 * Use Hazelcast's {@code ReflectionHelper} to extract a field in an entry, and use this is in the comparison.
	 * </P>
	 *
	 * @param o1 An entry in a map
	 * @param o2 Another entry in the map
	 * @return Comparison result
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int compare(Entry<?, ?> o1, Entry<?, ?> o2) {

		try {
			Object o1Field = ReflectionHelper.extractValue(o1.getValue(), this.attributeName);
			Object o2Field = ReflectionHelper.extractValue(o2.getValue(), this.attributeName);

			if (o1Field == null) {
				return this.direction;
			}
			if (o2Field == null) {
				return -1 * this.direction;
			}
			if (o1Field instanceof Comparable && o2Field instanceof Comparable) {
				return this.direction * ((Comparable) o1Field).compareTo((Comparable) o2Field);
			}

		} catch (Exception ignore) {
			return 0;
		}

		return 0;
	}

}
