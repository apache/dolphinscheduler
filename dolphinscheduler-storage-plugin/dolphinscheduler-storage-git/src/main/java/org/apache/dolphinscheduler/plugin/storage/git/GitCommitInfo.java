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
 * Represents a Git commit as returned by the API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitCommitInfo {

    /**
     * The commit SHA.
     */
    private String sha;

    /**
     * The commit message.
     */
    private String message;

    /**
     * The author name.
     */
    private String authorName;

    /**
     * The author email.
     */
    private String authorEmail;

    /**
     * The date string (ISO 8601).
     */
    private String date;
}
