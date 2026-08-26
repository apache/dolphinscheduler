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

package org.apache.dolphinscheduler.plugin.storage.gitlab.dto;

import lombok.Data;

/**
 * A single node returned by the GitLab repository tree API
 * {@code GET /api/v4/projects/:id/repository/tree}.
 *
 * <pre>
 * [
 *   {
 *     "id": "a1e8f8d7...",
 *     "name": "src",
 *     "type": "tree",        // "tree" (directory) or "blob" (file)
 *     "path": "src",
 *     "mode": "040000"
 *   }
 * ]
 * </pre>
 */
@Data
public class GitlabFileNode {

    private String id;

    private String name;

    // tree is directory, blob is file
    private String type;

    private String path;

    private String mode;

    private boolean directory;

    public boolean isDirectory() {
        return type.equals("tree");
    }

}
