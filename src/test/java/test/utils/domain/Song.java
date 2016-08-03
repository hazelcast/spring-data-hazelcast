package test.utils.domain;

import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * <P>A {@code Song} is a kind of {@link MyTitle}, as the fields here are just the
 * year and the song title.
 * </P>
 * <P>
 * Don't provide a value for the {@code @KeySpace} annotation, so the map name
 * will default to the fully qualified class name, "{@code test.utils.Song}".
 * </P>
 * @author Neil Stevenson
 */
@KeySpace
public class Song extends MyTitle {
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return "Song [id=" + super.getId() + ", title=" + super.getTitle() + "]";
	}

}