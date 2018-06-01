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

package org.springframework.data.hazelcast.repository.config;

import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.data.hazelcast.HazelcastKeyValueAdapter;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

/**
 * Hazelcast-specific {@link RepositoryConfigurationExtension}.
 *
 * @author Oliver Gierke
 * @author Rafal Leszko
 */
class HazelcastRepositoryConfigurationExtension
        extends KeyValueRepositoryConfigurationExtension {

    private static final String HAZELCAST_ADAPTER_BEAN_NAME = "hazelcastKeyValueAdapter";

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension
     *                          #getModuleName()
     */
    @Override
    public String getModuleName() {
        return "Hazelcast";
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension
     *                          #getModulePrefix()
     */
    @Override
    protected String getModulePrefix() {
        return "hazelcast";
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension
     *                          #getDefaultKeyValueTemplateRef()
     */
    @Override
    protected String getDefaultKeyValueTemplateRef() {
        return "keyValueTemplate";
    }

    @Override
    public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource configurationSource) {
        // register HazelcastKeyValueAdapter
        String hazelcastInstanceRef = configurationSource.getAttribute("hazelcastInstanceRef").get();

        RootBeanDefinition hazelcastKeyValueAdapterDefinition = new RootBeanDefinition(HazelcastKeyValueAdapter.class);
        ConstructorArgumentValues constructorArgumentValuesForHazelcastKeyValueAdapter = new ConstructorArgumentValues();
        constructorArgumentValuesForHazelcastKeyValueAdapter
                .addIndexedArgumentValue(0, new RuntimeBeanReference(hazelcastInstanceRef));
        hazelcastKeyValueAdapterDefinition.setConstructorArgumentValues(constructorArgumentValuesForHazelcastKeyValueAdapter);
        registerIfNotAlreadyRegistered(hazelcastKeyValueAdapterDefinition, registry, HAZELCAST_ADAPTER_BEAN_NAME,
                configurationSource);

        super.registerBeansForRoot(registry, configurationSource);
    }

    @Override
    protected AbstractBeanDefinition getDefaultKeyValueTemplateBeanDefinition(RepositoryConfigurationSource configurationSource) {
        RootBeanDefinition keyValueTemplateDefinition = new RootBeanDefinition(KeyValueTemplate.class);
        ConstructorArgumentValues constructorArgumentValuesForKeyValueTemplate = new ConstructorArgumentValues();
        constructorArgumentValuesForKeyValueTemplate
                .addIndexedArgumentValue(0, new RuntimeBeanReference(HAZELCAST_ADAPTER_BEAN_NAME));
        constructorArgumentValuesForKeyValueTemplate
                .addIndexedArgumentValue(1, new RuntimeBeanReference(MAPPING_CONTEXT_BEAN_NAME));

        keyValueTemplateDefinition.setConstructorArgumentValues(constructorArgumentValuesForKeyValueTemplate);

        return keyValueTemplateDefinition;
    }

}
