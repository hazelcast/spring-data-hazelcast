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

package org.springframework.data.hazelcast.topology;

import org.springframework.test.context.ActiveProfiles;
import test.utils.TestConstants;

/**
 * <p>
 * Run the {@link AbstractTopologyIT} tests with the server-only profile, that defines a cluster with multiple server
 * nodes.
 * </P>
 * <p>
 * Spring Data Hazelcast selects one of server nodes to connect to, so the tests compare expected outcome against
 * another server node.
 * </P>
 *
 * @author Neil Stevenson
 */
@ActiveProfiles(TestConstants.SPRING_TEST_PROFILE_CLUSTER)
public class ClusterIT
        extends AbstractTopologyIT {
}
