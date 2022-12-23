/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.storage.api;

import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_STORAGE_TYPE;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.util.Optional;
import java.util.ServiceLoader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// todo: If we move the config to yaml
@Configuration
public class StorageConfiguration {

    @Bean
    public StorageOperate storageOperate() {
        Optional<StorageType> storageTypeOptional =
                StorageType.getStorageType(PropertyUtils.getUpperCaseString(RESOURCE_STORAGE_TYPE));
        Optional<StorageOperate> storageOperate = storageTypeOptional.map(storageType -> {
            ServiceLoader<StorageOperateFactory> storageOperateFactories =
                    ServiceLoader.load(StorageOperateFactory.class);
            for (StorageOperateFactory storageOperateFactory : storageOperateFactories) {
                if (storageOperateFactory.getStorageOperate() == storageType) {
                    return storageOperateFactory.createStorageOperate();
                }
            }
            return null;
        });
        return storageOperate.orElse(null);
    }
}
