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

package org.apache.dolphinscheduler.plugin.task.hivecli;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RWXR_XR_X;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.ShellInterceptorBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
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

public class HiveCliTask extends AbstractRemoteTask {

    private HiveCliParameters hiveCliParameters;

    private final ShellCommandExecutor shellCommandExecutor;

    private final TaskExecutionContext taskExecutionContext;

    public HiveCliTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;

        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                log);
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void init() {
        log.info("hiveCli task params {}", taskExecutionContext.getTaskParams());

        hiveCliParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), HiveCliParameters.class);

        if (!hiveCliParameters.checkParameters()) {
            throw new TaskException("hiveCli task params is not valid");
        }
    }

    // todo split handle to submit and track
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            IShellInterceptorBuilder<?, ?> shellActuatorBuilder = ShellInterceptorBuilderFactory.newBuilder()
                    .appendScript(buildCommand());
            final TaskResponse taskResponse = shellCommandExecutor.run(shellActuatorBuilder, taskCallBack);
            setExitStatusCode(taskResponse.getExitStatusCode());
            setAppIds(taskResponse.getAppIds());
            setProcessId(taskResponse.getProcessId());
            setVarPool(shellCommandExecutor.getVarPool());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("The current HiveCLI Task has been interrupted", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("The current HiveCLI Task has been interrupted", e);
        } catch (Exception e) {
            log.error("hiveCli task failure", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("run hiveCli task error", e);
        }
    }

    @Override
    public void submitApplication() throws TaskException {

    }

    @Override
    public void trackApplicationStatus() throws TaskException {

    }

    protected String buildCommand() {

        final List<String> args = new ArrayList<>();

        final String type = hiveCliParameters.getHiveCliTaskExecutionType();

        String sqlContent = "";
        String resourceFileName = "";
        // TODO: make sure type is not unknown
        if (HiveCliConstants.TYPE_FILE.equals(type)) {
            final List<ResourceInfo> resourceInfos = hiveCliParameters.getResourceList();
            if (resourceInfos.size() > 1) {
                log.warn("more than 1 files detected, use the first one by default");
            }

            try {
                resourceFileName = resourceInfos.get(0).getResourceName();
                sqlContent = FileUtils.readFileToString(
                        new File(String.format("%s/%s", taskExecutionContext.getExecutePath(), resourceFileName)),
                        StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("read hive sql content from file {} error ", resourceFileName, e);
                throw new TaskException("read hive sql content error", e);
            }
        } else {
            sqlContent = hiveCliParameters.getHiveSqlScript();
        }

        final Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        sqlContent = ParameterUtils.convertParameterPlaceholders(sqlContent, ParameterUtils.convert(paramsMap));
        log.info("HiveCli sql content: {}", sqlContent);
        String sqlFilePath = generateSqlScriptFile(sqlContent);

        args.add(HiveCliConstants.HIVE_CLI_EXECUTE_FILE);
        args.add(new File(sqlFilePath).getName());
        final String hiveCliOptions = hiveCliParameters.getHiveCliOptions();
        if (StringUtils.isNotEmpty(hiveCliOptions)) {
            args.add(hiveCliOptions);
        }

        String command = String.join(" ", args);
        log.info("hiveCli task command: {}", command);

        return command;

    }

    @Override
    public AbstractParameters getParameters() {
        return hiveCliParameters;
    }

    @Override
    public void cancelApplication() throws TaskException {
        try {
            shellCommandExecutor.cancelApplication();
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

    protected String generateSqlScriptFile(String rawScript) {
        String scriptFileName = String.format("%s/%s_node.sql", taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());

        File file = new File(scriptFileName);
        Path path = file.toPath();

        if (!Files.exists(path)) {
            String script = rawScript.replaceAll("\\r\\n", "\n");

            Set<PosixFilePermission> perms = PosixFilePermissions.fromString(RWXR_XR_X);
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            try {
                if (OSUtils.isWindows()) {
                    Files.createFile(path);
                } else {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    Files.createFile(path, attr);
                }
                Files.write(path, script.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                log.error("generate hivecli sql script error", e);
                throw new TaskException("generate hivecli sql script error", e);
            }
        }
        return scriptFileName;
    }

}
