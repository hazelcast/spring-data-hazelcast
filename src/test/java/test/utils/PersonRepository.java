package test.utils;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.hazelcast.repository.HazelcastRepository;
import org.springframework.data.repository.query.Param;

/**
 *<P>Repository class used for tests, re-factored from inner class in {@link EnableHazelcastRepositoriesIT}
 * for wider use.
 *</P>
 *<P>The specified methods are implemented by Spring at run-time, using the method name and parameters to
 *deduce the query syntax.
 *</P>
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
public interface PersonRepository extends HazelcastRepository<Person, String> {

	public Long				countByFirstname(String firstname);
	
    public Person           findMinById();
    
    public List<Person>     findByFirstname(String firstname);

    // Optional underscores for readibility
    public List<Person>     findBy_Firstname_And_Lastname(String firstname, String lastname);
    
    // Params are positional unless tagged
    public List<Person>     findByFirstnameOrLastname(@Param("lastname") String s1, @Param("firstname") String s2);
    
    public List<Person>     findByFirstnameGreaterThan(String firstname);
    
    public List<Person>     findByFirstnameLike(String firstname);
    
    public List<Person>     findByFirstnameOrderById(String firstname);
    
    public List<Person>     findByLastnameOrderByIdAsc(String lastname);
    
    public List<Person>     findByFirstnameOrderByLastnameDesc(String firstname);
    
    public List<Person>     findByFirstname(String firstname, Sort sort);
    
    public List<Person>     findByLastnameIgnoreCase(String lastname);
    
    public List<Person>     findByLastnameNotNull(Sort sort);

    public List<Person>   	findFirst2();

    public List<Person>   	findAllFirst3OrderByFirstname();

    public Stream<Person>   findAllFirst4();

    public Slice<Person>    find();

    public Page<Person>     findFirst2ByLastname(Pageable pageable, String lastname);
}
