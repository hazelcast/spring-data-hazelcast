package org.springframework.data.hazelcast.repository.custom;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import test.utils.TestDataHelper;
import test.utils.domain.Movie;
import test.utils.domain.Song;
import test.utils.repository.custom.MovieRepository;
import test.utils.repository.custom.MyTitleRepositoryFactoryBean;
import test.utils.repository.custom.SongRepository;
import test.utils.TestConstants;

/**
 * <P>
 * Test customized repository functionality.
 * </P>
 * <P>
 * {@link MovieRepository} and {@link SongRepository} both extend custom repository
 * base class {@link MyTitleRepository}. Test each has inherited custom and unique
 * behaviours.
 * </P>
 * <P>
 * In addition to package scanning for standard repositories in {@link TestDataHelper},
 * package scanning also constructs custom / non-standard repositories using a
 * special factory bean for tests only, {@link MyTitleRepositoryFactoryBean}.
 * </P>
 *
 * @author Neil Stevenson
 */
@ActiveProfiles(TestConstants.SPRING_TEST_PROFILE_SINGLETON)
public class CustomRepoIT extends TestDataHelper {
	private static final String YEAR_1939 = "1939";
	private static final String YEAR_1959 = "1959";
	private static final String YEAR_1993 = "1993";
	private static final String YEAR_1997 = "1997";

	@Resource private MovieRepository movieRepository;
	@Resource private SongRepository songRepository;
	
	/* Although what we're testing here is that we can execute a generic
	 * method, may as well be thorough and check the correctness of the
	 * logic for hyphens and apostrophes.
	 */
	@Test
	public void movieRepositoryGenericBehaviour() {
		long countGoneWithTheWind = this.movieRepository.wordsInTitle(YEAR_1939);
		long countBenHur = this.movieRepository.wordsInTitle(YEAR_1959);
		long countSchindlersList = this.movieRepository.wordsInTitle(YEAR_1993);
		long countTitanic = this.movieRepository.wordsInTitle(YEAR_1997);
		
		assertThat("4 words in 'Gone With The Wind'", countGoneWithTheWind, equalTo(4L));
		assertThat("2 words in 'Ben-Hur'", countBenHur, equalTo(2L));
		assertThat("2 words in 'Schindler's List'", countSchindlersList, equalTo(2L));
		assertThat("1 word in 'Titanic'", countTitanic, equalTo(1L));
	}
	
	@Test
	public void movieRepositorySpecificBehaviour() {
		String WORD = "The";
		
		int all=0;
		int allWithWord=0;
		
		Iterable<Movie> iterable = this.movieRepository.findAll();
		assertThat("iterable", iterable, not(nullValue()));
		Iterator<Movie> iterator = iterable.iterator();

		while (iterator.hasNext()) {
			Movie movie = iterator.next();
			all++;
			if (movie.getTitle().contains(WORD)) {
				allWithWord++;
			}
		}
		
		assertThat("Some movies found", all, greaterThan(0));
		assertThat(String.format("Some movies found with word '%s' in title title", WORD), 
				allWithWord, greaterThan(0));
		assertThat(String.format("Not all movies found with word '%s' in title title", WORD), 
				allWithWord, lessThan(all));

		Object result = this.movieRepository.findByTitleLike("%" + WORD + "%");
		
		assertThat("Result exists", result, not(nullValue()));
		assertThat("Result list", result, instanceOf(List.class));
		
		@SuppressWarnings("unchecked")
		List<Movie> resultList = (List<Movie>) result;
		
		assertThat("Result size", resultList.size(), equalTo(allWithWord));
	}
	
	@Test
	public void songRepositoryGenericBehaviour() {
		long count = this.songRepository.wordsInTitle(YEAR_1939);
		
		assertThat("3 words in 'Over The Rainbow'", count, equalTo(3L));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void songRepositorySpecificBehaviour() {
		Object result = this.songRepository.findByIdLessThan(YEAR_1939);
		
		assertThat("Result exists", result, not(nullValue()));
		assertThat("Result list", result, instanceOf(List.class));
		
		List<Song> resultList = (List<Song>) result;
		
		assertThat("First song prize was 1934", resultList.size(), equalTo(5));
		
		assertThat("1934 through 1938", resultList,
				containsInAnyOrder(hasProperty("id", equalTo("1934")), 
						hasProperty("id", equalTo("1935")),
						hasProperty("id", equalTo("1936")),
						hasProperty("id", equalTo("1937")),
						hasProperty("id", equalTo("1938"))
						));

	}
}
