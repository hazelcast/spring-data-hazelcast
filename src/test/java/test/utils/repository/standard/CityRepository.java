package test.utils.repository.standard;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Shape;
import org.springframework.data.hazelcast.repository.HazelcastRepository;

import test.utils.domain.City;

/**
 * <p>
 * Repository class used for tests to test geospatial data.
 * </P>
 * <p>
 * The specified methods are implemented by Spring at run-time, using the method name and parameters to deduce the query
 * syntax.
 * </P>
 * <p>
 * See {@link org.springframework.data.repository.query.parser.PartTree PartTree} for details of the query syntax. A
 * simple example being a concatenation:
 * <UL>
 * <LI>'<B>{@code Near}</B>' - sorts entities by distance from a given point</LI>
 * <LI>'<B>{@code Within}</B>' - both sorts and filters entities, returning those within the given distance, range or shape</LI>
 * </UL>
 * </P>
 *
 * @author Ulhas R Manekar
 */
public interface CityRepository
        extends HazelcastRepository<City, String> {

    public List<City> findByLocationNear(Point point, Distance distance);
    
    public Long countByLocationNear(Point point, Distance distance);
    
    public List<City> findByLocationNear(Point point, Object distance);
    
    public List<City> findByLocationNear(Shape shape);
    
    public List<City> findByLocationWithin(Circle circle);
    
    public Page<City> findByLocationNear(Point point, Distance distance, Pageable pageable);
}
