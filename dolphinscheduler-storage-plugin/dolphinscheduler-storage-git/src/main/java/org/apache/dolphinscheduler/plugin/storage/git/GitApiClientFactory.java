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

import org.apache.commons.lang3.StringUtils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating the correct {@link GitApiClient} based on the configured provider.
 */
@Slf4j
@UtilityClass
public final class GitApiClientFactory {

    /**
     * Creates a {@link GitApiClient} based on the provider configuration.
     *
     * @param properties Git storage properties
     * @return configured API client
     */
    public static GitApiClient create(GitStorageProperties properties) {
        GitProvider provider = GitProvider.fromName(properties.getProvider());

        // Default the API URL if not set
        if (StringUtils.isEmpty(properties.getApiUrl())) {
            properties.setApiUrl(provider.getDefaultApiUrl());
        }

        switch (provider) {
            case GITHUB:
                log.info("Creating GitHub API client with url: {}", properties.getApiUrl());
                return new GitHubApiClient(properties);
            case GITLAB:
                log.info("Creating GitLab API client with url: {}", properties.getApiUrl());
                return new GitLabApiClient(properties);
            case GITEA:
                log.info("Creating Gitea API client with url: {}", properties.getApiUrl());
                return new GiteaApiClient(properties);
            case CUSTOM:
            default:
                // For custom URLs, detect based on URL pattern
                return createFromUrl(properties);
        }
    }

    private static GitApiClient createFromUrl(GitStorageProperties properties) {
        String url = properties.getApiUrl() == null ? "" : properties.getApiUrl().toLowerCase();
        if (url.contains("github.com")) {
            log.info("Detected GitHub from API URL: {}", properties.getApiUrl());
            return new GitHubApiClient(properties);
        }
        if (url.contains("gitlab.com") || url.contains("/api/v4")) {
            log.info("Detected GitLab from API URL: {}", properties.getApiUrl());
            return new GitLabApiClient(properties);
        }
        if (url.contains("gitea") || url.contains("/api/v1")) {
            log.info("Detected Gitea from API URL: {}", properties.getApiUrl());
            return new GiteaApiClient(properties);
        }
        // Default to Gitea-compatible (many self-hosted services follow GitHub/Gitea conventions)
        log.warn("Unknown Git provider, defaulting to GitHub-compatible client for url: {}",
                properties.getApiUrl());
        return new GitHubApiClient(properties);
    }
}
