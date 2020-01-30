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
package org.springframework.data.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Christoph Strobl
 */
public class HazelcastUtils {

    static Config hazelcastConfig() {

        Config hazelcastConfig = new Config();
        hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        hazelcastConfig.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);

        return hazelcastConfig;
    }

    public static HazelcastKeyValueAdapter preconfiguredHazelcastKeyValueAdapter() {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastConfig());
        HazelcastKeyValueAdapter hazelcastKeyValueAdapter = new HazelcastKeyValueAdapter(hazelcastInstance);
        return hazelcastKeyValueAdapter;
    }

}
