/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.data.hazelcast.topology;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.junit.Before;
import org.junit.Test;
import test.utils.TestConstants;
import test.utils.TestDataHelper;
import test.utils.domain.Person;
import test.utils.repository.standard.PersonRepository;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * <p>
 * A single Hazelcast instance is sufficient for most tests, but does not confirm the operations invoked via Spring Data
 * Hazelcast are distributed.
 * </P>
 * <p>
 * Define common tests to prove distribution, for use in more advanced topologies. The nature of these tests is to have
 * more than one Hazelcast instance, and to verify object state on an instance other than the one Spring is using.
 * </P>
 * <p>
 * Note that when multiple Hazelcast instances are used, they are all part of the same JVM. This is to simplify the
 * co-ordination of process starting and stopping, since networking isn't relevant to the tests.
 * </P>
 *
 * @author Neil Stevenson
 */
public abstract class AbstractTopologyIT
        extends TestDataHelper {

    // From the server that isn't Spring's Hazelcast instance
    protected IMap<String, Person> server_personMap = null;

    @Resource
    private PersonRepository personRepository;

    @Before
    public void setUp() {
        super.setUp();

        HazelcastInstance hazelcastServer = null;

        // Look for any instance apart from Spring's one
        hazelcastServer = Hazelcast.getHazelcastInstanceByName(TestConstants.SERVER_INSTANCE_NAME);

        assertThat("Server found", hazelcastServer, notNullValue());
        this.server_personMap = hazelcastServer.getMap(TestConstants.PERSON_MAP_NAME);
    }

    /* Data manipulated via the other instance should be visible
     * to the Spring instance, and vice versa.
     */
    @Test
    public void obiWanKenobi() {
        String NINETEEN_FIFTY_SEVEN = "1957";

        Person springBefore = this.personRepository.findById(NINETEEN_FIFTY_SEVEN).orElse(null);

        assertThat("1957 winner found", springBefore, notNullValue());
        assertThat("Firstname Alec", springBefore.getFirstname(), equalTo("Alec"));
        assertThat("Lastname Guinness", springBefore.getLastname(), equalTo("Guinness"));

        Person person = new Person();
        person.setFirstname("Obi-Wan");
        person.setLastname("Kenobi");
        person.setId(NINETEEN_FIFTY_SEVEN);

        this.server_personMap.put(person.getId(), person);

        Person springAfter = this.personRepository.findById(NINETEEN_FIFTY_SEVEN).orElse(null);

        assertThat("1957 winner found again", springAfter, notNullValue());
        assertThat("Firstname changed", springAfter.getFirstname(), not(equalTo("Alec")));
        assertThat("Lastname changed", springAfter.getLastname(), not(equalTo("Guinness")));

        this.personRepository.deleteById(NINETEEN_FIFTY_SEVEN);

        assertThat("Obi-Wan has vanished", this.server_personMap.containsKey(NINETEEN_FIFTY_SEVEN), equalTo(false));
    }

}
