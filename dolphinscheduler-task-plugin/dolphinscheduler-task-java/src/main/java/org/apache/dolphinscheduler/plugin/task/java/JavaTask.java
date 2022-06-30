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

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
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
import org.apache.dolphinscheduler.plugin.task.java.exception.JavaSourceFileExistException;
import org.apache.dolphinscheduler.plugin.task.java.exception.PublicClassNotFoundException;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        if (javaParameters.getRunType().equals(JavaConstants.RUN_TYPE_JAR)) {
            setMainJarName();
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
            // Step 1 judge if is java or jar run type.
            // The  jar run type builds the command directly, adding resource to the java -jar class when building the command
            // The java run type, first replace the custom parameters, then compile the code, and then build the command will add resource
            // To run the coma
            String command = null;
            switch (javaParameters.getRunType()) {
                case JavaConstants.RUN_TYPE_JAVA:
                    command = buildJavaCommand();
                    break;
                case JavaConstants.RUN_TYPE_JAR:
                    command = buildJarCommand();
                    break;
            }
            Preconditions.checkNotNull(command, "command not be null.");
            TaskResponse taskResponse = shellCommandExecutor.run(command);
            logger.info("java task run result : " + taskResponse);
            setExitStatusCode(taskResponse.getExitStatusCode());
            setAppIds(taskResponse.getAppIds());
            setProcessId(taskResponse.getProcessId());
            setVarPool(shellCommandExecutor.getVarPool());
        } catch (InterruptedException e) {
            logger.error("java task interrupted ", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("java task failed ", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("run java task error", e);
        }
    }

    protected String buildJavaCommand() throws Exception {
        String sourceCode = buildJavaSourceContent();
        String className = compilerRawScript(sourceCode);
        StringBuilder builder = new StringBuilder();
        builder.append(getJavaHomeBinAbsolutePath())
                .append("java").append(" ")
                .append(buildResourcePath())
                .append(" ")
                .append(className).append(" ")
                .append(javaParameters.getMainArgs().trim()).append(" ")
                .append(javaParameters.getJvmArgs().trim());
        return builder.toString();
    }

    private void setMainJarName() {
        ResourceInfo mainJar = javaParameters.getMainJar();
        String resourceName = getResourceNameOfMainJar(mainJar);
        mainJar.setRes(resourceName);
        javaParameters.setMainJar(mainJar);
    }

    private String getResourceNameOfMainJar(ResourceInfo mainJar) {
        if (null == mainJar) {
            throw new RuntimeException("The jar for the task is required.");
        }

        return mainJar.getId() == 0
                ? mainJar.getRes()
                // when update resource maybe has error
                : mainJar.getResourceName().replaceFirst("/", "");
    }

    protected String buildJarCommand() {
        String fullName = javaParameters.getMainJar().getResourceName();
        String mainJarName = fullName.substring(0, fullName.lastIndexOf('.'));
        mainJarName = mainJarName.substring(mainJarName.lastIndexOf('.') + 1) + ".jar";
        StringBuilder builder = new StringBuilder();
        builder.append(getJavaHomeBinAbsolutePath())
                .append("java").append(" ")
                .append(buildResourcePath()).append(" ")
                .append("-jar").append(" ")
                .append(taskRequest.getExecutePath())
                .append(mainJarName).append(" ")
                .append(javaParameters.getMainArgs().trim()).append(" ")
                .append(javaParameters.getJvmArgs().trim());
        System.out.println(builder.toString());
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
        } else {
            throw new JavaSourceFileExistException("java source file exists, please report an issue on official.");
        }
    }


    protected String buildJavaSourceCodeFileFullName(String publicClassName) {
        return String.format(JavaConstants.JAVA_SOURCE_CODE_NAME_TEMPLATE, taskRequest.getExecutePath(), publicClassName);
    }

    protected String buildResourcePath() {
        StringBuilder builder = new StringBuilder();
        if (javaParameters.isModulePath()) {
            builder.append("--module-path");
        }else{
            builder.append("--class-path");
        }
        builder.append(" ").append(JavaConstants.CLASSPATH_CURRENT_DIR)
                .append(JavaConstants.PATH_SEPARATOR)
                .append(taskRequest.getExecutePath());
        for (ResourceInfo info : javaParameters.getResourceFilesList()) {
            builder.append(JavaConstants.PATH_SEPARATOR);
            builder.append(taskRequest.getExecutePath())
                    .append(info.getResourceName());
        }
        return builder.toString();
    }

    protected String compilerRawScript(String sourceCode) throws IOException, InterruptedException {
        String publicClassName = getPublicClassName(sourceCode);
        String fileName =  buildJavaSourceCodeFileFullName(publicClassName);
        createJavaSourceFileIfNotExists(sourceCode, fileName);
        String compileCommand = buildJavaCompileCommand(fileName, sourceCode);
        Preconditions.checkNotNull(compileCommand, "command not be null.");
        TaskResponse compileResponse = shellCommandExecutor.run(compileCommand);
        // must drop the command file ,if do not ,the next command will not run. because be limited the ShellCommandExecutor's create file rules
        dropShellCommandFile();
        shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskRequest,
                logger);
        logger.info("java task code compile result : " + compileResponse);
        return publicClassName;
    }

    private void dropShellCommandFile() throws IOException {
        String commandFilePath = String.format("%s/%s.%s"
                , taskRequest.getExecutePath()
                , taskRequest.getTaskAppId()
                , SystemUtils.IS_OS_WINDOWS ? "bat" : "command");
        Path path = Paths.get(commandFilePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    protected String buildJavaCompileCommand(String fileName, String sourceCode) throws IOException {

        StringBuilder compilerCommand = new StringBuilder()
                .append(getJavaHomeBinAbsolutePath())
                .append("javac").append(" ")
                .append(buildResourcePath()).append(" ")
                .append(fileName);
        return compilerCommand.toString();
    }


    protected String buildJavaSourceContent(){
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



    private String getJavaHomeBinAbsolutePath() {
        String javaHomeAbsolutePath = System.getenv(JavaConstants.JAVA_HOME);
        Preconditions.checkNotNull(javaHomeAbsolutePath, "not find the java home in the version. ");
        return javaHomeAbsolutePath + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator");
    }

    public String getPublicClassName(String sourceCode) {
        String pattern = "(.*\\s+public\\s+class\\s+)([a-zA-Z_]+[//w_]*)([.\\s\\S]*)";
        Pattern compile = Pattern.compile(pattern);
        Matcher matcher = compile.matcher(sourceCode);
        if (!matcher.find()) {
            throw new PublicClassNotFoundException("public class is not be found in sourcecode : " + sourceCode);
        }
        return matcher.group(2).trim();
    }

}
