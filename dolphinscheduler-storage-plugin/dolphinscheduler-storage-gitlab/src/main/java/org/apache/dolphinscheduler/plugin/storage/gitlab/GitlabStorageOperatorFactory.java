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

package org.apache.dolphinscheduler.plugin.storage.gitlab;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperatorFactory;
import org.apache.dolphinscheduler.plugin.storage.api.StorageType;
import org.apache.dolphinscheduler.plugin.storage.api.constants.StorageConstants;

import org.apache.commons.lang3.StringUtils;

import org.gitlab4j.api.GitLabApi;

import com.google.auto.service.AutoService;

@AutoService(StorageOperatorFactory.class)
public class GitlabStorageOperatorFactory implements StorageOperatorFactory {

    @Override
    public StorageOperator createStorageOperate() {
        int projectId = PropertyUtils.getInt(StorageConstants.GITLAB_STORAGE_PROJECT_ID, 0);
        String branchRef = PropertyUtils.getString(StorageConstants.GITLAB_STORAGE_BRANCH_REF, StringUtils.EMPTY);
        // gitlab storage base dir is empty.
        return new GitlabStorageOperator(StringUtils.EMPTY, getGitLabApi(), projectId, branchRef);
    }

    private GitLabApi getGitLabApi() {
        String gitlabHost = PropertyUtils.getString(StorageConstants.GITLAB_STORAGE_HOST, "localhost");
        String privateToken = PropertyUtils.getString(StorageConstants.GITLAB_STORAGE_PRIVATE_TOKEN, StringUtils.EMPTY);
        return new GitLabApi(GitLabApi.ApiVersion.V4, gitlabHost, privateToken);
    }

    @Override
    public StorageType getStorageOperate() {
        return StorageType.GITLAB;
    }
}
