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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Gitea REST API client implementing the {@link GitApiClient} contract.
 * Uses the Gitea Contents API which closely mirrors GitHub's API.
 */
@Slf4j
public class GiteaApiClient extends GitApiClient {

    public GiteaApiClient(GitStorageProperties properties) {
        super(properties);
    }

    @Override
    public String getProviderName() {
        return "Gitea";
    }

    @Override
    public List<GitFileEntry> listContents(String path) {
        String repo = properties.getRepository();
        String encodedPath = encodeFilePathForUrl(stripLeadingSlash(path));
        String url;
        if (StringUtils.isEmpty(encodedPath)) {
            url = buildFullUrl("/repos/" + repo + "/contents/");
        } else {
            url = buildFullUrl("/repos/" + repo + "/contents/" + encodedPath);
        }
        String response = httpGet(url, defaultQueryParams());
        JsonNode node = JsonUtil.parse(response);

        if (node.isArray()) {
            List<GitFileEntry> entries = new java.util.ArrayList<>();
            Iterator<JsonNode> it = node.elements();
            while (it.hasNext()) {
                entries.add(parseFileEntry(it.next()));
            }
            return entries;
        }
        // Single file
        List<GitFileEntry> entries = new java.util.ArrayList<>();
        entries.add(parseFileEntry(node));
        return entries;
    }

    @Override
    public Optional<GitFileEntry> getFile(String path) {
        String repo = properties.getRepository();
        String encodedPath = encodeFilePathForUrl(stripLeadingSlash(path));
        String url;
        if (StringUtils.isEmpty(encodedPath)) {
            url = buildFullUrl("/repos/" + repo + "/contents/");
        } else {
            url = buildFullUrl("/repos/" + repo + "/contents/" + encodedPath);
        }
        try {
            String response = httpGet(url, defaultQueryParams());
            JsonNode node = JsonUtil.parse(response);
            if (node.isArray()) {
                String dirName = extractDirName(path);
                return Optional.of(GitFileEntry.builder()
                        .name(dirName)
                        .path(stripLeadingSlash(path))
                        .type(GitStorageConstants.TYPE_DIR)
                        .build());
            }
            return Optional.of(parseFileEntry(node));
        } catch (GitApiException e) {
            if (e.getStatusCode() == GitStorageConstants.HTTP_NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public String getFileContent(String path) {
        Optional<String> base64Opt = getFileContentBase64(path);
        if (!base64Opt.isPresent()) {
            throw new GitApiException("File not found: " + path);
        }
        return decodeBase64(base64Opt.get());
    }

    @Override
    public Optional<String> getFileContentBase64(String path) {
        String repo = properties.getRepository();
        String encodedPath = encodeFilePathForUrl(stripLeadingSlash(path));
        String url = buildFullUrl("/repos/" + repo + "/contents/" + encodedPath);
        try {
            String response = httpGet(url, defaultQueryParams());
            JsonNode node = JsonUtil.parse(response);
            JsonNode contentNode = node.get("content");
            if (contentNode == null || contentNode.isNull()) {
                return Optional.empty();
            }
            String content = contentNode.asText().replace("\n", "").replace("\r", "");
            return Optional.of(content);
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
        String repo = properties.getRepository();
        String encodedPath = encodeFilePathForUrl(stripLeadingSlash(path));
        String url = buildFullUrl("/repos/" + repo + "/contents/" + encodedPath);

        Map<String, Object> body = new HashMap<>();
        body.put("message", commitMessage);
        body.put("content", base64Content);
        body.put("branch", properties.getBranch());

        if (existingFileSha != null && !existingFileSha.isEmpty()) {
            body.put("sha", existingFileSha);
        }

        String response = httpPut(url, body);
        JsonNode node = JsonUtil.parse(response);
        return parseCommitInfo(node);
    }

    @Override
    public Optional<GitCommitInfo> deleteFile(String path, String fileSha, String commitMessage) {
        String repo = properties.getRepository();
        String encodedPath = encodeFilePathForUrl(stripLeadingSlash(path));
        String url = buildFullUrl("/repos/" + repo + "/contents/" + encodedPath);

        Map<String, Object> body = new HashMap<>();
        body.put("message", commitMessage);
        body.put("branch", properties.getBranch());
        if (StringUtils.isNotEmpty(fileSha)) {
            body.put("sha", fileSha);
        }

        String response = httpDelete(url, body);
        if (StringUtils.isEmpty(response)) {
            return Optional.empty();
        }
        return Optional.of(parseCommitInfo(JsonUtil.parse(response)));
    }

    // --- Parsing helpers (identical pattern to GitHub) ---

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
        n = node.get("sha");
        if (n != null && !n.isNull()) {
            builder.sha(n.asText());
        }
        n = node.get("size");
        if (n != null && !n.isNull()) {
            builder.size(n.asLong(0));
        }
        n = node.get("html_url");
        if (n != null && !n.isNull()) {
            builder.htmlUrl(n.asText());
        }
        n = node.get("download_url");
        if (n != null && !n.isNull()) {
            builder.downloadUrl(n.asText());
        }
        n = node.get("type");
        if (n != null && !n.isNull()) {
            builder.type(n.asText());
        } else {
            JsonNode enc = node.get("encoding");
            if (enc != null && "base64".equals(enc.asText())) {
                builder.type(GitStorageConstants.TYPE_FILE);
            } else {
                builder.type(GitStorageConstants.TYPE_DIR);
            }
        }
        return builder.build();
    }

    private GitCommitInfo parseCommitInfo(JsonNode node) {
        GitCommitInfo.GitCommitInfoBuilder builder = GitCommitInfo.builder();
        if (node == null) {
            return builder.build();
        }
        builder.sha(textOrNull(node.get("sha")));
        builder.message(textOrNull(node.get("message")));
        JsonNode commit = node.get("commit");
        if (commit != null && !commit.isNull()) {
            JsonNode author = commit.get("author");
            if (author != null) {
                builder.authorName(textOrNull(author.get("name")));
                builder.authorEmail(textOrNull(author.get("email")));
            }
        }
        return builder.build();
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
