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
import org.apache.dolphinscheduler.extract.base.exception.MethodInvocationException;
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
     * Stream the entire task instance log to {@code outputStream} using bounded chunk RPCs.
     *
     * <p>Strategy:
     * <ul>
     *   <li>If the worker node is gone, read straight from remote log storage (archive), streamed
     *       in bounded chunks.</li>
     *   <li>Otherwise stream via the chunk RPC. If the FIRST chunk fails — e.g. an old worker
     *       during a rolling upgrade does not implement {@code getTaskInstanceLogFileChunk} —
     *       fall back to remote log storage; if that also fails, throw an explicit error asking
     *       for a worker upgrade.</li>
     *   <li>The legacy whole-file worker RPC is deliberately NEVER used: it reads the entire file
     *       into the worker's heap before serialization, so a large log can OOM the worker. A
     *       receiver-side maxFrameSize cannot prevent that allocation, and an old worker (already
     *       deployed, cannot be patched) offers no way to establish a safe size before the whole
     *       payload is built — so small-log compatibility with old workers is intentionally not
     *       preserved either.</li>
     *   <li>If a failure happens mid-stream (bytes already written), throw IOException to avoid
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
        while (true) {
            final TaskInstanceLogFileDownloadResponse chunk;
            try {
                chunk = localLogClient.getLogChunk(taskInstance, offset, LOG_CHUNK_SIZE);
            } catch (Exception e) {
                if (offset > 0) {
                    throw new IOException("Log streaming failed at offset " + offset, e);
                }
                log.warn("Chunked log RPC failed for task instance {}, falling back to remote log storage",
                        taskInstance.getId(), e);
                // A MethodInvocationException means the worker ANSWERED but could not dispatch the
                // method — that is the old-worker signal (rolling upgrade: the chunk method does
                // not exist there), so the upgrade guidance is accurate. Any other transport
                // failure (connect refused, timeout) just means the worker is unreachable; blaming
                // the worker version would mislead operations.
                final String errorMessage =
                        ExceptionUtils.throwableOfType(e, MethodInvocationException.class) != null
                                ? "Worker upgrade required for large log download: chunked log RPC is not available"
                                        + " on worker " + taskInstance.getHost() + " and remote log storage also failed"
                                : "Chunked log RPC to worker " + taskInstance.getHost()
                                        + " failed (the worker may be down or unreachable)"
                                        + " and remote log storage also failed";
                fallbackToRemoteStorage(taskInstance, outputStream, errorMessage);
                return;
            }
            if (chunk == null || chunk.getCode() != LogResponseStatus.SUCCESS) {
                final String failure = chunk == null
                        ? "worker returned no response"
                        : chunk.getCode() + ": " + chunk.getMessage();
                if (offset > 0) {
                    throw new IOException("Worker chunk failed at offset " + offset + ": " + failure);
                }
                log.warn("First chunk failed for task instance {} ({}), falling back to remote log storage",
                        taskInstance.getId(), failure);
                // The worker ANSWERED (structured response) — it supports the chunk RPC, so no
                // upgrade guidance here; report both failures as they are.
                fallbackToRemoteStorage(taskInstance, outputStream,
                        "Chunked log fetch failed on worker " + taskInstance.getHost() + " for task instance "
                                + taskInstance.getId() + " (" + failure + ") and remote log storage also failed");
                return;
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
    }

    /**
     * The ONLY fallback of the streaming path: stream the log from remote log storage. If remote
     * storage cannot serve the log either, fail with an explicit error — the legacy whole-file
     * worker RPC is deliberately never used (it is unbounded on the worker side, see
     * {@link #streamWholeLog}). Exceptions from the fallback propagate directly; there is no
     * second fallback to re-enter.
     */
    private void fallbackToRemoteStorage(final TaskInstance taskInstance,
                                         final OutputStream outputStream,
                                         final String errorMessage) throws IOException {
        try {
            remoteLogClient.streamWholeLog(taskInstance, outputStream);
        } catch (Exception e) {
            throw new IOException(errorMessage, e);
        }
    }

}
