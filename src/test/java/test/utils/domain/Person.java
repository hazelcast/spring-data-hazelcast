/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.utils.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;
import test.utils.TestConstants;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * Domain class used for tests, re-factored from inner class in {@link EnableHazelcastRepositoriesIT} for wider use.
 * </P>
 * <p>
 * A simple Pojo for the <I>value</I> part of a key-value store. Although it is not necessary for the <I>value</I>
 * object to contain the <I>key</I>, this is done here and the field is indicated with {@code @Id}.
 * </P>
 * <p>
 * Use {@code @KeySpace} to name the map used for storage, "@{code Actors}".
 * </P>
 *
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Neil Stevenson
 */
@KeySpace(TestConstants.PERSON_MAP_NAME)
public class Person
        implements Comparable<Person>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String firstname;
    private String lastname;
    private boolean isChild = false;

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

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        isChild = child;
    }

    // Sort by lastname then firstname
    @Override
    public int compareTo(Person that) {
        int lastnameCompare = this.lastname.compareTo(that.getLastname());
        return (lastnameCompare != 0 ? lastnameCompare : this.firstname.compareTo(that.getFirstname()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Person person = (Person) o;
        return isChild == person.isChild && Objects.equals(id, person.id) && Objects.equals(firstname, person.firstname)
                && Objects.equals(lastname, person.lastname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstname, lastname, isChild);
    }

    @Override
    public String toString() {
        return "Person{" + "id='" + id + '\'' + ", firstname='" + firstname + '\'' + ", lastname='" + lastname + '\''
                + ", isChild=" + isChild + '}';
    }
}
