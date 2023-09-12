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

package org.apache.dolphinscheduler.plugin.task.datax;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.dolphinscheduler.common.log.SensitiveDataConverter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.ShellInterceptorBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.datax.content.BuildDataxJobContentJsonFactory;
import org.apache.dolphinscheduler.plugin.task.datax.content.reader.AbstractBuildDataxJobContentJsonReader;
import org.apache.dolphinscheduler.plugin.task.datax.content.writer.AbstractBuildDataxJobContentJsonWriter;
import org.apache.dolphinscheduler.spi.enums.Flag;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataxTask extends AbstractTask {

    /**
     * jvm parameters
     */
    public static final String JVM_PARAM = "--jvm=\"-Xms%sG -Xmx%sG\" ";

    public static final String CUSTOM_PARAM = " -D%s='%s'";
    /**
     * todo: Create a shell script to execute the datax task, and read the python version from the env, so we can support multiple versions of datax python
     */
    private static final String PYTHON_LAUNCHER = "${PYTHON_LAUNCHER}";

    BuildDataxJobContentJsonFactory buildDataxJobContentJsonFactory;

    /**
     * post jdbc info regex
     */
    private static final String POST_JDBC_INFO_REGEX = "(?<=(post jdbc info:)).*(?=)";
    /**
     * datax path
     */
    private static final String DATAX_LAUNCHER = "${DATAX_LAUNCHER}";
    /**
     * datax channel count
     */
    private static final int DATAX_CHANNEL_COUNT = 1;

    /**
     * datax parameters
     */
    private DataxParameters dataXParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    private DataxTaskExecutionContext dataxTaskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public DataxTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;

        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext, log);
    }

    /**
     * init DataX config
     */
    @Override
    public void init() {
        dataXParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), DataxParameters.class);
        log.info("Initialize datax task params {}", JSONUtils.toPrettyJsonString(dataXParameters));

        if (dataXParameters == null || !dataXParameters.checkParameters()) {
            throw new RuntimeException("datax task params is not valid");
        }
        SensitiveDataConverter.addMaskPattern(POST_JDBC_INFO_REGEX);
        dataxTaskExecutionContext =
                dataXParameters.generateExtendedContext(taskExecutionContext.getResourceParametersHelper());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            // replace placeholder,and combine local and global parameters
            Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();

            IShellInterceptorBuilder<?, ?> shellActuatorBuilder = ShellInterceptorBuilderFactory.newBuilder()
                    .properties(ParameterUtils.convert(paramsMap))
                    .appendScript(buildCommand(buildDataxJsonFile(paramsMap), paramsMap));

            TaskResponse commandExecuteResult = shellCommandExecutor.run(shellActuatorBuilder, taskCallBack);

            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setProcessId(commandExecuteResult.getProcessId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("The current DataX task has been interrupted", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("The current DataX task has been interrupted", e);
        } catch (Exception e) {
            log.error("datax task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw new TaskException("Execute DataX task failed", e);
        }
    }

    /**
     * cancel DataX process
     *
     * @throws TaskException if error throws Exception
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

    /**
     * build datax configuration file
     *
     * @return datax json file name
     * @throws Exception if error throws Exception
     */
    private String buildDataxJsonFile(Map<String, Property> paramsMap) throws Exception {
        // generate json
        String fileName = String.format("%s/%s_job.json",
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());
        String json;

        Path path = new File(fileName).toPath();
        if (Files.exists(path)) {
            return fileName;
        }

        if (dataXParameters.getCustomConfig() == Flag.YES.ordinal()) {
            json = dataXParameters.getJson().replaceAll("\\r\\n", System.lineSeparator());
        } else {
            ObjectNode job = JSONUtils.createObjectNode();
            job.putArray("content").addAll(buildDataxJobContentJson());
            job.set("setting", buildDataxJobSettingJson());

            ObjectNode root = JSONUtils.createObjectNode();
            root.set("job", job);
            root.set("core", buildDataxCoreJson());
            json = root.toString();
        }

        // replace placeholder
        json = ParameterUtils.convertParameterPlaceholders(json, ParameterUtils.convert(paramsMap));

        log.debug("datax job json : {}", json);

        // create datax json file
        FileUtils.writeStringToFile(new File(fileName), json, StandardCharsets.UTF_8);
        return fileName;
    }

    /**
     * build datax job config
     *
     * @return collection of datax job config JSONObject
     */
    private List<ObjectNode> buildDataxJobContentJson() {
        buildDataxJobContentJsonFactory = new BuildDataxJobContentJsonFactory();
        AbstractBuildDataxJobContentJsonReader reader =
                buildDataxJobContentJsonFactory.getReader(dataxTaskExecutionContext.getSourcetype());
        AbstractBuildDataxJobContentJsonWriter writer =
                buildDataxJobContentJsonFactory.getWriter(dataxTaskExecutionContext.getTargetType());

        reader.init(dataxTaskExecutionContext, dataXParameters);
        writer.init(dataxTaskExecutionContext, dataXParameters);

        List<ObjectNode> contentList = new ArrayList<>();
        ObjectNode content = JSONUtils.createObjectNode();
        content.set("reader", reader.reader());
        content.set("writer", writer.writer());
        contentList.add(content);

        return contentList;
    }

    /**
     * build datax setting config
     *
     * @return datax setting config JSONObject
     */
    private ObjectNode buildDataxJobSettingJson() {

        ObjectNode speed = JSONUtils.createObjectNode();

        speed.put("channel", DATAX_CHANNEL_COUNT);

        if (dataXParameters.getJobSpeedByte() > 0) {
            speed.put("byte", dataXParameters.getJobSpeedByte());
        }

        if (dataXParameters.getJobSpeedRecord() > 0) {
            speed.put("record", dataXParameters.getJobSpeedRecord());
        }

        ObjectNode errorLimit = JSONUtils.createObjectNode();
        errorLimit.put("record", 0);
        errorLimit.put("percentage", 0);

        ObjectNode setting = JSONUtils.createObjectNode();
        setting.set("speed", speed);
        setting.set("errorLimit", errorLimit);

        return setting;
    }

    private ObjectNode buildDataxCoreJson() {

        ObjectNode speed = JSONUtils.createObjectNode();
        speed.put("channel", DATAX_CHANNEL_COUNT);

        if (dataXParameters.getJobSpeedByte() > 0) {
            speed.put("byte", dataXParameters.getJobSpeedByte());
        }

        if (dataXParameters.getJobSpeedRecord() > 0) {
            speed.put("record", dataXParameters.getJobSpeedRecord());
        }

        ObjectNode channel = JSONUtils.createObjectNode();
        channel.set("speed", speed);

        ObjectNode transport = JSONUtils.createObjectNode();
        transport.set("channel", channel);

        ObjectNode core = JSONUtils.createObjectNode();
        core.set("transport", transport);

        return core;
    }

    /**
     * create command
     *
     * @return shell command file name
     */
    protected String buildCommand(String jobConfigFilePath, Map<String, Property> paramsMap) {
        // datax python command
        return PYTHON_LAUNCHER +
                " " +
                DATAX_LAUNCHER +
                " " +
                loadJvmEnv(dataXParameters) +
                addCustomParameters(paramsMap) +
                " " +
                jobConfigFilePath;
    }

    private StringBuilder addCustomParameters(Map<String, Property> paramsMap) {
        if (paramsMap == null || paramsMap.size() == 0) {
            return new StringBuilder();
        }
        StringBuilder customParameters = new StringBuilder("-p \"");
        for (Map.Entry<String, Property> entry : paramsMap.entrySet()) {
            customParameters.append(String.format(CUSTOM_PARAM, entry.getKey(), entry.getValue().getValue()));
        }
        customParameters.replace(4, 5, "");
        customParameters.append("\"");
        return customParameters;
    }

    public String loadJvmEnv(DataxParameters dataXParameters) {
        int xms = Math.max(dataXParameters.getXms(), 1);
        int xmx = Math.max(dataXParameters.getXmx(), 1);
        return String.format(JVM_PARAM, xms, xmx);
    }

    @Override
    public AbstractParameters getParameters() {
        return dataXParameters;
    }

}
