package org.springframework.data.hazelcast.topology;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

import test.utils.TestConstants;
import test.utils.domain.Person;

/**
 * <P>
 * Run the {@link AbstractTopologyIT} tests with the client-server profile.
 * </P>
 * <P>
 * Spring Data Hazelcast uses the client, so the tests examine the server content to confirm client operations are sent
 * there.
 * </P>
 *
 * @author Neil Stevenson
 */
@ActiveProfiles(TestConstants.SPRING_TEST_PROFILE_CLIENT_SERVER)
public class ClientServerIT extends AbstractTopologyIT {

	/* Test data loaded into the client should exist on the
	 * server.
	 */
	@Test
	public void notJavaDuke() {
		String FIRST_NAME_IS_JOHN = "John";
		String LAST_NAME_IS_WAYNE = "Wayne";
		String NINETEEN_SIXTY_NINE = "1969";

		Predicate<?, ?> predicate = Predicates.and(Predicates.equal("firstname", FIRST_NAME_IS_JOHN),
				Predicates.equal("lastname", LAST_NAME_IS_WAYNE));

		// Force operation to server's content, not remote
		Set<String> localKeySet = super.server_personMap.localKeySet(predicate);

		assertThat("Entry exists", localKeySet.size(), equalTo(1));
		String key = localKeySet.iterator().next();

		assertThat("Correct key", key, equalTo(NINETEEN_SIXTY_NINE));

		Person person = super.server_personMap.get(key);
		assertThat("Not invalidated", person, notNullValue());
		assertThat("@Id matches key", person.getId(), equalTo(key));
		assertThat("First name", person.getFirstname(), equalTo(FIRST_NAME_IS_JOHN));
		assertThat("Last name", person.getLastname(), equalTo(LAST_NAME_IS_WAYNE));
	}

}
