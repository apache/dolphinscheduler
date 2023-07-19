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

package org.apache.dolphinscheduler.plugin.task.chunjun;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.ShellInterceptorBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.enums.Flag;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * chunjun task
 */
public class ChunJunTask extends AbstractTask {

    /**
     * chunjun path
     */
    private static final String CHUNJUN_PATH = "${CHUNJUN_HOME}/bin/start-chunjun";

    /**
     * chunjun dist
     */
    private static final String CHUNJUN_DIST_DIR = "${CHUNJUN_HOME}/chunjun-dist";

    /**
     * chunJun parameters
     */
    private ChunJunParameters chunJunParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    public ChunJunTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;

        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext, log);
    }

    /**
     * init chunjun config
     */
    @Override
    public void init() {
        chunJunParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ChunJunParameters.class);
        log.info("Initialize chunjun task params {}",
                JSONUtils.toPrettyJsonString(taskExecutionContext.getTaskParams()));

        if (!chunJunParameters.checkParameters()) {
            throw new RuntimeException("chunjun task params is not valid");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();

            IShellInterceptorBuilder<?, ?> shellActuatorBuilder = ShellInterceptorBuilderFactory.newBuilder()
                    .properties(ParameterUtils.convert(paramsMap))
                    .appendScript(buildCommand(buildChunJunJsonFile(paramsMap)));
            TaskResponse commandExecuteResult = shellCommandExecutor.run(shellActuatorBuilder, taskCallBack);

            setExitStatusCode(commandExecuteResult.getExitStatusCode());

            // todo get applicationId
            setAppIds(String.join(TaskConstants.COMMA, Collections.emptySet()));
            setProcessId(commandExecuteResult.getProcessId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("The current ChunJun Task has been interrupted", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("The current ChunJun Task has been interrupted", e);
        } catch (Exception e) {
            log.error("chunjun task failed.", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("Execute chunjun task failed", e);
        }
    }

    /**
     * build chunjun json file
     *
     * @param paramsMap
     * @return
     * @throws Exception
     */
    private String buildChunJunJsonFile(Map<String, Property> paramsMap) throws Exception {
        // generate json
        String fileName = String.format("%s/%s_job.json",
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());

        String json = null;

        Path path = new File(fileName).toPath();
        if (Files.exists(path)) {
            return fileName;
        }

        if (chunJunParameters.getCustomConfig() == Flag.YES.ordinal()) {
            json = chunJunParameters.getJson().replaceAll("\\r\\n", "\n");
        }

        // replace placeholder
        json = ParameterUtils.convertParameterPlaceholders(json, ParameterUtils.convert(paramsMap));

        log.debug("chunjun job json : {}", json);

        // create chunjun json file
        FileUtils.writeStringToFile(new File(fileName), json, StandardCharsets.UTF_8);
        return fileName;
    }

    private String buildCommand(String jobConfigFilePath) {
        // chunjun command
        List<String> args = new ArrayList<>();

        args.add(CHUNJUN_PATH);
        args.add("-mode");
        args.add(getExecMode(chunJunParameters));
        args.add("-jobType sync");
        args.add("-job");
        args.add(jobConfigFilePath);
        args.add("-chunjunDistDir");
        args.add(CHUNJUN_DIST_DIR);

        if (!"local".equalsIgnoreCase(getExecMode(chunJunParameters))) {
            args.add("-flinkConfDir");
            args.add(ChunJunConstants.FLINK_CONF_DIR);

            args.add("-flinkLibDir");
            args.add(ChunJunConstants.FLINK_LIB_DIR);

            args.add("-hadoopConfDir");
            args.add(ChunJunConstants.HADOOP_CONF_DIR);
        }

        if (chunJunParameters.getOthers() != null) {
            args.add(chunJunParameters.getOthers());
        }

        String command = String.join(" ", args);

        return command;
    }

    public String getExecMode(ChunJunParameters chunJunParameters) {
        if (chunJunParameters.getDeployMode() == null) {
            return "local";
        }
        return chunJunParameters.getDeployMode();
    }

    /**
     * get task parameters
     *
     * @return AbstractParameters
     */
    @Override
    public AbstractParameters getParameters() {
        return chunJunParameters;
    }

    /**
     * cancel ChunJun process
     *
     * @throws Exception if error throws Exception
     */
    @Override
    public void cancel() throws TaskException {
        // cancel process
        try {
            shellCommandExecutor.cancelApplication();
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

}
