package test.utils.domain;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import test.utils.TestConstants;

/**
 * <P>
 * Domain class used for tests, re-factored from inner class in {@link EnableHazelcastRepositoriesIT} for wider use.
 * </P>
 * <P>
 * A simple Pojo for the <I>value</I> part of a key-value store. Although it is not necessary for the <I>value</I>
 * object to contain the <I>key</I>, this is done here and the field is indicated with {@code @Id}.
 * </P>
 * <P>
 * Use {@code @KeySpace} to name the map used for storage, "@{code Actors}".
 * </P>
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Neil Stevenson
 */
@KeySpace(TestConstants.PERSON_MAP_NAME)
public class Person implements Comparable<Person>, Serializable {

	private static final long serialVersionUID = 1L;

	@Id private String id;
	private String firstname;
	private String lastname;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	// Sort by lastname then firstname
	@Override
	public int compareTo(Person that) {
		int lastnameCompare = this.lastname.compareTo(that.getLastname());
		return (lastnameCompare != 0 ? lastnameCompare : this.firstname.compareTo(that.getFirstname()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstname == null) ? 0 : firstname.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lastname == null) ? 0 : lastname.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (firstname == null) {
			if (other.firstname != null)
				return false;
		} else if (!firstname.equals(other.firstname))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lastname == null) {
			if (other.lastname != null)
				return false;
		} else if (!lastname.equals(other.lastname))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", firstname=" + firstname + ", lastname=" + lastname + "]";
	}

}
