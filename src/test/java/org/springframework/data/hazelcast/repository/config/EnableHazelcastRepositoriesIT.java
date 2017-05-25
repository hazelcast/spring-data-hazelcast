/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hazelcast.repository.config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import test.utils.TestConstants;
import test.utils.InstanceHelper;
import test.utils.domain.Person;
import test.utils.repository.standard.PersonRepository;

/**
 * <P>
 * Integration test for Hazelcast repositories.
 * </P>
 * <P>
 * Domain class {@link Person} and repository {@link PersonRepository} made into outer classes for use in other tests.
 * </P>
 * 
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Neil Stevenson
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { InstanceHelper.class })
@ActiveProfiles(TestConstants.SPRING_TEST_PROFILE_SINGLETON)
@DirtiesContext
public class EnableHazelcastRepositoriesIT {

	@Autowired PersonRepository repo;

	@Test
	public void shouldEnableKeyValueRepositoryCorrectly() {

		assertThat(repo, notNullValue());

		Person person = new Person();
		person.setFirstname("foo");
		repo.save(person);

		List<Person> result = repo.findByFirstname("foo");

		assertThat(result, hasSize(1));
		assertThat(result.get(0).getFirstname(), is("foo"));
	}

}
