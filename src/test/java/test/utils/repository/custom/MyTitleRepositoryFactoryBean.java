package test.utils.repository.custom;

import java.io.Serializable;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.hazelcast.repository.support.HazelcastRepositoryFactoryBean;
import org.springframework.data.keyvalue.core.KeyValueOperations;

/**
 * <P>Factory bean for creating instances of {@link MyTitleRepository},
 * being {@link MovieRepository} and {@link SongRepository}.
 * </P>
 * 
 * @author Neil Stevenson
 *
 * @param <T>  Repository type
 * @param <S>  Domain object type
 * @param <ID> Key of domain object type
 */
public class MyTitleRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
 extends HazelcastRepositoryFactoryBean<T, S, ID> {

	/*
	 * Creates a new {@link MyTitleRepositoryFactoryBean} for the given repository interface.
	 *
	 * @param repositoryInterface must not be {@literal null}.
	 */
	public MyTitleRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}
	/* Create a specialised repository factory.
	 * 
	 * (non-Javadoc)
	 * @see org.springframework.data.hazelcast.repository.support.HazelcastRepositoryFactoryBean#createRepositoryFactory(org.springframework.data.keyvalue.core.KeyValueOperations, java.lang.Class, java.lang.Class)
	 */
	@Override
	protected MyTitleRepositoryFactory createRepositoryFactory(KeyValueOperations operations,
			Class<? extends AbstractQueryCreator<?, ?>> queryCreator, Class<? extends RepositoryQuery> repositoryQueryType) {

		return new MyTitleRepositoryFactory(operations, queryCreator);
	}

}

