package org.springframework.data.hazelcast.repository.query;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import test.utils.TestConstants;
import test.utils.Oscars;
import test.utils.TestDataHelper;
import test.utils.domain.Person;
import test.utils.repository.standard.PersonRepository;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * <P>
 * Test querying functionality.
 * </P>
 * <P>
 * Sorting with querying is only fully tested in methods where the collation sequence is parameter, and different
 * sequences can be tried.
 * </P>
 *
 * @author Neil Stevenson
 */
@ActiveProfiles(TestConstants.SPRING_TEST_PROFILE_SINGLETON)
public class QueryIT extends TestDataHelper {
	private static final int PAGE_0 = 0;
	private static final int SIZE_1 = 1;
	private static final int SIZE_5 = 5;

	@Resource private PersonRepository personRepository;

	@Rule public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void countByFirstname() {
		Long count = this.personRepository.countByFirstname("James");
		assertThat("1940 and 1942", count, equalTo(2L));

		count = this.personRepository.countByFirstname("Bing");
		assertThat("1944", count, equalTo(1L));
	}

	@Test
	public void countByIdLessThanEqual() {
		Long count = this.personRepository.countByIdLessThanEqual("1940");
		assertThat("Oscars began in 1928", count, equalTo(13L));
	}

	@Test
	public void countDistinctLastnameByFirstname() {
		this.expectedException.expect(UnsupportedOperationException.class);
		this.expectedException.expectMessage(startsWith("DISTINCT"));

		this.personRepository.countDistinctLastnameByFirstname("Daniel");
	}

	@Test
	public void countByLastnameAllIgnoreCase() {
		Long count = this.personRepository.countByLastnameAllIgnoreCase("day-LEWIS");
		assertThat("1989, 2007 and 2012", count, equalTo(3L));
	}

	@Test
	public void countByFirstnameOrLastnameAllIgnoreCase() {
		Long count = this.personRepository.countByFirstnameOrLastnameAllIgnoreCase("james", "GUINNESS");
		assertThat("1940, 1942 and 1957", count, equalTo(3L));
	}

	@Test
	public void countByFirstnameAndLastnameAllIgnoreCase() {
		Long count = this.personRepository.countByFirstnameAndLastnameAllIgnoreCase("JAMES", "CAGNEY");
		assertThat("1942", count, equalTo(1L));
	}

	@Test
	public void findByFirstnameIgnoreCaseOrLastname() {
		Person person = this.personRepository.findByFirstnameIgnoreCaseOrLastname("alec", "");
		assertThat("1957 ignore case applied to firstname not lastname", person, notNullValue());
		assertThat("1957 firstname", person.getId(), equalTo("1957"));
		person = this.personRepository.findByFirstnameIgnoreCaseOrLastname("", "guinness");
		assertThat("1957 lastname", person, nullValue());
	}

	@Test
	public void findByFirstnameOrLastnameIgnoreCase() {
		Person person = this.personRepository.findByFirstnameOrLastnameIgnoreCase("alec", "");
		assertThat("1957 firstname", person, nullValue());
		person = this.personRepository.findByFirstnameOrLastnameIgnoreCase("", "guinness");
		assertThat("1957 ignore case applied to lastname not firstname", person, notNullValue());
		assertThat("1957 lastname", person.getId(), equalTo("1957"));
	}

	@Test
	public void findByFirstnameOrLastnameAllIgnoreCase() {
		Person person = this.personRepository.findByFirstnameOrLastnameAllIgnoreCase("alec", "");
		assertThat("1957 firstname", person, notNullValue());
		assertThat("1957 firstname", person.getId(), equalTo("1957"));
		person = this.personRepository.findByFirstnameOrLastnameAllIgnoreCase("", "guinness");
		assertThat("1957 lastname", person, notNullValue());
		assertThat("1957 lastname", person.getId(), equalTo("1957"));
	}
	
