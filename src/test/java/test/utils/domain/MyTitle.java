package test.utils.domain;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

/**
 * <P>
 * An abstract base clase for key-value objects that have a string
 * key and another string field.
 * </P>
 * 
 * @see {@link Movie}
 * @see {@link Song}
 * 
 * @author Neil Stevenson
 */
public abstract class MyTitle implements Comparable<MyTitle>, Serializable {
	private static final long serialVersionUID = 1L;

	@Id private String id;
	private String title;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		MyTitle other = (MyTitle) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	
	// Sort by title only
	@Override
	public int compareTo(MyTitle that) {
		return this.title.compareTo(that.getTitle());
	}

}
