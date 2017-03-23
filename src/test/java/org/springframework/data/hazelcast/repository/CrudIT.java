package org.springframework.data.hazelcast.repository;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Resource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ActiveProfiles;

import test.utils.TestDataHelper;
import test.utils.domain.Person;
import test.utils.TestConstants;

/**
 * <P>
 * Downcast a {@link HazelcastRepository} into a {@link CrudRepository} to test basic C/R/U/D functionality.
 * </P>
 * <P>
 * Where possible, verify the repository against the underlying Hazelcast instance directly.
 * </P>
 *
 * @author Neil Stevenson
 */
@ActiveProfiles(TestConstants.SPRING_TEST_PROFILE_SINGLETON)
public class CrudIT extends TestDataHelper {
	private static final String EIGHTEEN_HUNDRED = "1800";
	private static final String NINETEEN_HUNDRED = "1900";
	private static final String TWO_THOUSAND = "2000";
	private static Person suggestedWinner1900 = new Person();
	private static Person suggestedWinner2000 = new Person();

	// PersonRepository is really a HazelcastRepository
	@Resource private CrudRepository<Person, String> personRepository;

	@Rule public TestName testName = new TestName();

	private long hazelcastCountBefore = -1L;
	private long springCountBefore = -1L;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Suggest a winner for 1900 - https://en.wikipedia.org/wiki/Georges_M%C3%A9li%C3%A8s
		suggestedWinner1900.setId(NINETEEN_HUNDRED);
		suggestedWinner1900.setFirstname("Georges");
		suggestedWinner1900.setLastname("Melies");

