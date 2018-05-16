package test.utils.domain;


import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;


import java.io.Serializable;
import java.util.Objects;

/**
 * An entity with no {@link Id} annotation
 *
 * @author Gokhan Oner
 */
@KeySpace
public class NoIdEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NoIdEntity [name=" + name + "]";
    }
}
