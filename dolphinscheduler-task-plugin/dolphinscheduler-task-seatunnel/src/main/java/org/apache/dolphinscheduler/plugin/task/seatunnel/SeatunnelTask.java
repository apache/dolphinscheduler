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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.seatunnel.Constants.CONFIG_OPTIONS;

import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * seatunnel task
 */
public class SeatunnelTask extends AbstractRemoteTask {

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
    protected final TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public SeatunnelTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                logger);
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void init() {
        logger.info("SeaTunnel task params {}", taskExecutionContext.getTaskParams());
        if (!seatunnelParameters.checkParameters()) {
            throw new RuntimeException("SeaTunnel task params is not valid");
        }
    }

    // todo split handle to submit and track
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            // construct process
            String command = buildCommand();
            TaskResponse commandExecuteResult = shellCommandExecutor.run(command);
            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setAppIds(String.join(TaskConstants.COMMA, getApplicationIds()));
            setProcessId(commandExecuteResult.getProcessId());
            seatunnelParameters.dealOutParam(shellCommandExecutor.getVarPool());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("The current SeaTunnel task has been interrupted", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("The current SeaTunnel task has been interrupted", e);
        } catch (Exception e) {
            logger.error("SeaTunnel task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("Execute Seatunnel task failed", e);
        }
    }

    @Override
    public void submitApplication() throws TaskException {

    }

    @Override
    public void trackApplicationStatus() throws TaskException {

    }

    @Override
    public void cancelApplication() throws TaskException {
        // cancel process
        try {
            shellCommandExecutor.cancelApplication();
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

    private String buildCommand() throws Exception {

        List<String> args = new ArrayList<>();
        args.add(seatunnelParameters.getEngine().getCommand());
        args.addAll(buildOptions());

        String command = String.join(" ", args);
        logger.info("SeaTunnel task command: {}", command);

        return command;
    }

    protected List<String> buildOptions() throws Exception {
        List<String> args = new ArrayList<>();
        if (BooleanUtils.isTrue(seatunnelParameters.getUseCustom())) {
            args.add(CONFIG_OPTIONS);
            args.add(buildCustomConfigCommand());
        } else {
            seatunnelParameters.getResourceList().forEach(resourceInfo -> {
                args.add(CONFIG_OPTIONS);
                // TODO Currently resourceName is `/xxx.sh`, it has more `/` and needs to be optimized
                args.add(resourceInfo.getResourceName().substring(1));
            });
        }
        return args;
    }

    protected String buildCustomConfigCommand() throws Exception {
        String config = buildCustomConfigContent();
        String filePath = buildConfigFilePath();
        createConfigFileIfNotExists(config, filePath);

        return filePath;
    }

    private String buildCustomConfigContent() {
        logger.info("raw custom config content : {}", seatunnelParameters.getRawScript());
        String script = seatunnelParameters.getRawScript().replaceAll("\\r\\n", "\n");
        script = parseScript(script);
        return script;
    }

    private String buildConfigFilePath() {
        return String.format("%s/seatunnel_%s.conf", taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());
    }

    private void createConfigFileIfNotExists(String script, String scriptFile) throws IOException {
        logger.info("tenantCode :{}, task dir:{}", taskExecutionContext.getTenantCode(),
                taskExecutionContext.getExecutePath());

        if (!Files.exists(Paths.get(scriptFile))) {
            logger.info("generate script file:{}", scriptFile);

            // write data to file
            FileUtils.writeStringToFile(new File(scriptFile), script, StandardCharsets.UTF_8);
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return seatunnelParameters;
    }

    private String parseScript(String script) {
        // combining local and global parameters
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        return ParameterUtils.convertParameterPlaceholders(script, ParamUtils.convert(paramsMap));
    }

    public void setSeatunnelParameters(SeatunnelParameters seatunnelParameters) {
        this.seatunnelParameters = seatunnelParameters;
    }
}
