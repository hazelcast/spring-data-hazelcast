package test.utils;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hazelcast.repository.config.Constants;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.utils.domain.Makeup;
import test.utils.domain.Movie;
import test.utils.domain.Person;
import test.utils.domain.Song;

import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;

/**
 * <P>
 * Common processing for integration tests.
 * </P>
 * <P>
 * Test data is based around
 * <a href=https://en.wikipedia.org/wiki/Academy_Awards>the Oscars</a>.
 * </P>
 * <P>
 * Load the {@code Movie} {@code IMap} with the Oscar winners for best movies,
 * {@code Person} with the Oscar winners for best actor, and {@code Song}
 * with the best theme songs.
 * </P>
 *
 * @author Neil Stevenson
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { InstanceHelper.class })
@DirtiesContext
public abstract class TestDataHelper {
	@Autowired protected HazelcastInstance hazelcastInstance;

	protected IMap<String, Makeup>  makeupMap;
	protected IMap<String, Movie>  movieMap;
	protected IMap<String, Person> personMap;
	protected IMap<String, Song>   songMap;
	
	/* Use Hazelcast directly, minimise reliance on Spring as the object is
	 * to test Spring encapsulation of Hazelcast.
	 */
	@Before
	public void setUp() {
		assertThat("Correct Hazelcast instance", this.hazelcastInstance.getName(),
				equalTo(Constants.HAZELCAST_INSTANCE_NAME));

		checkMapsEmpty("setUp");

		this.makeupMap = this.hazelcastInstance.getMap(TestConstants.MAKEUP_MAP_NAME);
		loadMakeup(this.makeupMap);

		this.movieMap = this.hazelcastInstance.getMap(TestConstants.MOVIE_MAP_NAME);
		loadMovie(this.movieMap);
		
		this.personMap = this.hazelcastInstance.getMap(TestConstants.PERSON_MAP_NAME);
		loadPerson(this.personMap);
		
		this.songMap = this.hazelcastInstance.getMap(TestConstants.SONG_MAP_NAME);
		loadSong(this.songMap);
		
		checkMapsNotEmpty("setUp");

		/* As Hazelcast will create objects on demand, check no more are present
		 * than should be.
		 */
		Collection<DistributedObject> distributedObjects = this.hazelcastInstance.getDistributedObjects();
		assertThat("Correct number of distributed objects",
				distributedObjects.size(), equalTo(TestConstants.OSCAR_MAP_NAMES.length));
	}

	private void checkMapsEmpty(String phase) {
		for (String mapName : TestConstants.OSCAR_MAP_NAMES) {
			IMap<String, ?> iMap = this.hazelcastInstance.getMap(mapName);
			assertThat(phase + "(): No test data left behind by previous tests in '" + iMap.getName() + "'", 
					iMap.size(), equalTo(0));
		}
	}

	private void checkMapsNotEmpty(String phase) {
		for (String mapName : TestConstants.OSCAR_MAP_NAMES) {
			IMap<String, ?> iMap = this.hazelcastInstance.getMap(mapName);
			assertThat(phase + "(): Test data has been loaded into '" + iMap.getName() + "'", 
					iMap.size(), greaterThan(0));
		}
	}

	private void loadMakeup(IMap<String, Makeup> akeupMap) {
		for (int i = 0; i < Oscars.bestMakeUp.length; i++) {
			Makeup makeup = new Makeup();

			makeup.setId(Integer.toString((int) Oscars.bestMakeUp[i][0]));
			makeup.setFilmTitle(Oscars.bestMakeUp[i][1].toString());
			makeup.setArtistOrArtists(Oscars.bestMakeUp[i][2].toString());

			makeupMap.put(makeup.getId(), makeup);
		}
	}

	private void loadMovie(IMap<String, Movie> movieMap) {
		for (int i = 0; i < Oscars.bestPictures.length; i++) {
			Movie movie = new Movie();

			movie.setId(Integer.toString((int) Oscars.bestPictures[i][0]));
			movie.setTitle(Oscars.bestPictures[i][1].toString());

			movieMap.put(movie.getId(), movie);
		}
	}

	private void loadPerson(IMap<String, Person> personMap) {
		for (int i = 0; i < Oscars.bestActors.length; i++) {
			Person person = new Person();

			person.setId(Integer.toString((int) Oscars.bestActors[i][0]));
			person.setFirstname(Oscars.bestActors[i][1].toString());
			person.setLastname(Oscars.bestActors[i][2].toString());

			personMap.put(person.getId(), person);
		}
	}

	private void loadSong(IMap<String, Song> songMap) {
		for (int i = 0; i < Oscars.bestSongs.length; i++) {
			Song song = new Song();

			song.setId(Integer.toString((int) Oscars.bestSongs[i][0]));
			song.setTitle(Oscars.bestSongs[i][1].toString());

			songMap.put(song.getId(), song);
		}
	}

	@After
	public void tearDown() {
		for (String mapName : TestConstants.OSCAR_MAP_NAMES) {
			IMap<String, ?> iMap = this.hazelcastInstance.getMap(mapName);
			iMap.clear();
		}
		
		checkMapsEmpty("tearDown");

		Collection<DistributedObject> distributedObjects = this.hazelcastInstance.getDistributedObjects();
		
		for (DistributedObject distributedObject : distributedObjects) {
			assertThat(distributedObject.getName(), distributedObject, instanceOf(IMap.class));
			assertThat(distributedObject.getName(), isIn(TestConstants.OSCAR_MAP_NAMES));
		}

		assertThat("Correct number of distributed objects",
				distributedObjects.size(), equalTo(TestConstants.OSCAR_MAP_NAMES.length));

	}

}
