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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration properties for Git-based storage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitStorageProperties {

    /**
     * The Git repository provider: github, gitlab, gitea, or custom.
     */
    private String provider;

    /**
     * The base API URL for the Git provider.
     * For GitHub: https://api.github.com (default)
     * For GitLab: https://gitlab.com/api/v4 (default)
     * For Gitea: https://gitea.com/api/v1 (default)
     */
    private String apiUrl;

    /**
     * The repository name in format "owner/repo" (GitHub/Gitea) or numeric/project path (GitLab).
     * For GitHub/Gitea: "apache/dolphinscheduler"
     * For GitLab: "12345" or "group/project"
     */
    private String repository;

    /**
     * The branch to use. Default is "main".
     */
    @Builder.Default
    private String branch = "main";

    /**
     * The authentication token (Personal Access Token).
     */
    private String authToken;

    /**
     * The commit message prefix used when making changes.
     */
    @Builder.Default
    private String commitMessage = "DolphinScheduler storage update";

    /**
     * The base directory in the repository where resources are stored.
     */
    @Builder.Default
    private String resourceUploadPath = "resources";

    /**
     * The username used for commits (optional, defaults to "dolphinscheduler-bot").
     */
    @Builder.Default
    private String commitAuthor = "dolphinscheduler-bot";

    /**
     * The email used for commits (optional).
     */
    @Builder.Default
    private String commitAuthorEmail = "dolphinscheduler@apache.org";

    /**
     * Connect timeout in milliseconds.
     */
    @Builder.Default
    private int connectTimeout = 10000;

    /**
     * Read timeout in milliseconds.
     */
    @Builder.Default
    private int readTimeout = 30000;

    /**
     * Write timeout in milliseconds.
     */
    @Builder.Default
    private int writeTimeout = 30000;
}
