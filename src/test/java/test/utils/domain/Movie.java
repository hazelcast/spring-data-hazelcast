/*
* Copyright 2020 Hazelcast Inc.
*
* Licensed under the Hazelcast Community License (the "License"); you may not use
* this file except in compliance with the License. You may obtain a copy of the
* License at
*
* http://hazelcast.com/hazelcast-community-license
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OF ANY KIND, either express or implied. See the License for the
* specific language governing permissions and limitations under the License.
*/

package test.utils.domain;

import org.springframework.data.keyvalue.annotation.KeySpace;
import test.utils.TestConstants;

/**
 * <P>A {@code Movie} is a kind of {@link MyTitle}, as the fields here are just the
 * year and the movie title.
 * </P>
 * <p>
 * Give an argument {@code @KeySpace} to name the map used, "{@code Movie}"
 * rather than the default fully class name.
 * </P>
 *
 * @author Neil Stevenson
 */
@KeySpace(TestConstants.MOVIE_MAP_NAME)
public class Movie
        extends MyTitle {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "Movie [id=" + super.getId() + ", title=" + super.getTitle() + "]";
    }

}
