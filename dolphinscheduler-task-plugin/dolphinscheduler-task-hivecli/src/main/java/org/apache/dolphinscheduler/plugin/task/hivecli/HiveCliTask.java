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

import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HiveCliTask extends AbstractRemoteTask {

    private HiveCliParameters hiveCliParameters;

    private final ShellCommandExecutor shellCommandExecutor;

    private final TaskExecutionContext taskExecutionContext;

    public HiveCliTask(TaskExecutionContext taskExecutionContext) {
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
        logger.info("hiveCli task params {}", taskExecutionContext.getTaskParams());

        hiveCliParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), HiveCliParameters.class);

        if (!hiveCliParameters.checkParameters()) {
            throw new TaskException("hiveCli task params is not valid");
        }
    }

    // todo split handle to submit and track
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            final TaskResponse taskResponse = shellCommandExecutor.run(buildCommand());
            setExitStatusCode(taskResponse.getExitStatusCode());
            setAppIds(taskResponse.getAppIds());
            setProcessId(taskResponse.getProcessId());
            setVarPool(shellCommandExecutor.getVarPool());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("The current HiveCLI Task has been interrupted", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("The current HiveCLI Task has been interrupted", e);
        } catch (Exception e) {
            logger.error("hiveCli task failure", e);
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

        // TODO: make sure type is not unknown
        if (HiveCliConstants.TYPE_FILE.equals(type)) {
            args.add(HiveCliConstants.HIVE_CLI_EXECUTE_FILE);
            final List<ResourceInfo> resourceInfos = hiveCliParameters.getResourceList();
            if (resourceInfos.size() > 1) {
                logger.warn("more than 1 files detected, use the first one by default");
            }

            args.add(StringUtils.stripStart(resourceInfos.get(0).getResourceName(), "/"));
        } else {
            final String script = hiveCliParameters.getHiveSqlScript();
            args.add(String.format(HiveCliConstants.HIVE_CLI_EXECUTE_SCRIPT, script));
        }

        final String hiveCliOptions = hiveCliParameters.getHiveCliOptions();
        if (StringUtils.isNotEmpty(hiveCliOptions)) {
            args.add(hiveCliOptions);
        }

        final Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        final String command =
                ParameterUtils.convertParameterPlaceholders(String.join(" ", args), ParamUtils.convert(paramsMap));

        logger.info("hiveCli task command: {}", command);

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

}
