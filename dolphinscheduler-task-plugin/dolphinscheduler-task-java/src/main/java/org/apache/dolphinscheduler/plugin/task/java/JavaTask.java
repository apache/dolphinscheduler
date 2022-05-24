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

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * java task
 */
public class JavaTask extends AbstractTaskExecutor {

    /**
     * java parameters
     */
    private JavaParameters javaParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    private TaskExecutionContext taskRequest;


    /**
     * constructor
     *
     * @param taskRequest taskRequest
     */
    public JavaTask(TaskExecutionContext taskRequest) {
        super(taskRequest);
        this.taskRequest = taskRequest;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskRequest,
                logger);
    }

    @Override
    public void init() {
        logger.info("java task params {}", taskRequest.getTaskParams());
        javaParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), JavaParameters.class);
        if (javaParameters==null||!javaParameters.checkParameters()) {
            throw new TaskException("java task params is not valid");
        }
    }

    @Override
    public String getPreScript() {
        String rawJavaScript = javaParameters.getRawScript().replaceAll("\\r\\n", "\n");
        try {
            rawJavaScript = convertJavaSourceCodePlaceholders(rawJavaScript);
        } catch (StringIndexOutOfBoundsException e) {
            logger.error("setShareVar field format error, raw java script : {}", rawJavaScript);
        }
        return rawJavaScript;
    }

    @Override
    public void handle() throws Exception {
        try {
            // 第一步，区分java还是jar类型
            // jar类型直接构建命令，构建命令时将resource路劲加入类路劲  java -jar
            // java类型，先替换自定义参数，然后编译代码，然后构建命令时将resource路劲加入类路劲，如果没有依赖的资源直接使用私有classloader加速执行。 java classname
            // shell执行器run命令
            String command = null;
            switch (javaParameters.getRunType()) {
                case JavaConstants.RUN_TYPE_JAVA:
                    command = buildJavaCommand();
                    break;
                case JavaConstants.RUN_TYPE_JAR:
                    command = buildJarCommand();
                    break;
            }
            TaskResponse taskResponse = shellCommandExecutor.run(buildJavaExecuteCommand(command));
            setExitStatusCode(taskResponse.getExitStatusCode());
            setAppIds(taskResponse.getAppIds());
            setProcessId(taskResponse.getProcessId());
            setVarPool(shellCommandExecutor.getVarPool());
        } catch (Exception e) {
            logger.error("java task failure", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("run java task error", e);
        }
    }

    private String buildJavaCommand() throws Exception {
        String sourceCode = buildJavaSourceContent();
        String className = compilerRawScript(sourceCode);
        StringBuilder builder = new StringBuilder();
        builder.append("java").append(" ")
                .append(className).append(" ")
                .append(javaParameters.getMainArgs().trim()).append(" ")
                .append(javaParameters.getJvmArgs().trim()).append(" ")
                .append(buildResourcePath());
        return builder.toString();
    }



    private String buildJarCommand() {
        String fullName = javaParameters.getMainJar().getResourceName();
        String mainJarName = fullName.substring(0, fullName.lastIndexOf('.'));
        mainJarName = mainJarName.substring(mainJarName.lastIndexOf('.') + 1) + ".jar";
        StringBuilder builder = new StringBuilder();
        builder.append("java").append(" ")
                .append("-jar").append(" ")
                .append(mainJarName).append(" ")
                .append(javaParameters.getMainArgs().trim()).append(" ")
                .append(javaParameters.getJvmArgs().trim()).append(" ")
                .append(buildResourcePath());
        return builder.toString();
    }
    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    @Override
    public AbstractParameters getParameters() {
        return javaParameters;
    }

    /**
     * convertJavaScriptPlaceholders
     *
     * @param rawScript rawScript
     * @return String
     * @throws StringIndexOutOfBoundsException StringIndexOutOfBoundsException
     */
    private static String convertJavaSourceCodePlaceholders(String rawScript) throws StringIndexOutOfBoundsException {
        int len = "${setShareVar(${".length();
        int scriptStart = 0;
        while ((scriptStart = rawScript.indexOf("${setShareVar(${", scriptStart)) != -1) {
            int start = -1;
            int end = rawScript.indexOf('}', scriptStart + len);
            String prop = rawScript.substring(scriptStart + len, end);

            start = rawScript.indexOf(',', end);
            end = rawScript.indexOf(')', start);

            String value = rawScript.substring(start + 1, end);

            start = rawScript.indexOf('}', start) + 1;
            end = rawScript.length();

            String replaceScript = String.format("print(\"${{setValue({},{})}}\".format(\"%s\",%s))", prop, value);

            rawScript = rawScript.substring(0, scriptStart) + replaceScript + rawScript.substring(start, end);

            scriptStart += replaceScript.length();
        }
        return rawScript;
    }


    protected void createJavaSourceFileIfNotExists(String sourceCode, String fileName) throws IOException {
        logger.info("tenantCode :{}, task dir:{}", taskRequest.getTenantCode(), taskRequest.getExecutePath());

        if (!Files.exists(Paths.get(fileName))) {
            logger.info("generate java source file:{}", fileName);

            StringBuilder sb = new StringBuilder();
            sb.append(sourceCode);
            logger.info(sb.toString());

            // write data to file
            FileUtils.writeStringToFile(new File(fileName),
                    sb.toString(),
                    StandardCharsets.UTF_8);
        }
    }


    protected String buildJavaSourceCodeFileFullName() {
        return String.format(JavaConstants.JAVA_SOURCE_CODE_NAME_TEMPLATE, taskRequest.getExecutePath(), taskRequest.getTaskAppId());
    }

    private String buildResourcePath() {
        StringBuilder builder = new StringBuilder();
        if (javaParameters.getJavaVersion() == JavaVersion.JAVA_8) {
            builder.append("-class-path");
        }else{
            builder.append("-module-path");
        }
        builder.append(" ").append(JavaConstants.CLASSPATH_CURRENT_DIR);
        for (ResourceInfo info : javaParameters.getResourceFilesList()) {
            builder.append(JavaConstants.PATH_SEPARATOR);
            builder.append(info.getResourceName());
        }
        return builder.toString();
    }

    protected String compilerRawScript(String sourceCode) throws IOException, InterruptedException {
        String fileName =  buildJavaSourceCodeFileFullName();
        createJavaSourceFileIfNotExists(sourceCode, fileName);
        String className = fileName.substring(0 ,fileName.lastIndexOf('.'));
        className = className.substring(className.lastIndexOf('.') + 1);
        StringBuilder compilerCommand = new StringBuilder().append("javac").append(" ")
                .append(className + ".java").append(" ")
                .append(buildResourcePath());
        shellCommandExecutor.run(compilerCommand.toString());
        return className;
    }


    private String buildJavaSourceContent(){
        String rawJavaScript = javaParameters.getRawScript().replaceAll("\\r\\n", "\n");
        // replace placeholder
        Map<String, Property> paramsMap = ParamUtils.convert(taskRequest, javaParameters);
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskRequest.getParamsMap())) {
            paramsMap.putAll(taskRequest.getParamsMap());
        }
        rawJavaScript = ParameterUtils.convertParameterPlaceholders(rawJavaScript, ParamUtils.convert(paramsMap));
        logger.info("raw java script : {}", javaParameters.getRawScript());
        return rawJavaScript;
    }

    private String buildJavaExecuteCommand(String args) {
        Preconditions.checkNotNull(args, "command's args cannot be null");
        String javaHome = null;
        switch (javaParameters.getJavaVersion()) {
            case JAVA_11:
                 javaHome = System.getProperty(JavaConstants.JAVA_HOME11);
                break;
            case JAVA_13:
                 javaHome = System.getProperty(JavaConstants.JAVA_HOME13);
                break;
            case JAVA_15:
                 javaHome = System.getProperty(JavaConstants.JAVA_HOME15);
                break;
            case JAVA_17:
                 javaHome = System.getProperty(JavaConstants.JAVA_HOME17);
                break;
            case JAVA_8:
            default:
                javaHome = System.getProperty(JavaConstants.JAVA_HOME8);
        }
        Preconditions.checkNotNull(javaHome, "java home cannot be null");
        return javaHome + System.getProperty("file.separator") + args;
    }

}