	@Test
	public void deleteByLastname() {
		assertThat("Map fully populated before", this.personMap.size(), equalTo(Oscars.bestActors.length));
		
		Person person = this.personRepository.deleteByLastname("abcdefghijklmnopqrstuvwxyz");
		
		assertThat("Delete for unmatched name does nothing to map", this.personMap.size(), equalTo(Oscars.bestActors.length));
		assertThat("Delete for unmatched name does nothing to @Repository", this.personRepository.count(), equalTo(Long.valueOf(Oscars.bestActors.length)));
		assertThat("Delete for unmatched name returns null", person, nullValue());
		
		// Spencer Tracy, 1937 & 1938
		int count = 0;
		while ( (person = this.personRepository.deleteByLastname("Tracy")) != null ) {
			count++;
		}
		
		assertThat("Delete for matched name removes from map", this.personMap.size(), equalTo(Oscars.bestActors.length - 2));
		assertThat("Delete for matched name removes from @Repository", this.personRepository.count(), equalTo(Long.valueOf(Oscars.bestActors.length - 2)));
		assertThat("Delete for matched name returns correct count", count, equalTo(2));
		assertThat("1937 deleted", this.personMap.get("1937"), nullValue());
		assertThat("1938 deleted", this.personMap.get("1938"), nullValue());
	}

	// First by ascending == Min
	@Test
	public void findFirstIdByOrderById() {
		Object result = this.personRepository.findFirstIdByOrderById();

		assertThat("First Winner", result, notNullValue());
		assertThat("Person", result, instanceOf(Person.class));
		assertThat("Emil Jannings", ((Person) result).getId(), equalTo("1928"));
	}

