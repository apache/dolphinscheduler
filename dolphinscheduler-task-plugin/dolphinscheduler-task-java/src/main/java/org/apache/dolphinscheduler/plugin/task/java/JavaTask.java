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

package org.apache.dolphinscheduler.plugin.task.java;

import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.JAVA_HOME_VAR;
import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.PUBLIC_CLASS_NAME_REGEX;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.ShellInterceptorBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.java.exception.JavaSourceFileExistException;
import org.apache.dolphinscheduler.plugin.task.java.exception.PublicClassNotFoundException;
import org.apache.dolphinscheduler.plugin.task.java.exception.RunTypeNotFoundException;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

public class JavaTask extends AbstractTask {

    /**
     * Contains various parameters for this task
     */
    private JavaParameters javaParameters;

    /**
     * To run shell commands
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * task execution context
     */
    private TaskExecutionContext taskRequest;

    /**
     * class name regex pattern
     */
    private static final Pattern classNamePattern = Pattern.compile(PUBLIC_CLASS_NAME_REGEX);

    public JavaTask(TaskExecutionContext taskRequest) {
        super(taskRequest);
        this.taskRequest = taskRequest;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskRequest,
                log);
    }

    /**
     * Initializes a Java task
     * @return void
     **/
    @Override
    public void init() {
        javaParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), JavaParameters.class);
        if (javaParameters == null || !javaParameters.checkParameters()) {
            throw new TaskException("java task params is not valid");
        }
        log.info("Initialize java task params {}", JSONUtils.toPrettyJsonString(javaParameters));
    }

    /**
     * Execute Java tasks
     *
     * @return void
     * @throws Exception
     */
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            // Step 1: judge if is java or jar run type.
            // Step 2 case1: the jar run type builds the command directly, adding resource to the java -jar class when
            // building the command
            // Step 2 case2: the java run type, first replace the custom parameters, then compile the code, and then
            // build the command will add resource
            // Step 3: to run the command
            String command = null;
            switch (javaParameters.getRunType()) {
                case JavaConstants.RUN_TYPE_JAVA:
                    command = buildJavaCommand();
                    break;
                case JavaConstants.RUN_TYPE_JAR:
                    command = buildJarCommand();
                    break;
                default:
                    throw new RunTypeNotFoundException("run type is required, but it is null now.");
            }
            Preconditions.checkNotNull(command, "command not be null.");
            IShellInterceptorBuilder<?, ?> shellActuatorBuilder = ShellInterceptorBuilderFactory.newBuilder()
                    .appendScript(command);
            TaskResponse taskResponse = shellCommandExecutor.run(shellActuatorBuilder, taskCallBack);
            log.info("java task run result: {}", taskResponse);
            setExitStatusCode(taskResponse.getExitStatusCode());
            setAppIds(taskResponse.getAppIds());
            setProcessId(taskResponse.getProcessId());
            setVarPool(shellCommandExecutor.getVarPool());
        } catch (InterruptedException e) {
            log.error("java task interrupted ", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            Thread.currentThread().interrupt();
        } catch (RunTypeNotFoundException e) {
            log.error(e.getMessage());
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw e;
        } catch (Exception e) {
            log.error("java task failed ", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("run java task error", e);
        }
    }

    /**
     * Construct a shell command for the java Run mode
     *
     * @return String
     * @throws Exception
     **/
    protected String buildJavaCommand() throws Exception {
        StringBuilder builder = new StringBuilder();
        String sourceCode = buildJavaSourceContent();
        builder.append(buildJavaCompileCommand(sourceCode))
                .append(";")
                .append(getJavaCommandPath())
                .append("java").append(" ")
                .append(buildResourcePath())
                .append(" ")
                .append(getPublicClassName(sourceCode))
                .append(" ")
                .append(javaParameters.getMainArgs().trim()).append(" ")
                .append(javaParameters.getJvmArgs().trim());
        return builder.toString();
    }

    /**
     * Construct a shell command for the java -jar Run mode
     *
     * @return String
     **/
    protected String buildJarCommand() {
        String mainJarName = taskRequest.getResources().get(javaParameters.getMainJar().getResourceName());
        StringBuilder builder = new StringBuilder();
        builder.append(getJavaCommandPath())
                .append("java").append(" ")
                .append(buildResourcePath()).append(" ")
                .append("-jar").append(" ")
                .append(taskRequest.getExecutePath()).append(FOLDER_SEPARATOR)
                .append(mainJarName).append(" ")
                .append(javaParameters.getMainArgs().trim()).append(" ")
                .append(javaParameters.getJvmArgs().trim());
        return builder.toString();
    }

    @Override
    public void cancel() throws TaskException {
        // cancel process
        try {
            shellCommandExecutor.cancelApplication();
        } catch (Exception e) {
            throw new TaskException();
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return javaParameters;
    }

    /**
     * Replaces placeholders such as local variables in source files
     *
     * @param rawScript
     * @return String
     * @throws StringIndexOutOfBoundsException
     */
    protected static String convertJavaSourceCodePlaceholders(String rawScript) throws StringIndexOutOfBoundsException {
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

    /**
     * Creates a Java source file when it does not exist
     *
     * @param sourceCode
     * @param fileName
     * @return String
     **/
    protected void createJavaSourceFileIfNotExists(String sourceCode, String fileName) throws IOException {
        log.info("tenantCode: {}, task dir:{}", taskRequest.getTenantCode(), taskRequest.getExecutePath());
        if (!Files.exists(Paths.get(fileName))) {
            log.info("the java source code:{}, will be write to the file: {}", fileName, sourceCode);
            // write data to file
            FileUtils.writeStringToFile(new File(fileName),
                    sourceCode,
                    StandardCharsets.UTF_8);
        } else {
            throw new JavaSourceFileExistException("java source file exists, please report an issue on official.");
        }
    }

    /**
     * Construct the full path name of the Java source file from the temporary execution path of the task
     *
     * @return String
     **/
    protected String buildJavaSourceCodeFileFullName(String publicClassName) {
        return String.format(JavaConstants.JAVA_SOURCE_CODE_NAME_TEMPLATE, taskRequest.getExecutePath(),
                publicClassName);
    }

    /**
     * Construct a Classpath or module path based on isModulePath
     *
     * @return String
     **/
    protected String buildResourcePath() {
        StringBuilder builder = new StringBuilder();
        if (javaParameters.isModulePath()) {
            builder.append("--module-path");
        } else {
            builder.append("--class-path");
        }
        builder.append(" ").append(JavaConstants.CLASSPATH_CURRENT_DIR)
                .append(JavaConstants.PATH_SEPARATOR)
                .append(taskRequest.getExecutePath());
        Map<String, String> resourceMap = taskRequest.getResources();
        for (ResourceInfo info : javaParameters.getResourceFilesList()) {
            builder.append(JavaConstants.PATH_SEPARATOR);
            builder.append(taskRequest.getExecutePath()).append(FOLDER_SEPARATOR)
                    .append(resourceMap.get(info.getResourceName()));
        }
        return builder.toString();
    }

    /**
     * Constructs a shell command compiled from a Java source file
     *
     * @param sourceCode
     * @return String
     * @throws IOException
     **/
    protected String buildJavaCompileCommand(String sourceCode) throws IOException {
        String publicClassName = getPublicClassName(sourceCode);
        String fileName = buildJavaSourceCodeFileFullName(publicClassName);
        createJavaSourceFileIfNotExists(sourceCode, fileName);

        StringBuilder compilerCommand = new StringBuilder()
                .append(getJavaCommandPath())
                .append("javac").append(" ")
                .append(buildResourcePath()).append(" ")
                .append(fileName);
        return compilerCommand.toString();
    }

    /**
     * Work with Java source file content, such as replacing local variables
     *
     * @return String
     **/
    protected String buildJavaSourceContent() {
        String rawJavaScript = javaParameters.getRawScript().replaceAll("\\r\\n", "\n");
        // replace placeholder

        Map<String, Property> paramsMap = taskRequest.getPrepareParamsMap();
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskRequest.getParamsMap())) {
            paramsMap.putAll(taskRequest.getParamsMap());
        }
        log.info("The current java source code will begin to replace the placeholder: {}", rawJavaScript);
        rawJavaScript = ParameterUtils.convertParameterPlaceholders(rawJavaScript, ParameterUtils.convert(paramsMap));
        return rawJavaScript;
    }

    /**
     * Gets the operating system absolute path to the Java command
     *
     * @return String
     **/
    private String getJavaCommandPath() {
        return JAVA_HOME_VAR + File.separator + "bin" + File.separator;
    }

    /**
     * Gets the public class name from the Java source file
     *
     * @param sourceCode
     * @return String
     **/
    public String getPublicClassName(String sourceCode) {
        Matcher matcher = classNamePattern.matcher(sourceCode);
        if (!matcher.find()) {
            throw new PublicClassNotFoundException("public class is not be found in source code : " + sourceCode);
        }
        return matcher.group(2).trim();
    }
}
