package org.springframework.data.hazelcast.topology;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.hazelcast.repository.config.Constants;
import test.utils.TestConstants;
import test.utils.TestDataHelper;
import test.utils.domain.Person;
import test.utils.repository.standard.PersonRepository;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * <P>
 * A single Hazelcast instance is sufficient for most tests, but does not confirm the operations invoked via Spring Data
 * Hazelcast are distributed.
 * </P>
 * <P>
 * Define common tests to prove distribution, for use in more advanced topologies. The nature of these tests is to have
 * more than one Hazelcast instance, and to verify object state on an instance other than the one Spring is using.
 * </P>
 * <P>
 * Note that when multiple Hazelcast instances are used, they are all part of the same JVM. This is to simplify the
 * co-ordination of process starting and stopping, since networking isn't relevant to the tests.
 * </P>
 *
 * @author Neil Stevenson
 */
public abstract class AbstractTopologyIT extends TestDataHelper {

	// From the server that isn't Spring's Hazelcast instance
	protected IMap<String, Person> server_personMap = null;

	@Resource private PersonRepository personRepository;

	@Before
	public void setUp() {
		super.setUp();

		HazelcastInstance hazelcastServer = null;

		// Look for any instance apart from Spring's one
		for (HazelcastInstance hazelcastInstance : Hazelcast.getAllHazelcastInstances()) {
			if (!hazelcastInstance.getName().equals(Constants.HAZELCAST_INSTANCE_NAME)) {
				hazelcastServer = hazelcastInstance;
			}
		}

		assertThat("Server found", hazelcastServer, notNullValue());
		this.server_personMap = hazelcastServer.getMap(TestConstants.PERSON_MAP_NAME);
	}

	/* Data manipulated via the other instance should be visible
	 * to the Spring instance, and vice versa.
	 */
	@Test
	public void obiWanKenobi() {
		String NINETEEN_FIFTY_SEVEN = "1957";

		Person springBefore = this.personRepository.findOne(NINETEEN_FIFTY_SEVEN);

		assertThat("1957 winner found", springBefore, notNullValue());
		assertThat("Firstname Alec", springBefore.getFirstname(), equalTo("Alec"));
		assertThat("Lastname Guinness", springBefore.getLastname(), equalTo("Guinness"));

		Person person = new Person();
		person.setFirstname("Obi-Wan");
		person.setLastname("Kenobi");
		person.setId(NINETEEN_FIFTY_SEVEN);

		this.server_personMap.put(person.getId(), person);

		Person springAfter = this.personRepository.findOne(NINETEEN_FIFTY_SEVEN);

		assertThat("1957 winner found again", springAfter, notNullValue());
		assertThat("Firstname changed", springAfter.getFirstname(), not(equalTo("Alec")));
		assertThat("Lastname changed", springAfter.getLastname(), not(equalTo("Guinness")));

		this.personRepository.delete(NINETEEN_FIFTY_SEVEN);

		assertThat("Obi-Wan has vanished", this.server_personMap.containsKey(NINETEEN_FIFTY_SEVEN), equalTo(false));
	}

}
