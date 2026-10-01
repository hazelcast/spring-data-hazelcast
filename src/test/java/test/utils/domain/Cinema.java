package test.utils.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;
import test.utils.TestConstants;

import java.io.Serial;
import java.io.Serializable;

@KeySpace(TestConstants.CINEMA_MAP_NAME)
public record Cinema (@Id String id, String name, String cityName) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
