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

/**
 * Internal constants for Git storage plugin.
 */
public final class GitStorageConstants {

    private GitStorageConstants() {
        // prevent instantiation
    }

    /**
     * Content type for JSON requests.
     */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * Content type for form-encoded requests.
     */
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    /**
     * GitHub API "Accept" header for v3.
     */
    public static final String GITHUB_API_ACCEPT = "application/vnd.github+json";

    /**
     * GitLab API "Accept" header.
     */
    public static final String GITLAB_API_ACCEPT = "application/json";

    /**
     * Default HTTP status codes.
     */
    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_NO_CONTENT = 204;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_TOO_MANY_REQUESTS = 429;
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    /**
     * Git file type identifiers.
     */
    public static final String TYPE_FILE = "file";
    public static final String TYPE_DIR = "dir";
    public static final String TYPE_SUBDIR = "subdir";
    public static final String TYPE_SYMLINK = "symlink";

    /**
     * Token header names per provider.
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_AUTHORIZATION_PREFIX_BEARER = "Bearer ";
    public static final String HEADER_PRIVATE_TOKEN = "PRIVATE-TOKEN";
}
