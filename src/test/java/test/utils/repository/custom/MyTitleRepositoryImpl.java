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

import org.springframework.data.hazelcast.repository.support.SimpleHazelcastRepository;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.repository.core.EntityInformation;
import test.utils.domain.MyTitle;

import java.io.Serializable;

/**
 * <P>Implement a custom repository for {@link MyTitleRepository}, but
 * inherit most of the behaviour from {@link SimpleHazelcastRepository}.
 * </P>
 *
 * @param <T>  The domain object
 * @param <ID> The key of the domain object
 * @author Neil Stevenson
 */
public class MyTitleRepositoryImpl<T extends Serializable, ID extends Serializable>
        extends SimpleHazelcastRepository<T, ID>
        implements MyTitleRepository<T, ID> {

    public MyTitleRepositoryImpl(EntityInformation<T, ID> metadata, KeyValueOperations keyValueOperations) {
        super(metadata, keyValueOperations);
    }

    /**
     * <p>
     * Count the words in a particular title.
     * </P>
     *
     * @param Key to lookup
     * @return Tokens in string, -1 if not found
     */
    public int wordsInTitle(String year) {
        @SuppressWarnings("unchecked") MyTitle myTitle = (MyTitle) super.findById((ID) year).orElse(null);

        if (myTitle == null) {
            return -1;
        } else {
            return myTitle.getTitle().split("[-\\s]").length;
        }
    }

}
