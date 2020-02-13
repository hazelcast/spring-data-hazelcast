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

import org.springframework.data.keyvalue.annotation.KeySpace;

/**
 * <P>A {@code Song} is a kind of {@link MyTitle}, as the fields here are just the
 * year and the song title.
 * </P>
 * <p>
 * Don't provide a value for the {@code @KeySpace} annotation, so the map name
 * will default to the fully qualified class name, "{@code test.utils.Song}".
 * </P>
 *
 * @author Neil Stevenson
 */
@KeySpace
public class Song
        extends MyTitle {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "Song [id=" + super.getId() + ", title=" + super.getTitle() + "]";
    }

}