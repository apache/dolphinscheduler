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

package org.apache.dolphinscheduler.extract.common.transportor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskInstanceLogFileDownloadRequest {

    private long taskInstanceId;

    private String taskInstanceLogAbsolutePath;

    /**
     * Chunk read offset in bytes. Only read by the chunk RPC — the legacy whole-file method
     * ignores it. Non-positive values are clamped to 0 by the worker.
     */
    private long offset = 0;

    /**
     * Chunk read length in bytes. Only read by the chunk RPC — the legacy whole-file method
     * ignores it. Non-positive values are clamped to the worker's maximum chunk size.
     */
    private int length = 0;

    /**
     * The file length the caller pinned (from the first chunk's {@code observedLength}) for this
     * download; 0 = not pinned. The worker stats before every read: a file SHORTER than this
     * means rotation/truncation (LOG_TRUNCATED); the API additionally rejects a shrink on its
     * side. Only read by the chunk RPC.
     */
    private long expectedFileLength = 0;

    /**
     * The mtime the caller pinned (from the first chunk's {@code observedLastModifiedMillis});
     * 0 = not pinned. The worker rejects the chunk when the file's current mtime is EARLIER than
     * the pin: that means the file was replaced (a fresh file starts mtime "now", never in the
     * past) rather than appended to. Appends (mtime moving FORWARD) are accepted — a live task
     * legitimately grows the log mid-download. Only read by the chunk RPC.
     */
    private long expectedLastModifiedMillis = 0;

    /**
     * The inode identity the caller pinned (from the first chunk's {@code observedFileKey});
     * null = not pinned or the worker's filesystem does not expose inodes. When present on both
     * sides it is the strongest check: the worker rejects the chunk when the file at the path is
     * now a different INODE — catching a same-size, back-dated-mtime replacement that mtime
     * alone would miss. Only read by the chunk RPC.
     */
    private String expectedFileKey;

    public TaskInstanceLogFileDownloadRequest(long taskInstanceId, String taskInstanceLogAbsolutePath) {
        this.taskInstanceId = taskInstanceId;
        this.taskInstanceLogAbsolutePath = taskInstanceLogAbsolutePath;
    }

    public TaskInstanceLogFileDownloadRequest(long taskInstanceId, String taskInstanceLogAbsolutePath,
                                              long offset, int length) {
        this(taskInstanceId, taskInstanceLogAbsolutePath);
        this.offset = offset;
        this.length = length;
    }

}
