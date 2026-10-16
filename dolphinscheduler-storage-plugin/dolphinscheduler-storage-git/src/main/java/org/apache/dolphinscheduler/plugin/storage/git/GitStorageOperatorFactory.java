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

package org.apache.dolphinscheduler.plugin.storage.git;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperatorFactory;
import org.apache.dolphinscheduler.plugin.storage.api.StorageType;
import org.apache.dolphinscheduler.plugin.storage.api.constants.StorageConstants;

import com.google.auto.service.AutoService;

/**
 * Factory that creates {@link GitStorageOperator} instances based on configuration.
 * AutoService discovers this factory automatically via the SPI mechanism.
 */
@AutoService(StorageOperatorFactory.class)
public class GitStorageOperatorFactory implements StorageOperatorFactory {

    @Override
    public StorageOperator createStorageOperate() {
        final GitStorageProperties properties = buildProperties();
        return new GitStorageOperator(properties);
    }

    private GitStorageProperties buildProperties() {
        return GitStorageProperties.builder()
                .provider(PropertyUtils.getString(StorageConstants.GIT_PROVIDER, "github"))
                .apiUrl(PropertyUtils.getString(StorageConstants.GIT_API_URL, null))
                .repository(PropertyUtils.getString(StorageConstants.GIT_REPOSITORY, ""))
                .branch(PropertyUtils.getString(StorageConstants.GIT_BRANCH, "main"))
                .authToken(PropertyUtils.getString(StorageConstants.GIT_AUTH_TOKEN, ""))
                .commitMessage(PropertyUtils.getString(StorageConstants.GIT_COMMIT_MESSAGE,
                        "DolphinScheduler storage update"))
                .resourceUploadPath(PropertyUtils.getString(StorageConstants.RESOURCE_UPLOAD_PATH, "resources"))
                .build();
    }

    @Override
    public StorageType getStorageOperate() {
        return StorageType.GIT;
    }
}
