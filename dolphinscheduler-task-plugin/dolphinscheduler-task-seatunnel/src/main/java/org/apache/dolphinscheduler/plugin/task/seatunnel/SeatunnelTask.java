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

package org.apache.dolphinscheduler.plugin.task.seatunnel;

import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.RWXR_XR_X;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.util.OSUtils;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.Property;
import org.apache.dolphinscheduler.spi.task.paramparser.ParamUtils;
import org.apache.dolphinscheduler.spi.task.paramparser.ParameterUtils;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.commons.collections4.MapUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * seatunnel task
 */
public class SeatunnelTask extends AbstractTaskExecutor {

    /**
     * seatunnel parameters
     */
    private SeatunnelParameters seatunnelParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskRequest taskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public SeatunnelTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                logger);
    }

    @Override
    public void init() {
        logger.info("seatunnel task params {}", taskExecutionContext.getTaskParams());

        seatunnelParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SeatunnelParameters.class);

        if (!seatunnelParameters.checkParameters()) {
            throw new RuntimeException("seatunnel task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
        try {
            // construct process
            String command = buildCommand();
            TaskResponse commandExecuteResult = shellCommandExecutor.run(command);
            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setAppIds(commandExecuteResult.getAppIds());
            setProcessId(commandExecuteResult.getProcessId());
            seatunnelParameters.dealOutParam(shellCommandExecutor.getVarPool());
        } catch (Exception e) {
            logger.error("seatunnel task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
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
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId(), OSUtils.isWindows() ? "bat" : "sh");

        Path path = new File(fileName).toPath();

        if (Files.exists(path)) {
            return fileName;
        }

        String script = seatunnelParameters.getRawScript().replaceAll("\\r\\n", "\n");
        script = parseScript(script);
        seatunnelParameters.setRawScript(script);

        logger.info("raw script : {}", seatunnelParameters.getRawScript());
        logger.info("task execute path : {}", taskExecutionContext.getExecutePath());

        Set<PosixFilePermission> perms = PosixFilePermissions.fromString(RWXR_XR_X);
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        if (OSUtils.isWindows()) {
            Files.createFile(path);
        } else {
            Files.createFile(path, attr);
        }

        Files.write(path, seatunnelParameters.getRawScript().getBytes(), StandardOpenOption.APPEND);

        return fileName;
    }

    @Override
    public AbstractParameters getParameters() {
        return seatunnelParameters;
    }

    private String parseScript(String script) {
        // combining local and global parameters
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext,getParameters());
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            paramsMap.putAll(taskExecutionContext.getParamsMap());
        }
        return ParameterUtils.convertParameterPlaceholders(script, ParamUtils.convert(paramsMap));
    }
}
