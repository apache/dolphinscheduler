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
 * Represents a file or directory entry returned from a Git provider API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitFileEntry {

    /**
     * The file/directory name (basename).
     */
    private String name;

    /**
     * The full relative path from repo root (e.g. "dolphinscheduler/resources/foo.sh").
     */
    private String path;

    /**
     * The blob SHA (for files).
     */
    private String sha;

    /**
     * The size in bytes (for files).
     */
    private long size;

    /**
     * The HTML URL for viewing the file.
     */
    private String htmlUrl;

    /**
     * The download URL for the file content.
     */
    private String downloadUrl;

    /**
     * The type: "file", "dir", "symlink".
     */
    private String type;

    /**
     * Whether this entry represents a directory.
     */
    public boolean isDirectory() {
        return GitStorageConstants.TYPE_DIR.equalsIgnoreCase(type)
                || GitStorageConstants.TYPE_SUBDIR.equalsIgnoreCase(type);
    }
}
