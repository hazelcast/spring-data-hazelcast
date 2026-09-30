package test.utils.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;
import test.utils.TestConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@KeySpace(TestConstants.CINEMA_MAP_NAME)
public class Cinema implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
	@Id
	private String id;
	private String name;
    private String cityName;

    public Cinema() {
    }

    public Cinema(String id, String name, String cityName) {
        this.id = id;
        this.name = name;
        this.cityName = cityName;
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

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cinema cinema)) {
            return false;
        }
        return Objects.equals(id, cinema.id) && Objects.equals(name, cinema.name) && Objects.equals(cityName, cinema.cityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cityName);
    }
}
