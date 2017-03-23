package org.springframework.data.hazelcast.repository.support;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.repository.core.support.ReflectionEntityInformation;
import org.springframework.test.context.ActiveProfiles;
import test.utils.TestConstants;
import test.utils.Oscars;
import test.utils.TestDataHelper;
import test.utils.domain.Makeup;

/**
 * <P>
 * Validate the methods provided by {@link SimpleHazelcastRepository}
 * by creating a repository for {@link Makeup} which doesn't
 * have one created by Spring from a repository interface.
 * </P>
 *
 * @author Neil Stevenson
 */
@ActiveProfiles(TestConstants.SPRING_TEST_PROFILE_SINGLETON)
public class SimpleHazelcastRepositoryIT extends TestDataHelper {
	private static final String YEAR_1939 = "1939";
	private static final String YEAR_1941 = "1941";
	private static final String YEAR_1986 = "1986";
	private static final String YEAR_2009 = "2009";
	private static final String YEAR_9999 = "9999";
	private static final int PAGE_0 = 0;
	private static final int SIZE_10 = 10;

	private SimpleHazelcastRepository<Makeup, String> theRepository;

	@Autowired
	private KeyValueOperations keyValueOperations;
	
	@Before
	public void setUp_After_Super_SetUp() throws Exception {
		ReflectionEntityInformation<Makeup, String> entityInformation = new ReflectionEntityInformation<>(Makeup.class);

		this.theRepository = new SimpleHazelcastRepository<>(entityInformation, keyValueOperations);
	}
	
	@Test
	public void findAll_Sort() {
		Sort sort = new Sort(Sort.Direction.DESC, "id");
		
		Iterable<Makeup> iterable = this.theRepository.findAll(sort);
		assertThat("iterable", iterable, not(nullValue()));
		
		Iterator<Makeup> iterator = iterable.iterator();
		int count = 0;
		String previous = YEAR_9999;
		while (iterator.hasNext()) {
			Makeup makeup = iterator.next();
			assertThat("Makeup after " + previous, makeup, not(nullValue()));
			
			count++;
			String current = makeup.getId();

			assertThat("CCYY " + current, current.length(), equalTo(4));
			assertThat(current, lessThan(previous));
			
			previous = current;
		}
		
		assertThat(count, equalTo(Oscars.bestMakeUp.length));
	}

	@Test
	public void findAll_Pageable() {
		Set<String> yearsExpected = new TreeSet<>();
		for (Object[] datum : Oscars.bestMakeUp) {
			yearsExpected.add(datum[0].toString());
		}

		Pageable pageRequest = new PageRequest(PAGE_0, SIZE_10);
		
		Page<Makeup> pageResponse = this.theRepository.findAll(pageRequest);
		int page = 0;
		while (pageResponse != null) {
			assertThat("Page " + page + ", has content", pageResponse.hasContent(), equalTo(true));
			
			List<Makeup> makeups = pageResponse.getContent();
			assertThat("Page " + page + ", has makeups", makeups.size(), greaterThan(0));
			
			for (Makeup makeup : makeups) {
				assertThat(makeup.toString(), yearsExpected.contains(makeup.getId()), equalTo(true));
				yearsExpected.remove(makeup.getId());
			}
			
			
			if (pageResponse.hasNext()) {
				pageRequest = pageResponse.nextPageable();
				pageResponse = this.theRepository.findAll(pageRequest);
			} else {
				pageResponse = null;
			}
			page++;
		}
		
		assertThat("All years matched", yearsExpected, hasSize(0));
	}

	@Test
	public void count() {
		long count = this.theRepository.count();
		assertThat(count, equalTo(Long.valueOf(Oscars.bestMakeUp.length)));
	}

	@Test
	public void delete_ID() {
		assertThat("Exists before", this.makeupMap.containsKey(YEAR_2009), equalTo(true));
		
		this.theRepository.delete(YEAR_2009);
		
		assertThat("Does not exist after", this.makeupMap.containsKey(YEAR_2009), equalTo(false));
	}

	@Test
	public void delete_T() {
		Makeup starTrek = new Makeup();
		starTrek.setId(YEAR_2009);
		starTrek.setFilmTitle("Star Trek");
		starTrek.setArtistOrArtists("Barney Burman & Mindy Hall & Joel Harlow");

		assertThat("2009 exists before", this.makeupMap.containsKey(YEAR_2009), equalTo(true));

		this.theRepository.delete(starTrek);

		assertThat("2009 does not exist after", this.makeupMap.containsKey(YEAR_2009), equalTo(false));
	}

	@Test
	public void delete_Iterable_T() {
		Makeup starTrek = new Makeup();
		starTrek.setId(YEAR_2009);
		starTrek.setFilmTitle("Star Trek");
		starTrek.setArtistOrArtists("Barney Burman & Mindy Hall & Joel Harlow");
		
		Makeup theFly = new Makeup();
		theFly.setId(YEAR_1986);
		theFly.setFilmTitle("The Fly");
		theFly.setArtistOrArtists("Chris Walas & Stephan Dupuis");
		
		assertThat("2009 exists before", this.makeupMap.containsKey(YEAR_2009), equalTo(true));
		assertThat("1986 exists before", this.makeupMap.containsKey(YEAR_1986), equalTo(true));
		
		List<Makeup> list = new ArrayList<>();
		list.add(starTrek);
		list.add(theFly);
		
		this.theRepository.delete(list);

		assertThat("2009 does not exist after", this.makeupMap.containsKey(YEAR_2009), equalTo(false));
		assertThat("1986 does not exist after", this.makeupMap.containsKey(YEAR_1986), equalTo(false));
	}