	// First by descending == Max
	@Test
	public void findFirstIdByFirstnameOrderByIdDesc() {
		Object result = this.personRepository.findFirstIdByFirstnameOrderByIdDesc("James");

		assertThat("Last Winner", result, notNullValue());
		assertThat("Person", result, instanceOf(Person.class));
		assertThat("James Cagney", ((Person) result).getId(), equalTo("1942"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findByFirstname() {
		List<Person> matches = this.personRepository.findByFirstname("James");
		assertThat("1940 and 1942", matches.size(), equalTo(2));
		assertThat("1940 and 1942", matches,
				containsInAnyOrder(hasProperty("lastname", equalTo("Cagney")), hasProperty("lastname", equalTo("Stewart"))));

		matches = this.personRepository.findByFirstname("Bing");
		assertThat("1944", matches.size(), equalTo(1));
		assertThat("1944", matches.get(0).getLastname(), equalTo("Crosby"));
	}

	@Test
	public void queryAnnotation() {
		List<Person> matches = this.personRepository.peoplewiththeirFirstNameIsJames();
		assertThat("1940 and 1942", matches.size(), equalTo(2));
		assertThat("1940 and 1942", matches,
				containsInAnyOrder(hasProperty("lastname", equalTo("Cagney")), hasProperty("lastname", equalTo("Stewart"))));

	}

	@Test
	public void queryAnnotationWithOneParameter() {
		List<Person> matches = this.personRepository.peoplewiththeirFirstName("Bing");
		assertThat("1944", matches.size(), equalTo(1));
		assertThat("1944", matches.get(0).getLastname(), equalTo("Crosby"));
	}

	@Test
	public void queryAnnotationWithMultipleParameter() {
		List<Person> matches = this.personRepository.peoplewithFirstAndLastName("James", "Stewart");
		assertThat("1940", matches.size(), equalTo(1));
		assertThat("1940", matches.get(0).getId(), equalTo("1940"));
	}


	@SuppressWarnings("unchecked")
	@Test
	public void findBy_Firstname_And_Lastname() {
		List<Person> matches = this.personRepository.findByFirstname_AndLastname("James", "Stewart");
		assertThat("1940", matches.size(), equalTo(1));
		assertThat("1940", matches.get(0).getId(), equalTo("1940"));

		matches = this.personRepository.findByFirstname_AndLastname("Gary", "Cooper");
		assertThat("1941 and 1952", matches.size(), equalTo(2));
		assertThat("1941 and 1952", matches,
				containsInAnyOrder(hasProperty("id", equalTo("1941")), hasProperty("id", equalTo("1952"))));

		matches = this.personRepository.findByFirstname_AndLastname("James", "");
		assertThat("Mismatch on lastname", matches.size(), equalTo(0));

		matches = this.personRepository.findByFirstname_AndLastname("", "Stewart");
		assertThat("Mismatch on firstname", matches.size(), equalTo(0));
	}

	// Firstname and lastname in different order when arguments compared to method name
	@Test
	public void findByFirstnameOrLastname() {
		List<Person> matches = this.personRepository.findByFirstnameOrLastname("Niven", null);
		assertThat("1958", matches.size(), equalTo(1));
		assertThat("1958", matches.get(0).getFirstname(), equalTo("David"));

		matches = this.personRepository.findByFirstnameOrLastname("Heston", "");
		assertThat("1959", matches.size(), equalTo(1));
		assertThat("1959", matches.get(0).getFirstname(), equalTo("Charlton"));

		matches = this.personRepository.findByFirstnameOrLastname("Lancaster", "Hazelcast");
		assertThat("1960", matches.size(), equalTo(1));
		assertThat("1960", matches.get(0).getFirstname(), equalTo("Burt"));

		matches = this.personRepository.findByFirstnameOrLastname("Schell", "Maximilian");
		assertThat("1961", matches.size(), equalTo(1));
		assertThat("1961", matches.get(0).getId(), equalTo("1961"));

		matches = this.personRepository.findByFirstnameOrLastname("Hazelcast", "Gregory");
		assertThat("1962", matches.size(), equalTo(1));
		assertThat("1962", matches.get(0).getLastname(), equalTo("Peck"));

		matches = this.personRepository.findByFirstnameOrLastname("", "Sidney");
		assertThat("1963", matches.size(), equalTo(1));
		assertThat("1963", matches.get(0).getLastname(), equalTo("Poitier"));

		matches = this.personRepository.findByFirstnameOrLastname(null, "Rex");
		assertThat("1964", matches.size(), equalTo(1));
		assertThat("1964", matches.get(0).getLastname(), equalTo("Harrison"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findByFirstnameGreaterThan() {
		List<Person> matches = this.personRepository.findByFirstnameGreaterThan("U");

		assertThat("Wallace, 3xWilliam, Victor & Yul", matches.size(), equalTo(6));
		assertThat("Wallace, 3xWilliam, Victor & Yul", matches,
				hasItems(allOf(hasProperty("firstname", equalTo("Wallace")), hasProperty("lastname", equalTo("Beery"))),
						allOf(hasProperty("firstname", equalTo("Warner")), hasProperty("lastname", equalTo("Baxter"))),
						allOf(hasProperty("firstname", equalTo("William")), hasProperty("lastname", equalTo("Holden"))),
						allOf(hasProperty("firstname", equalTo("William")), hasProperty("lastname", equalTo("Hurt"))),
						allOf(hasProperty("firstname", equalTo("Victor")), hasProperty("lastname", equalTo("McLaglen"))),
						allOf(hasProperty("firstname", equalTo("Yul")), hasProperty("lastname", equalTo("Brynner")))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findByFirstnameLike() {
		List<Person> matches = this.personRepository.findByFirstnameLike("H%y");

		assertThat("Wallace, 3xWilliam, Victor & Yul", matches.size(), equalTo(2));
		assertThat("Wallace, 3xWilliam, Victor & Yul", matches,
				hasItems(allOf(hasProperty("firstname", equalTo("Humphrey")), hasProperty("lastname", equalTo("Bogart"))),
						allOf(hasProperty("firstname", equalTo("Henry")), hasProperty("lastname", equalTo("Fonda")))));
	}

	@Test
	public void findByFirstnameOrderById() {
		List<Person> matches = this.personRepository.findByFirstnameOrderById("Robert");

		assertThat("Donat, De Niro, Duvall", matches.size(), equalTo(3));
		assertThat("1939", matches.get(0).getLastname(), equalTo("Donat"));
		assertThat("1980", matches.get(1).getLastname(), equalTo("De Niro"));
		assertThat("1983", matches.get(2).getLastname(), equalTo("Duvall"));
	}

	@Test
	public void findByLastnameOrderByIdAsc() {
		List<Person> matches = this.personRepository.findByLastnameOrderByIdAsc("Day-Lewis");

		assertThat("Three times winner", matches.size(), equalTo(3));
		assertThat("Daniel", matches.get(0).getId(), equalTo("1989"));
		assertThat("Day", matches.get(1).getId(), equalTo("2007"));
		assertThat("Lewis", matches.get(2).getId(), equalTo("2012"));
	}

	@Test
	public void findByFirstnameOrderByLastnameDesc() {
		List<Person> matches = this.personRepository.findByFirstnameOrderByLastnameDesc("Jack");

		assertThat("Lemmon x1, Nicholson x2", matches.size(), equalTo(3));
		assertThat("1975 or 1997", matches.get(0), hasProperty("lastname", equalTo("Nicholson")));
		assertThat("1975 or 1997 again", matches.get(1), hasProperty("lastname", equalTo("Nicholson")));
		assertThat("1973", matches.get(2), hasProperty("lastname", equalTo("Lemmon")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findByFirstname_with_sort() {
		List<Person> matches = this.personRepository.findByFirstname("Jack", null);
		assertThat("NULL 2 x Nicholson and 1 x Lemmon", matches.size(), equalTo(3));
		assertThat("NULL sort", matches, containsInAnyOrder(hasProperty("id", equalTo("1973")),
				hasProperty("id", equalTo("1975")), hasProperty("id", equalTo("1997"))));

		Sort sort = new Sort(Sort.Direction.DESC, "id");
		matches = this.personRepository.findByFirstname("Jack", sort);

		assertThat("DESC 2 x Nicholson and 1 x Lemmon", matches.size(), equalTo(3));
		assertThat("DESC 1st", matches.get(0).getId(), equalTo("1997"));
		assertThat("DESC 2nd", matches.get(1).getId(), equalTo("1975"));
		assertThat("DESC 3rd", matches.get(2).getId(), equalTo("1973"));

		sort = new Sort(Sort.Direction.ASC, "id");
		matches = this.personRepository.findByFirstname("Jack", sort);

		assertThat("ASC 2 x Nicholson and 1 x Lemmon", matches.size(), equalTo(3));
		assertThat("ASC 1st", matches.get(0).getId(), equalTo("1973"));
		assertThat("ASC 2nd", matches.get(1).getId(), equalTo("1975"));
		assertThat("ASC 3rd", matches.get(2).getId(), equalTo("1997"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findByLastnameIgnoreCase() {
		List<Person> matches = this.personRepository.findByLastnameIgnoreCase("hAnKs");

		assertThat("Tom Hanks", matches.size(), equalTo(2));
		assertThat("1993 & 1994", matches,
				containsInAnyOrder(hasProperty("id", equalTo("1993")), hasProperty("id", equalTo("1994"))));
	}

	@Test
	public void findByLastnameNotNull() {
		Sort sort = new Sort(Sort.DEFAULT_DIRECTION, "firstname");

		List<Person> matches = this.personRepository.findByLastnameNotNull(sort);
		int len = matches.size();

		assertThat("Everyone returned", len, equalTo(Oscars.bestActors.length));
		assertThat("First firstname - Adrien Brody", matches.get(0).getFirstname(), equalTo("Adrien"));
		assertThat("Last firstname - Yul Brynner", matches.get(len - 1).getFirstname(), equalTo("Yul"));
	}

	@Test
	public void findFirst3ByOrderByFirstnameAsc() {
		List<Person> matches = this.personRepository.findFirst3ByOrderByFirstnameAsc();

		assertThat("First, 2002, Brody", matches.get(0).getFirstname(), equalTo("Adrien"));
		assertThat("Second, 1992, Pacino", matches.get(1).getFirstname(), equalTo("Al"));
		assertThat("Third, 1957, Guinness", matches.get(2).getFirstname(), equalTo("Alec"));
		assertThat("Three matches", matches.size(), equalTo(3));
	}

	@Test
	public void findFirst30ByOrderByFirstnameDescLastnameAsc() {
		List<Person> matches = this.personRepository.findFirst30ByOrderByFirstnameDescLastnameAsc();

		assertThat("First thirty", matches.size(), equalTo(30));
		assertThat("1st - Yul Brynner", matches.get(0).getId(), equalTo("1956"));
		assertThat("2nd - William Holden", matches.get(1).getId(), equalTo("1953"));
		assertThat("3rd - William Hurt", matches.get(2).getId(), equalTo("1985"));
		assertThat("30th - Nicolas Cage", matches.get(29).getId(), equalTo("1995"));
	}

	@Test
	public void findFirst4By() {
		AtomicInteger count = new AtomicInteger();

		try (Stream<Person> matches = this.personRepository.findFirst4By()) {

			matches.forEach(match -> {
				count.incrementAndGet();
			});
		}

		assertThat("Any four", count.get(), equalTo(4));
	}

	@Test
	public void streamByLastnameGreaterThanEqual() {
		AtomicInteger count = new AtomicInteger();

		try (Stream<Person> matches = this.personRepository.streamByLastnameGreaterThanEqual("Wayne")) {

			matches.forEach(match -> {

				assertThat("Wayne or Whitaker", match,
						anyOf(hasProperty("lastname", equalTo("Wayne")), hasProperty("lastname", equalTo("Whitaker"))));

				count.incrementAndGet();
			});
		}

		assertThat("Wayne and Whitaker", count.get(), equalTo(2));
	}

	@Test
	public void findByIdGreaterThanEqualAndFirstnameGreaterThanAndFirstnameLessThanEqual() {
		String[] LASTNAMES = { "Bridges", "Dujardin", "Foxx", "Irons", "Nicholson" };
		Set<String> lastnames = new TreeSet<>(Arrays.asList(LASTNAMES));

		Pageable pageRequest = new PageRequest(PAGE_0, SIZE_1);
		Slice<Person> pageResponse = this.personRepository
				.findByIdGreaterThanEqualAndFirstnameGreaterThanAndFirstnameLessThanEqual("1990", "I", "K", pageRequest);
		int slice = 0;
		while (pageResponse != null) {

			assertThat("Slice " + slice + ", has content", pageResponse.hasContent(), equalTo(true));

			List<Person> pageMatches = pageResponse.getContent();

			assertThat("Slice " + slice + ", contains a person", pageMatches.size(), equalTo(1));

			String lastname = pageMatches.get(0).getLastname();
			assertThat("Slice " + slice + ", lastname " + lastname + " expected", lastnames.contains(lastname),
					equalTo(true));
			lastnames.remove(lastname);

			if (pageResponse.hasNext()) {
				pageRequest = pageResponse.nextPageable();
				pageResponse = this.personRepository
						.findByIdGreaterThanEqualAndFirstnameGreaterThanAndFirstnameLessThanEqual("1990", "I", "K", pageRequest);
			} else {
				pageResponse = null;
			}
			slice++;
		}

		assertThat("All lastnames matched", lastnames, hasSize(0));
	}

	@Test
	public void findByLastname() {
		String[] YEARS = { "1989", "2007", "2012" };
		Set<String> years = new TreeSet<>(Arrays.asList(YEARS));

		for (int page = 0; page < YEARS.length; page++) {
			Pageable pageRequest = new PageRequest(page, SIZE_1);

			Page<Person> pageResponse = this.personRepository.findByLastname("Day-Lewis", pageRequest);
			assertThat("Page " + page + ", has content", pageResponse.hasContent(), equalTo(true));

			List<Person> pageMatches = pageResponse.getContent();

			assertThat("Page " + page + ", one of three oscars", pageMatches.size(), equalTo(1));
			String year = pageMatches.get(0).getId();

			assertThat("Page " + page + ", year " + year + " expected", years.contains(year), equalTo(true));
			years.remove(year);
		}

		assertThat("All years matched", years, hasSize(0));
	}

	@Test
	public void findByOrderByLastnameDesc() {
		int expectedNumberOfPages = Oscars.bestActors.length / SIZE_5 + 1;
		int pagesRetrieved = 0;
		String previousLastname = null;

		Pageable pageRequest = new PageRequest(PAGE_0, SIZE_5);
		Page<Person> pageResponse = this.personRepository.findByOrderByLastnameDesc(pageRequest);
		while (pageResponse != null) {

			assertThat("Not more than expected pages", pagesRetrieved, lessThan(expectedNumberOfPages));
			pagesRetrieved++;

			assertThat("Page " + pagesRetrieved + " has content", pageResponse.hasContent(), equalTo(true));
			List<Person> pageContent = pageResponse.getContent();
			assertThat("Page " + pagesRetrieved + " content not null", pageContent, notNullValue());
			assertThat("Page " + pagesRetrieved + " content not empty", pageContent.size(), greaterThan(0));

			assertThat("Page " + pagesRetrieved + ", this page number", pageResponse.getNumber(),
					equalTo(pagesRetrieved - 1));
			assertThat("Page " + pagesRetrieved + ", total page number", pageResponse.getTotalPages(),
					equalTo(expectedNumberOfPages));

			assertThat("Page " + pagesRetrieved + ", item count v content", pageResponse.getNumberOfElements(),
					equalTo(pageContent.size()));
			if (pagesRetrieved != expectedNumberOfPages) {
				assertThat("Page " + pagesRetrieved + ", item count", pageResponse.getNumberOfElements(), equalTo(5));
			} else {
				assertThat("Page " + pagesRetrieved + ", item count", pageResponse.getNumberOfElements(), greaterThan(0));
				assertThat("Page " + pagesRetrieved + ", item count", pageResponse.getNumberOfElements(),
						lessThanOrEqualTo(SIZE_5));
			}
			assertThat("Page " + pagesRetrieved + ", total item count", pageResponse.getTotalElements(),
					equalTo(new Long(Oscars.bestActors.length)));

			for (Person person : pageContent) {
				if (previousLastname != null) {
					assertThat("Descending lastname", person.getLastname(), lessThanOrEqualTo(previousLastname));
				}
				previousLastname = new String(person.getLastname());
			}

			pageRequest = pageResponse.nextPageable();
			if (pagesRetrieved == expectedNumberOfPages) {
				assertThat("Page " + pagesRetrieved + ", is last", pageResponse.hasNext(), equalTo(false));
				assertThat("Page " + pagesRetrieved + ", no following page", pageRequest, nullValue());
				pageResponse = null;
			} else {
				assertThat("Page " + pagesRetrieved + ", not last", pageResponse.hasNext(), equalTo(true));
				assertThat("Page " + pagesRetrieved + ", has following page", pageRequest, notNullValue());
				pageResponse = this.personRepository.findByOrderByLastnameDesc(pageRequest);
			}
		}

		assertThat("Not less than expected pages", pagesRetrieved, equalTo(expectedNumberOfPages));
	}

	@Test
	public void findByIdLike() {
		String PATTERN = "19%0";
		String[] EXPECTED_YEARS = { "1930", "1940", "1950", "1960", "1970", "1980", "1990" };
		Set<String> expectedYears = new TreeSet<>(Arrays.asList(EXPECTED_YEARS));

		Pageable pageRequest = new PageRequest(PAGE_0, SIZE_5);
		Slice<Person> pageResponse = this.personRepository.findByIdLike(PATTERN, pageRequest);
		int slice = 0;
		while (pageResponse != null) {

			assertThat("Slice " + slice + ", has content", pageResponse.hasContent(), equalTo(true));
			List<Person> pageMatches = pageResponse.getContent();
			assertThat("Slice " + slice + ", contains data", pageMatches.size(), greaterThan(0));

			pageMatches.forEach(person -> {
				String year = person.getId();
				assertThat("Year " + year + " expected", expectedYears.contains(year), equalTo(true));
				expectedYears.remove(year);
			});

			if (pageResponse.hasNext()) {
				assertThat("Slice " + slice + ", is full", pageMatches.size(), equalTo(SIZE_5));

				pageRequest = pageResponse.nextPageable();
				pageResponse = this.personRepository.findByIdLike(PATTERN, pageRequest);

				assertThat("Slice " + slice + ", expected next slice", pageResponse, notNullValue());
			} else {
				pageResponse = null;
			}
			slice++;
		}

		assertThat("All years matched", expectedYears, hasSize(0));
	}

	@Test
	public void findByFirstnameContains() {
		List<Person> matches = this.personRepository.findByFirstnameContains("ll");

		assertThat("Wallace, 2xWilliam, Russell", matches.size(), equalTo(4));
		assertThat("Wallace, 2xWilliam, Russell", matches,
				hasItems(allOf(hasProperty("firstname", equalTo("Wallace")), hasProperty("lastname", equalTo("Beery"))),
						allOf(hasProperty("firstname", equalTo("William")), hasProperty("lastname", equalTo("Holden"))),
						allOf(hasProperty("firstname", equalTo("William")), hasProperty("lastname", equalTo("Hurt"))),
						allOf(hasProperty("firstname", equalTo("Russell")), hasProperty("lastname", equalTo("Crowe")))));
	}

	@Test
	public void findByFirstnameContainsAndLastnameStartsWithAllIgnoreCase() {
		List<Person> matches = this.personRepository.findByFirstnameContainsAndLastnameStartsWithAllIgnoreCase("En", "tR");

		assertThat("2xSpencer", matches.size(), equalTo(2));
		assertThat("2xSpencer", matches,
				hasItems(allOf(hasProperty("firstname", equalTo("Spencer")), hasProperty("lastname", equalTo("Tracy")))));

	}

	@Test
	public void countByIdAfter() {
		Long count = this.personRepository.countByIdAfter("2000");
		assertThat("> 2000 & <= 2015 ", count, equalTo(15L));
	}

	@Test
	public void countByIdBetween() {
		Long count = this.personRepository.countByIdBetween("1959", "1962");
		assertThat("between 1959 and 1962", count, equalTo(4L));
	}

	@Test
	public void findByFirstnameIn() {
		List<Person> matches = this.personRepository.findByFirstnameIn(Arrays.asList("Jack", "Robert"));

		assertThat("3xJack, 3xRobert", matches.size(), equalTo(6));
		assertThat("3xJack, 3xRobert", matches,
				hasItems(allOf(hasProperty("firstname", equalTo("Jack")), hasProperty("lastname", equalTo("Lemmon"))),
						allOf(hasProperty("firstname", equalTo("Jack")), hasProperty("lastname", equalTo("Nicholson"))),
						allOf(hasProperty("firstname", equalTo("Jack")), hasProperty("lastname", equalTo("Nicholson"))),
						allOf(hasProperty("firstname", equalTo("Robert")), hasProperty("lastname", equalTo("Donat"))),
						allOf(hasProperty("firstname", equalTo("Robert")), hasProperty("lastname", equalTo("De Niro"))),
						allOf(hasProperty("firstname", equalTo("Robert")), hasProperty("lastname", equalTo("Duvall")))));

	}

	@Test
	public void findByFirstnameEndsWithAndLastnameNotIn() {
		List<Person> matches = this.personRepository.findByFirstnameEndsWithAndLastnameNotIn("on", Arrays.asList("Heston", "Brando"));

		assertThat("Jon", matches.size(), equalTo(1));
		assertThat("Jon", matches,
				hasItems(allOf(hasProperty("firstname", equalTo("Jon")), hasProperty("lastname", equalTo("Voight")))));

	}
}
