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

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.OutputStream;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.handler.codec.TooLongFrameException;

@Slf4j
@Component
public class LogClientDelegate {

    private static final int LOG_CHUNK_SIZE = 8 * 1024 * 1024; // 8 MB

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

    /**
     * Stream the entire task instance log to {@code outputStream} using chunked RPC.
     *
     * <p>Strategy:
     * <ul>
     *   <li>If the worker node is gone, read straight from remote log storage (archive).</li>
     *   <li>Otherwise try the chunk RPC. If it fails before any byte is written (e.g. an old worker
     *       that does not implement {@code getTaskInstanceLogFileChunk}), fall back to the legacy
     *       whole-file worker RPC; if that also fails, fall back to remote storage.</li>
     *   <li>If the failure happens mid-stream (bytes already written), throw IOException to avoid
     *       corrupting the download.</li>
     * </ul>
     */
    public void streamWholeLog(final TaskInstance taskInstance, final OutputStream outputStream) throws IOException {
        checkArgs(taskInstance);
        if (!checkNodeExists(taskInstance)) {
            remoteLogClient.streamWholeLog(taskInstance, outputStream);
            return;
        }
        long offset = 0;
        boolean needFallback = false;
        try {
            while (true) {
                final TaskInstanceLogFileDownloadResponse chunk =
                        localLogClient.getLogChunk(taskInstance, offset, LOG_CHUNK_SIZE);
                if (chunk.getCode() != LogResponseStatus.SUCCESS) {
                    if (offset == 0) {
                        if (chunk.getCode() == LogResponseStatus.LOG_FILE_NOT_FOUND) {
                            // A NEW worker authoritatively reports the file is gone (an old
                            // worker cannot: it lacks the chunk method and fails the RPC
                            // instead). Skip the redundant legacy probe on the same worker and
                            // go straight to the remote archive.
                            log.warn("Log file not found on worker for task instance {}, "
                                    + "fetching from remote storage", taskInstance.getId());
                            remoteLogClient.streamWholeLog(taskInstance, outputStream);
                            return;
                        }
                        log.warn("First chunk failed for task instance {}: {}, falling back to legacy whole-file RPC",
                                taskInstance.getId(), chunk.getMessage());
                        needFallback = true;
                        break;
                    }
                    throw new IOException("Worker chunk failed at offset " + offset + ": " + chunk.getMessage());
                }
                final byte[] data = chunk.getLogBytes();
                if (data != null && data.length > 0) {
                    outputStream.write(data);
                    offset += data.length;
                }
                if (chunk.isEof() || (data == null || data.length == 0)) {
                    return;
                }
            }
        } catch (Exception e) {
            if (offset > 0) {
                throw new IOException("Log streaming failed at offset " + offset, e);
            }
            log.warn("Chunked streaming failed before any byte written for task instance {}, "
                    + "falling back to legacy whole-file RPC", taskInstance.getId(), e);
            needFallback = true;
        }
        // Fallback OUTSIDE the try — its exceptions propagate directly, no re-entry.
        if (needFallback) {
            writeLocalLegacy(taskInstance, outputStream);
        }
    }

    /**
     * Fall back to the legacy whole-file worker RPC ({@code getTaskInstanceWholeLogFileBytes}),
     * bounded by the TransporterDecoder maxFrameSize guard (64 MB). If the log exceeds that limit,
     * the RPC response is rejected and this method throws an explicit {@link IOException} rather
     * than falling through to remote storage, so the caller does not see a misleading
     * "log not available" error. If the RPC fails for other reasons or returns nothing, fall
     * through to remote log storage (streamed, also bounded).
     */
    private void writeLocalLegacy(final TaskInstance taskInstance,
                                  final OutputStream outputStream) throws IOException {
        try {
            final TaskInstanceLogFileDownloadResponse response = localLogClient.getWholeLog(taskInstance);
            if (response != null && response.getCode() == LogResponseStatus.SUCCESS) {
                // SUCCESS is the worker's authoritative answer: an EMPTY body is a valid
                // terminal state ("task produced no output"), not a reason to fall through to
                // remote storage — falling through would turn a legitimate empty log into an
                // error once no archive exists. Only an explicit non-SUCCESS keeps the
                // fallback chain going.
                final byte[] bytes = response.getLogBytes();
                outputStream.write(bytes == null ? new byte[0] : bytes);
                outputStream.flush();
                return;
            }
        } catch (Exception e) {
            if (ExceptionUtils.throwableOfType(e, TooLongFrameException.class) != null) {
                throw new IOException("Log file for task instance " + taskInstance.getId()
                        + " exceeds the maximum legacy download size; "
                        + "enable remote log archiving or increase the RPC maxFrameSize", e);
            }
            log.warn("Legacy whole-file RPC failed for task instance {}, falling back to remote storage",
                    taskInstance.getId(), e);
        }
        remoteLogClient.streamWholeLog(taskInstance, outputStream);
    }

}
