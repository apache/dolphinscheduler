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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * GitLab REST API client implementing the {@link GitApiClient} contract.
 * Uses the GitLab Repository Files API (https://docs.gitlab.com/ee/api/repository_files.html).
 */
@Slf4j
public class GitLabApiClient extends GitApiClient {

    public GitLabApiClient(GitStorageProperties properties) {
        super(properties);
    }

    @Override
    public String getProviderName() {
        return "GitLab";
    }

    @Override
    protected Headers buildAuthHeaders() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", GitStorageConstants.GITLAB_API_ACCEPT);
        headerMap.put("Content-Type", GitStorageConstants.CONTENT_TYPE_JSON);
        if (StringUtils.isNotEmpty(properties.getAuthToken())) {
            headerMap.put(GitStorageConstants.HEADER_PRIVATE_TOKEN, properties.getAuthToken());
        }
        return Headers.of(headerMap);
    }

    @Override
    public List<GitFileEntry> listContents(String path) {
        String repo = urlEncode(properties.getRepository());
        String ref = properties.getBranch();
        String encodedPath = encodeFilePathForUrl(stripLeadingSlash(path));
        String url = buildFullUrl("/projects/" + repo + "/repository/tree");

        Map<String, String> params = new HashMap<>();
        params.put("ref", ref);
        if (StringUtils.isNotEmpty(encodedPath)) {
            params.put("path", encodedPath);
        }

        String response = httpGet(url, params);
        JsonNode node = JsonUtil.parse(response);

        List<GitFileEntry> entries = new java.util.ArrayList<>();
        if (node.isArray()) {
            Iterator<JsonNode> it = node.elements();
            while (it.hasNext()) {
                entries.add(parseFileEntry(it.next()));
            }
        }
        return entries;
    }

    @Override
    public Optional<GitFileEntry> getFile(String path) {
        String repo = urlEncode(properties.getRepository());
        String encodedPath = encodeFilePathForUrl(stripLeadingSlash(path));

        // Try as a file first
        try {
            String url = buildFullUrl("/projects/" + repo + "/repository/files/" + encodedPath);
            Map<String, String> params = new HashMap<>();
            params.put("ref", properties.getBranch());
            String response = httpGet(url, params);
            JsonNode node = JsonUtil.parse(response);
            JsonNode fileName = node.get("file_name");
            if (fileName != null) {
                // This is a file – but we need to return an entry
                GitFileEntry entry = GitFileEntry.builder()
                        .name(fileName.asText())
                        .path(stripLeadingSlash(path))
                        .sha(textOrNull(node.get("blob_id")))
                        .size(textOrNull(node.get("size")) != null ? node.get("size").asLong() : 0L)
                        .htmlUrl(textOrNull(node.get("web_url")))
                        .downloadUrl(null)
                        .type(GitStorageConstants.TYPE_FILE)
                        .build();
                return Optional.of(entry);
            }
        } catch (GitApiException e) {
            if (e.getStatusCode() != GitStorageConstants.HTTP_NOT_FOUND) {
                throw e;
            }
        }

        // Try as directory – list tree and see if any children exist
        try {
            List<GitFileEntry> children = listContents(stripLeadingSlash(path));
            if (!children.isEmpty()) {
                String dirName = extractDirName(path);
                GitFileEntry entry = GitFileEntry.builder()
                        .name(dirName)
                        .path(stripLeadingSlash(path))
                        .type(GitStorageConstants.TYPE_DIR)
                        .build();
                return Optional.of(entry);
            }
        } catch (GitApiException e) {
            if (e.getStatusCode() != GitStorageConstants.HTTP_NOT_FOUND) {
                throw e;
            }
        }

        return Optional.empty();
    }

    @Override
    public String getFileContent(String path) {
        Optional<String> content = getFileContentBase64(path);
        if (!content.isPresent()) {
            throw new GitApiException("File not found: " + path);
        }
        return content.get();
    }

    @Override
    public Optional<String> getFileContentBase64(String path) {
        String repo = urlEncode(properties.getRepository());
        String encodedPath = encodeFilePathForUrl(stripLeadingSlash(path));
        String url = buildFullUrl("/projects/" + repo + "/repository/files/" + encodedPath);

        Map<String, String> params = new HashMap<>();
        params.put("ref", properties.getBranch());

        try {
            String response = httpGet(url, params);
            JsonNode node = JsonUtil.parse(response);
            JsonNode encodingNode = node.get("encoding");
            JsonNode contentNode = node.get("content");
            if (contentNode == null || contentNode.isNull()) {
                return Optional.empty();
            }
            String content = contentNode.asText();
            String encoding = (encodingNode != null && !encodingNode.isNull()) ? encodingNode.asText() : "";
            if ("base64".equalsIgnoreCase(encoding)) {
                return Optional.of(content);
            }
            // GitLab may return raw content
            return Optional.of(encodeBase64(content));
        } catch (GitApiException e) {
            if (e.getStatusCode() == GitStorageConstants.HTTP_NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public GitCommitInfo upsertFile(String path, String content, String existingFileSha, String commitMessage) {
        return upsertFileBase64(path, encodeBase64(content), existingFileSha, commitMessage);
    }

    @Override
    public GitCommitInfo upsertFileBase64(String path, String base64Content, String existingFileSha,
                                          String commitMessage) {
        String repo = urlEncode(properties.getRepository());
        String encodedPath = encodeFilePathForUrl(stripLeadingSlash(path));

        Map<String, Object> body = new HashMap<>();
        body.put("branch", properties.getBranch());
        body.put("commit_message", commitMessage);
        body.put("content", base64Content);
        body.put("encoding", "base64");
        body.put("author_name", properties.getCommitAuthor());
        body.put("author_email", properties.getCommitAuthorEmail());

        String url = buildFullUrl("/projects/" + repo + "/repository/files/" + encodedPath);

        try {
            httpGet(url, defaultQueryParams());
            // File exists – update via PUT
            String response = httpPut(url, body);
            return parseCommitInfo(JsonUtil.parse(response));
        } catch (GitApiException e) {
            if (e.getStatusCode() == GitStorageConstants.HTTP_NOT_FOUND) {
                // Create via POST
                String response = httpPost(url, body);
                return parseCommitInfo(JsonUtil.parse(response));
            }
            throw e;
        }
    }

    @Override
    public Optional<GitCommitInfo> deleteFile(String path, String fileSha, String commitMessage) {
        String repo = urlEncode(properties.getRepository());
        String encodedPath = encodeFilePathForUrl(stripLeadingSlash(path));
        String url = buildFullUrl("/projects/" + repo + "/repository/files/" + encodedPath);

        Map<String, Object> body = new HashMap<>();
        body.put("branch", properties.getBranch());
        body.put("commit_message", commitMessage);
        body.put("author_name", properties.getCommitAuthor());
        body.put("author_email", properties.getCommitAuthorEmail());

        String response = httpDelete(url, body);
        if (StringUtils.isEmpty(response)) {
            return Optional.empty();
        }
        return Optional.of(parseCommitInfo(JsonUtil.parse(response)));
    }

    // --- Parsing helpers ---

    private GitFileEntry parseFileEntry(JsonNode node) {
        if (node == null) {
            return null;
        }
        GitFileEntry.GitFileEntryBuilder builder = GitFileEntry.builder();
        JsonNode n = node.get("name");
        if (n != null && !n.isNull()) {
            builder.name(n.asText());
        }
        n = node.get("path");
        if (n != null && !n.isNull()) {
            builder.path(n.asText());
        }
        n = node.get("id");
        if (n != null && !n.isNull()) {
            builder.sha(n.asText());
        }
        n = node.get("size");
        if (n != null && !n.isNull()) {
            builder.size(n.asLong(0));
        }
        n = node.get("web_url");
        if (n != null && !n.isNull()) {
            builder.htmlUrl(n.asText());
        }
        n = node.get("type");
        if (n != null && !n.isNull()) {
            String type = n.asText();
            // GitLab uses "blob" and "tree" instead of "file" and "dir"
            if ("blob".equalsIgnoreCase(type)) {
                builder.type(GitStorageConstants.TYPE_FILE);
            } else if ("tree".equalsIgnoreCase(type)) {
                builder.type(GitStorageConstants.TYPE_DIR);
            } else {
                builder.type(type);
            }
        } else {
            builder.type(GitStorageConstants.TYPE_FILE);
        }
        return builder.build();
    }

    private GitCommitInfo parseCommitInfo(JsonNode node) {
        if (node == null) {
            return GitCommitInfo.builder().build();
        }
        String sha = textOrNull(node.get("commit_id"));
        if (StringUtils.isEmpty(sha)) {
            sha = textOrNull(node.get("sha"));
        }
        JsonNode lastCommit = node.get("last_commit_id");
        if (lastCommit != null && !lastCommit.isNull()) {
            sha = lastCommit.asText();
        }
        return GitCommitInfo.builder()
                .sha(sha)
                .message(textOrNull(node.get("message")))
                .build();
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }

    private String extractDirName(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlash = trimmed.lastIndexOf('/');
        if (lastSlash < 0) {
            return trimmed;
        }
        return trimmed.substring(lastSlash + 1);
    }
}
