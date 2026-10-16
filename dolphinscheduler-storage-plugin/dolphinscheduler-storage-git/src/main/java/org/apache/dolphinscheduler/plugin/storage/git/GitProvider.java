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

import java.util.Locale;

/**
 * Supported Git repository providers.
 */
public enum GitProvider {

    GITHUB("https://api.github.com"),
    GITLAB("https://gitlab.com/api/v4"),
    GITEA("https://gitea.com/api/v1"),
    CUSTOM(null);

    private final String defaultApiUrl;

    GitProvider(String defaultApiUrl) {
        this.defaultApiUrl = defaultApiUrl;
    }

    public String getDefaultApiUrl() {
        return defaultApiUrl;
    }

    public static GitProvider fromName(String name) {
        if (name == null) {
            return CUSTOM;
        }
        switch (name.toUpperCase(Locale.ROOT)) {
            case "GITHUB":
                return GITHUB;
            case "GITLAB":
                return GITLAB;
            case "GITEA":
                return GITEA;
            default:
                return CUSTOM;
        }
    }
}
