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

import static org.apache.dolphinscheduler.plugin.task.java.JavaConstants.JAVA_HOME_VAR;

import org.apache.dolphinscheduler.common.constants.Constants;
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
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.ShellInterceptorBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.java.exception.RunTypeNotFoundException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

@Slf4j
public class JavaTask extends AbstractTask {

    private JavaParameters javaParameters;

    private Path jshellScriptPath;

    private Path userScriptPath;

    private final ShellCommandExecutor shellCommandExecutor;

    public JavaTask(TaskExecutionContext taskRequest) {
        super(taskRequest);
        this.taskRequest = taskRequest;
        this.shellCommandExecutor = new ShellCommandExecutor(taskRequest);
    }

    /**
     * Initializes a Java task
     *
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
            // Step 2 case1: the fat jar run type builds the command directly, adding resource to the java -jar class
            // when
            // building the command
            // Step 2 case2: the normal jar run type builds the command directly, adding resource to the java -cp class
            // when
            // building the command
            // Step 3: to run the command
            String command = null;
            switch (javaParameters.getRunType()) {

                case JavaConstants.RUN_TYPE_FAT_JAR:
                    command = buildJarCommand();
                    break;
                case JavaConstants.RUN_TYPE_NORMAL_JAR:
                    command = buildNormalJarCommand();
                    break;
                case JavaConstants.RUN_TYPE_JSHELL:
                    command = buildJshellCommand();
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
            setTaskOutputParams(shellCommandExecutor.getTaskOutputParams());
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
        } finally {
            if (jshellScriptPath != null) {
                try {
                    Files.deleteIfExists(jshellScriptPath);
                } catch (IOException e) {
                    log.warn("Failed to delete temporary JShell script {}", jshellScriptPath, e);
                }
            }
            if (userScriptPath != null) {
                try {
                    Files.deleteIfExists(userScriptPath);
                } catch (IOException e) {
                    log.warn("Failed to delete temporary User script {}", userScriptPath, e);
                }
            }
        }
    }

    /**
     * Construct a shell command for the java -jar Run mode
     *
     * @return String
     **/
    protected String buildJarCommand() {
        ResourceContext resourceContext = taskRequest.getResourceContext();
        String mainJarAbsolutePathInLocal = resourceContext
                .getResourceItem(javaParameters.getMainJar().getResourceName())
                .getResourceAbsolutePathInLocal();
        StringBuilder builder = new StringBuilder();
        builder.append(getJavaCommandPath())
                .append(Constants.SPACE)
                .append(javaParameters.getJvmArgs().trim()).append(Constants.SPACE)
                .append(buildResourcePath()).append(Constants.SPACE)
                .append("-jar").append(Constants.SPACE)
                .append(mainJarAbsolutePathInLocal).append(Constants.SPACE)
                .append(javaParameters.getMainArgs().trim());
        return parseParameter(builder.toString());
    }

    /**
     * Construct a shell command for the java -cp run mode
     *
     * @return String
     **/
    protected String buildNormalJarCommand() {
        ResourceContext resourceContext = taskRequest.getResourceContext();
        String mainJarAbsolutePathInLocal = resourceContext.getResourceItem(
                javaParameters.getMainJar()
                        .getResourceName())
                .getResourceAbsolutePathInLocal();
        String mainClassName = javaParameters.getMainClass();
        String mainJarName;
        if (mainClassName == null || StringUtils.isEmpty(mainClassName)) {
            mainJarName = MainClassExtractor.getMainClassName(mainJarAbsolutePathInLocal);
        } else {
            mainJarName = mainClassName;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getJavaCommandPath())
                .append(Constants.SPACE)
                .append(javaParameters.getJvmArgs().trim()).append(Constants.SPACE)
                .append(buildResourcePath()).append(Constants.SPACE)
                .append(mainJarName).append(Constants.SPACE)
                .append(javaParameters.getMainArgs().trim());
        return parseParameter(builder.toString());
    }

    protected String buildJshellCommand() throws IOException {
        Path executePath = Paths.get(taskRequest.getExecutePath());

        userScriptPath = Files.createTempFile(executePath, "user-script-", ".jsh");
        jshellScriptPath = Files.createTempFile(executePath, "jshell-wrapper-", ".java");

        if (!userScriptPath.toFile().setReadable(true, false)) {
            throw new IOException("Failed to set world-readable permissions on " + userScriptPath);
        }
        if (!jshellScriptPath.toFile().setReadable(true, false)) {
            throw new IOException("Failed to set world-readable permissions on " + jshellScriptPath);
        }

        String source = parseParameter(javaParameters.getJshellScript());
        Files.write(userScriptPath, source.getBytes(StandardCharsets.UTF_8));

        String wrapperCode = generateJshellWrapper();
        Files.write(jshellScriptPath, wrapperCode.getBytes(StandardCharsets.UTF_8));

        StringBuilder builder = new StringBuilder(getJavaCommandPath());

        builder.append(Constants.SPACE)
                .append(buildResourcePath())
                .append(Constants.SPACE)
                .append(quoteShellArgument(jshellScriptPath.toAbsolutePath().toString()))
                .append(Constants.SPACE)
                .append(quoteShellArgument(userScriptPath.toAbsolutePath().toString()));

        String jvmArgs = StringUtils.trimToEmpty(javaParameters.getJvmArgs());
        if (!jvmArgs.isEmpty()) {
            builder.append(Constants.SPACE).append(jvmArgs);
        }

        return parseParameter(builder.toString());
    }

