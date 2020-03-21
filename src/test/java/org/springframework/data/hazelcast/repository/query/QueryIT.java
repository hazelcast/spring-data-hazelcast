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

package org.springframework.data.hazelcast.repository.query;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.concurrent.ListenableFuture;
import test.utils.TestConstants;
import test.utils.TestDataHelper;
import test.utils.domain.Person;
import test.utils.repository.standard.PersonRepository;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * <p>
 * Test querying functionality.
 * </P>
 * <p>
 * Sorting with querying is only fully tested in methods where the collation sequence is parameter, and different
 * sequences can be tried.
 * </P>
 *
 * @author Neil Stevenson
 * @author Rafal Leszko
 */
@ActiveProfiles(TestConstants.SPRING_TEST_PROFILE_SINGLETON)
public class QueryIT
        extends TestDataHelper {
    private static final int PAGE_0 = 0;
    private static final int SIZE_1 = 1;
    private static final int SIZE_3 = 3;
    private static final int SIZE_5 = 5;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Resource
    private PersonRepository personRepository;

    // Count methods
    @Override
    public void setUp() {
        assertThat("Correct Hazelcast instance", this.hazelcastInstance.getName(), equalTo(TestConstants.CLIENT_INSTANCE_NAME));

        this.makeupMap = this.hazelcastInstance.getMap(TestConstants.MAKEUP_MAP_NAME);

        this.movieMap = this.hazelcastInstance.getMap(TestConstants.MOVIE_MAP_NAME);

        this.personMap = this.hazelcastInstance.getMap(TestConstants.PERSON_MAP_NAME);

        this.songMap = this.hazelcastInstance.getMap(TestConstants.SONG_MAP_NAME);

        checkMapsEmpty("setUp");
    }

    @Test
    public void countByFirstname() {
        Person jamesStewart = new Person();
        jamesStewart.setId("1940");
        jamesStewart.setFirstname("James");
        jamesStewart.setLastname("Stewart");
        this.personMap.put(jamesStewart.getId(), jamesStewart);

        Person jamesCagney = new Person();
        jamesCagney.setId("1942");
        jamesCagney.setFirstname("James");
        jamesCagney.setLastname("Cagney");
        this.personMap.put(jamesCagney.getId(), jamesCagney);

        Person bingCrosby = new Person();
        bingCrosby.setId("1944");
        bingCrosby.setFirstname("Bing");
        bingCrosby.setLastname("Crosby");
        this.personMap.put(bingCrosby.getId(), bingCrosby);


        Long count = this.personRepository.countByFirstname("James");

        assertThat("1940 and 1942", count, equalTo(2L));

        count = this.personRepository.countByFirstname("Bing");
        assertThat("1944", count, equalTo(1L));
    }

    @Test
    public void countByIdLessThanEqual() {
        Person jamesStewart = new Person();
        jamesStewart.setId("1928");
        jamesStewart.setFirstname("James");
        jamesStewart.setLastname("Stewart");
        this.personMap.put(jamesStewart.getId(), jamesStewart);

        Person jamesCagney = new Person();
        jamesCagney.setId("1942");
        jamesCagney.setFirstname("James");
        jamesCagney.setLastname("Cagney");
        this.personMap.put(jamesCagney.getId(), jamesCagney);

        Person bingCrosby = new Person();
        bingCrosby.setId("1944");
        bingCrosby.setFirstname("Bing");
        bingCrosby.setLastname("Crosby");
        this.personMap.put(bingCrosby.getId(), bingCrosby);
        Long count = this.personRepository.countByIdLessThanEqual("1940");

        assertThat("Oscars began in 1928", count, equalTo(1L));
    }

    @Test
    public void countByLastnameAllIgnoreCase() {
        String name = "Daniel";
        String secondName = "Day-Lewis";

        Person daniel2007 = new Person();
        daniel2007.setId("2007");
        daniel2007.setFirstname(name);
        daniel2007.setLastname(secondName);
        this.personMap.put(daniel2007.getId(), daniel2007);

        Person daniel2012 = new Person();
        daniel2012.setId("2012");
        daniel2012.setFirstname(name);
        daniel2012.setLastname(secondName);
        this.personMap.put(daniel2012.getId(), daniel2012);

        Person daniel989 = new Person();
        daniel989.setId("1989");
        daniel989.setFirstname(name);
        daniel989.setLastname(secondName);
        this.personMap.put(daniel989.getId(), daniel989);

        Long count = this.personRepository.countByLastnameAllIgnoreCase("day-LEWIS");

        assertThat("1989, 2007 and 2012", count, equalTo(3L));
    }

    @Test
    public void countByFirstnameOrLastnameAllIgnoreCase() {
        Person jamesStewart = new Person();
        jamesStewart.setId("1940");
        jamesStewart.setFirstname("James");
        jamesStewart.setLastname("Stewart");
        this.personMap.put(jamesStewart.getId(), jamesStewart);

        Person jamesCagney = new Person();
        jamesCagney.setId("1942");
        jamesCagney.setFirstname("James");
        jamesCagney.setLastname("Cagney");
        this.personMap.put(jamesCagney.getId(), jamesCagney);

        Person alecGuiness = new Person();
        alecGuiness.setId("1957");
        alecGuiness.setFirstname("Alec");
        alecGuiness.setLastname("Guinness");
        this.personMap.put(alecGuiness.getId(), alecGuiness);

        Long count = this.personRepository.countByFirstnameOrLastnameAllIgnoreCase("james", "GUINNESS");
        assertThat("1940, 1942 and 1957", count, equalTo(3L));
    }

    @Test
    public void countByFirstnameAndLastnameAllIgnoreCase() {
        Person jamesCagney = new Person();
        jamesCagney.setId("1942");
        jamesCagney.setFirstname("James");
        jamesCagney.setLastname("Cagney");
        this.personMap.put(jamesCagney.getId(), jamesCagney);

        Long count = this.personRepository.countByFirstnameAndLastnameAllIgnoreCase("JAMES", "CAGNEY");
        assertThat("1942", count, equalTo(1L));
    }

    @Test
    public void countByIdAfter() {
        Person jamesCagney = new Person();
        jamesCagney.setId("1942");
        jamesCagney.setFirstname("James");
        jamesCagney.setLastname("Cagney");
        this.personMap.put(jamesCagney.getId(), jamesCagney);

        Person philipSeymour = new Person();
        philipSeymour.setId("2005");
        philipSeymour.setFirstname("Philip");
        philipSeymour.setLastname("Seymour");
        this.personMap.put(philipSeymour.getId(), philipSeymour);

        Person forestWhitaker = new Person();
        forestWhitaker.setId("2006");
        forestWhitaker.setFirstname("Forest");
        forestWhitaker.setLastname("Whitaker");
        this.personMap.put(forestWhitaker.getId(), forestWhitaker);

        Long count = this.personRepository.countByIdAfter("2000");
        assertThat("> 2000 & <= 2015 ", count, equalTo(2L));
    }

    @Test
    public void countByIdBetween() {
        Person david = new Person();
        david.setId("1958");
        david.setFirstname("David");
        david.setLastname("Niven");
        this.personMap.put(david.getId(), david);

        Person burt = new Person();
        burt.setId("1960");
        burt.setFirstname("Burt");
        burt.setLastname("Lancaster");
        this.personMap.put(burt.getId(), burt);

        Person charlton = new Person();
        charlton.setId("1962");
        charlton.setFirstname("Charlton");
        charlton.setLastname("Heston");
        this.personMap.put(charlton.getId(), charlton);

        Person gene = new Person();
        gene.setId("1971");
        gene.setFirstname("Gene");
        gene.setLastname("Hackman");
        this.personMap.put(gene.getId(), gene);

        Long count = this.personRepository.countByIdBetween("1959", "1962");
        assertThat("between 1959 and 1962", count, equalTo(2L));
    }

    // Find methods

    @Test
    public void findByFirstnameOrLastnameIgnoreCase() {
        Person alecGuiness = new Person();
        alecGuiness.setId("1957");
        alecGuiness.setFirstname("Alec");
        alecGuiness.setLastname("Guinness");
        this.personMap.put(alecGuiness.getId(), alecGuiness);

        Person person = this.personRepository.findByFirstnameOrLastnameIgnoreCase("alec", "");
        assertThat("1957 firstname", person, nullValue());
        person = this.personRepository.findByFirstnameOrLastnameIgnoreCase("", "guinness");
        assertThat("1957 ignore case applied to lastname not firstname", person, notNullValue());
        assertThat("1957 lastname", person.getId(), equalTo("1957"));
    }

    @Test
    public void findByFirstnameIgnoreCaseOrLastname() {
        Person alecGuiness = new Person();
        alecGuiness.setId("1957");
        alecGuiness.setFirstname("Alec");
        alecGuiness.setLastname("Guinness");
        this.personMap.put(alecGuiness.getId(), alecGuiness);

        Person person = this.personRepository.findByFirstnameIgnoreCaseOrLastname("alec", "");
        assertThat("1957 ignore case applied to firstname not lastname", person, notNullValue());
        assertThat("1957 firstname", person.getId(), equalTo("1957"));
        person = this.personRepository.findByFirstnameIgnoreCaseOrLastname("", "guinness");
        assertThat("1957 lastname", person, nullValue());
    }

    @Test
    public void findByFirstnameOrLastnameAllIgnoreCase() {
        Person alecGuiness = new Person();
        alecGuiness.setId("1957");
        alecGuiness.setFirstname("Alec");
        alecGuiness.setLastname("Guinness");
        this.personMap.put(alecGuiness.getId(), alecGuiness);

        Person person = this.personRepository.findByFirstnameOrLastnameAllIgnoreCase("alec", "");
        assertThat("1957 firstname", person, notNullValue());
        assertThat("1957 firstname", person.getId(), equalTo("1957"));
        person = this.personRepository.findByFirstnameOrLastnameAllIgnoreCase("", "guinness");
        assertThat("1957 lastname", person, notNullValue());
        assertThat("1957 lastname", person.getId(), equalTo("1957"));
    }

    // First by ascending == Min
    @Test
    public void findFirstIdByOrderById() {
        Person warnerBaxter = new Person();
        warnerBaxter.setId("1929");
        warnerBaxter.setFirstname("Warner");
        warnerBaxter.setLastname("Baxter");
        this.personMap.put(warnerBaxter.getId(), warnerBaxter);

        Person emilJannings = new Person();
        emilJannings.setId("1928");
        emilJannings.setFirstname("Emil");
        emilJannings.setLastname("Jannings");
        this.personMap.put(emilJannings.getId(), emilJannings);

        Object result = this.personRepository.findFirstIdByOrderById();

        assertThat("First Winner", result, notNullValue());
        assertThat("Person", result, instanceOf(Person.class));
        assertThat("Emil Jannings", ((Person) result).getId(), equalTo("1928"));
    }

    // First by descending == Max
    @Test
    public void findFirstIdByFirstnameOrderByIdDesc() {
        Person jamesStewart = new Person();
        jamesStewart.setId("1940");
        jamesStewart.setFirstname("James");
        jamesStewart.setLastname("Stewart");
        this.personMap.put(jamesStewart.getId(), jamesStewart);

        Person jamesCagney = new Person();
        jamesCagney.setId("1942");
        jamesCagney.setFirstname("James");
        jamesCagney.setLastname("Cagney");
        this.personMap.put(jamesCagney.getId(), jamesCagney);

        Object result = this.personRepository.findFirstIdByFirstnameOrderByIdDesc("James");

        assertThat("Last Winner", result, notNullValue());
        assertThat("Person", result, instanceOf(Person.class));
        assertThat("James Cagney", ((Person) result).getId(), equalTo("1942"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findByFirstname() {
        Person jamesCagney = new Person();
        jamesCagney.setId("1942");
        jamesCagney.setFirstname("James");
        jamesCagney.setLastname("Cagney");
        this.personMap.put(jamesCagney.getId(), jamesCagney);

        Person jamesStewart = new Person();
        jamesStewart.setId("1940");
        jamesStewart.setFirstname("James");
        jamesStewart.setLastname("Stewart");
        this.personMap.put(jamesStewart.getId(), jamesStewart);

        List<Person> matches = this.personRepository.findByFirstname("James");
        assertThat("1940 and 1942", matches.size(), equalTo(2));
        assertThat("1940 and 1942", matches,
                containsInAnyOrder(hasProperty("lastname", equalTo("Cagney")), hasProperty("lastname", equalTo("Stewart"))));

        Person bingCrosby = new Person();
        bingCrosby.setId("1944");
        bingCrosby.setFirstname("Bing");
        bingCrosby.setLastname("Crosby");
        this.personMap.put(bingCrosby.getId(), bingCrosby);

        matches = this.personRepository.findByFirstname("Bing");
        assertThat("1944", matches.size(), equalTo(1));
        assertThat("1944", matches.get(0).getLastname(), equalTo("Crosby"));
    }

    @Test
    public void findByLastnameIsNull() {
        // given
        Person person = new Person();
        person.setId("johnId");
        person.setFirstname("John");
        this.personRepository.save(person);

        // when
        List<Person> result = this.personRepository.findByLastnameIsNull();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0), equalTo(person));
    }

    @Test
    public void findByIsChildTrue() {
        // given
        Person person = new Person();
        person.setId("johnId");
        person.setFirstname("John");
        person.setLastname("Porter");
        person.setChild(true);
        this.personRepository.save(person);

        // when
        List<Person> result = this.personRepository.findByIsChildTrue();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0), equalTo(person));
    }

    @Test
    public void findByLastnameRegex() {
        // given
        Person person1 = new Person();
        person1.setId("porterId");
        person1.setLastname("Porter");
        this.personRepository.save(person1);

        Person person2 = new Person();
        person2.setId("portersonId");
        person2.setLastname("Porterson");
        this.personRepository.save(person2);

        // when
        List<Person> result = this.personRepository.findByLastnameRegex(".orter.*");

        // then
        assertThat(result, containsInAnyOrder(person1, person2));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findBy_Firstname_And_Lastname() {
        Person jamesStewart = new Person();
        jamesStewart.setId("1940");
        jamesStewart.setFirstname("James");
        jamesStewart.setLastname("Stewart");
        this.personMap.put(jamesStewart.getId(), jamesStewart);

        Person garyCooper1941 = new Person();
        garyCooper1941.setId("1941");
        garyCooper1941.setFirstname("Gary");
        garyCooper1941.setLastname("Cooper");
        this.personMap.put(garyCooper1941.getId(), garyCooper1941);

        Person garyCooper1952 = new Person();
        garyCooper1952.setId("1952");
        garyCooper1952.setFirstname("Gary");
        garyCooper1952.setLastname("Cooper");
        this.personMap.put(garyCooper1952.getId(), garyCooper1952);

        List<Person> matches = this.personRepository.findByFirstname_AndLastname("James", "Stewart");
        assertThat("1940", matches.size(), equalTo(1));
        assertThat("1940", matches.get(0).getId(), equalTo("1940"));

        matches = this.personRepository.findByFirstname_AndLastname("Gary", "Cooper");
        assertThat("1941 and 1952", matches.size(), equalTo(2));
        assertThat("1941 and 1952", matches,
                containsInAnyOrder(hasProperty("id", equalTo("1941")), hasProperty("id", equalTo("1952"))));

        matches = this.personRepository.findByFirstname_AndLastname("James", "");
        assertThat("Mismatch on lastname", matches.size(), equalTo(0));

        matches = this.personRepository.findByFirstname_AndLastname("", "Stewart");
        assertThat("Mismatch on firstname", matches.size(), equalTo(0));
    }

    // Firstname and lastname in different order when arguments compared to method name
    @Test
    public void findByFirstnameOrLastname() {
        Person davidNiven = new Person();
        davidNiven.setId("1958");
        davidNiven.setFirstname("David");
        davidNiven.setLastname("Niven");
        this.personMap.put(davidNiven.getId(), davidNiven);
        List<Person> matches = this.personRepository.findByFirstnameOrLastname("Niven", null);
        assertThat("1958", matches.size(), equalTo(1));
        assertThat("1958", matches.get(0).getFirstname(), equalTo("David"));

        Person charltonHeston = new Person();
        charltonHeston.setId("1959");
        charltonHeston.setFirstname("Charlton");
        charltonHeston.setLastname("Heston");
        this.personMap.put(charltonHeston.getId(), charltonHeston);
        matches = this.personRepository.findByFirstnameOrLastname("Heston", "");
        assertThat("1959", matches.size(), equalTo(1));
        assertThat("1959", matches.get(0).getFirstname(), equalTo("Charlton"));

        Person burtLancaster = new Person();
        burtLancaster.setId("1960");
        burtLancaster.setFirstname("Burt");
        burtLancaster.setLastname("Lancaster");
        this.personMap.put(burtLancaster.getId(), burtLancaster);
        matches = this.personRepository.findByFirstnameOrLastname("Lancaster", "Hazelcast");
        assertThat("1960", matches.size(), equalTo(1));
        assertThat("1960", matches.get(0).getFirstname(), equalTo("Burt"));

        Person maximilianSchell = new Person();
        maximilianSchell.setId("1961");
        maximilianSchell.setFirstname("Maximilian");
        maximilianSchell.setLastname("Schell");
        this.personMap.put(maximilianSchell.getId(), maximilianSchell);
        matches = this.personRepository.findByFirstnameOrLastname("Schell", "Maximilian");
        assertThat("1961", matches.size(), equalTo(1));
        assertThat("1961", matches.get(0).getId(), equalTo("1961"));

        Person gregoryPeck = new Person();
        gregoryPeck.setId("1962");
        gregoryPeck.setFirstname("Gregory");
        gregoryPeck.setLastname("Peck");
        this.personMap.put(gregoryPeck.getId(), gregoryPeck);
        matches = this.personRepository.findByFirstnameOrLastname("Hazelcast", "Gregory");
        assertThat("1962", matches.size(), equalTo(1));
        assertThat("1962", matches.get(0).getLastname(), equalTo("Peck"));

        Person sidneyPoitier = new Person();
        sidneyPoitier.setId("1963");
        sidneyPoitier.setFirstname("Sidney");
        sidneyPoitier.setLastname("Poitier");
        this.personMap.put(sidneyPoitier.getId(), sidneyPoitier);
        matches = this.personRepository.findByFirstnameOrLastname("", "Sidney");
        assertThat("1963", matches.size(), equalTo(1));
        assertThat("1963", matches.get(0).getLastname(), equalTo("Poitier"));

        Person rexHarrison = new Person();
        rexHarrison.setId("1964");
        rexHarrison.setFirstname("Rex");
        rexHarrison.setLastname("Harrison");
        this.personMap.put(rexHarrison.getId(), rexHarrison);
        matches = this.personRepository.findByFirstnameOrLastname(null, "Rex");
        assertThat("1964", matches.size(), equalTo(1));
        assertThat("1964", matches.get(0).getLastname(), equalTo("Harrison"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findByFirstnameGreaterThan() {
        Person rexHarrison = new Person();
        rexHarrison.setId("1964");
        rexHarrison.setFirstname("Rex");
        rexHarrison.setLastname("Harrison");
        this.personMap.put(rexHarrison.getId(), rexHarrison);

        Person wallaceBeery = new Person();
        wallaceBeery.setId("1932");
        wallaceBeery.setFirstname("Wallace");
        wallaceBeery.setLastname("Beery");
        this.personMap.put(wallaceBeery.getId(), wallaceBeery);

        Person victorMcLaglen = new Person();
        victorMcLaglen.setId("1935");
        victorMcLaglen.setFirstname("Victor");
        victorMcLaglen.setLastname("McLaglen");
        this.personMap.put(victorMcLaglen.getId(), victorMcLaglen);

        List<Person> matches = this.personRepository.findByFirstnameGreaterThan("U");

        assertThat("Wallace, Victor", matches.size(), equalTo(2));
        assertThat("Wallace, Victor", matches,
                hasItems(allOf(hasProperty("firstname", equalTo("Wallace")), hasProperty("lastname", equalTo("Beery"))),
                        allOf(hasProperty("firstname", equalTo("Victor")), hasProperty("lastname", equalTo("McLaglen")))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void queryByFirstnameLike() {
        Person victorMcLaglen = new Person();
        victorMcLaglen.setId("1935");
        victorMcLaglen.setFirstname("Victor");
        victorMcLaglen.setLastname("McLaglen");
        this.personMap.put(victorMcLaglen.getId(), victorMcLaglen);

        Person humphreyBogart = new Person();
        humphreyBogart.setId("1951");
        humphreyBogart.setFirstname("Humphrey");
        humphreyBogart.setLastname("Bogart");
        this.personMap.put(humphreyBogart.getId(), humphreyBogart);

        Person henryFonda = new Person();
        henryFonda.setId("1981");
        henryFonda.setFirstname("Henry");
        henryFonda.setLastname("Fonda");
        this.personMap.put(henryFonda.getId(), henryFonda);

        List<Person> matches = this.personRepository.queryByFirstnameLike("H%y");

        assertThat("Humphrey & Henry", matches.size(), equalTo(2));
        assertThat("Humphrey & Henry", matches,
                hasItems(allOf(hasProperty("firstname", equalTo("Humphrey")), hasProperty("lastname", equalTo("Bogart"))),
                        allOf(hasProperty("firstname", equalTo("Henry")), hasProperty("lastname", equalTo("Fonda")))));
    }

    @Test
    public void findByFirstnameContains() {
        Person wallaceBeery = new Person();
        wallaceBeery.setId("1932");
        wallaceBeery.setFirstname("Wallace");
        wallaceBeery.setLastname("Beery");
        this.personMap.put(wallaceBeery.getId(), wallaceBeery);

        Person walaceBeery = new Person();
        walaceBeery.setId("1931");
        walaceBeery.setFirstname("Walace");
        walaceBeery.setLastname("Beery");
        this.personMap.put(walaceBeery.getId(), walaceBeery);

        Person russellCrowe = new Person();
        russellCrowe.setId("2000");
        russellCrowe.setFirstname("Russell");
        russellCrowe.setLastname("Crowe");
        this.personMap.put(russellCrowe.getId(), russellCrowe);

        List<Person> matches = this.personRepository.findByFirstnameContains("ll");

        assertThat("Wallace, Russell", matches.size(), equalTo(2));
        assertThat("Wallace, Russell", matches,
                hasItems(allOf(hasProperty("firstname", equalTo("Wallace")), hasProperty("lastname", equalTo("Beery"))),
                        allOf(hasProperty("firstname", equalTo("Russell")), hasProperty("lastname", equalTo("Crowe")))));
    }

    @Test
    public void findByFirstnameContainsAndLastnameStartsWithAllIgnoreCase() {
        Person tracy1937 = new Person();
        tracy1937.setId("1937");
        tracy1937.setFirstname("Spencer");
        tracy1937.setLastname("Tracy");
        this.personMap.put(tracy1937.getId(), tracy1937);

        Person tracy1938 = new Person();
        tracy1938.setId("1938");
        tracy1938.setFirstname("Spencer");
        tracy1938.setLastname("Tracy");
        this.personMap.put(tracy1938.getId(), tracy1938);

        List<Person> matches = this.personRepository.findByFirstnameContainsAndLastnameStartsWithAllIgnoreCase("En", "tR");

        assertThat("2xSpencer", matches.size(), equalTo(2));
        assertThat("2xSpencer", matches,
                hasItems(allOf(hasProperty("firstname", equalTo("Spencer")), hasProperty("lastname", equalTo("Tracy")))));

    }

    @Test
    public void findByFirstnameOrderById() {
        Person robertDonat = new Person();
        robertDonat.setId("1939");
        robertDonat.setFirstname("Robert");
        robertDonat.setLastname("Donat");
        this.personMap.put(robertDonat.getId(), robertDonat);

        Person robertDeNiro = new Person();
        robertDeNiro.setId("1980");
        robertDeNiro.setFirstname("Robert");
        robertDeNiro.setLastname("De Niro");
        this.personMap.put(robertDeNiro.getId(), robertDeNiro);

        Person robertDuvall = new Person();
        robertDuvall.setId("1983");
        robertDuvall.setFirstname("Robert");
        robertDuvall.setLastname("Duvall");
        this.personMap.put(robertDuvall.getId(), robertDuvall);

        List<Person> matches = this.personRepository.findByFirstnameOrderById("Robert");

        assertThat("Donat, De Niro, Duvall", matches.size(), equalTo(3));
        assertThat("1939", matches.get(0).getLastname(), equalTo("Donat"));
        assertThat("1980", matches.get(1).getLastname(), equalTo("De Niro"));
        assertThat("1983", matches.get(2).getLastname(), equalTo("Duvall"));
    }

    @Test
    public void findByLastnameOrderByIdAsc() {
        Person daniel2007 = new Person();
        daniel2007.setId("2007");
        daniel2007.setFirstname("Daniel");
        daniel2007.setLastname("Day-Lewis");
        this.personMap.put(daniel2007.getId(), daniel2007);

        Person daniel1989 = new Person();
        daniel1989.setId("1989");
        daniel1989.setFirstname("Daniel");
        daniel1989.setLastname("Day-Lewis");
        this.personMap.put(daniel1989.getId(), daniel1989);

        Person daniel2012 = new Person();
        daniel2012.setId("2012");
        daniel2012.setFirstname("Daniel");
        daniel2012.setLastname("Day-Lewis");
        this.personMap.put(daniel2012.getId(), daniel2012);

        List<Person> matches = this.personRepository.findByLastnameOrderByIdAsc("Day-Lewis");

        assertThat("Three times winner", matches.size(), equalTo(3));
        assertThat("Daniel", matches.get(0).getId(), equalTo("1989"));
        assertThat("Day", matches.get(1).getId(), equalTo("2007"));
        assertThat("Lewis", matches.get(2).getId(), equalTo("2012"));
    }

    @Test
    public void findByFirstnameOrderByLastnameDesc() {
        Person jackNicholson1975 = new Person();
        jackNicholson1975.setId("1975");
        jackNicholson1975.setFirstname("Jack");
        jackNicholson1975.setLastname("Nicholson");
        this.personMap.put(jackNicholson1975.getId(), jackNicholson1975);

        Person jackLemmon = new Person();
        jackLemmon.setId("1973");
        jackLemmon.setFirstname("Jack");
        jackLemmon.setLastname("Lemmon");
        this.personMap.put(jackLemmon.getId(), jackLemmon);

        Person jackNicholson1997 = new Person();
        jackNicholson1997.setId("1997");
        jackNicholson1997.setFirstname("Jack");
        jackNicholson1997.setLastname("Nicholson");
        this.personMap.put(jackNicholson1997.getId(), jackNicholson1997);

        List<Person> matches = this.personRepository.findByFirstnameOrderByLastnameDesc("Jack");

        assertThat("Lemmon x1, Nicholson x2", matches.size(), equalTo(3));
        assertThat("1975 or 1997", matches.get(0), hasProperty("lastname", equalTo("Nicholson")));
        assertThat("1975 or 1997 again", matches.get(1), hasProperty("lastname", equalTo("Nicholson")));
        assertThat("1973", matches.get(2), hasProperty("lastname", equalTo("Lemmon")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findByLastnameIgnoreCase() {
        Person tomHanks1993 = new Person();
        tomHanks1993.setId("1993");
        tomHanks1993.setFirstname("Tom");
        tomHanks1993.setLastname("Hanks");
        this.personMap.put(tomHanks1993.getId(), tomHanks1993);

        Person tomHanks1994 = new Person();
        tomHanks1994.setId("1994");
        tomHanks1994.setFirstname("Tom");
        tomHanks1994.setLastname("Hanks");
        this.personMap.put(tomHanks1994.getId(), tomHanks1994);

        List<Person> matches = this.personRepository.findByLastnameIgnoreCase("hAnKs");

        assertThat("Tom Hanks", matches.size(), equalTo(2));
        assertThat("1993 & 1994", matches,
                containsInAnyOrder(hasProperty("id", equalTo("1993")), hasProperty("id", equalTo("1994"))));
    }

    @Test
    public void findTop3ByOrderByFirstnameAsc() {
        Person adrien = new Person();
        adrien.setId("2002");
        adrien.setFirstname("Adrien");
        adrien.setLastname("Brody");
        this.personMap.put(adrien.getId(), adrien);

        Person al = new Person();
        al.setId("1992");
        al.setFirstname("Al");
        al.setLastname("Pacino");
        this.personMap.put(al.getId(), al);

        Person alec = new Person();
        alec.setId("1957");
        alec.setFirstname("Alec");
        alec.setLastname("Guinness");
        this.personMap.put(alec.getId(), alec);

        List<Person> matches = this.personRepository.findTop3ByOrderByFirstnameAsc();

        assertThat("First, 2002, Brody", matches.get(0).getFirstname(), equalTo("Adrien"));
        assertThat("Second, 1992, Pacino", matches.get(1).getFirstname(), equalTo("Al"));
        assertThat("Third, 1957, Guinness", matches.get(2).getFirstname(), equalTo("Alec"));
        assertThat("Three matches", matches.size(), equalTo(3));
    }

    @Test
    public void findFirst30ByOrderByFirstnameDescLastnameAsc() {
        Person alecGuinness = new Person();
        alecGuinness.setId("2000");
        alecGuinness.setFirstname("Alec");
        alecGuinness.setLastname("Guinness");
        this.personMap.put(alecGuinness.getId(), alecGuinness);

        Person alecBenner = new Person();
        alecBenner.setId("2001");
        alecBenner.setFirstname("Alec");
        alecBenner.setLastname("Benner");
        this.personMap.put(alecBenner.getId(), alecBenner);

        Person yulBrynner = new Person();
        yulBrynner.setId("1956");
        yulBrynner.setFirstname("Yul");
        yulBrynner.setLastname("Brynner");
        this.personMap.put(yulBrynner.getId(), yulBrynner);

        List<Person> matches = this.personRepository.findFirst3ByOrderByFirstnameDescLastnameAsc();

        assertThat("First thirty", matches.size(), equalTo(3));
        assertThat("1st - Yul Brynner", matches.get(0).getId(), equalTo("1956"));
        assertThat("2nd - Alec Benner", matches.get(1).getId(), equalTo("2001"));
        assertThat("3rd - Alec Guinness", matches.get(2).getId(), equalTo("2000"));
    }

    @Test
    public void findByFirstnameIn() {
        Person jackLemmon = new Person();
        jackLemmon.setId("1948");
        jackLemmon.setFirstname("Jack");
        jackLemmon.setLastname("Lemmon");
        this.personMap.put(jackLemmon.getId(), jackLemmon);

        Person jackNicholson = new Person();
        jackNicholson.setId("1997");
        jackNicholson.setFirstname("Jack");
        jackNicholson.setLastname("Nicholson");
        this.personMap.put(jackNicholson.getId(), jackNicholson);

        Person robertDonat = new Person();
        robertDonat.setId("1950");
        robertDonat.setFirstname("Robert");
        robertDonat.setLastname("Donat");
        this.personMap.put(robertDonat.getId(), robertDonat);

        Person leonardoDiCaprio = new Person();
        leonardoDiCaprio.setId("2016");
        leonardoDiCaprio.setFirstname("Leonardo");
        leonardoDiCaprio.setLastname("DiCaprio");
        this.personMap.put(leonardoDiCaprio.getId(), leonardoDiCaprio);

        List<Person> matches = this.personRepository.findByFirstnameIn(asList("Jack", "Robert"));

        assertThat("2xJack, Robert", matches.size(), equalTo(3));
        assertThat("3xJack, 3xRobert", matches,
                hasItems(allOf(hasProperty("firstname", equalTo("Jack")), hasProperty("lastname", equalTo("Lemmon"))),
                        allOf(hasProperty("firstname", equalTo("Jack")), hasProperty("lastname", equalTo("Nicholson"))),
                        allOf(hasProperty("firstname", equalTo("Robert")), hasProperty("lastname", equalTo("Donat")))));

    }

    @Test
    public void findByFirstnameEndsWithAndLastnameNotIn() {
        Person jonVoight = new Person();
        jonVoight.setId("1");
        jonVoight.setFirstname("Jon");
        jonVoight.setLastname("Voight");
        this.personMap.put(jonVoight.getId(), jonVoight);

        Person jonHeston = new Person();
        jonHeston.setId("2");
        jonHeston.setFirstname("Jon");
        jonHeston.setLastname("Heston");
        this.personMap.put(jonHeston.getId(), jonHeston);

        Person jonBrando = new Person();
        jonBrando.setId("3");
        jonBrando.setFirstname("Jon");
        jonBrando.setLastname("Brando");
        this.personMap.put(jonBrando.getId(), jonBrando);

        List<Person> matches = this.personRepository
                .findByFirstnameEndsWithAndLastnameNotIn("on", asList("Heston", "Brando"));

        assertThat("Jon", matches.size(), equalTo(1));
        assertThat("Jon", matches,
                hasItems(allOf(hasProperty("firstname", equalTo("Jon")), hasProperty("lastname", equalTo("Voight")))));

    }

    @Test
    public void findFirst4By() {
        Person emil = new Person();
        emil.setId("1");
        emil.setFirstname("Emil");
        emil.setLastname("Jannings");
        this.personMap.put(emil.getId(), emil);

        Person warner = new Person();
        warner.setId("2");
        warner.setFirstname("Warner");
        warner.setLastname("Baxter");
        this.personMap.put(warner.getId(), warner);

        Person george = new Person();
        george.setId("3");
        george.setFirstname("George");
        george.setLastname("Arliss");
        this.personMap.put(george.getId(), george);

        Person lionel = new Person();
        lionel.setId("4");
        lionel.setFirstname("Lionel");
        lionel.setLastname("Barrymore");
        this.personMap.put(lionel.getId(), lionel);

        Person wallace = new Person();
        wallace.setId("5");
        wallace.setFirstname("Wallace");
        wallace.setLastname("Beery");
        this.personMap.put(wallace.getId(), wallace);

        AtomicInteger count = new AtomicInteger();

        try (Stream<Person> matches = this.personRepository.findFirst4By()) {

            matches.forEach(match -> {
                count.incrementAndGet();
            });
        }

        assertThat("Any four", count.get(), equalTo(4));
    }

    @Test
    public void streamByLastnameGreaterThanEqual() {
        Person johnWayne = new Person();
        johnWayne.setId("1");
        johnWayne.setFirstname("John");
        johnWayne.setLastname("Wayne");
        this.personMap.put(johnWayne.getId(), johnWayne);

        Person bradPitt = new Person();
        bradPitt.setId("2");
        bradPitt.setFirstname("Brad");
        bradPitt.setLastname("Pitt");
        this.personMap.put(bradPitt.getId(), bradPitt);

        Person forestWhitaker = new Person();
        forestWhitaker.setId("3");
        forestWhitaker.setFirstname("Forest");
        forestWhitaker.setLastname("Whitaker");
        this.personMap.put(forestWhitaker.getId(), forestWhitaker);

        AtomicInteger count = new AtomicInteger();

        try (Stream<Person> matches = this.personRepository.streamByLastnameGreaterThanEqual("Wayne")) {

            matches.forEach(match -> {

                assertThat("Wayne or Whitaker", match,
                        anyOf(hasProperty("lastname", equalTo("Wayne")), hasProperty("lastname", equalTo("Whitaker"))));

                count.incrementAndGet();
            });
        }

        assertThat("Wayne and Whitaker", count.get(), equalTo(2));
    }

    // Find methods with special parameters

    @SuppressWarnings("unchecked")
    @Test
    public void findByFirstnameWithSort() {
        Person jackNicholson1975 = new Person();
        jackNicholson1975.setId("1975");
        jackNicholson1975.setFirstname("Jack");
        jackNicholson1975.setLastname("Nicholson");
        this.personMap.put(jackNicholson1975.getId(), jackNicholson1975);

        Person jackLemmon = new Person();
        jackLemmon.setId("1973");
        jackLemmon.setFirstname("Jack");
        jackLemmon.setLastname("Lemmon");
        this.personMap.put(jackLemmon.getId(), jackLemmon);

        Person jackNicholson1997 = new Person();
        jackNicholson1997.setId("1997");
        jackNicholson1997.setFirstname("Jack");
        jackNicholson1997.setLastname("Nicholson");
        this.personMap.put(jackNicholson1997.getId(), jackNicholson1997);

        List<Person> matches = this.personRepository.findByFirstname("Jack", null);
        assertThat("NULL 2 x Nicholson and 1 x Lemmon", matches.size(), equalTo(3));
        assertThat("NULL sort", matches,
                containsInAnyOrder(hasProperty("id", equalTo("1973")), hasProperty("id", equalTo("1975")),
                        hasProperty("id", equalTo("1997"))));

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        matches = this.personRepository.findByFirstname("Jack", sort);

        assertThat("DESC 2 x Nicholson and 1 x Lemmon", matches.size(), equalTo(3));
        assertThat("DESC 1st", matches.get(0).getId(), equalTo("1997"));
        assertThat("DESC 2nd", matches.get(1).getId(), equalTo("1975"));
        assertThat("DESC 3rd", matches.get(2).getId(), equalTo("1973"));

        sort = Sort.by(Sort.Direction.ASC, "id");
        matches = this.personRepository.findByFirstname("Jack", sort);

        assertThat("ASC 2 x Nicholson and 1 x Lemmon", matches.size(), equalTo(3));
        assertThat("ASC 1st", matches.get(0).getId(), equalTo("1973"));
        assertThat("ASC 2nd", matches.get(1).getId(), equalTo("1975"));
        assertThat("ASC 3rd", matches.get(2).getId(), equalTo("1997"));
    }

    @Test
    public void findByLastnameNotNull() {
        Person adrienBrody = new Person();
        adrienBrody.setId("2010");
        adrienBrody.setFirstname("Adrien");
        adrienBrody.setLastname("Brody");
        this.personMap.put(adrienBrody.getId(), adrienBrody);

        Person yulBrynner = new Person();
        yulBrynner.setId("1973");
        yulBrynner.setFirstname("Yul");
        yulBrynner.setLastname("Brynner");
        this.personMap.put(yulBrynner.getId(), yulBrynner);

        Person jackNicholson1997 = new Person();
        jackNicholson1997.setId("1997");
        jackNicholson1997.setFirstname("Jack");
        jackNicholson1997.setLastname(null);
        this.personMap.put(jackNicholson1997.getId(), jackNicholson1997);

        Sort sort = Sort.by(Sort.DEFAULT_DIRECTION, "firstname");

        List<Person> matches = this.personRepository.findByLastnameNotNull(sort);
        int len = matches.size();

        assertThat("Everyone returned", len, equalTo(2));
        assertThat("First firstname - Adrien Brody", matches.get(0).getFirstname(), equalTo("Adrien"));
        assertThat("Last firstname - Yul Brynner", matches.get(len - 1).getFirstname(), equalTo("Yul"));
    }

    @Test
    public void findByLastname() {
        Person daniel1989 = new Person();
        daniel1989.setId("1989");
        daniel1989.setFirstname("Daniel");
        daniel1989.setLastname("Day-Lewis");
        this.personMap.put(daniel1989.getId(), daniel1989);

        Person daniel2007 = new Person();
        daniel2007.setId("2007");
        daniel2007.setFirstname("Daniel");
        daniel2007.setLastname("Day-Lewis");
        this.personMap.put(daniel2007.getId(), daniel2007);

        Person daniel2012 = new Person();
        daniel2012.setId("2012");
        daniel2012.setFirstname("Daniel");
        daniel2012.setLastname("Day-Lewis");
        this.personMap.put(daniel2012.getId(), daniel2012);

        Person daniel2013 = new Person();
        daniel2013.setId("2013");
        daniel2013.setFirstname("Daniel");
        daniel2013.setLastname("Day-Lewis");
        this.personMap.put(daniel2013.getId(), daniel2013);

        String[] YEARS = {"1989", "2007", "2012"};
        Set<String> years = new TreeSet<>(asList(YEARS));

        for (int page = 0; page < YEARS.length; page++) {
            Pageable pageRequest = PageRequest.of(page, SIZE_1);

            Page<Person> pageResponse = this.personRepository.findByLastname("Day-Lewis", pageRequest);
            assertThat("Page " + page + ", has content", pageResponse.hasContent(), equalTo(true));

            List<Person> pageMatches = pageResponse.getContent();

            assertThat("Page " + page + ", one of three oscars", pageMatches.size(), equalTo(1));
            String year = pageMatches.get(0).getId();

            assertThat("Page " + page + ", year " + year + " expected", years.contains(year), equalTo(true));
            years.remove(year);
        }
        assertThat("All years matched", years, hasSize(0));
    }

    @Test
    public void findByOrderByLastnameDesc() {
        Person robert = new Person();
        robert.setId("1980");
        robert.setFirstname("Robert");
        robert.setLastname("De Niro");
        this.personMap.put(robert.getId(), robert);

        Person henry = new Person();
        henry.setId("1981");
        henry.setFirstname("Henry");
        henry.setLastname("Fonda");
        this.personMap.put(henry.getId(), henry);

        Person ben = new Person();
        ben.setId("1982");
        ben.setFirstname("Ben");
        ben.setLastname("Kingsley");

        this.personMap.put(ben.getId(), ben);

        int expectedNumberOfPages = this.personMap.size() / SIZE_3;
        int pagesRetrieved = 0;
        String previousLastname = null;

        Pageable pageRequest = PageRequest.of(PAGE_0, SIZE_3);
        Page<Person> pageResponse = this.personRepository.findByOrderByLastnameDesc(pageRequest);
        while (pageResponse != null) {

            assertThat("Not more than expected pages", pagesRetrieved, lessThan(expectedNumberOfPages));
            pagesRetrieved++;

            assertThat("Page " + pagesRetrieved + " has content", pageResponse.hasContent(), equalTo(true));
            List<Person> pageContent = pageResponse.getContent();
            assertThat("Page " + pagesRetrieved + " content not null", pageContent, notNullValue());
            assertThat("Page " + pagesRetrieved + " content not empty", pageContent.size(), greaterThan(0));

            assertThat("Page " + pagesRetrieved + ", this page number", pageResponse.getNumber(), equalTo(pagesRetrieved - 1));
            assertThat("Page " + pagesRetrieved + ", total page number", pageResponse.getTotalPages(),
                    equalTo(expectedNumberOfPages));

            assertThat("Page " + pagesRetrieved + ", item count v content", pageResponse.getNumberOfElements(),
                    equalTo(pageContent.size()));
            if (pagesRetrieved != expectedNumberOfPages) {
                assertThat("Page " + pagesRetrieved + ", item count", pageResponse.getNumberOfElements(), equalTo(5));
            } else {
                assertThat("Page " + pagesRetrieved + ", item count", pageResponse.getNumberOfElements(), greaterThan(0));
                assertThat("Page " + pagesRetrieved + ", item count", pageResponse.getNumberOfElements(),
                        lessThanOrEqualTo(SIZE_3));
            }
            assertThat("Page " + pagesRetrieved + ", total item count", pageResponse.getTotalElements(),
                    equalTo((long) this.personMap.size()));

            for (Person person : pageContent) {
                if (previousLastname != null) {
                    assertThat("Descending lastname", person.getLastname(), lessThanOrEqualTo(previousLastname));
                }
                previousLastname = person.getLastname();
            }

            pageRequest = pageResponse.nextPageable();
            if (pagesRetrieved == expectedNumberOfPages) {
                assertThat("Page " + pagesRetrieved + ", is last", pageResponse.hasNext(), equalTo(false));
                assertThat("Page " + pagesRetrieved + ", no following page", pageRequest, equalTo(Pageable.unpaged()));
                pageResponse = null;
            } else {
                assertThat("Page " + pagesRetrieved + ", not last", pageResponse.hasNext(), equalTo(true));
                assertThat("Page " + pagesRetrieved + ", has following page", pageRequest, not(equalTo(Pageable.unpaged())));
                pageResponse = this.personRepository.findByOrderByLastnameDesc(pageRequest);
            }
        }

        assertThat("Not less than expected pages", pagesRetrieved, equalTo(expectedNumberOfPages));
    }

    @Test
    public void findByIdLike() {
        Person robertDonat = new Person();
        robertDonat.setId("1940");
        robertDonat.setFirstname("Robert");
        robertDonat.setLastname("Donat");
        this.personMap.put(robertDonat.getId(), robertDonat);

        Person jamesStewart = new Person();
        jamesStewart.setId("1950");
        jamesStewart.setFirstname("James");
        jamesStewart.setLastname("Stewart");
        this.personMap.put(jamesStewart.getId(), jamesStewart);

        Person garyCooper = new Person();
        garyCooper.setId("1960");
        garyCooper.setFirstname("Gary");
        garyCooper.setLastname("Cooper");
        this.personMap.put(garyCooper.getId(), garyCooper);

        Person leonardoDiCaprio = new Person();
        leonardoDiCaprio.setId("2016");
        leonardoDiCaprio.setFirstname("Leonardo");
        leonardoDiCaprio.setLastname("DiCaprio");
        this.personMap.put(leonardoDiCaprio.getId(), leonardoDiCaprio);

        String PATTERN = "19%0";
        String[] EXPECTED_YEARS = {"1940", "1950", "1960"};
        Set<String> expectedYears = new TreeSet<>(asList(EXPECTED_YEARS));

        Pageable pageRequest = PageRequest.of(PAGE_0, SIZE_5);
        Slice<Person> pageResponse = this.personRepository.findByIdLike(PATTERN, pageRequest);
        int slice = 0;
        while (pageResponse != null) {

            assertThat("Slice " + slice + ", has content", pageResponse.hasContent(), equalTo(true));
            List<Person> pageMatches = pageResponse.getContent();
            assertThat("Slice " + slice + ", contains data", pageMatches.size(), greaterThan(0));

            pageMatches.forEach(person -> {
                String year = person.getId();
                assertThat("Year " + year + " expected", expectedYears.contains(year), equalTo(true));
                expectedYears.remove(year);
            });

            if (pageResponse.hasNext()) {
                assertThat("Slice " + slice + ", is full", pageMatches.size(), equalTo(SIZE_5));

                pageRequest = pageResponse.nextPageable();
                pageResponse = this.personRepository.findByIdLike(PATTERN, pageRequest);

                assertThat("Slice " + slice + ", expected next slice", pageResponse, notNullValue());
            } else {
                pageResponse = null;
            }
            slice++;
        }

        assertThat("All years matched", expectedYears, hasSize(0));
    }

    @Test
    public void findByIdGreaterThanEqualAndFirstnameGreaterThanAndFirstnameLessThanEqual() {
        String[] LASTNAMES = {"Benner"};

        Person bruceBenner2005 = new Person();
        bruceBenner2005.setId("2005");
        bruceBenner2005.setFirstname("Bruce");
        bruceBenner2005.setLastname("Benner");
        this.personMap.put(bruceBenner2005.getId(), bruceBenner2005);

        Person bruceBenner1989 = new Person();
        bruceBenner1989.setId("1989");
        bruceBenner1989.setFirstname("Bruce");
        bruceBenner1989.setLastname("Benner");
        this.personMap.put(bruceBenner1989.getId(), bruceBenner1989);

        Person alecBaldwin = new Person();
        alecBaldwin.setId("2000");
        alecBaldwin.setFirstname("Alec");
        alecBaldwin.setLastname("Bolduin");
        this.personMap.put(alecBaldwin.getId(), alecBaldwin);

        Person harrisonFord = new Person();
        harrisonFord.setId("2010");
        harrisonFord.setFirstname("Harrison");
        harrisonFord.setLastname("Ford");
        this.personMap.put(harrisonFord.getId(), harrisonFord);

        Set<String> lastnames = new TreeSet<>(asList(LASTNAMES));

        Pageable pageRequest = PageRequest.of(PAGE_0, 2);
        Slice<Person> pageResponse = this.personRepository
                .findByIdGreaterThanEqualAndFirstnameGreaterThanAndFirstnameLessThanEqual("1990", "A", "D", pageRequest);
        int slice = 0;
        while (pageResponse != null) {

            assertThat("Slice " + slice + ", has content", pageResponse.hasContent(), equalTo(true));

            List<Person> pageMatches = pageResponse.getContent();

            assertThat("Slice " + slice + ", contains a person", pageMatches.size(), equalTo(2));

            String lastname = pageMatches.get(0).getLastname();
            assertThat("Slice " + slice + ", lastname " + lastname + " expected", lastnames.contains(lastname), equalTo(true));
            lastnames.remove(lastname);

            if (pageResponse.hasNext()) {
                pageRequest = pageResponse.nextPageable();
                pageResponse = this.personRepository
                        .findByIdGreaterThanEqualAndFirstnameGreaterThanAndFirstnameLessThanEqual("1990", "I", "K", pageRequest);
            } else {
                pageResponse = null;
            }
            slice++;
        }

        assertThat("All lastnames matched", lastnames, hasSize(0));
    }

    @Test
    public void findByIdPagedWithParam() {
        int PAGE_0 = 0;
        int SIZE_20 = 20;
        PageRequest firstPageOf20Request = PageRequest.of(PAGE_0, SIZE_20);
        Page<Person> firstPageOfParamResponse = this.personRepository.findAllById("1933", firstPageOf20Request);
        assertThat("1 onwards returned for @Param query", firstPageOfParamResponse, notNullValue());
    }

    // Delete methods

    @Test
    public void deleteByLastname() {
        // given
        // fully populated map
        Person spenser1937 = new Person();
        spenser1937.setId("1937");
        spenser1937.setFirstname("Spencer");
        spenser1937.setLastname("Tracy");
        this.personMap.put(spenser1937.getId(), spenser1937);

        Person spenser1938 = new Person();
        spenser1938.setId("1938");
        spenser1938.setFirstname("Spencer");
        spenser1938.setLastname("Tracy");
        this.personMap.put(spenser1938.getId(), spenser1938);
        // when
        long deletedPersonsSize = this.personRepository.deleteByLastname("Tracy");

        // then
        assertThat("Delete for matched name removes from map", this.personMap.size(), equalTo(0));
        assertThat("Delete for matched name removes from @Repository", this.personRepository.count(),
                equalTo(0L));
        assertThat("Delete for matched name returns correct count", deletedPersonsSize, equalTo(2L));
        assertThat("1937 deleted", this.personMap.get("1937"), nullValue());
        assertThat("1938 deleted", this.personMap.get("1938"), nullValue());
    }

    @Test
    public void deleteByLastnameUnmatched() {
        // given
        Person spenser = new Person();
        spenser.setId("1937");
        spenser.setFirstname("Spencer");
        spenser.setLastname("Tracy");
        this.personMap.put(spenser.getId(), spenser);
        // when
        long deletedPersonsSize = this.personRepository.deleteByLastname("abcdefghijklmnopqrstuvwxyz");

        // then
        assertThat("Delete for unmatched name does nothing to map", this.personMap.size(), equalTo(1));
        assertThat("Delete for unmatched name does nothing to @Repository", this.personRepository.count(),
                equalTo(1L));
        assertThat("Delete for unmatched name returns null", deletedPersonsSize, equalTo(0L));
    }

    @Test
    public void deleteByFirstname() {
        // given
        Person spenser1937 = new Person();
        spenser1937.setId("1937");
        spenser1937.setFirstname("Spencer");
        spenser1937.setLastname("Tracy");
        this.personMap.put(spenser1937.getId(), spenser1937);

        Person spenser1938 = new Person();
        spenser1938.setId("1938");
        spenser1938.setFirstname("Spencer");
        spenser1938.setLastname("Tracy");
        this.personMap.put(spenser1938.getId(), spenser1938);

        Person robertDonat = new Person();
        robertDonat.setId("1939");
        robertDonat.setFirstname("Robert");
        robertDonat.setLastname("Donat");
        this.personMap.put(robertDonat.getId(), robertDonat);

        // when
        Collection<Person> deletedPersons = this.personRepository.deleteByFirstname("Spencer");

        // then
        assertThat("Delete for matched name removes from map", this.personMap.size(), equalTo(1));
        assertThat("Delete for matched name removes from @Repository", this.personRepository.count(),
                equalTo(1L));
        assertThat("Delete for matched name returns correct count", deletedPersons.size(), equalTo(2));
        assertThat("1937 deleted", this.personMap.get("1937"), nullValue());
        assertThat("1938 deleted", this.personMap.get("1938"), nullValue());
    }

    // Query methods

    @Test
    public void peoplewiththeirFirstNameIsJames() {
        Person jamesCagney = new Person();
        jamesCagney.setId("1940");
        jamesCagney.setFirstname("James");
        jamesCagney.setLastname("Cagney");
        this.personMap.put(jamesCagney.getId(), jamesCagney);

        Person jamesStewart = new Person();
        jamesStewart.setId("1942");
        jamesStewart.setFirstname("James");
        jamesStewart.setLastname("Stewart");
        this.personMap.put(jamesStewart.getId(), jamesStewart);

        Person johnCarter = new Person();
        johnCarter.setId("1943");
        johnCarter.setFirstname("John");
        johnCarter.setLastname("Carter");
        this.personMap.put(johnCarter.getId(), johnCarter);

        List<Person> matches = this.personRepository.peopleWithTheirFirstNameIsJames();
        assertThat("1940 and 1942", matches.size(), equalTo(2));
        assertThat("1940 and 1942", matches,
                containsInAnyOrder(hasProperty("lastname", equalTo("Cagney")), hasProperty("lastname", equalTo("Stewart"))));

    }

    @Test
    public void peoplewiththeirFirstName() {
        Person bingCrosby = new Person();
        bingCrosby.setId("1944");
        bingCrosby.setFirstname("Bing");
        bingCrosby.setLastname("Crosby");
        this.personMap.put(bingCrosby.getId(), bingCrosby);

        List<Person> matches = this.personRepository.peopleWithTheirFirstName("Bing");
        assertThat("1944", matches.size(), equalTo(1));
        assertThat("1944", matches.get(0).getLastname(), equalTo("Crosby"));
    }

    @Test
    public void peoplewithFirstAndLastName() {
        Person james = new Person();
        james.setId("1940");
        james.setFirstname("James");
        james.setLastname("Stewart");
        this.personMap.put(james.getId(), james);
        List<Person> matches = this.personRepository.peopleWithFirstAndLastName("James", "Stewart");
        assertThat("1940", matches.size(), equalTo(1));
        assertThat("1940", matches.get(0).getId(), equalTo("1940"));
    }

    @Test
    public void peoplewithLastNameLike() {
        Person james = new Person();
        james.setId("1940");
        james.setFirstname("James");
        james.setLastname("Stewart");
        this.personMap.put(james.getId(), james);
        List<Person> matches = this.personRepository.peopleWithLastNameLike("Stewar%");
        assertThat("1940", matches.size(), equalTo(1));
        assertThat("1940", matches.get(0).getId(), equalTo("1940"));
    }

    @Test
    public void peoplewithLastNameInCollection() {
        Person jamesStewart = new Person();
        jamesStewart.setId("1940");
        jamesStewart.setFirstname("James");
        jamesStewart.setLastname("Stewart");
        this.personMap.put(jamesStewart.getId(), jamesStewart);

        Person jamesCagney = new Person();
        jamesCagney.setId("1940");
        jamesCagney.setFirstname("James");
        jamesCagney.setLastname("Stewart");
        this.personMap.put(jamesCagney.getId(), jamesCagney);

        List<Person> matches = this.personRepository.peopleWithLastNameIn(asList("Stewart", "Smith"));
        assertThat("1940", matches.size(), equalTo(1));
        assertThat("1940", matches.get(0).getId(), equalTo("1940"));
    }

    // Null handling methods

    @Test
    public void getByLastname() {
        Person robertPort = new Person();
        robertPort.setId("1939");
        robertPort.setFirstname("Robert");
        robertPort.setLastname("Port");
        this.personMap.put(robertPort.getId(), robertPort);
        // when
        Optional<Person> result = this.personRepository.getByLastname("Porter");

        // then
        assertThat(result.isPresent(), equalTo(false));
    }

    // Async methods

    @Test
    public void findOneByFirstname()
            throws Exception {
        // given
        String firstname = "Porter";
        Person person = new Person();
        person.setId("porterId");
        person.setFirstname(firstname);
        this.personRepository.save(person);

        // when
        Future<Person> result = this.personRepository.findOneByFirstname(firstname);

        // then
        assertThat(result.get(), equalTo(person));
    }

    @Test
    public void findOneByLastname()
            throws Exception {
        // given
        String lastname = "Porter";
        Person person = new Person();
        person.setId("porterId");
        person.setLastname(lastname);
        this.personRepository.save(person);

        // when
        CompletableFuture<Person> result = this.personRepository.findOneByLastname(lastname);

        // then
        assertThat(result.get(), equalTo(person));
    }

    @Test
    public void findByLastnameListenableFuture()
            throws Exception {
        // given
        String lastname = "Porter";
        Person person1 = new Person();
        person1.setId("porterId");
        person1.setLastname(lastname);
        this.personRepository.save(person1);
        Person person2 = new Person();
        person2.setId("porterId2");
        person2.setLastname(lastname);
        this.personRepository.save(person2);

        // when
        ListenableFuture<List<Person>> result = this.personRepository.findByLastname(lastname);

        // then
        assertThat(result.get(), containsInAnyOrder(person1, person2));
    }

    @Test
    public void countDistinctByFirstname() {
        //adding same Person twice to create duplicate
        Person p = new Person();
        p.setId("2020");
        p.setFirstname("Sachin");
        p.setLastname("Tendulkar");
        this.personMap.put("1001", p);
        this.personMap.put("1002", p);

        final Long persons = this.personRepository.countByFirstname("Sachin");
        assertThat(persons, equalTo(2L));
        final Long distinctPersons = this.personRepository.countDistinctByFirstname("Sachin");
        assertThat(distinctPersons, equalTo(1L));
        this.personMap.remove("1001");
        this.personMap.remove("1002");
    }

    @Test
    public void findDistinctByFirstname() {
        //adding same Person twice to create duplicate
        Person p = new Person();
        p.setId("2020");
        p.setFirstname("Sachin");
        p.setLastname("Tendulkar");
        this.personMap.put("1001", p);
        this.personMap.put("1002", p);

        final List<Person> persons = this.personRepository.findByFirstname("Sachin");
        assertThat(persons.size(), equalTo(2));
        final List<Person> distinctPersons = this.personRepository.findDistinctByFirstname("Sachin");
        assertThat(distinctPersons.size(), equalTo(1));
        this.personMap.remove("1001");
        this.personMap.remove("1002");
    }

    @Test
    public void streamDistinctByFirstname() {
        AtomicInteger count = new AtomicInteger();
        //adding same Person twice to create duplicate
        Person p = new Person();
        p.setId("2020");
        p.setFirstname("Sachin");
        p.setLastname("Tendulkar");
        this.personMap.put("1001", p);
        this.personMap.put("1002", p);

        try (Stream<Person> matches = this.personRepository.streamByFirstname("Sachin")) {

            matches.forEach(match -> {

                assertThat("Tendulkar", match,
                        is(hasProperty("lastname", equalTo("Tendulkar"))));

                count.incrementAndGet();
            });
        }
        assertThat("Tendulkar", count.get(), equalTo(2));
        count.set(0);

        try (Stream<Person> matches = this.personRepository.streamDistinctByFirstname("Sachin")) {

            matches.forEach(match -> {

                assertThat("Tendulkar", match,
                        is(hasProperty("lastname", equalTo("Tendulkar"))));

                count.incrementAndGet();
            });
        }

        assertThat("Tendulkar", count.get(), equalTo(1));
        this.personMap.remove("1001");
        this.personMap.remove("1002");
    }

    @Test
    public void existsByFirstname() {
        final List<Person> persons = this.personRepository.findByFirstname("Ulhas");
        assertThat(persons.size(), equalTo(0));
        boolean exists = this.personRepository.existsByFirstname("Ulhas");
        assertThat(exists, equalTo(false));

        Person p = new Person();
        p.setId("2020");
        p.setFirstname("Sachin");
        p.setLastname("Tendulkar");
        this.personMap.put("2020", p);

        Person p2 = new Person();
        p2.setId("2021");
        p2.setFirstname("Sachin");
        p2.setLastname("Pilgaonkar");
        this.personMap.put("2021", p2);

        final List<Person> personsExists = this.personRepository.findByFirstname("Sachin");
        assertThat(personsExists.size(), equalTo(2));
        exists = this.personRepository.existsByFirstname("Sachin");
        assertThat(exists, equalTo(true));
        this.personMap.remove("2020");
        this.personMap.remove("2021");
    }

    @Test
    public void findByFirstnameExists() {
        final List<Person> persons = this.personRepository.findByFirstname("Ulhas");
        assertThat(persons.size(), equalTo(0));
        boolean exists = this.personRepository.existsByFirstname("Ulhas");
        assertThat(exists, equalTo(false));

        Person p = new Person();
        p.setId("2020");
        p.setFirstname("Sachin");
        p.setLastname("Tendulkar");
        this.personMap.put("2020", p);

        Person p2 = new Person();
        p2.setId("2021");
        p2.setFirstname("Sachin");
        p2.setLastname("Pilgaonkar");
        this.personMap.put("2021", p2);

        final List<Person> personsExists = this.personRepository.findByFirstname("Sachin");
        assertThat(personsExists.size(), equalTo(2));
        exists = this.personRepository.existsByFirstname("Sachin");
        assertThat(exists, equalTo(true));
    }

    @Test
    public void findByLastnameEmpty() {
        final List<Person> persons = this.personRepository.findByLastnameEmpty();
        assertThat(persons.size(), equalTo(0));

        Person p = new Person();
        p.setId("2020");
        p.setFirstname("Virat");
        p.setLastname("");
        this.personMap.put("1001", p);

        List<Person> personsWithEmptyLastName = this.personRepository.findByLastnameEmpty();
        assertThat(personsWithEmptyLastName.size(), equalTo(1));
        assertThat(personsWithEmptyLastName.get(0), equalTo(p));

        personsWithEmptyLastName = this.personRepository.findByLastnameIsEmpty();
        assertThat(personsWithEmptyLastName.size(), equalTo(1));
        assertThat(personsWithEmptyLastName.get(0), equalTo(p));
        this.personMap.remove("1001");
    }

    @Test
    public void findByLastnameNotEmpty() {
        Person p = new Person();
        p.setId("2020");
        p.setFirstname("Virat");
        p.setLastname("");
        this.personMap.put("1001", p);

        List<Person> matches = this.personRepository.findByLastnameNotEmpty();
        int len = matches.size();
        assertThat("Everyone except Virat returned", len, equalTo(this.personMap.size() - 1));

        matches = this.personRepository.findByLastnameIsNotEmpty();
        len = matches.size();
        assertThat("Everyone except Virat returned", len, equalTo(this.personMap.size() - 1));
        this.personMap.remove("1001");
    }
}
