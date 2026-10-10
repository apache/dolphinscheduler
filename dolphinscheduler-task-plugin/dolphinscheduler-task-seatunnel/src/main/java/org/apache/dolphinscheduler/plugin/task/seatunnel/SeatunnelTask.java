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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.ShellInterceptorBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeatunnelTask extends AbstractRemoteTask {

    protected static final String SEATUNNEL_BIN_DIR = "${SEATUNNEL_HOME}/bin/";

    private static final Pattern SEATUNNEL_JOB_ID_PATTERN = Pattern.compile(Constants.SEATUNNEL_JOB_ID_REGEX);

    private SeatunnelParameters seatunnelParameters;

    private final ShellCommandExecutor shellCommandExecutor;

    public SeatunnelTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);

        this.shellCommandExecutor = new ShellCommandExecutor(taskExecutionContext);
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void init() {
        log.info("Intialize SeaTunnel task params {}", JSONUtils.toPrettyJsonString(seatunnelParameters));
        if (seatunnelParameters == null || !seatunnelParameters.checkParameters()) {
            throw new TaskException("SeaTunnel task params is not valid");
        }
    }

    // todo split handle to submit and track
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            // construct process
            String command = buildCommand();
            IShellInterceptorBuilder<?, ?> shellActuatorBuilder = ShellInterceptorBuilderFactory.newBuilder()
                    .appendScript(command);

            TaskResponse commandExecuteResult = shellCommandExecutor.run(shellActuatorBuilder, taskCallBack);
            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setAppIds(String.join(TaskConstants.COMMA, getApplicationIds()));
            setProcessId(commandExecuteResult.getProcessId());
            seatunnelParameters.dealOutParam(shellCommandExecutor.getTaskOutputParams());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("The current SeaTunnel task has been interrupted", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("The current SeaTunnel task has been interrupted", e);
        } catch (Exception e) {
            log.error("SeaTunnel task error", e);
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
            cancelShellProcess();
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

    protected void cancelShellProcess() throws Exception {
        shellCommandExecutor.cancelApplication();
    }

    /**
     * Extract SeaTunnel Engine job id from a log line.
     *
     * @param line log line
     * @return job id or null
     */
    public static String findSeaTunnelJobId(String line) {
        if (line == null) {
            return null;
        }
        Matcher matcher = SEATUNNEL_JOB_ID_PATTERN.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Parse SeaTunnel Engine job ids from the task log file.
     */
    protected List<String> findSeaTunnelJobIdsFromLog() {
        String logPath = taskRequest.getLogPath();
        if (logPath == null || logPath.isEmpty()) {
            return Collections.emptyList();
        }
        File logFile = new File(logPath);
        if (!logFile.exists() || !logFile.isFile()) {
            return Collections.emptyList();
        }
        Set<String> jobIds = new HashSet<>();
        try {
            for (String line : Files.readAllLines(Paths.get(logPath), StandardCharsets.UTF_8)) {
                String jobId = findSeaTunnelJobId(line);
                if (jobId != null) {
                    jobIds.add(jobId);
                }
            }
        } catch (IOException e) {
            log.error("Failed to parse SeaTunnel job id from log: {}", logPath, e);
            return Collections.emptyList();
        }
        return new ArrayList<>(jobIds);
    }

    private String buildCommand() throws Exception {

        List<String> args = new ArrayList<>();
        args.add(SEATUNNEL_BIN_DIR + seatunnelParameters.getStartupScript());
        args.addAll(buildOptions());

        String command = String.join(" ", args);
        log.info("SeaTunnel task command: {}", command);

        return command;
    }

    protected List<String> buildOptions() throws Exception {
        List<String> args = new ArrayList<>();
        args.add(CONFIG_OPTIONS);
        String scriptContent;
        if (BooleanUtils.isTrue(seatunnelParameters.getUseCustom())) {
            scriptContent = buildCustomConfigContent();
        } else {
            String resourceFileName = seatunnelParameters.getResourceList().get(0).getResourceName();
            ResourceContext resourceContext = taskRequest.getResourceContext();
            scriptContent = FileUtils.readFileToString(
                    new File(resourceContext.getResourceItem(resourceFileName).getResourceAbsolutePathInLocal()),
                    StandardCharsets.UTF_8);
        }
        String filePath = buildConfigFilePath();
        createConfigFileIfNotExists(scriptContent, filePath);
        args.add(filePath);
        args.addAll(generateTaskParameters());
        return args;
    }

    private List<String> generateTaskParameters() {
        Map<String, String> variables = new HashMap<>();
        Map<String, Property> paramsMap = taskRequest.getPrepareParamsMap();
        List<Property> propertyList = GlobalParameterUtils.deserializeGlobalParameter(taskRequest.getGlobalParams());
        if (propertyList != null && !propertyList.isEmpty()) {
            for (Property property : propertyList) {
                variables.put(property.getProp(), paramsMap.get(property.getProp()).getValue());
            }
        }
        List<Property> localParams = this.seatunnelParameters.getLocalParams();
        if (localParams != null && !localParams.isEmpty()) {
            for (Property property : localParams) {
                if (property.getDirect().equals(Direct.IN)) {
                    variables.put(property.getProp(), paramsMap.get(property.getProp()).getValue());
                }
            }
        }
        List<String> parameters = new ArrayList<>();
        variables.forEach((k, v) -> {
            parameters.add("-i");
            parameters.add(String.format("%s=%s", k, quoteForBash(v)));
        });
        return parameters;
    }

    private static String quoteForBash(String value) {
        if (value == null) {
            return "''";
        }
        // Escape single quotes in a bash-safe way: abc'def -> 'abc'"'"'def'
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private String buildCustomConfigContent() {
        log.info("raw custom config content : {}", seatunnelParameters.getRawScript());
        String script = seatunnelParameters.getRawScript().replaceAll("\\r\\n", System.lineSeparator());
        script = parseScript(script);
        return script;
    }

    private String buildConfigFilePath() {
        return String.format("%s/seatunnel_%s.%s", taskRequest.getExecutePath(),
                taskRequest.getTaskAppId(), formatDetector());
    }

    private String formatDetector() {
        return JSONUtils.checkJsonValid(seatunnelParameters.getRawScript(), false) ? Constants.JSON_SUFFIX
                : Constants.CONF_SUFFIX;
    }

    private void createConfigFileIfNotExists(String script, String scriptFile) throws IOException {
        log.info("tenantCode :{}, task dir:{}", taskRequest.getTenantCode(),
                taskRequest.getExecutePath());

        if (!Files.exists(Paths.get(scriptFile))) {
            log.info("generate script file:{}", scriptFile);

            // write data to file
            FileUtils.writeStringToFile(new File(scriptFile), script, StandardCharsets.UTF_8);
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return seatunnelParameters;
    }

    private String parseScript(String script) {
        Map<String, Property> paramsMap = taskRequest.getPrepareParamsMap();
        return ParameterUtils.convertParameterPlaceholders(script, ParameterUtils.convert(paramsMap));
    }

    public void setSeatunnelParameters(SeatunnelParameters seatunnelParameters) {
        this.seatunnelParameters = seatunnelParameters;
    }
}
