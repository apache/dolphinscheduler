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

package org.apache.dolphinscheduler.common.log.remote;

import java.io.IOException;

public interface RemoteLogHandler {

    void sendRemoteLog(String logPath);

    /**
     * Downloads the remote archived log to {@code logPath}.
     *
     * <p>Implementations MUST NOT leave a partially downloaded file at {@code logPath}: the
     * download is written to a private staging file and published to {@code logPath} only after
     * the transfer completed — implementations should use
     * {@link RemoteLogUtils#downloadToLocalFileAtomically(String, RemoteLogUtils.RemoteLogDownloader)}.
     *
     * <p>Failures MUST propagate as {@link IOException}: swallowing them lets the API serve a
     * failed (or truncated) download as a successful one.
     *
     * @throws IOException if the remote object cannot be downloaded; the file at {@code logPath}
     *                     (if any) is left untouched
     */
    void getRemoteLog(String logPath) throws IOException;
}
