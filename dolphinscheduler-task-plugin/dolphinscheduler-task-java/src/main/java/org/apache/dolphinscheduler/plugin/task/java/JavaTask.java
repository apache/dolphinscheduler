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
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.ShellInterceptorBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.java.exception.RunTypeNotFoundException;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

@Slf4j
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

    public JavaTask(TaskExecutionContext taskRequest) {
        super(taskRequest);
        this.taskRequest = taskRequest;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle, taskRequest);
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
                .append("java").append(Constants.SPACE)
                .append(buildResourcePath()).append(Constants.SPACE)
                .append("-jar").append(Constants.SPACE)
                .append(mainJarAbsolutePathInLocal).append(Constants.SPACE)
                .append(javaParameters.getMainArgs().trim()).append(Constants.SPACE)
                .append(javaParameters.getJvmArgs().trim());
        return builder.toString();
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
                .append("java").append(Constants.SPACE)
                .append(buildResourcePath()).append(Constants.SPACE)
                .append(mainJarName).append(Constants.SPACE)
                .append(javaParameters.getMainArgs().trim()).append(Constants.SPACE)
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
        return JAVA_HOME_VAR + File.separator + "bin" + File.separator;
    }

}
