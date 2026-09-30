package test.utils.repository.standard;

import org.springframework.data.hazelcast.repository.HazelcastRepository;
import test.utils.domain.Cinema;

import java.util.List;

/**
 * Repository used to test existsBy and findFirst/findTop.
 */
public interface CinemaRepository
    extends HazelcastRepository<Cinema, String> {

    public List<Cinema> findFirst3ByCityName(String cityName);
}
