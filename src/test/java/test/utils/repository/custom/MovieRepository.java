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

package test.utils.repository.custom;

import test.utils.domain.Movie;

import java.util.List;

/**
 * <P>Extend the {@link MyTitleRepository} for movie titles,
 * inheriting some methods application to the base class {@link MyTitle}
 * and adding some methods specific to {@link Movie} objects.
 * </P>
 *
 * @author Neil Stevenson
 */
public interface MovieRepository
        extends MyTitleRepository<Movie, String> {

    /**
     * <p>
     * This could be generic behaviour, but make specific
     * to {@link MovieRepository} for tests.
     * </P>
     *
     * @param text A word in the title
     * @return Matches
     */
    public List<Movie> findByTitleLike(String text);

}
