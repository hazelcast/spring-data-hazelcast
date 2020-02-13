/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.hazelcast.topology;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import test.utils.TestConstants;
import test.utils.domain.Person;

import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * <p>
 * Run the {@link AbstractTopologyIT} tests with the client-server profile.
 * </P>
 * <p>
 * Spring Data Hazelcast uses the client, so the tests examine the server content to confirm client operations are sent
 * there.
 * </P>
 *
 * @author Neil Stevenson
 */
@ActiveProfiles(TestConstants.SPRING_TEST_PROFILE_CLIENT_SERVER)
public class ClientServerIT
        extends AbstractTopologyIT {

    /* Test data loaded into the client should exist on the
     * server.
     */
    @Test
    public void notJavaDuke() {
        String FIRST_NAME_IS_JOHN = "John";
        String LAST_NAME_IS_WAYNE = "Wayne";
        String NINETEEN_SIXTY_NINE = "1969";

        Predicate<?, ?> predicate = Predicates
                .and(Predicates.equal("firstname", FIRST_NAME_IS_JOHN), Predicates.equal("lastname", LAST_NAME_IS_WAYNE));

        // Force operation to server's content, not remote
        Set<String> localKeySet = super.server_personMap.localKeySet((Predicate<String, Person>) predicate);

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