    /**
     * Generates a Java program that explicitly evaluates the JShell script using SourceCodeAnalysis,
     * configures the JShell execution environment, and propagates a failure exit code.
     */
    private String generateJshellWrapper() {
        String pathOption = javaParameters.isModulePath() ? "--module-path" : "--class-path";
        String resolvedPath = buildJshellResourcePath();

        return "import jdk.jshell.JShell;\n" +
                "import jdk.jshell.SnippetEvent;\n" +
                "import jdk.jshell.SourceCodeAnalysis;\n" +
                "import java.nio.file.Files;\n" +
                "import java.nio.file.Paths;\n" +
                "import java.util.List;\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.Arrays;\n\n" +
                "public class JShellExecutor {\n" +
                "    public static void main(String[] args) throws Exception {\n" +
                "        if (args.length < 1) {\n" +
                "            throw new IllegalArgumentException(\"Missing user script path\");\n" +
                "        }\n" +
                "        String source = new String(Files.readAllBytes(Paths.get(args[0])));\n" +
                "        \n" +
                "        List<String> remoteOptions = new ArrayList<>();\n" +
                "        remoteOptions.add(\"" + pathOption + "\");\n" +
                "        remoteOptions.add(\"" + resolvedPath.replace("\\", "\\\\") + "\");\n" +
                "        if (args.length > 1) {\n" +
                "            remoteOptions.addAll(Arrays.asList(args).subList(1, args.length));\n" +
                "        }\n" +
                "        \n" +
                "        try (JShell jshell = JShell.builder()\n" +
                "                .compilerOptions(\"" + pathOption + "\", \"" + resolvedPath.replace("\\", "\\\\")
                + "\")\n" +
                "                .remoteVMOptions(remoteOptions.toArray(new String[0]))\n" +
                "                .build()) {\n" +
                "            \n" +
                "            boolean hasError = false;\n" +
                "            SourceCodeAnalysis analyzer = jshell.sourceCodeAnalysis();\n" +
                "            \n" +
                "            while (!source.trim().isEmpty()) {\n" +
                "                SourceCodeAnalysis.CompletionInfo info = analyzer.analyzeCompletion(source);\n" +
                "                if (!info.completeness().isComplete()) {\n" +
                "                    System.err.println(\"Incomplete snippet detected: \" + info.source());\n" +
                "                    hasError = true;\n" +
                "                    break;\n" +
                "                }\n" +
                "                \n" +
                "                List<SnippetEvent> events = jshell.eval(info.source());\n" +
                "                for (SnippetEvent event : events) {\n" +
                "                    if (event.status() == jdk.jshell.Snippet.Status.REJECTED) {\n" +
                "                        System.err.println(\"Compilation failed: \" + event.snippet().source());\n" +
                "                        jshell.diagnostics(event.snippet()).forEach(diag -> \n" +
                "                            System.err.println(diag.getMessage(java.util.Locale.getDefault()))\n" +
                "                        );\n" +
                "                        hasError = true;\n" +
                "                    } else if (event.exception() != null) {\n" +
                "                        System.err.println(\"Exception during execution:\");\n" +
                "                        event.exception().printStackTrace();\n" +
                "                        hasError = true;\n" +
                "                    }\n" +
                "                }\n" +
                "                if (hasError) break;\n" +
                "                source = info.remaining();\n" +
                "            }\n" +
                "            if (hasError) {\n" +
                "                System.exit(1);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
    }

    private String getJshellCommandPath() {
        return "\"" + Paths.get(JAVA_HOME_VAR, "bin", "jshell") + "\"";
    }

    /**
     * Returns the unquoted raw classpath/module-path string to be injected
     * directly into the generated JShell wrapper environment.
     */
    protected String buildJshellResourcePath() {
        StringBuilder resourcePath = new StringBuilder(JavaConstants.CLASSPATH_CURRENT_DIR);

        resourcePath.append(JavaConstants.PATH_SEPARATOR)
                .append(taskRequest.getExecutePath());

        ResourceContext resourceContext = taskRequest.getResourceContext();
        if (resourceContext != null && javaParameters.getResourceFilesList() != null) {
            for (ResourceInfo info : javaParameters.getResourceFilesList()) {
                ResourceContext.ResourceItem resourceItem = resourceContext.getResourceItem(info.getResourceName());
                resourcePath.append(JavaConstants.PATH_SEPARATOR)
                        .append(resourceItem.getResourceAbsolutePathInLocal());
            }
        }
        return resourcePath.toString();
    }

    private String quoteShellArgument(String argument) {
        return "'" + argument.replace("'", "'\\''") + "'";
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
     * Construct a Classpath or module path based on isModulePath
     *
     * @return String
     **/
    protected String buildResourcePath() {
        StringBuilder builder = new StringBuilder();
        if (javaParameters.isModulePath()) {
            builder.append("--module-path");
        } else {
            builder.append("-classpath");
        }
        builder.append(" ")
                .append(JavaConstants.CLASSPATH_CURRENT_DIR)
                .append(JavaConstants.PATH_SEPARATOR)
                .append(taskRequest.getExecutePath());
        ResourceContext resourceContext = taskRequest.getResourceContext();
        for (ResourceInfo info : javaParameters.getResourceFilesList()) {
            builder.append(JavaConstants.PATH_SEPARATOR);
            builder
                    .append(resourceContext.getResourceItem(info.getResourceName()).getResourceAbsolutePathInLocal());
        }
        return builder.toString();
    }

    /**
     * Gets the operating system absolute path to the Java command
     *
     * @return String
     **/
    private String getJavaCommandPath() {
        return Paths.get(JAVA_HOME_VAR, "bin", "java").toString();
    }

    private String parseParameter(String script) {
        Map<String, Property> paramsMap = taskRequest.getPrepareParamsMap();
        return ParameterUtils.convertParameterPlaceholders(script, ParameterUtils.convert(paramsMap));
    }
}
