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

package org.apache.dolphinscheduler.api.executor.logging;

import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileChunkResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.io.IOException;
import java.io.OutputStream;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogClientDelegate {

    @Autowired
    private LocalLogClient localLogClient;
    @Autowired
    private RemoteLogClient remoteLogClient;
    @Autowired
    private RegistryClient registryClient;

    /**
     * Retrieves a portion of the log string for a given task instance.
     * This method first attempts to fetch the log from local storage; if unsuccessful, it tries to obtain the log from remote storage.
     *
     * @param taskInstance The task instance object, containing information needed for log retrieval.
     * @param skipLineNum The number of log lines to skip from the beginning.
     * @param limit The maximum number of log lines to retrieve.
     * @return A string containing the specified portion of the log.
     */
    public String getPartLogString(TaskInstance taskInstance, int skipLineNum, int limit) {
        checkArgs(taskInstance);
        if (checkNodeExists(taskInstance)) {
            TaskInstanceLogPageQueryResponse response = localLogClient.getPartLog(taskInstance, skipLineNum, limit);
            if (response.getCode() == LogResponseStatus.SUCCESS) {
                return response.getLogContent();
            } else {
                log.warn("get part log string is not success for task instance {}; reason :{}",
                        taskInstance.getId(), response.getMessage());
                return remoteLogClient.getPartLog(taskInstance, skipLineNum, limit);
            }
        } else {
            return remoteLogClient.getPartLog(taskInstance, skipLineNum, limit);
        }
    }

    /**
     * Retrieves the complete log content for a given task instance as a byte array.
     * This method first attempts to fetch the log from local storage; if unsuccessful, it tries to obtain the log from remote storage.
     *
     * @param taskInstance The task instance object, containing information needed for log retrieval.
     * @return A byte array containing the complete log content.
     */
    public byte[] getWholeLogBytes(TaskInstance taskInstance) {
        checkArgs(taskInstance);
        if (checkNodeExists(taskInstance)) {
            TaskInstanceLogFileDownloadResponse response = localLogClient.getWholeLog(taskInstance);
            if (response.getCode() == LogResponseStatus.SUCCESS) {
                return response.getLogBytes();
            } else {
                log.warn("get whole log bytes is not success for task instance {}; reason :{}", taskInstance.getId(),
                        response.getMessage());
                return remoteLogClient.getWholeLog(taskInstance);
            }
        } else {
            return remoteLogClient.getWholeLog(taskInstance);
        }
    }

    /**
     * Stream the entire task instance log to {@code outputStream} in fixed-size chunks, keeping
     * memory bounded regardless of total log size. Prefers the worker (RPC chunked); if the worker
     * node is gone, falls back to remote storage. If both fail, the exception is propagated.
     *
     * <p>If a streaming source fails <b>after</b> bytes have already been written to the output, no
     * fallback is attempted — restarting from offset 0 would duplicate the prefix and corrupt the
     * downloaded file. Instead an {@link IOException} is thrown so the caller can surface the
     * truncation. Fallback only happens when nothing has been written yet (e.g. the worker is
     * unreachable on the first chunk).
     */
    public void streamWholeLog(final TaskInstance taskInstance, final OutputStream outputStream) throws IOException {
        checkArgs(taskInstance);
        if (checkNodeExists(taskInstance)) {
            final long[] written = {0};
            try {
                streamFromLocalWorker(taskInstance, outputStream, written);
                outputStream.flush();
                return;
            } catch (IOException | RuntimeException e) {
                if (written[0] > 0) {
                    throw new IOException("Worker streaming failed after " + written[0]
                            + " bytes written; cannot fall back without corrupting the stream", e);
                }
                log.warn("Streaming from worker failed before any byte was written for task instance {}, "
                        + "falling back to remote storage", taskInstance.getId(), e);
            }
        }
        final long[] written = {0};
        try {
            streamFromRemote(taskInstance, outputStream, written);
            outputStream.flush();
            return;
        } catch (IOException | RuntimeException e) {
            if (written[0] > 0) {
                throw new IOException("Remote streaming failed after " + written[0]
                        + " bytes written; cannot fall back without corrupting the stream", e);
            }
            throw new IOException("Log streaming failed for task instance " + taskInstance.getId(), e);
        }
    }

    private void streamFromLocalWorker(final TaskInstance taskInstance, final OutputStream outputStream,
                                       final long[] written) throws IOException {
        long offset = 0;
        while (true) {
            final TaskInstanceLogFileChunkResponse chunk = localLogClient.getLogChunk(taskInstance, offset,
                    LogUtils.MAX_LOG_CHUNK_SIZE);
            if (chunk.getCode() != LogResponseStatus.SUCCESS) {
                throw new IOException("Worker chunk request failed for task instance " + taskInstance.getId()
                        + "; reason: " + chunk.getMessage());
            }
            final byte[] bytes = chunk.getBytes();
            if (bytes == null || bytes.length == 0) {
                return; // empty file or EOF
            }
            outputStream.write(bytes);
            written[0] += bytes.length;
            offset += bytes.length;
            if (chunk.isEof()) {
                return;
            }
        }
    }

    private void streamFromRemote(final TaskInstance taskInstance, final OutputStream outputStream,
                                  final long[] written) throws IOException {
        // Sync the remote object to a local file exactly once, then read ranges of that local file,
        // instead of re-syncing the whole remote object on every chunk (a 1GB log / 1MB chunk would
        // otherwise re-download the entire object 1024 times).
        final java.io.File localFile = remoteLogClient.prepareLocalLog(taskInstance);
        if (localFile == null) {
            throw new IOException("Remote log prepare failed for task instance " + taskInstance.getId());
        }
        long offset = 0;
        while (true) {
            final TaskInstanceLogFileChunkResponse chunk =
                    remoteLogClient.getLocalLogChunk(localFile, offset, LogUtils.MAX_LOG_CHUNK_SIZE);
            if (chunk.getCode() != LogResponseStatus.SUCCESS) {
                throw new IOException(
                        "Remote chunk read failed at offset " + offset + ": " + chunk.getMessage());
            }
            final byte[] bytes = chunk.getBytes();
            if (bytes == null || bytes.length == 0) {
                return;
            }
            outputStream.write(bytes);
            written[0] += bytes.length;
            offset += bytes.length;
            if (chunk.isEof()) {
                return;
            }
        }
    }

    private static void checkArgs(TaskInstance taskInstance) {
        if (taskInstance == null) {
            throw new IllegalArgumentException("canFetchLog task instance is null");
        }
    }

    private boolean checkNodeExists(TaskInstance taskInstance) {
        RegistryNodeType nodeType;
        if (TaskTypeUtils.isLogicTask(taskInstance.getTaskType())) {
            nodeType = RegistryNodeType.MASTER;
        } else {
            nodeType = RegistryNodeType.WORKER;
        }
        boolean exists = registryClient.checkNodeExists(taskInstance.getHost(), nodeType);
        if (!exists) {
            log.warn("Node {} does not exist for task instance {}", taskInstance.getHost(), taskInstance.getId());
        }
        return exists;
    }

}
