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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.fake;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.LogicFakeTaskParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.TaskOutputParameterParser;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.AbstractLogicTask;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.ITaskParameterDeserializer;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;

/**
 * This task is used for testing purposes only.
 * <p> More details about the task can be found in the `it/cases`.
 */
@Slf4j
@VisibleForTesting
public class LogicFakeTask extends AbstractLogicTask<LogicFakeTaskParameters> {

    private volatile Process process;

    public LogicFakeTask(final TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        onTaskRunning();
    }

    @Override
    public void start() throws MasterTaskExecuteException {
        try {
            log.info("Begin to execute LogicFakeTask: {}", taskExecutionContext.getTaskName());

            String shellScript = ParameterUtils.convertParameterPlaceholders(
                    taskParameters.getShellScript(),
                    ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap()));

            if (StringUtils.isNotEmpty(taskExecutionContext.getEnvironmentConfig())) {
                shellScript = taskExecutionContext.getEnvironmentConfig() + "\n" + shellScript;
            }
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", shellScript);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            final Future<Map<String, String>> parseVarPoolFuture = parseVarPool();
            int exitCode = process.waitFor();
            log.info("LogicFakeTask: {} execute finished with exit code: {}",
                    taskExecutionContext.getTaskName(),
                    exitCode);
            if (taskExecutionStatus == TaskExecutionStatus.KILL) {
                try {
                    parseVarPoolFuture.get(1, TimeUnit.SECONDS);
                } catch (TimeoutException interruptedException) {
                    // ignore
                }
                // The task has been killed
                log.info("LogicFakeTask: {} has been killed", taskExecutionContext.getTaskName());
                return;
            }

            final Map<String, String> taskOutputParams = parseVarPoolFuture.get();
            if (exitCode == 0) {
                log.info("LogicFakeTask: {} execute success", taskExecutionContext.getTaskName());
                taskParameters.dealOutParam(taskOutputParams);
                taskExecutionContext.setVarPool(taskParameters.getVarPool());
                onTaskSuccess();
            } else {
                log.info("LogicFakeTask: {} execute failed", taskExecutionContext.getTaskName());
                onTaskFailed();
            }
        } catch (Exception ex) {
            throw new MasterTaskExecuteException("FakeTask execute failed", ex);
        }
    }

    @Override
    public void pause() throws MasterTaskExecuteException {
        log.info("LogicFakeTask: {} doesn't support pause", taskExecutionContext.getTaskName());
    }

    @Override
    public void kill() throws MasterTaskExecuteException {
        if (process != null && process.isAlive()) {
            // todo: use shell script to kill the process?
            onTaskKilled();
            process.destroyForcibly();
            log.info("Kill LogicFakeTask: {} succeed", taskExecutionContext.getTaskName());
        }
    }

    @Override
    public ITaskParameterDeserializer<LogicFakeTaskParameters> getTaskParameterDeserializer() {
        return taskParamsJson -> JSONUtils.parseObject(taskParamsJson, new TypeReference<LogicFakeTaskParameters>() {
        });
    }

    private Future<Map<String, String>> parseVarPool() {
        ExecutorService varPoolParseThreadPool = ThreadUtils.newSingleDaemonScheduledExecutorService(
                "ResolveOutputLog-thread-" + taskExecutionContext.getTaskName());
        Future<Map<String, String>> future = varPoolParseThreadPool.submit(() -> {
            TaskOutputParameterParser taskOutputParameterParser = new TaskOutputParameterParser();
            try (BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    log.info(line);
                    taskOutputParameterParser.appendParseLog(line);
                }
            } catch (Exception e) {
                log.error("Parse var pool error", e);
            }
            return taskOutputParameterParser.getTaskOutputParams();
        });

        varPoolParseThreadPool.shutdown();
        return future;
    }

}
