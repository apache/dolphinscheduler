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

package org.apache.dolphinscheduler.plugin.task.linkis;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;

import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * linkis task
 */
public class LinkisTask extends AbstractRemoteTask {

    /**
     * linkis parameters
     */
    private LinkisParameters linkisParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    protected final TaskExecutionContext taskExecutionContext;

    private String taskId;

    protected static final Pattern LINKIS_TASK_ID_REGEX = Pattern.compile(Constants.LINKIS_TASK_ID_REGEX);

    protected static final Pattern LINKIS_STATUS_REGEX = Pattern.compile(Constants.LINKIS_STATUS_REGEX);

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public LinkisTask(TaskExecutionContext taskExecutionContext) {
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
        logger.info("Linkis task params {}", taskExecutionContext.getTaskParams());
        if (!linkisParameters.checkParameters()) {
            throw new RuntimeException("Linkis task params is not valid");
        }
    }

    @Override
    public void submitApplication() throws TaskException {
        try {
            // construct process
            String command = buildCommand();
            TaskResponse commandExecuteResult = shellCommandExecutor.run(command);
            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setAppIds(findTaskId(commandExecuteResult.getResultString()));
            setProcessId(commandExecuteResult.getProcessId());
            linkisParameters.dealOutParam(shellCommandExecutor.getVarPool());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("The current Linkis task has been interrupted", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("The current Linkis task has been interrupted", e);
        } catch (Exception e) {
            logger.error("Linkis task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("Execute Linkis task failed", e);
        }
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        initTaskId();
        try {
            List<String> args = new ArrayList<>();
            args.add(Constants.SHELL_CLI_OPTIONS);
            args.add(Constants.STATUS_OPTIONS);
            args.add(taskId);
            String command = String.join(Constants.SPACE, args);
            TaskResponse commandExecuteResult = shellCommandExecutor.run(command);
            String status = findStatus(commandExecuteResult.getResultString());
            LinkisJobStatus jobStatus = LinkisJobStatus.convertFromJobStatusString(status);
            switch (jobStatus) {
                case FAILED:
                    setExitStatusCode(EXIT_CODE_FAILURE);
                    break;
                case SUCCEED:
                    setExitStatusCode(EXIT_CODE_SUCCESS);
                    break;
                case CANCELLED:
                    setExitStatusCode(EXIT_CODE_KILL);
                    break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("The current Linkis task has been interrupted", e);
            throw new TaskException("The current Linkis task has been interrupted", e);
        } catch (Exception e) {
            throw new TaskException("track linkis status error", e);
        }
    }

    @Override
    public void cancelApplication() throws TaskException {
        // cancel process
        initTaskId();
        try {
            List<String> args = new ArrayList<>();
            args.add(Constants.SHELL_CLI_OPTIONS);
            args.add(Constants.KILL_OPTIONS);
            args.add(taskId);
            String command = String.join(Constants.SPACE, args);
            shellCommandExecutor.run(command);
            setExitStatusCode(EXIT_CODE_KILL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("The current Linkis task has been interrupted", e);
            throw new TaskException("The current Linkis task has been interrupted", e);
        } catch (Exception e) {
            throw new TaskException("cancel linkis task error", e);
        }
    }

    private String buildCommand() {

        List<String> args = new ArrayList<>();
        args.addAll(buildOptions());

        String command = String.join(Constants.SPACE, args);
        logger.info("Linkis task command: {}", command);

        return command;
    }

    protected List<String> buildOptions() {
        List<String> args = new ArrayList<>();
        args.add(Constants.SHELL_CLI_OPTIONS);
        args.add(Constants.ASYNC_OPTIONS);
        if (BooleanUtils.isTrue(linkisParameters.getUseCustom())) {
            args.add(buildCustomConfigContent());
        } else {
            args.add(buildParamConfigContent());
        }
        return args;
    }

    private String buildCustomConfigContent() {
        logger.info("raw custom config content : {}", linkisParameters.getRawScript());
        String script = linkisParameters.getRawScript().replaceAll("\\r\\n", "\n");
        script = parseScript(script);
        return script;
    }

    private String buildParamConfigContent() {
        logger.info("raw param config content : {}", linkisParameters.getParamScript());
        String script = "";
        List<LinkisParameters.Param> paramList = linkisParameters.getParamScript();
        for (LinkisParameters.Param param : paramList) {
            script = script.concat(param.getProps())
                    .concat(Constants.SPACE)
                    .concat(param.getValue());
        }
        script = parseScript(script);
        return script;
    }

    private void initTaskId() {
        if (taskId == null) {
            if (StringUtils.isNotEmpty(getAppIds())) {
                taskId = getAppIds();
            }
        }
        if (taskId == null) {
            throw new TaskException("linkis task id is null");
        }
    }

    protected String findTaskId(String line) {
        Matcher matcher = LINKIS_TASK_ID_REGEX.matcher(line);
        if (matcher.find()) {
            String str = matcher.group();
            return str.substring(11);
        }
        return null;
    }

    protected String findStatus(String line) {
        Matcher matcher = LINKIS_STATUS_REGEX.matcher(line);
        if (matcher.find()) {
            String str = matcher.group();
            return str.substring(11);
        }
        return null;
    }

    @Override
    public AbstractParameters getParameters() {
        return linkisParameters;
    }

    private String parseScript(String script) {
        // combining local and global parameters
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        return ParameterUtils.convertParameterPlaceholders(script, ParamUtils.convert(paramsMap));
    }

    public void setLinkisParameters(LinkisParameters linkisParameters) {
        this.linkisParameters = linkisParameters;
    }
}
