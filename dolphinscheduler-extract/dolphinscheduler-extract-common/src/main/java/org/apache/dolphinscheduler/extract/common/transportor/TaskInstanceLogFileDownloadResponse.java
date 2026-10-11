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
public class TaskInstanceLogFileDownloadResponse {

    private byte[] logBytes;

    private LogResponseStatus code = LogResponseStatus.SUCCESS;

    private String message;

    /**
     * Whether this is the last chunk (or the only chunk for whole-file responses).
     */
    private boolean eof = true;

    /**
     * The file length the worker observed on this chunk's single stat; 0 when unknown (e.g. an
     * old worker that does not report it). The API pins the download's target length to the FIRST
     * chunk's value, so streaming a live log that keeps growing still yields a snapshot taken at
     * request time instead of an unbounded tail.
     */
    private long observedLength;

    /**
     * Convenience constructor for callers that do not report {@link #observedLength} — it stays
     * 0, i.e. unknown.
     */
    public TaskInstanceLogFileDownloadResponse(final byte[] logBytes, final LogResponseStatus code,
                                               final String message, final boolean eof) {
        this.logBytes = logBytes;
        this.code = code;
        this.message = message;
        this.eof = eof;
    }

}
