package test.utils;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import org.springframework.data.hazelcast.repository.config.Constants;
import test.utils.repository.custom.MyTitleRepositoryFactoryBean;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.hazelcast.HazelcastKeyValueAdapter;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.KeyValueTemplate;

/**
 * <P>
 * Bootstrap Spring objects as part of test context. Creates a {@link HazelcastInstance} {@code @Bean} for all tests,
 * and uses {@link EnableHazelcastRepositories @EnableHazelcastRepositories} to initiate a class scan to load
 * repositories and domain classes. Depending on the Spring active profile, the Hazelcast instance may be isolated or
 * connected to others in this JVM.
 * </P>
 * <P>
 * Package scanning adds standard repositories, from "{@code test.utils.repository.standard}",
 * using {@link HazelcastRepositoryFactoryBean}.
 * </P>
 *
 * @author Neil Stevenson
 */
@Configuration
@EnableHazelcastRepositories(basePackages="test.utils.repository.standard")
public class InstanceHelper {
	private static final Logger LOG = LoggerFactory.getLogger(InstanceHelper.class);
	private static final String CLUSTER_HOST = "127.0.0.1";
	private static final int CLUSTER_PORT = 5701;
	private static final String MASTER_SERVER = CLUSTER_HOST + ":" + CLUSTER_PORT;

	static {
		System.setProperty("hazelcast.logging.type", "slf4j");
	}

	@Resource(name = Constants.HAZELCAST_INSTANCE_NAME) private HazelcastInstance hazelcastInstance;

	/**
	 * <P>The {@code @EnableHazelcastRepositories} annotation is not repeatable,
	 * so use an inner class to scan a second package.
	 * </P>
	 */
	@EnableHazelcastRepositories(basePackages="test.utils.repository.custom",
		repositoryFactoryBeanClass=MyTitleRepositoryFactoryBean.class)
	static class InstanceHelperInner {
	}
	
	/**
	 * <P>
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
	 * <P>
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
				if (Constants.HAZELCAST_INSTANCE_NAME.equals(hazelcastInstance.getName())) {
					testInstanceWasRunning = true;
				}
				LOG.debug("Closing '{}'", hazelcastInstance);
				hazelcastInstance.shutdown();
			}
		}
		;

		if (testInstanceWasRunning) {
			LOG.error("'{}' was still running", Constants.HAZELCAST_INSTANCE_NAME);
		} else {
			LOG.debug("'{}' already closed by Spring", Constants.HAZELCAST_INSTANCE_NAME);
		}
	}

	/**
	 * <P>
	 * A single Hazelcast instance is the simplest, and sufficient for most of the tests.
	 * </P>
	 */
	@Configuration
	@Profile(TestConstants.SPRING_TEST_PROFILE_SINGLETON)
	public static class Singleton {
		/**
		 * <P>
		 * Create a singleton {@link HazelcastInstance} server {@code @Bean}.
		 * </P>
		 * 
		 * @return A standalone Hazelcast instance, a cluster of one
		 */
		@Bean(name = Constants.HAZELCAST_INSTANCE_NAME)
		public HazelcastInstance singleton() {
			HazelcastInstance hazelcastInstance = InstanceHelper.makeServer(Constants.HAZELCAST_INSTANCE_NAME,
					CLUSTER_PORT);
			LOG.trace("@Bean=='{}'", hazelcastInstance);
			return hazelcastInstance;
		}
	}

	/**
	 * <P>
	 * Create a cluster with more than one instance, for use in more complex tests.
	 * </P>
	 * <P>
	 * Although one per JVM is more typical, create them all in the one JVM to simplify control from JUnit.
	 * </P>
	 * <P>
	 * To avoid overloading the JVM, "multiple" instances means 2.
	 * </P>
	 */
	@Configuration
	@Profile(TestConstants.SPRING_TEST_PROFILE_CLUSTER)
	public static class Cluster {
		/**
		 * <P>
		 * Create two {@link HazelcastInstance} server {@code @Bean}s clustered together.
		 * </P>
		 * 
		 * @return One of two Hazelcast instances created.
		 */
		@Bean(name = Constants.HAZELCAST_INSTANCE_NAME)
		public HazelcastInstance cluster() {
			HazelcastInstance hazelcastInstance = InstanceHelper.makeServer(Constants.HAZELCAST_INSTANCE_NAME,
					CLUSTER_PORT);
			LOG.trace("@Bean == '{}'", hazelcastInstance);

			InstanceHelper.makeServer("Not" + Constants.HAZELCAST_INSTANCE_NAME, (1 + CLUSTER_PORT));

			return hazelcastInstance;
		}
	}

	/**
	 * <P>
	 * Create a client-server topology, for use in more complex tests.
	 * </P>
	 * <P>
	 * As per {@code Constants.SPRING_TEST_PROFILE_CLUSTER}, these instances are all in the one JVM and the minimum number
	 * (1 client, 1 server) is made.
	 * </P>
	 */
	@Configuration
	@Profile(TestConstants.SPRING_TEST_PROFILE_CLIENT_SERVER)
	public static class ClientServer {
		/**
		 * <P>
		 * Create a client-server topology, using one {@link HazelcastInstance} server and one {@link HazelcastInstance}
		 * client. The client is the returned as the {@code @Bean}.
		 * </P>
		 * 
		 * @return The client Hazelcast instance.
		 */
		@Bean(name = Constants.HAZELCAST_INSTANCE_NAME)
		public HazelcastInstance cluster() {
			InstanceHelper.makeServer("Not" + Constants.HAZELCAST_INSTANCE_NAME, CLUSTER_PORT);

			HazelcastInstance hazelcastInstance = InstanceHelper.makeClient(Constants.HAZELCAST_INSTANCE_NAME);
			LOG.trace("@Bean == '{}'", hazelcastInstance);

			return hazelcastInstance;
		}
	}

	/**
	 * <P>
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
	 * <P>
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

}
