package org.springframework.data.hazelcast.repository;

import javax.annotation.Resource;
import org.junit.Test;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import test.utils.domain.Person;

/**
 * <P>
 * Downcast a {@link HazelcastRepository} into a {@link KeyValueRepository} to test any Key-Value additions to
 * {@link PagingAndSortingRepository}. At the moment, there are none so this is empty.
 * </P>
 *
 * @author Neil Stevenson
 */
public class KeyValueIT {

	// PersonRepository is really a HazelcastRepository
	@Resource private KeyValueRepository<Person, String> personRepository;

	/* No tests required, see class comments above.
	 */
	@Test
	public void no_op() {
		;
	}
}