	@Test
	public void deleteAll() {
		assertThat("Before", this.makeupMap.size(), equalTo(Oscars.bestMakeUp.length));
		
		this.theRepository.deleteAll();
		
		assertThat("After", this.makeupMap.size(), equalTo(0));
	}

	@Test
	public void exists_ID() {
		assertThat(YEAR_1986, this.theRepository.exists(YEAR_1986), equalTo(true));
		assertThat(YEAR_9999, this.theRepository.exists(YEAR_9999), equalTo(false));
	}

	@Test
	public void findAll() {
		
		Set<Makeup> expected = new TreeSet<>();
		for (Object[] datum : Oscars.bestMakeUp) {
			Makeup makeup = new Makeup();
			makeup.setId(datum[0].toString());
			makeup.setFilmTitle(datum[1].toString());
			makeup.setArtistOrArtists(datum[2].toString());
			
			expected.add(makeup);
		}
		
		Iterable<Makeup> iterable = this.theRepository.findAll();
		assertThat("iterable", iterable, not(nullValue()));
		
		Iterator<Makeup> iterator = iterable.iterator();
		while (iterator.hasNext()) {
			Makeup makeup = iterator.next();

			assertThat(makeup.toString(), expected.contains(makeup), equalTo(true));
			expected.remove(makeup);
		}
		
		assertThat("All expected accounted for", expected, equalTo(new TreeSet<Makeup>()));
	}

	@Test
	public void findAll_Iterable_ID() {
		List<String> years = new ArrayList<>();
		years.add(YEAR_1986);
		years.add(YEAR_2009);
		
		Iterable<Makeup> iterable = this.theRepository.findAll(years);
		assertThat("iterable", iterable, not(nullValue()));
		
		Iterator<Makeup> iterator = iterable.iterator();

		boolean found1986 = false;
		boolean found2009 = false;
		int count = 0;
		while (iterator.hasNext()) {
			Makeup makeup = iterator.next();
			count++;
			
			if (makeup.getId().equals(YEAR_1986)) {
				found1986 = true;
			}
			if (makeup.getId().equals(YEAR_2009)) {
				found2009 = true;
			}
		}

		assertThat(YEAR_1986, found1986, equalTo(true));
		assertThat(YEAR_2009, found2009, equalTo(true));
		assertThat("Only 1986 & 2009 found", count, equalTo(2));
	}

	@Test
	public void findOne_ID() {
		Makeup makeup = this.theRepository.findOne(YEAR_1986);
		
		assertThat("1986 found", makeup, not(nullValue()));
		assertThat(makeup.getId(), equalTo(YEAR_1986));
		assertThat(makeup.getFilmTitle(), equalTo("The Fly"));
		assertThat(makeup.getArtistOrArtists(), equalTo("Chris Walas & Stephan Dupuis"));
	}

	@Test
	public void save_T() {
		Makeup citizenKane = new Makeup();
		citizenKane.setId(YEAR_1941);
		citizenKane.setFilmTitle("Citizen Kane");
		citizenKane.setArtistOrArtists("Maurice Seiderman");

		assertThat("Before", this.makeupMap.size(), equalTo(Oscars.bestMakeUp.length));
		
		Makeup saved = this.theRepository.save(citizenKane);

		assertThat("Saved entry", saved, not(nullValue()));
		
		assertThat("After", this.makeupMap.size(), equalTo(Oscars.bestMakeUp.length + 1));
	}

	@Test
	public void save_Iterable_T() {
		Makeup goneWithTheWind = new Makeup();
		goneWithTheWind.setId(YEAR_1939);
		goneWithTheWind.setFilmTitle("Gone With The Wind");
		goneWithTheWind.setArtistOrArtists("Monte Westmore");

		Makeup citizenKane = new Makeup();
		citizenKane.setId(YEAR_1941);
		citizenKane.setFilmTitle("Citizen Kane");
		citizenKane.setArtistOrArtists("Maurice Seiderman");

		assertThat("Before", this.makeupMap.size(), equalTo(Oscars.bestMakeUp.length));
		
		List<Makeup> list = new ArrayList<>();
		list.add(citizenKane);
		list.add(goneWithTheWind);
		
		Iterable<Makeup> iterable = this.theRepository.save(list);
		assertThat("iterable", iterable, not(nullValue()));
		
		Iterator<Makeup> iterator = iterable.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			iterator.next();
			count++;
		}
		assertThat("Correct number saved", count, equalTo(list.size()));
		
		assertThat("After", this.makeupMap.size(), equalTo(Oscars.bestMakeUp.length + list.size()));
	}

}
