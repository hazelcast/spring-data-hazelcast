package test.utils.domain;

import org.springframework.data.keyvalue.annotation.KeySpace;

import test.utils.TestConstants;

/**
 * <P>A {@code Movie} is a kind of {@link MyTitle}, as the fields here are just the
 * year and the movie title.
 * </P>
 * <P>
 * Give an argument {@code @KeySpace} to name the map used, "{@code Movie}"
 * rather than the default fully class name.
 * </P>
 * 
 * @author Neil Stevenson
 */
@KeySpace(TestConstants.MOVIE_MAP_NAME)
public class Movie extends MyTitle {
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return "Movie [id=" + super.getId() + ", title=" + super.getTitle() + "]";
	}

}
