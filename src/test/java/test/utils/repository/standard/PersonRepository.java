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

package test.utils.repository.standard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.hazelcast.repository.HazelcastRepository;
import org.springframework.data.hazelcast.repository.query.Query;
import org.springframework.data.repository.query.Param;
import test.utils.domain.Person;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * Repository class used for tests, re-factored from inner class in {@link EnableHazelcastRepositoriesIT} for wider use.
 * </P>
 * <p>
 * The specified methods are implemented by Spring at run-time, using the method name and parameters to deduce the query
 * syntax.
 * </P>
 * <p>
 * See {@link org.springframework.data.repository.query.parser.PartTree PartTree} for details of the query syntax. A
 * simple example being a concatenation:
 * <UL>
 * <LI>'<B>{@code find}</B>' - return results or result</LI>
 * <LI>[optional] '<B>{@code first}<I>nn</I></B>' - limit results to the first <I>nn</I> matches</LI>
 * <LI>'<B>{@code By}</B>' - filters follow this token
 * <LI>[optional] '<I><B>{@code fieldname}</B>'</I> - select on a field in the {@code Map.Entry.getValue()}
 * <LI>[optional] '<B>{@code OrderBy}</B>' - sorts follow this token
 * <LI>[optional] '<I><B>{@code fieldname}</B>'</I> - select on a field in the {@code Map.Entry.getValue()}
 * </UL>
 * </P>
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Neil Stevenson
 */
public interface PersonRepository
        extends HazelcastRepository<Person, String> {

    // Count methods

    public Long countByFirstname(String firstname);

    public Long countByIdLessThanEqual(String id);

    public Long countDistinctLastnameByFirstname(String firstname);

    public Long countByLastnameAllIgnoreCase(String lastname);

    public Long countByFirstnameOrLastnameAllIgnoreCase(String firstname, String lastname);

    public Long countByFirstnameAndLastnameAllIgnoreCase(String firstname, String lastname);

    public Long countByIdAfter(String id);

    public Long countByIdBetween(String firstId, String lastId);

    // Find methods

    public Person findByFirstnameOrLastnameIgnoreCase(String firstname, String lastname);

    public Person findByFirstnameIgnoreCaseOrLastname(String firstname, String lastname);

    public Person findByFirstnameOrLastnameAllIgnoreCase(String firstname, String lastname);

    public Person findFirstIdByOrderById();

    public Person findFirstIdByFirstnameOrderByIdDesc(String firstname);

    public List<Person> findByFirstname(String firstname);

    public List<Person> findByLastnameIsNull();

    public List<Person> findByIsChildTrue();

    public List<Person> findByLastnameRegex(String regex);

    // Underscores are permitted after field names, improving readability slightly
    public List<Person> findByFirstname_AndLastname(String firstname, String lastname);

    // Params are positional unless tagged
    public List<Person> findByFirstnameOrLastname(@Param("lastname") String s1, @Param("firstname") String s2);

    public List<Person> findByFirstnameGreaterThan(String firstname);

    public List<Person> queryByFirstnameLike(String firstname);

    public List<Person> findByFirstnameContains(String firstname);

    public List<Person> findByFirstnameContainsAndLastnameStartsWithAllIgnoreCase(String firstname, String lastname);

    public List<Person> findByFirstnameOrderById(String firstname);

    public List<Person> findByLastnameOrderByIdAsc(String lastname);

    public List<Person> findByFirstnameOrderByLastnameDesc(String firstname);

    public List<Person> findByLastnameIgnoreCase(String lastname);

    public List<Person> findTop3ByOrderByFirstnameAsc();

    public List<Person> findFirst30ByOrderByFirstnameDescLastnameAsc();

    public List<Person> findByFirstnameIn(Collection<String> firstnames);

    public List<Person> findByFirstnameEndsWithAndLastnameNotIn(String firstname, Collection<String> lastnames);

    public Stream<Person> findFirst4By();

    public Stream<Person> streamByLastnameGreaterThanEqual(String lastname);

    // Find methods with special parameters

    public List<Person> findByFirstname(String firstname, Sort sort);

    public List<Person> findByLastnameNotNull(Sort sort);

    public Page<Person> findByLastname(String lastname, Pageable pageable);

    public Page<Person> findByOrderByLastnameDesc(Pageable pageable);

    public Slice<Person> findByIdLike(String pattern, Pageable pageable);

    public Slice<Person> findByIdGreaterThanEqualAndFirstnameGreaterThanAndFirstnameLessThanEqual(String id, String firstname1,
                                                                                                  String firstname2,
                                                                                                  Pageable pageable);

    // Delete methods

    public long deleteByLastname(String firstname);

    public List<Person> deleteByFirstname(String firstname);

    // Query methods

    @Query("firstname=James")
    public List<Person> peoplewiththeirFirstNameIsJames();

    @Query("firstname=%s")
    public List<Person> peoplewiththeirFirstName(String firstName);

    @Query("firstname=%s and lastname=%s")
    public List<Person> peoplewithFirstAndLastName(String firstName, String lastName);

    // Null handling methods

    public Optional<Person> getByLastname(String lastname);
}
