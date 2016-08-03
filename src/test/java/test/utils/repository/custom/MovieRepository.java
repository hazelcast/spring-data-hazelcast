package test.utils.repository.custom;

import java.util.List;

import test.utils.domain.Movie;

/**
 * <P>Extend the {@link MyTitleRepository} for movie titles,
 * inheriting some methods application to the base class {@link MyTitle}
 * and adding some methods specific to {@link Movie} objects.
 * </P>
 * 
 * @author Neil Stevenson
 */
public interface MovieRepository extends MyTitleRepository<Movie, String> {

	/**
	 * <P>
	 *  This could be generic behaviour, but make specific
	 * to {@link MovieRepository} for tests.
	 * </P>
	 *
	 * @param text A word in the title
	 * @return Matches
	 */
	public List<Movie> findByTitleLike(String text);

}
