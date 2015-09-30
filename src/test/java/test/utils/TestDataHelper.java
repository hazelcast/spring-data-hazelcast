package test.utils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <P>Common processing for integration tests.
 * </P>
 * <P>Load the {@code Person} {@link IMap} with data prior to a test,
 * delete it after.
 * </P>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={InstanceHelper.class})
@DirtiesContext
public abstract class TestDataHelper {
	@Autowired
	protected HazelcastInstance hazelcastInstance;
	
    protected IMap<String, Person> personMap;

    /* Use Hazelcast directly, minimise reliance on Spring as the object is
     * to test Spring encapsulation of Hazelcast.
     */
    @Before
    public void setUp() {
    	assertThat("Correct Hazelcast instance", this.hazelcastInstance.getName(), equalTo(Constants.HAZELCAST_TEST_INSTANCE_NAME));

    	this.personMap = this.hazelcastInstance.getMap(Constants.PERSON_MAP_NAME);
        assertThat("No test data left behind by other tests", this.personMap.size(), equalTo(0));

        for(int i=0 ; i<Oscars.bestActors.length ; i++) {
            Person person = new Person();
            
            person.setId(Integer.toString((int)Oscars.bestActors[i][0]));
            person.setFirstname(Oscars.bestActors[i][1].toString());
            person.setLastname(Oscars.bestActors[i][2].toString());
            
            this.personMap.put(person.getId(), person);
        }
    }

    @After
    public void tearDown() {
        this.personMap.clear();
    }

}
