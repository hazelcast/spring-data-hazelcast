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

import org.springframework.data.hazelcast.repository.HazelcastRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * <P>Define a generic repository bean for extension by other interfaces.
 * Mark with {@code @NoRepositoryBean} so Spring doesn't try to create one
 * of these at runtime.
 * </P>
 *
 * @author Neil Stevenson
 */
@NoRepositoryBean
public interface MyTitleRepository<T extends Serializable, ID extends Serializable>
        extends HazelcastRepository<T, ID> {

    /**
     * <p>
     * A method generic to {@code MyTitle} objects in
     * the {@code MovieRepository} and {@code SongRepository}
     * </P>
     */
    public int wordsInTitle(String year);

}
