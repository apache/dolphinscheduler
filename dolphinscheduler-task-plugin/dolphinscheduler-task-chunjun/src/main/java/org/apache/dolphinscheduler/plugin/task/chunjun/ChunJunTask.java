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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
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
import org.apache.dolphinscheduler.spi.enums.Flag;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RWXR_XR_X;

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
            taskExecutionContext, logger);
    }

    /**
     * init chunjun config
     */
    @Override
    public void init() {
        logger.info("chunjun task params {}", taskExecutionContext.getTaskParams());
        chunJunParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ChunJunParameters.class);

        if (!chunJunParameters.checkParameters()) {
            throw new RuntimeException("chunjun task params is not valid");
        }
    }

    /**
     * run chunjun process
     *
     * @throws TaskException exception
     */
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();

            String jsonFilePath = buildChunJunJsonFile(paramsMap);
            String shellCommandFilePath = buildShellCommandFile(jsonFilePath, paramsMap);
            TaskResponse commandExecuteResult = shellCommandExecutor.run(shellCommandFilePath);

            setExitStatusCode(commandExecuteResult.getExitStatusCode());

            // todo get applicationId
            setAppIds(String.join(TaskConstants.COMMA, Collections.emptySet()));
            setProcessId(commandExecuteResult.getProcessId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("The current ChunJun Task has been interrupted", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("The current ChunJun Task has been interrupted", e);
        } catch (Exception e) {
            logger.error("chunjun task failed.", e);
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
    private String buildChunJunJsonFile(Map<String, Property> paramsMap)
        throws Exception {
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
        json = ParameterUtils.convertParameterPlaceholders(json, ParamUtils.convert(paramsMap));

        logger.debug("chunjun job json : {}", json);

        // create chunjun json file
        FileUtils.writeStringToFile(new File(fileName), json, StandardCharsets.UTF_8);
        return fileName;
    }

    /**
     * create command
     *
     * @return shell command file name
     * @throws Exception if error throws Exception
     */
    private String buildShellCommandFile(String jobConfigFilePath, Map<String, Property> paramsMap)
        throws Exception {
        // generate scripts
        String fileName = String.format("%s/%s_node.%s",
            taskExecutionContext.getExecutePath(),
            taskExecutionContext.getTaskAppId(),
            SystemUtils.IS_OS_WINDOWS ? "bat" : "sh");

        Path path = new File(fileName).toPath();

        if (Files.exists(path)) {
            return fileName;
        }

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

        // replace placeholder
        String chunjunCommand = ParameterUtils.convertParameterPlaceholders(command, ParamUtils.convert(paramsMap));

        logger.info("raw script : {}", chunjunCommand);

        // create shell command file
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString(RWXR_XR_X);
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        if (SystemUtils.IS_OS_WINDOWS) {
            Files.createFile(path);
        } else {
            Files.createFile(path, attr);
        }

        Files.write(path, chunjunCommand.getBytes(), StandardOpenOption.APPEND);

        return fileName;
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
