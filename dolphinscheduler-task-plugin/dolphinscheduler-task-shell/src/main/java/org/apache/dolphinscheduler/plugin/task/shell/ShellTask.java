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

package org.apache.dolphinscheduler.plugin.task.shell;

import static java.util.Calendar.DAY_OF_MONTH;


import org.apache.dolphinscheduler.plugin.task.api.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.Direct;
import org.apache.dolphinscheduler.plugin.task.api.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.Property;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskRequest;
import org.apache.dolphinscheduler.plugin.task.api.TaskResponse;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.commons.lang.time.DateUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

/**
 * shell task
 */
public class ShellTask extends AbstractTask {

    /**
     * shell parameters
     */
    private ShellParameters shellParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskRequest taskRequest;

    /**
     * constructor
     *
     * @param taskRequest taskRequest
     * @param logger               logger
     */
    public ShellTask(TaskRequest taskRequest, Logger logger) {
        super(taskRequest, logger);

        this.taskRequest = taskRequest;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
            taskRequest,
            logger);
    }

    @Override
    public void init() {
        logger.info("shell task params {}", taskRequest.getTaskParams());

        shellParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), ShellParameters.class);

        if (!shellParameters.checkParameters()) {
            throw new RuntimeException("shell task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
        try {
            // construct process
            TaskResponse response = shellCommandExecutor.run(buildCommand());
            setExitStatusCode(response.getExitStatusCode());
            setAppIds(response.getAppIds());
            setProcessId(response.getProcessId());
            setResult(shellCommandExecutor.getTaskResultString());
        } catch (Exception e) {
            logger.error("shell task error", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw e;
        }
    }

    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    /**
     * create command
     *
     * @return file name
     * @throws Exception exception
     */
    private String buildCommand() throws Exception {
        // generate scripts
        String fileName = String.format("%s/%s_node.%s",
                taskRequest.getExecutePath(),
                taskRequest.getTaskAppId(), OSUtils.isWindows() ? "bat" : "sh");

        Path path = new File(fileName).toPath();

        if (Files.exists(path)) {
            return fileName;
        }

        String script = shellParameters.getRawScript().replaceAll("\\r\\n", "\n");
        script = parseScript(script);
        shellParameters.setRawScript(script);

        logger.info("raw script : {}", shellParameters.getRawScript());
        logger.info("task execute path : {}", taskRequest.getExecutePath());

        Set<PosixFilePermission> perms = PosixFilePermissions.fromString(TaskConstants.RWXR_XR_X);
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        if (OSUtils.isWindows()) {
            Files.createFile(path);
        } else {
            Files.createFile(path, attr);
        }

        Files.write(path, shellParameters.getRawScript().getBytes(), StandardOpenOption.APPEND);

        return fileName;
    }

    @Override
    public AbstractParameters getParameters() {
        return shellParameters;
    }

    private String parseScript(String script) {

        return ParameterUtils.convertParameterPlaceholders(script, null);
    }

    public void setResult(String result) {
        Map<String, Property> localParams = shellParameters.getLocalParametersMap();
        List<Map<String, String>> outProperties = new ArrayList<>();
        Map<String, String> p = new HashMap<>();
        localParams.forEach((k,v) -> {
            if (v.getDirect() == Direct.OUT) {
                p.put(k, result);
            }
        });
        outProperties.add(p);
        resultString = JSONUtils.toJsonString(outProperties);
    }
}
