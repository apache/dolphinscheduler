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

package org.apache.dolphinscheduler.plugin.task.remoteshell;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.ssh.param.SSHConnectionParam;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * shell task
 */
public class RemoteShellTask extends AbstractTask {

    static final String TASK_ID_PREFIX = "dolphinscheduler-remoteshell-";

    /**
     * shell parameters
     */
    private RemoteShellParameters remoteShellParameters;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    private RemoteExecutor remoteExecutor;

    private String taskId;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public RemoteShellTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        log.info("shell task params {}", taskExecutionContext.getTaskParams());

        remoteShellParameters =
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), RemoteShellParameters.class);

        if (!remoteShellParameters.checkParameters()) {
            throw new TaskException("sell task params is not valid");
        }

        taskId = taskExecutionContext.getAppIds();
        if (taskId == null) {
            taskId = TASK_ID_PREFIX + taskExecutionContext.getTaskInstanceId();
        }
        setAppIds(taskId);
        taskExecutionContext.setAppIds(taskId);

        initRemoteExecutor();
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            // construct process
            String localFile = buildCommand();
            int exitCode = remoteExecutor.run(taskId, localFile);
            setExitStatusCode(exitCode);
            remoteShellParameters.dealOutParam(remoteExecutor.getVarPool());
        } catch (Exception e) {
            log.error("shell task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("Execute shell task error", e);
        }
    }

    @Override
    public void cancel() throws TaskException {
        // cancel process
        try {
            log.info("kill remote task {}", taskId);
            remoteExecutor.kill(taskId);
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

    /**
     * create command
     *
     * @return file name
     * @throws Exception exception
     */
    public String buildCommand() throws Exception {
        // generate scripts
        String fileName = String.format("%s/%s_node.%s",
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId(), SystemUtils.IS_OS_WINDOWS ? "bat" : "sh");

        File file = new File(fileName);
        Path path = file.toPath();

        if (Files.exists(path)) {
            // this shouldn't happen
            log.warn("The command file: {} is already exist", path);
            return fileName;
        }

        String script = remoteShellParameters.getRawScript().replaceAll("\\r\\n", "\n");
        script = parseScript(script);

        String environment = taskExecutionContext.getEnvironmentConfig();
        if (environment != null) {
            environment = environment.replaceAll("\\r\\n", "\n");
            environment = environment.replace("\r\n", "\n");
            script = environment + "\n" + script;
        }
        script = String.format(RemoteExecutor.COMMAND.HEADER) + script;
        script += String.format(RemoteExecutor.COMMAND.ADD_STATUS_COMMAND, RemoteExecutor.STATUS_TAG_MESSAGE);

        FileUtils.createFileWith755(path);
        Files.write(path, script.getBytes(), StandardOpenOption.APPEND);
        log.info("raw script : {}", script);
        return fileName;
    }

    @Override
    public AbstractParameters getParameters() {
        return remoteShellParameters;
    }

    private String parseScript(String script) {
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        return ParameterUtils.convertParameterPlaceholders(script, ParameterUtils.convert(paramsMap));
    }

    public void initRemoteExecutor() {
        DataSourceParameters dbSource = (DataSourceParameters) taskExecutionContext.getResourceParametersHelper()
                .getResourceParameters(ResourceType.DATASOURCE, remoteShellParameters.getDatasource());
        taskExecutionContext.getResourceParametersHelper().getResourceParameters(ResourceType.DATASOURCE,
                remoteShellParameters.getDatasource());
        SSHConnectionParam sshConnectionParam = (SSHConnectionParam) DataSourceUtils.buildConnectionParams(
                DbType.valueOf(remoteShellParameters.getType()),
                dbSource.getConnectionParams());
        remoteExecutor = new RemoteExecutor(sshConnectionParam);
    }
}
