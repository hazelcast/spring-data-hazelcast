/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hazelcast.repository.config;

import java.lang.annotation.Annotation;

import org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

/**
 * Special {@link KeyValueRepositoriesRegistrar} to point the infrastructure to inspect
 * {@link EnableHazelcastRepositories}.
 * 
 * @author Oliver Gierke
 */
class HazelcastRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoriesRegistrar#getAnnotation()
	 */
	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableHazelcastRepositories.class;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getExtension()
	 */
	@Override
	protected RepositoryConfigurationExtension getExtension() {
		return new HazelcastRepositoryConfigurationExtension();
	}

	/**
	 * Hazelcast-specific {@link RepositoryConfigurationExtension}.
	 *
	 * @author Oliver Gierke
	 */
	private static class HazelcastRepositoryConfigurationExtension extends KeyValueRepositoryConfigurationExtension {

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension#getModuleName()
		 */
		@Override
		public String getModuleName() {
			return "Hazelcast";
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension#getModulePrefix()
		 */
		@Override
		protected String getModulePrefix() {
			return "hazelcast";
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension#getDefaultKeyValueTemplateRef()
		 */
		@Override
		protected String getDefaultKeyValueTemplateRef() {
			return "hazelcastKeyValueTemplate";
		}
	}
}
