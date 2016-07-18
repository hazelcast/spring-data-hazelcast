package test.utils.repository.custom;

import java.io.Serializable;
import org.springframework.data.hazelcast.repository.HazelcastRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * <P>Define a generic repository bean for extension by other interfaces.
 * Mark with {@code @NoRepositoryBean} so Spring doesn't try to create one
 * of these at runtime.
 * </P>
 *
 * @author Neil Stevenson
 */
@NoRepositoryBean
public interface MyTitleRepository<T extends Serializable, ID extends Serializable> extends HazelcastRepository<T, ID> {

	/**
	 * <P> 
	 * A method generic to {@code MyTitle} objects in
	 * the {@code MovieRepository} and {@code SongRepository}
	 * </P>
	 */
	public int wordsInTitle(String year);

}
