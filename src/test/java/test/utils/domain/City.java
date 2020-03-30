package test.utils.domain;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.keyvalue.annotation.KeySpace;

import test.utils.TestConstants;

/**
 * Domain class used for tests to test geospatial data.
 *
 * @author Ulhas R Manekar
 */
@KeySpace(TestConstants.CITY_MAP_NAME)
public class City 
        implements Comparable<City>, Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	private String name;
	private Point location;

	public City() {
		super();
	}

	public City(String id, String name, Point location) {
		super();
		this.id = id;
		this.name = name;
		this.location = location;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, location);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		City city = (City) o;
		return Objects.equals(id, city.id) && Objects.equals(name, city.name)
				&& Objects.equals(location, city.location);
	}

    // Sort by name
    @Override
    public int compareTo(City that) {
        return this.name.compareTo(that.getName());
    }
}
