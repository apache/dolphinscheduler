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

package org.apache.dolphinscheduler.extract.common;

import org.apache.dolphinscheduler.extract.base.RpcMethod;
import org.apache.dolphinscheduler.extract.base.RpcService;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

@RpcService
public interface ILogService {

    @RpcMethod
    TaskInstanceLogFileDownloadResponse getTaskInstanceWholeLogFileBytes(TaskInstanceLogFileDownloadRequest taskInstanceLogFileDownloadRequest);

    @RpcMethod
    TaskInstanceLogPageQueryResponse pageQueryTaskInstanceLog(TaskInstanceLogPageQueryRequest taskInstanceLogPageQueryRequest);

    @RpcMethod
    void removeTaskInstanceLog(String taskInstanceLogAbsolutePath);

    /**
     * Read a bounded chunk of the log file [offset, offset+length) for streaming download.
     * The worker clamps length to a maximum chunk size and returns eof metadata.
     *
     * <p>Each response also reports the file length observed by the worker's single stat
     * ({@link TaskInstanceLogFileDownloadResponse#getObservedLength()}). The API uses the FIRST
     * chunk's value as the download's target length, so streaming a live, growing log yields a
     * snapshot taken at request time. A 0 means the worker does not report it (e.g. an old
     * worker) and the caller must end the stream on {@code eof} instead.
     */
    @RpcMethod(timeout = 30_000)
    TaskInstanceLogFileDownloadResponse getTaskInstanceLogFileChunk(TaskInstanceLogFileDownloadRequest request);

}
