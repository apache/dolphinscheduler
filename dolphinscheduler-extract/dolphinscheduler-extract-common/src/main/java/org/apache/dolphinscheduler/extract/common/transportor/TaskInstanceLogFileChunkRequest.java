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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request a single bounded chunk {@code [offset, offset + length)} of a task instance log file, used
 * by the streaming download path so the caller can page through an arbitrarily large log without
 * loading it all into memory in one RPC round-trip.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskInstanceLogFileChunkRequest {

    private long taskInstanceId;

    private String taskInstanceLogAbsolutePath;

    /**
     * Start byte offset within the log file (>= 0).
     */
    private long offset;

    /**
     * Maximum number of bytes to return in this chunk (> 0). The server clamps this to
     * {@code LogUtils.MAX_LOG_CHUNK_SIZE}.
     */
    private int length;
}
