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

package org.apache.dolphinscheduler.plugin.kubeflow;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KubeflowTask extends AbstractRemoteTask {

    private final TaskExecutionContext taskExecutionContext;
    protected KubeflowHelper kubeflowHelper;
    private KubeflowParameters kubeflowParameters;
    private Path clusterYAMLPath;

    private Path yamlPath;

    public KubeflowTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() throws TaskException {
        kubeflowParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), KubeflowParameters.class);
        log.info("Initialize Kubeflow task params {}", taskExecutionContext.getTaskParams());

        kubeflowParameters.setClusterYAML(taskExecutionContext.getK8sTaskExecutionContext().getConfigYaml());
        if (!kubeflowParameters.checkParameters()) {
            throw new TaskException("Kubeflow task params is not valid");
        }

        writeFiles();
        kubeflowHelper = new KubeflowHelper(clusterYAMLPath.toString());
    }

    @Override
    public void submitApplication() throws TaskException {
        String command = kubeflowHelper.buildSubmitCommand(yamlPath.toString());
        log.info("Kubeflow task submit command: \n{}", command);
        String message = runCommand(command);
        log.info("Kubeflow task submit result: \n{}", message);

        KubeflowHelper.ApplicationIds applicationIds = new KubeflowHelper.ApplicationIds();
        applicationIds.setAlreadySubmitted(true);
        setAppIds(JSONUtils.toJsonString(applicationIds));
    }

    /**
     * keep checking application status
     *
     * @throws TaskException
     */
    @Override
    public void trackApplicationStatus() throws TaskException {
        String command = kubeflowHelper.buildGetCommand(yamlPath.toString());
        log.info("Kubeflow task get command: \n{}", command);
        do {
            ThreadUtils.sleep(KubeflowHelper.CONSTANTS.TRACK_INTERVAL);
            String message = runCommand(command);
            String phase = kubeflowHelper.parseGetMessage(message);
            if (KubeflowHelper.STATUS.FAILED_SET.contains(phase)) {
                exitStatusCode = TaskConstants.EXIT_CODE_FAILURE;
                log.info("Kubeflow task get Failed result: \n{}", message);
                break;
            } else if (KubeflowHelper.STATUS.SUCCESS_SET.contains(phase)) {
                exitStatusCode = TaskConstants.EXIT_CODE_SUCCESS;
                log.info("Kubeflow task get Succeeded result: \n{}", message);
                break;
            }
        } while (true);

    }

    @Override
    public void cancelApplication() throws TaskException {
        String command = kubeflowHelper.buildDeleteCommand(yamlPath.toString());
        log.info("Kubeflow task delete command: \n{}", command);
        String message = runCommand(command);
        log.info("Kubeflow task delete result: \n{}", message);
        exitStatusCode = TaskConstants.EXIT_CODE_KILL;
    }

    protected String runCommand(String command) {
        try {
            exitStatusCode = TaskConstants.EXIT_CODE_SUCCESS;
            return OSUtils.exeShell(new String[]{"sh", "-c", command});
        } catch (Exception e) {
            exitStatusCode = TaskConstants.EXIT_CODE_FAILURE;
            throw new TaskException("Kubeflow task submit command failed", e);
        }
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    public void writeFiles() {
        String yamlContent = kubeflowParameters.getYamlContent();
        String clusterYAML = kubeflowParameters.getClusterYAML();

        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        yamlContent = ParameterUtils.convertParameterPlaceholders(yamlContent, ParameterUtils.convert(paramsMap));

        yamlPath = Paths.get(taskExecutionContext.getExecutePath(), KubeflowHelper.CONSTANTS.YAML_FILE_PATH);
        clusterYAMLPath =
                Paths.get(taskExecutionContext.getExecutePath(), KubeflowHelper.CONSTANTS.CLUSTER_CONFIG_PATH);

        log.info("Kubeflow task yaml content: \n{}", yamlContent);
        try {
            Files.write(yamlPath, yamlContent.getBytes(), StandardOpenOption.CREATE);
            Files.write(clusterYAMLPath, clusterYAML.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new TaskException("Kubeflow task write yaml file failed", e);
        }
    }

    @Override
    public KubeflowParameters getParameters() {
        return kubeflowParameters;
    }
}
