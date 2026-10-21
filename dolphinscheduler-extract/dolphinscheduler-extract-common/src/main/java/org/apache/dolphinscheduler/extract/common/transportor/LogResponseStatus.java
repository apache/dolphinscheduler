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

public enum LogResponseStatus {
    /**
     * Success status code.
     */
    SUCCESS,

    /**
     * General error status code.
     */
    ERROR,

    /**
     * Log file not found status code.
     */
    LOG_FILE_NOT_FOUND,

    /**
     * The log file shrank below the requested offset while streaming — e.g. log rotation
     * renamed the active file and a fresh one started from 0. The download the caller is
     * assembling is truncated and must fail explicitly instead of silently ending.
     */
    LOG_TRUNCATED,

    /**
     * The file at the log path is no longer the file this download pinned on its first chunk:
     * the identity the request carried (mtime and/or inode key) no longer matches. This catches
     * a replacement that per-chunk length checks cannot see — a rotation that swaps in a
     * different file of coincidentally equal or greater length would otherwise yield an
     * A-prefix + B-suffix "successful" download. The caller must fail the download explicitly.
     */
    LOG_FILE_REPLACED,
}
