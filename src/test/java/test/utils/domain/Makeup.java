package test.utils.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;
import test.utils.TestConstants;

import java.io.Serializable;

/**
 * <P>{@code Makeup} is for the prize for make-up and hair styling.
 * </P>
 * <P>
 * Give an argument {@code @KeySpace} to name the map used, "{@code Make-up}"
 * rather than the default fully class name.
 * </P>
 * 
 * @author Neil Stevenson
 */
@KeySpace(TestConstants.MAKEUP_MAP_NAME)
public class Makeup implements Comparable<Makeup>, Serializable {

	private static final long serialVersionUID = 1L;

	@Id private String id;
	private String filmTitle;
	private String artistOrArtists;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFilmTitle() {
		return filmTitle;
	}
	public void setFilmTitle(String filmTitle) {
		this.filmTitle = filmTitle;
	}
	public String getArtistOrArtists() {
		return artistOrArtists;
	}
	public void setArtistOrArtists(String artistOrArtists) {
		this.artistOrArtists = artistOrArtists;
	}
	@Override
	public String toString() {
		return "Makeup [id=" + id + ", filmTitle=" + filmTitle + ", artistOrArtists=" + artistOrArtists + "]";
	}
	
	// Sort by film title only
	@Override
	public int compareTo(Makeup that) {
		return this.filmTitle.compareTo(that.getFilmTitle());
	}

}
