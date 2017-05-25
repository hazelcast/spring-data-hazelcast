package org.springframework.data.hazelcast.repository;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.test.context.ActiveProfiles;

import test.utils.TestDataHelper;
import test.utils.domain.Person;
import test.utils.TestConstants;
import test.utils.Oscars;

/**
 * <P>
 * Downcast a {@link HazelcastRepository} into a {@link PagingAndSortingRepository} to test paging and sorting
 * additions.
 * </P>
 * <P>
 * Where possible, verify the repository against the underlying Hazelcast instance directly.
 * </P>
 *
 * @author Neil Stevenson
 */
@ActiveProfiles(TestConstants.SPRING_TEST_PROFILE_CLIENT_SERVER)
public class PagingSortingIT extends TestDataHelper {

	// PersonRepository is really a HazelcastRepository
	@Resource private PagingAndSortingRepository<Person, String> personRepository;

	// If no paging provided, everything is returned on a single page
	@Test
	public void pagingNull() {
		Page<Person> page = this.personRepository.findAll((Pageable) null);

		assertThat("Page returned for null input", page, notNullValue());

		List<Person> content = page.getContent();

		assertThat("First page is returned", page.getNumber(), equalTo(0));
		assertThat("First page count matches content", page.getNumberOfElements(), equalTo(content.size()));
		assertThat("First page has all content", new Long(page.getNumberOfElements()), equalTo(page.getTotalElements()));
		assertThat("First page has no upper limit", page.getSize(), equalTo(0));
		assertThat("First page has correct content count", page.getNumberOfElements(), equalTo(Oscars.bestActors.length));
		assertThat("First page is only page", page.getTotalPages(), equalTo(1));
	}

	@Test
	public void paging() {
		int PAGE_0 = 0;
		int PAGE_2 = 2;
		int SIZE_5 = 5;
		int SIZE_20 = 20;

		PageRequest thirdPageOf5Request = new PageRequest(PAGE_2, SIZE_5);
		Page<Person> thirdPageOf5Response = this.personRepository.findAll(thirdPageOf5Request);
		assertThat("11 onwards returned", thirdPageOf5Response, notNullValue());

		List<Person> thirdPageOf5Content = thirdPageOf5Response.getContent();
		assertThat("11-15 returned", thirdPageOf5Content.size(), equalTo(5));

		Pageable fourthPageOf5Request = thirdPageOf5Response.nextPageable();
		Page<Person> fourthPageOf5Response = this.personRepository.findAll(fourthPageOf5Request);
		assertThat("16 onwards returned", fourthPageOf5Response, notNullValue());

		List<Person> fourthPageOf5Content = fourthPageOf5Response.getContent();
		assertThat("16-20 returned", fourthPageOf5Content.size(), equalTo(5));

		PageRequest firstPageOf20Request = new PageRequest(PAGE_0, SIZE_20);
		Page<Person> firstPageOf20Response = this.personRepository.findAll(firstPageOf20Request);
		assertThat("1 onwards returned", firstPageOf20Response, notNullValue());

		List<Person> firstPageOf20Content = firstPageOf20Response.getContent();
		assertThat("1-20 returned", firstPageOf20Content.size(), equalTo(20));

		assertThat("11th", thirdPageOf5Content.get(0), equalTo(firstPageOf20Content.get(10)));
		assertThat("12th", thirdPageOf5Content.get(1), equalTo(firstPageOf20Content.get(11)));
		assertThat("13th", thirdPageOf5Content.get(2), equalTo(firstPageOf20Content.get(12)));
		assertThat("14th", thirdPageOf5Content.get(3), equalTo(firstPageOf20Content.get(13)));
		assertThat("15th", thirdPageOf5Content.get(4), equalTo(firstPageOf20Content.get(14)));
		assertThat("16th", fourthPageOf5Content.get(0), equalTo(firstPageOf20Content.get(15)));
		assertThat("17th", fourthPageOf5Content.get(1), equalTo(firstPageOf20Content.get(16)));
		assertThat("18th", fourthPageOf5Content.get(2), equalTo(firstPageOf20Content.get(17)));
		assertThat("19th", fourthPageOf5Content.get(3), equalTo(firstPageOf20Content.get(18)));
		assertThat("20th", fourthPageOf5Content.get(4), equalTo(firstPageOf20Content.get(19)));

		Set<String> ids = new TreeSet<>();
		firstPageOf20Content.forEach(person -> ids.add(person.getId()));

		assertThat("20 different years", ids.size(), equalTo(20));
	}

	@Test
	public void sortingNull() {
		Iterable<Person> iterable = this.personRepository.findAll((Sort) null);

		assertThat("Results returned", iterable, notNullValue());

		Iterator<Person> iterator = iterable.iterator();

		int count = 0;
		while (iterator.hasNext()) {
			count++;
			iterator.next();
		}

		assertThat("Correct number, order undefined", count, equalTo(Oscars.bestActors.length));
	}

	@Test
	public void sorting() {
		Sort sortAscending = new Sort(Sort.Direction.ASC, "firstname");
		Sort sortDescending = new Sort(Sort.Direction.DESC, "firstname");

		Iterable<Person> iterableAscending = this.personRepository.findAll(sortAscending);
		Iterable<Person> iterableDescending = this.personRepository.findAll(sortDescending);
		assertThat("Results returned ascending", iterableAscending, notNullValue());
		assertThat("Results returned descending", iterableDescending, notNullValue());

		Iterator<Person> iteratorAscending = iterableAscending.iterator();
		Iterator<Person> iteratorDescending = iterableDescending.iterator();
		assertThat("Not empty returned ascending", iteratorAscending.hasNext(), is(true));
		assertThat("Not empty returned descending", iteratorDescending.hasNext(), is(true));

		String previousFirstname = "";
		int count = 0;
		while (iteratorAscending.hasNext()) {
			Person person = iteratorAscending.next();
			assertThat("Firstname " + count + " ascending", person.getFirstname(), greaterThanOrEqualTo(previousFirstname));
			count++;
			previousFirstname = person.getFirstname();
		}
		assertThat("Everything found ascending", count, equalTo(Oscars.bestActors.length));

		assertThat("1956 winner, last firstname ascending", previousFirstname, equalTo("Yul"));

		while (iteratorDescending.hasNext()) {
			Person person = iteratorDescending.next();
			assertThat("Firstname " + count + " descending", person.getFirstname(), lessThanOrEqualTo(previousFirstname));
			count--;
			previousFirstname = person.getFirstname();
		}
		assertThat("Everything found decending", count, equalTo(0));

		assertThat("2002 winner, last firstname descending", previousFirstname, equalTo("Adrien"));
	}

}
