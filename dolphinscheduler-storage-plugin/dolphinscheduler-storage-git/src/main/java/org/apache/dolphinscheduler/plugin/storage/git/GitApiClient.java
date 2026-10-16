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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Abstract Git REST API client. Subclasses implement provider-specific path and payload conventions.
 */
@Slf4j
public abstract class GitApiClient {

    protected final GitStorageProperties properties;
    protected final OkHttpClient httpClient;
    protected final MediaType jsonMediaType = MediaType.parse(GitStorageConstants.CONTENT_TYPE_JSON);

    protected GitApiClient(GitStorageProperties properties) {
        this.properties = properties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(properties.getReadTimeout(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getWriteTimeout(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * Returns the provider name for logging.
     */
    public abstract String getProviderName();

    /**
     * List files/directories in the given path.
     *
     * @param path the path relative to the repo root, empty string for repo root
     * @return list of entries
     */
    public abstract List<GitFileEntry> listContents(String path);

    /**
     * Get a single file entry (or directory listing) at the given path.
     *
     * @param path the path relative to the repo root
     * @return the entry or empty if not found
     */
    public abstract Optional<GitFileEntry> getFile(String path);

    /**
     * Get the raw content of a file.
     *
     * @param path the path relative to the repo root
     * @return the file content as a string
     */
    public abstract String getFileContent(String path);

    /**
     * Get the base64-encoded content of a file.
     *
     * @param path the path relative to the repo root
     * @return base64 encoded content
     */
    public abstract Optional<String> getFileContentBase64(String path);

    /**
     * Create or update a file.
     *
     * @param path             the path relative to repo root
     * @param content          the new file content (raw, will be base64-encoded by the server or client depending on provider)
     * @param existingFileSha  the existing file SHA, null for create
     * @param commitMessage    the commit message
     * @return the resulting commit info
     */
    public abstract GitCommitInfo upsertFile(String path, String content, String existingFileSha,
                                             String commitMessage);

    /**
     * Create or update a file with explicit base64 content.
     *
     * @param path            the path relative to repo root
     * @param base64Content   base64-encoded content
     * @param existingFileSha the existing file SHA, null for create
     * @param commitMessage   the commit message
     * @return the resulting commit info
     */
    public abstract GitCommitInfo upsertFileBase64(String path, String base64Content,
                                                   String existingFileSha, String commitMessage);

    /**
     * Delete a file.
     *
     * @param path          the path relative to repo root
     * @param fileSha       the file blob SHA (required for GitHub, optional for others)
     * @param commitMessage the commit message
     * @return the resulting commit info, or empty if not applicable
     */
    public abstract Optional<GitCommitInfo> deleteFile(String path, String fileSha, String commitMessage);

    /**
     * Check if a path exists.
     *
     * @param path the path relative to repo root
     * @return true if exists
     */
    public boolean pathExists(String path) {
        try {
            return getFile(path).isPresent();
        } catch (GitApiException e) {
            if (e.getStatusCode() == GitStorageConstants.HTTP_NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }

    // --- Common HTTP helpers ---

    protected String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    protected String buildFullUrl(String path) {
        String apiUrl = properties.getApiUrl();
        if (apiUrl.endsWith("/")) {
            apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return apiUrl + path;
    }

    protected String buildRepoSegment() {
        return urlEncode(properties.getRepository());
    }

    protected String encodeFilePathForUrl(String filePath) {
        // Split on '/' and encode each segment
        if (StringUtils.isEmpty(filePath)) {
            return "";
        }
        String[] segments = filePath.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                sb.append("/");
            }
            sb.append(urlEncode(segments[i]));
        }
        return sb.toString();
    }

    /**
     * Returns the authorization header map. Provider-specific subclasses may override.
     */
    protected Headers buildAuthHeaders() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", GitStorageConstants.CONTENT_TYPE_JSON);
        if (StringUtils.isNotEmpty(properties.getAuthToken())) {
            headerMap.put(GitStorageConstants.HEADER_AUTHORIZATION,
                    GitStorageConstants.HEADER_AUTHORIZATION_PREFIX_BEARER + properties.getAuthToken());
        }
        return Headers.of(headerMap);
    }

    protected Headers buildAuthHeaders(Map<String, String> extra) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", GitStorageConstants.CONTENT_TYPE_JSON);
        if (StringUtils.isNotEmpty(properties.getAuthToken())) {
            headerMap.put(GitStorageConstants.HEADER_AUTHORIZATION,
                    GitStorageConstants.HEADER_AUTHORIZATION_PREFIX_BEARER + properties.getAuthToken());
        }
        if (extra != null) {
            headerMap.putAll(extra);
        }
        return Headers.of(headerMap);
    }

    /**
     * Performs an HTTP GET and returns the response body string.
     */
    protected String httpGet(String url, Map<String, String> queryParams) {
        String finalUrl = buildUrlWithParams(url, queryParams);
        Request request = new Request.Builder()
                .url(finalUrl)
                .headers(buildAuthHeaders())
                .get()
                .build();
        return execute(request);
    }

    protected String httpGet(String url, Map<String, String> queryParams, Headers headers) {
        String finalUrl = buildUrlWithParams(url, queryParams);
        Request request = new Request.Builder()
                .url(finalUrl)
                .headers(headers)
                .get()
                .build();
        return execute(request);
    }

    /**
     * Performs an HTTP POST with JSON body.
     */
    protected String httpPost(String url, Object jsonBody) {
        String json = (jsonBody == null) ? "{}" : JsonUtil.toJson(jsonBody);
        RequestBody body = RequestBody.create(json, jsonMediaType);
        Request request = new Request.Builder()
                .url(url)
                .headers(buildAuthHeaders())
                .post(body)
                .build();
        return execute(request);
    }

    /**
     * Performs an HTTP PUT with JSON body.
     */
    protected String httpPut(String url, Object jsonBody) {
        String json = (jsonBody == null) ? "{}" : JsonUtil.toJson(jsonBody);
        RequestBody body = RequestBody.create(json, jsonMediaType);
        Request request = new Request.Builder()
                .url(url)
                .headers(buildAuthHeaders())
                .put(body)
                .build();
        return execute(request);
    }

    /**
     * Performs an HTTP DELETE with optional JSON body.
     */
    protected String httpDelete(String url, Object jsonBody) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .headers(buildAuthHeaders());
        if (jsonBody != null) {
            String json = JsonUtil.toJson(jsonBody);
            RequestBody body = RequestBody.create(json, jsonMediaType);
            builder.delete(body);
        } else {
            builder.delete();
        }
        return execute(builder.build());
    }

    /**
     * Executes an OkHttp request and returns the response body string.
     */
    protected String execute(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            String bodyStr = body != null ? body.string() : "";
            int code = response.code();
            if (code >= 200 && code < 300) {
                return bodyStr;
            }
            if (code == GitStorageConstants.HTTP_NOT_FOUND) {
                throw new GitApiException(code,
                        String.format("Resource not found at %s: %s", request.url(), bodyStr));
            }
            if (code == GitStorageConstants.HTTP_UNAUTHORIZED || code == GitStorageConstants.HTTP_FORBIDDEN) {
                throw new GitApiException(code,
                        String.format("Authentication/authorization failed for %s: %s", request.url(), bodyStr));
            }
            if (code == GitStorageConstants.HTTP_TOO_MANY_REQUESTS) {
                throw new GitApiException(code,
                        String.format("Rate limited by %s: %s", request.url(), bodyStr));
            }
            throw new GitApiException(code,
                    String.format("Git API request failed [%s] %d: %s", request.url(), code, bodyStr));
        } catch (IOException e) {
            throw new GitApiException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    protected String buildUrlWithParams(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        boolean first = !url.contains("?");
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (first) {
                sb.append('?');
                first = false;
            } else {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=').append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    protected Map<String, String> defaultQueryParams() {
        Map<String, String> params = new HashMap<>();
        params.put("ref", properties.getBranch());
        return params;
    }

    protected Map<String, String> emptyMap() {
        return Collections.emptyMap();
    }

    protected String encodeBase64(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    protected String decodeBase64(String base64) {
        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }

    protected String stripLeadingSlash(String path) {
        if (path != null && path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }
}
