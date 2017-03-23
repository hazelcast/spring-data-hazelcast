package test.utils;

import test.utils.domain.Song;

/**
 * @author Neil Stevenson
 */
public class TestConstants {

	public static final String SPRING_TEST_PROFILE_CLIENT_SERVER = "client-server";
	public static final String SPRING_TEST_PROFILE_CLUSTER = "cluster";
	public static final String SPRING_TEST_PROFILE_SINGLETON = "singleton";

	public static final String MAKEUP_MAP_NAME = "Make-up";
	public static final String MOVIE_MAP_NAME = "Movie";
	public static final String PERSON_MAP_NAME = "Actors";
	public static final String SONG_MAP_NAME = Song.class.getCanonicalName();

	public static final String[] OSCAR_MAP_NAMES = { MAKEUP_MAP_NAME, MOVIE_MAP_NAME, PERSON_MAP_NAME, SONG_MAP_NAME };
}
