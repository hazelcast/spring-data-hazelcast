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

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.hazelcast.HazelcastKeyValueAdapter;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import test.utils.repository.custom.MyTitleRepositoryFactoryBean;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Set;

/**
 * <p>
 * Bootstrap Spring objects as part of test context. Creates a {@link HazelcastInstance} {@code @Bean} for all tests,
 * and uses {@link EnableHazelcastRepositories @EnableHazelcastRepositories} to initiate a class scan to load
 * repositories and domain classes. Depending on the Spring active profile, the Hazelcast instance may be isolated or
 * connected to others in this JVM.
 * </P>
 * <p>
 * Package scanning adds standard repositories, from "{@code test.utils.repository.standard}",
 * using {@link HazelcastRepositoryFactoryBean}.
 * </P>
 *
 * @author Neil Stevenson
 */
@Configuration
@EnableHazelcastRepositories(basePackages = "test.utils.repository.standard")
public class InstanceHelper {
    private static final Logger LOG = LoggerFactory.getLogger(InstanceHelper.class);
    private static final String CLUSTER_HOST = "127.0.0.1";
    private static final int CLUSTER_PORT = 5701;
    private static final String MASTER_SERVER = CLUSTER_HOST + ":" + CLUSTER_PORT;
    @Resource(name = TestConstants.CLIENT_INSTANCE_NAME)
    private HazelcastInstance hazelcastInstance;

    /**
     * <p>
     * Create a cluster using {@code 127.0.0.1:5701} as the master. The master must be created first, and may be the only
     * server instance in this JVM.
     * </P>
     *
     * @param name Enables easy identification
     * @param port The only port this server can use.
     * @return The master or the 2nd server in the cluster
     */
    public static HazelcastInstance makeServer(final String name, final int port) {
        Config hazelcastConfig = new Config(name);

        hazelcastConfig.getNetworkConfig().setReuseAddress(true);

        hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        hazelcastConfig.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);

        TcpIpConfig tcpIpConfig = hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        tcpIpConfig.setMembers(Arrays.asList(MASTER_SERVER));
        tcpIpConfig.setRequiredMember(MASTER_SERVER);

