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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GitApiClientFactory}.
 */
class GitApiClientFactoryTest {

    @Test
    @DisplayName("Creates GitHub client when provider is github")
    void createsGitHubClient() {
        GitStorageProperties props = GitStorageProperties.builder()
                .provider("github")
                .repository("owner/repo")
                .authToken("token")
                .branch("main")
                .build();

        GitApiClient client = GitApiClientFactory.create(props);

        assertThat(client).isInstanceOf(GitHubApiClient.class);
    }

    @Test
    @DisplayName("Creates GitLab client when provider is gitlab")
    void createsGitLabClient() {
        GitStorageProperties props = GitStorageProperties.builder()
                .provider("gitlab")
                .repository("12345")
                .authToken("token")
                .branch("main")
                .build();

        GitApiClient client = GitApiClientFactory.create(props);

        assertThat(client).isInstanceOf(GitLabApiClient.class);
    }

    @Test
    @DisplayName("Creates Gitea client when provider is gitea")
    void createsGiteaClient() {
        GitStorageProperties props = GitStorageProperties.builder()
                .provider("gitea")
                .repository("owner/repo")
                .authToken("token")
                .branch("main")
                .build();

        GitApiClient client = GitApiClientFactory.create(props);

        assertThat(client).isInstanceOf(GiteaApiClient.class);
    }

    @Test
    @DisplayName("Detects GitHub from URL when provider is custom")
    void detectsGitHubFromUrl() {
        GitStorageProperties props = GitStorageProperties.builder()
                .provider("custom")
                .apiUrl("https://api.github.com")
                .repository("owner/repo")
                .authToken("token")
                .build();

        GitApiClient client = GitApiClientFactory.create(props);

        assertThat(client).isInstanceOf(GitHubApiClient.class);
    }

    @Test
    @DisplayName("Detects GitLab from URL when provider is custom")
    void detectsGitLabFromUrl() {
        GitStorageProperties props = GitStorageProperties.builder()
                .provider("custom")
                .apiUrl("https://gitlab.example.com/api/v4")
                .repository("12345")
                .authToken("token")
                .build();

        GitApiClient client = GitApiClientFactory.create(props);

        assertThat(client).isInstanceOf(GitLabApiClient.class);
    }

    @Test
    @DisplayName("Detects Gitea from URL when provider is custom")
    void detectsGiteaFromUrl() {
        GitStorageProperties props = GitStorageProperties.builder()
                .provider("custom")
                .apiUrl("https://gitea.example.com/api/v1")
                .repository("owner/repo")
                .authToken("token")
                .build();

        GitApiClient client = GitApiClientFactory.create(props);

        assertThat(client).isInstanceOf(GiteaApiClient.class);
    }

    @Test
    @DisplayName("Defaults API URL to GitHub when not set for GitHub provider")
    void defaultsApiUrlForGitHub() {
        GitStorageProperties props = GitStorageProperties.builder()
                .provider("github")
                .repository("owner/repo")
                .authToken("token")
                .build();

        GitApiClientFactory.create(props);

        assertThat(props.getApiUrl()).isEqualTo("https://api.github.com");
    }

    @Test
    @DisplayName("Defaults API URL to GitLab when not set for GitLab provider")
    void defaultsApiUrlForGitLab() {
        GitStorageProperties props = GitStorageProperties.builder()
                .provider("gitlab")
                .repository("12345")
                .authToken("token")
                .build();

        GitApiClientFactory.create(props);

        assertThat(props.getApiUrl()).isEqualTo("https://gitlab.com/api/v4");
    }
}
