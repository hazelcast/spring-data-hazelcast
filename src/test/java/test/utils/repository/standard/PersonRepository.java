package test.utils.repository.standard;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.hazelcast.repository.HazelcastRepository;
import org.springframework.data.hazelcast.repository.query.Query;
import org.springframework.data.repository.query.Param;

import test.utils.domain.Person;

/**
 * <P>
 * Repository class used for tests, re-factored from inner class in {@link EnableHazelcastRepositoriesIT} for wider use.
 * </P>
 * <P>
 * The specified methods are implemented by Spring at run-time, using the method name and parameters to deduce the query
 * syntax.
 * </P>
 * <P>
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
public interface PersonRepository extends HazelcastRepository<Person, String> {

	public Long countByFirstname(String firstname);

	public Long countByIdLessThanEqual(String id);

	public Long countDistinctLastnameByFirstname(String firstname);

	public Person deleteByLastname(String firstname);

	public Person findFirstIdByOrderById();

	public Person findFirstIdByFirstnameOrderByIdDesc(String firstname);

	public List<Person> findByFirstname(String firstname);

	// Underscores are permitted after field names, improving readability slightly
	public List<Person> findByFirstname_AndLastname(String firstname, String lastname);

	// Params are positional unless tagged
	public List<Person> findByFirstnameOrLastname(@Param("lastname") String s1, @Param("firstname") String s2);

	public List<Person> findByFirstnameGreaterThan(String firstname);

	public List<Person> findByFirstnameLike(String firstname);

	public List<Person> findByFirstnameContains(String firstname);

	public List<Person> findByFirstnameContainsAndLastnameStartsWithAllIgnoreCase(String firstname, String lastname);

	public List<Person> findByFirstnameOrderById(String firstname);

	public List<Person> findByLastnameOrderByIdAsc(String lastname);

	public List<Person> findByFirstnameOrderByLastnameDesc(String firstname);

	public List<Person> findByFirstname(String firstname, Sort sort);

	public List<Person> findByLastnameIgnoreCase(String lastname);

	public List<Person> findByLastnameNotNull(Sort sort);

	public List<Person> findFirst3ByOrderByFirstnameAsc();

	public List<Person> findFirst30ByOrderByFirstnameDescLastnameAsc();

	public List<Person> findByFirstnameIn(Collection<String> firstnames);

	public List<Person> findByFirstnameEndsWithAndLastnameNotIn(String firstname, Collection<String> lastnames);

	public Stream<Person> findFirst4By();

	public Stream<Person> streamByLastnameGreaterThanEqual(String lastname);

	public Slice<Person> findByIdGreaterThanEqualAndFirstnameGreaterThanAndFirstnameLessThanEqual(String id,
																								  String firstname1, String firstname2, Pageable pageable);

	public Page<Person> findByLastname(String lastname, Pageable pageable);

	public Page<Person> findByOrderByLastnameDesc(Pageable pageable);

	public Slice<Person> findByIdLike(String pattern, Pageable pageable);

	public Long countByLastnameAllIgnoreCase(String lastname);

	public Long countByFirstnameOrLastnameAllIgnoreCase(String firstname, String lastname);

	public Long countByFirstnameAndLastnameAllIgnoreCase(String firstname, String lastname);

	public Long countByIdAfter(String id);

	public Long countByIdBetween(String firstId, String lastId);

	public Person findByFirstnameOrLastnameAllIgnoreCase(String firstname, String lastname);

	public Person findByFirstnameOrLastnameIgnoreCase(String firstname, String lastname);

	public Person findByFirstnameIgnoreCaseOrLastname(String firstname, String lastname);

	@Query("firstname=James")
	public List<Person> peoplewiththeirFirstNameIsJames();

	@Query("firstname=%s")
	public List<Person> peoplewiththeirFirstName(String firstName);

	@Query("firstname=%s and lastname=%s")
	public List<Person> peoplewithFirstAndLastName(String firstName,String lastName);

}