        hazelcastConfig.getNetworkConfig().setPort(port);
        hazelcastConfig.getNetworkConfig().setPortAutoIncrement(false);

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastConfig);

        LOG.debug("Created {}", hazelcastInstance);

        return hazelcastInstance;
    }

    /**
     * <p>
     * Create a client that can connect to the cluster via the master server {@code 127.0.0.1:5701}. The server will be in
     * the same JVM, but connect via the network.
     * </P>
     *
     * @param name The client's instance name
     * @return A client in a client-server topology.
     */
    public static HazelcastInstance makeClient(final String name) {
        ClientConfig clientConfig = new ClientConfig();

        clientConfig.setInstanceName(name);
        clientConfig.getNetworkConfig().setAddresses(Arrays.asList(MASTER_SERVER));
        clientConfig.getNetworkConfig().setConnectionAttemptLimit(1);

        HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

        LOG.debug("Created {}", hazelcastInstance);

        return hazelcastInstance;
    }

    /**
     * <p>
     * {@link org.springframework.data.keyvalue.core.KeyValueOperations KeyValueOperations} are implemented by a
     * {@link org.springframework.data.keyvalue.core.KeyValueTemplate KeyValueTemplate} that uses an adapter class
     * encapsulating the implementation.
     * </P>
     *
     * @return One Hazelcast instance wrapped as a key/value implementation
     */
    @Bean
    public KeyValueOperations keyValueTemplate() {
        HazelcastKeyValueAdapter hazelcastKeyValueAdapter = new HazelcastKeyValueAdapter(this.hazelcastInstance);
        return new KeyValueTemplate(hazelcastKeyValueAdapter);
    }

    /**
     * <p>
     * Spring will shutdown the test Hazelcast instance, as the {@code @Bean} is defined as a
     * {@link org.springframework.beans.factory.DisposableBean}. Shut down any other server instances started, which may
     * be needed for cluster tests.
     * </P>
     */
    @PreDestroy
    public void preDestroy() {
        boolean testInstanceWasRunning = false;

        Set<HazelcastInstance> hazelcastInstances = Hazelcast.getAllHazelcastInstances();
        if (hazelcastInstances.size() != 0) {
            for (HazelcastInstance hazelcastInstance : hazelcastInstances) {
                if (TestConstants.CLIENT_INSTANCE_NAME.equals(hazelcastInstance.getName())) {
                    testInstanceWasRunning = true;
                }
                LOG.debug("Closing '{}'", hazelcastInstance);
                hazelcastInstance.shutdown();
            }
        }
        ;

        if (testInstanceWasRunning) {
            LOG.error("'{}' was still running", TestConstants.CLIENT_INSTANCE_NAME);
        } else {
            LOG.debug("'{}' already closed by Spring", TestConstants.CLIENT_INSTANCE_NAME);
        }
    }

    /**
     * <P>The {@code @EnableHazelcastRepositories} annotation is not repeatable,
     * so use an inner class to scan a second package.
     * </P>
     */
    @EnableHazelcastRepositories(basePackages = "test.utils.repository.custom", repositoryFactoryBeanClass = MyTitleRepositoryFactoryBean.class)
    static class InstanceHelperInner {
    }

    /**
     * <p>
     * A single Hazelcast instance is the simplest, and sufficient for most of the tests.
     * </P>
     */
    @Configuration
    @Profile(TestConstants.SPRING_TEST_PROFILE_SINGLETON)
    public static class Singleton {
        /**
         * <p>
         * Create a singleton {@link HazelcastInstance} server {@code @Bean}.
         * </P>
         *
         * @return A standalone Hazelcast instance, a cluster of one
         */
        @Bean(name = TestConstants.CLIENT_INSTANCE_NAME)
        public HazelcastInstance singleton() {
            HazelcastInstance hazelcastInstance = InstanceHelper.makeServer(TestConstants.CLIENT_INSTANCE_NAME, CLUSTER_PORT);
            LOG.trace("@Bean=='{}'", hazelcastInstance);
            return hazelcastInstance;
        }
    }

    /**
     * <p>
     * Create a cluster with more than one instance, for use in more complex tests.
     * </P>
     * <p>
     * Although one per JVM is more typical, create them all in the one JVM to simplify control from JUnit.
     * </P>
     * <p>
     * To avoid overloading the JVM, "multiple" instances means 2.
     * </P>
     */
    @Configuration
    @Profile(TestConstants.SPRING_TEST_PROFILE_CLUSTER)
    public static class Cluster {
        /**
         * <p>
         * Create two {@link HazelcastInstance} server {@code @Bean}s clustered together.
         * </P>
         *
         * @return One of two Hazelcast instances created.
         */
        @Bean(name = TestConstants.CLIENT_INSTANCE_NAME)
        public HazelcastInstance cluster() {
            HazelcastInstance hazelcastInstance = InstanceHelper.makeServer(TestConstants.CLIENT_INSTANCE_NAME, CLUSTER_PORT);
            LOG.trace("@Bean == '{}'", hazelcastInstance);

            InstanceHelper.makeServer(TestConstants.SERVER_INSTANCE_NAME, (1 + CLUSTER_PORT));

            return hazelcastInstance;
        }
    }

    /**
     * <p>
     * Create a client-server topology, for use in more complex tests.
     * </P>
     * <p>
     * As per {@code Constants.SPRING_TEST_PROFILE_CLUSTER}, these instances are all in the one JVM and the minimum number
     * (1 client, 1 server) is made.
     * </P>
     */
    @Configuration
    @Profile(TestConstants.SPRING_TEST_PROFILE_CLIENT_SERVER)
    public static class ClientServer {
        /**
         * <p>
         * Create a client-server topology, using one {@link HazelcastInstance} server and one {@link HazelcastInstance}
         * client. The client is the returned as the {@code @Bean}.
         * </P>
         *
         * @return The client Hazelcast instance.
         */
        @Bean(name = TestConstants.CLIENT_INSTANCE_NAME)
        public HazelcastInstance cluster() {
            InstanceHelper.makeServer(TestConstants.SERVER_INSTANCE_NAME, CLUSTER_PORT);

            HazelcastInstance hazelcastInstance = InstanceHelper.makeClient(TestConstants.CLIENT_INSTANCE_NAME);
            LOG.trace("@Bean == '{}'", hazelcastInstance);

            return hazelcastInstance;
        }
    }

    static {
        System.setProperty("hazelcast.logging.type", "slf4j");
    }

}