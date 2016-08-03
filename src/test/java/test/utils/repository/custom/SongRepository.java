package test.utils.repository.custom;

import java.util.List;

import test.utils.domain.Song;

/**
 * <P>Extend the {@link MyTitleRepository} for song titles,
 * inheriting some methods application to the base class {@link MyTitle}
 * and adding some methods specific to {@link Song} objects.
 * </P>
 * 
 * @author Neil Stevenson
 */
public interface SongRepository extends MyTitleRepository<Song, String> {

	/**
	 * <P>
	 *  This could be generic behaviour, but make specific
	 * to {@link SongRepository} for tests.
	 * </P>
	 *
	 * @param text A year
	 * @return Previous years
	 */
	public List<Song> findByIdLessThan(String text);

}
