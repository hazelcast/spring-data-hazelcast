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

package test.utils;

import test.utils.domain.Song;

/**
 * @author Neil Stevenson
 */
public class TestConstants {

    public static final String CLIENT_INSTANCE_NAME = "hazelcast-instance-client";
    public static final String SERVER_INSTANCE_NAME = "hazelcast-instance-server";

    public static final String SPRING_TEST_PROFILE_CLIENT_SERVER = "client-server";
    public static final String SPRING_TEST_PROFILE_CLUSTER = "cluster";
    public static final String SPRING_TEST_PROFILE_SINGLETON = "singleton";

    public static final String MAKEUP_MAP_NAME = "Make-up";
    public static final String MOVIE_MAP_NAME = "Movie";
    public static final String PERSON_MAP_NAME = "Actors";
    public static final String SONG_MAP_NAME = Song.class.getCanonicalName();
    public static final String CITY_MAP_NAME = "Cities";

    public static final String[] OSCAR_MAP_NAMES = {MAKEUP_MAP_NAME, MOVIE_MAP_NAME, PERSON_MAP_NAME, SONG_MAP_NAME, CITY_MAP_NAME};
}