		// Suggest a winner for 2000, for Cast Away
		suggestedWinner2000.setId(TWO_THOUSAND);
		suggestedWinner2000.setFirstname("Tom");
		suggestedWinner2000.setLastname("Hanks");
	}

	@Before
	public void setUp() {
		super.setUp();

		this.hazelcastCountBefore = super.personMap.size();
		this.springCountBefore = this.personRepository.count();

		assertThat("Test data exists", this.springCountBefore, greaterThan(0L));
		assertThat("Spring==Hazelcast before", this.springCountBefore, equalTo(this.hazelcastCountBefore));

		assertNull("Key '1800' not in Hazelcast", super.personMap.get(EIGHTEEN_HUNDRED));
		assertNull("Key '1800' not in Spring", this.personRepository.findOne(EIGHTEEN_HUNDRED));

		assertNull("Key '1900' not in Hazelcast", super.personMap.get(NINETEEN_HUNDRED));
		assertNull("Key '1900' not in Spring", this.personRepository.findOne(NINETEEN_HUNDRED));

		assertNotNull("Key '2000' in Hazelcast", super.personMap.get(TWO_THOUSAND));
		assertNotNull("Key '2000' in Spring", this.personRepository.findOne(TWO_THOUSAND));
	}

	@Test
	public void count() {
		// CrudRepository.count() already tested in @Before this.setup().
		;
	}

	@Test
	public void deleteIterable() {
		List<Person> people = new ArrayList<>();

		String missingKey = EIGHTEEN_HUNDRED;
		Person missingPerson = new Person();
		missingPerson.setId(missingKey);

		people.add(missingPerson);

		for (int year = Integer.valueOf(NINETEEN_HUNDRED); year < Integer.valueOf(TWO_THOUSAND); year += 10) {
			Person person = this.personMap.get(Integer.toString(year));
			if (person != null) {
				people.add(person);
			}
		}

		assertThat("Deletion list contains both missing and existing entries", people.size(), greaterThan(1));

		this.personRepository.delete(people);

		// Missing items should not be deleted
		this.checkAfterCounts(1 - people.size());

		// First item didn't exist to delete, second onwards did
		for (int i = 1; i < people.size(); i++) {
			String key = people.get(i).getId();
			assertNull("List item " + i + ", key " + key + " not in Hazelcast", super.personMap.get(key));
			assertNull("List item " + i + ", key " + key + " not Spring", this.personRepository.findOne(key));
		}
	}

	@Test
	public void deleteValue() {
		String year = TWO_THOUSAND;

		Person person = super.personMap.get(year);

		// Use VALUE, otherwise same as deleteKey()
		this.personRepository.delete(person);

		assertNull("Person not in Hazelcast after deletion", super.personMap.get(year));
		assertNull("Person not in Spring after deletion", this.personRepository.findOne(year));

		this.checkAfterCounts(-1);
	}

	@Test
	public void deleteKey() {
		String year = TWO_THOUSAND;

		// Use KEY, otherwise same as deleteValue()
		this.personRepository.delete(year);

		assertNull("Person not in Hazelcast after deletion", super.personMap.get(year));
		assertNull("Person not in Spring after deletion", this.personRepository.findOne(year));

		this.checkAfterCounts(-1);
	}

	@Test
	public void deleteAll() {
		this.personRepository.deleteAll();

		long hazelcastCountAfter = super.personMap.size();
		long springCountAfter = this.personRepository.count();

		assertThat("All Hazelcast data deleted", hazelcastCountAfter, equalTo(0L));
		assertThat("All Spring data deleted", springCountAfter, equalTo(0L));
	}

	@Test
	public void exists() {
		assertNull("Key '1800' not in Hazelcast", super.personMap.get(EIGHTEEN_HUNDRED));
		assertFalse("Key '1800' not in Spring", this.personRepository.exists(EIGHTEEN_HUNDRED));

		assertNotNull("Key '2000' in Hazelcast", super.personMap.get(TWO_THOUSAND));
		assertTrue("Key '2000' in Spring", this.personRepository.exists(TWO_THOUSAND));
	}

	@Test
	public void findAll() {
		Iterable<Person> iterable = this.personRepository.findAll();

		assertNotNull("Iterable", iterable);

		int count = 0;
		Set<String> keysRetrieved = new TreeSet<>();

		for (Person springPerson : iterable) {
			assertNotNull("List item " + count, springPerson);

			String key = springPerson.getId();
			assertNotNull("List item " + count + ", key", key);

			assertFalse("List item " + count + ", key " + key + " is unique in the list", keysRetrieved.contains(key));

			Person hazelcastPerson = super.personMap.get(key);
			assertThat("List item " + count + ", key " + key + " is correct", hazelcastPerson, equalTo(springPerson));

			count++;
			keysRetrieved.add(key);
		}

		assertThat("All data retrieved", new Long(count), equalTo(hazelcastCountBefore));
	}

	@Test
	public void findAllIterable() {
		List<String> keys = new ArrayList<>();

		String missingKey = EIGHTEEN_HUNDRED;

		keys.add(missingKey);

		for (int year = Integer.valueOf(NINETEEN_HUNDRED); year < Integer.valueOf(TWO_THOUSAND); year += 10) {
			String key = Integer.toString(year);
			if (this.personMap.containsKey(key)) {
				keys.add(key);
			}
		}

		assertThat("Input list contains both missing and existing entries", keys.size(), greaterThan(1));
		assertThat("Input list is a true subset", new Long(keys.size()), lessThan(this.hazelcastCountBefore));

		Iterable<Person> iterable = this.personRepository.findAll(keys);

		assertNotNull("Iterable", iterable);

		// Missing items should not be retrieved
		int expectedRetrievals = keys.size() - 1;

		int count = 0;
		Set<String> keysRetrieved = new TreeSet<>();

		for (Person springPerson : iterable) {
			assertNotNull("List item " + count, springPerson);

			String key = springPerson.getId();
			assertNotNull("List item " + count + ", key", key);

			assertFalse("List item " + count + ", key " + key + " is unique in the list", keysRetrieved.contains(key));

			Person hazelcastPerson = super.personMap.get(key);
			assertThat("List item " + count + ", key " + key + " does exist", hazelcastPerson, equalTo(springPerson));

			assertTrue("List item " + count + ", key " + key + " was requested", keys.contains(key));

			count++;
			keysRetrieved.add(key);
		}

		assertThat("All available selections retrieved", expectedRetrievals, equalTo(keysRetrieved.size()));
	}

	@Test
	public void findOneKey() {
		String key = TWO_THOUSAND;
		Person winner = this.personRepository.findOne(key);

		assertNotNull("Winner found for year " + key, winner);
		assertThat("Correct id for " + key, winner.getId(), equalTo(key));
		assertThat("Correct last name for " + key, winner.getFirstname(), equalTo("Russell"));
		assertThat("Correct last name for " + key, winner.getLastname(), equalTo("Crowe"));
	}

	@Test
	public void saveIterable() {

		// Insert new for 1900 and update for 2000
		List<Person> suggestedWinners = new ArrayList<>();
		suggestedWinners.add(suggestedWinner1900);
		suggestedWinners.add(suggestedWinner2000);

		Iterable<Person> updatedWinners = this.personRepository.save(suggestedWinners);
		assertNotNull("Updated winners", updatedWinners);

		Set<String> keys = new TreeSet<>();
		for (Person updatedWinner : updatedWinners) {
			assertNotNull("updatedWinner", updatedWinner);

			String key = updatedWinner.getId();
			assertNotNull("updatedWinner.getKey()", key);

			// Check no duplicates in returned list
			assertFalse("Key only returned once for " + key, keys.contains(key));
			keys.add(key);

			// Capture match in list, not just that it contains it
			Person suggestedWinner = null;
			for (Person p : suggestedWinners) {
				if (p.getId().equals(key)) {
					suggestedWinner = p;
				}
			}
			assertNotNull("Key returned " + key + " was requested", suggestedWinner);

			// Save returns possibly updated instance, for Haelcast should be a clone
			assertThat("Unchanged id for " + key, updatedWinner.getId(), equalTo(suggestedWinner.getId()));
			assertThat("Unchanged last name for " + key, updatedWinner.getFirstname(),
					equalTo(suggestedWinner.getFirstname()));
			assertThat("Unchanged last name for " + key, updatedWinner.getLastname(), equalTo(suggestedWinner.getLastname()));

			// Confirm change stored
			Person springWinner = this.personRepository.findOne(key);
			Person hazelcastWinner = super.personMap.get(key);

			assertNotNull("Spring person for " + key, springWinner);
			assertThat("Spring id for " + key, springWinner.getId(), equalTo(suggestedWinner.getId()));
			assertThat("Spring last name for " + key, springWinner.getFirstname(), equalTo(suggestedWinner.getFirstname()));
			assertThat("Spring last name for " + key, springWinner.getLastname(), equalTo(suggestedWinner.getLastname()));

			assertNotNull("Hazelcast person for " + key, hazelcastWinner);
			assertThat("Hazelcast id for " + key, hazelcastWinner.getId(), equalTo(suggestedWinner.getId()));
			assertThat("Hazelcast last name for " + key, hazelcastWinner.getFirstname(),
					equalTo(suggestedWinner.getFirstname()));
			assertThat("Hazelcast last name for " + key, hazelcastWinner.getLastname(),
					equalTo(suggestedWinner.getLastname()));
		}

		assertThat("All updates processed", keys.size(), equalTo(suggestedWinners.size()));

		this.checkAfterCounts(+1);
	}

	@Test
	public void saveValue() {

		// Insert new for 1900 and update for 2000
		for (Person suggestedWinner : new Person[] { suggestedWinner1900, suggestedWinner2000 }) {

			// Save returns possibly updated instance, for Haelcast should be a clone
			String key = suggestedWinner.getId();
			Person updatedWinner = this.personRepository.save(suggestedWinner);
			assertNotNull("Updated person returned for " + key, updatedWinner);
			assertThat("Unchanged id for " + key, updatedWinner.getId(), equalTo(suggestedWinner.getId()));
			assertThat("Unchanged last name for " + key, updatedWinner.getFirstname(),
					equalTo(suggestedWinner.getFirstname()));
			assertThat("Unchanged last name for " + key, updatedWinner.getLastname(), equalTo(suggestedWinner.getLastname()));

			// Confirm change stored
			Person springWinner = this.personRepository.findOne(key);
			Person hazelcastWinner = super.personMap.get(key);

			assertNotNull("Spring person for " + key, springWinner);
			assertThat("Spring id for " + key, springWinner.getId(), equalTo(suggestedWinner.getId()));
			assertThat("Spring last name for " + key, springWinner.getFirstname(), equalTo(suggestedWinner.getFirstname()));
			assertThat("Spring last name for " + key, springWinner.getLastname(), equalTo(suggestedWinner.getLastname()));

			assertNotNull("Hazelcast person for " + key, hazelcastWinner);
			assertThat("Hazelcast id for " + key, hazelcastWinner.getId(), equalTo(suggestedWinner.getId()));
			assertThat("Hazelcast last name for " + key, hazelcastWinner.getFirstname(),
					equalTo(suggestedWinner.getFirstname()));
			assertThat("Hazelcast last name for " + key, hazelcastWinner.getLastname(),
					equalTo(suggestedWinner.getLastname()));
		}

		this.checkAfterCounts(+1);
	}

	// Common methods used by tests

	private void checkAfterCounts(int delta) {
		long hazelcastCountAfter = super.personMap.size();
		long springCountAfter = this.personRepository.count();

		String message;
		if (delta < 0) {
			message = this.testName.getMethodName() + "Inserted correct amount into ";
		} else {
			message = this.testName.getMethodName() + "Deleted correct amount from ";
		}

		assertThat(message + "Hazelcast", this.hazelcastCountBefore + delta, equalTo(hazelcastCountAfter));
		assertThat(message + "Spring", this.springCountBefore + delta, equalTo(springCountAfter));
		assertThat(this.testName.getMethodName() + ":Spring==Hazelcast after", springCountAfter,
				equalTo(hazelcastCountAfter));
	}

}
