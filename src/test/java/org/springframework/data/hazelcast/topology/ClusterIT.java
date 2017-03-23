package org.springframework.data.hazelcast.topology;

import org.springframework.test.context.ActiveProfiles;
import test.utils.TestConstants;

/**
 * <P>
 * Run the {@link AbstractTopologyIT} tests with the server-only profile, that defines a cluster with multiple server
 * nodes.
 * </P>
 * <P>
 * Spring Data Hazelcast selects one of server nodes to connect to, so the tests compare expected outcome against
 * another server node.
 * </P>
 *
 * @author Neil Stevenson
 */
@ActiveProfiles(TestConstants.SPRING_TEST_PROFILE_CLUSTER)
public class ClusterIT extends AbstractTopologyIT {}
