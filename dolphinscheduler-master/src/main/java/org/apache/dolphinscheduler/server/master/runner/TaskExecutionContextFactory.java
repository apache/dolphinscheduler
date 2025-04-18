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

package org.apache.dolphinscheduler.server.master.runner;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.CLUSTER;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.NAMESPACE_NAME;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.IEnvironmentDao;
import org.apache.dolphinscheduler.dao.utils.EnvironmentUtils;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.K8sTaskParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.AbstractResourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionContextCreateRequest;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskExecutionContextFactory {

    @Autowired
    private ProcessService processService;

    @Autowired
    private CuringParamsService curingParamsService;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private IEnvironmentDao environmentDao;

    public TaskExecutionContext createTaskExecutionContext(TaskExecutionContextCreateRequest request) {
        final TaskInstance taskInstance = request.getTaskInstance();
        final WorkflowInstance workflowInstance = request.getWorkflowInstance();
        final WorkflowDefinition workflowDefinition = request.getWorkflowDefinition();
        final Project project = request.getProject();

        return TaskExecutionContextBuilder.get()
                .buildWorkflowInstanceHost(masterConfig.getMasterAddress())
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildEnvironmentConfig(getEnvironmentConfigFromDB(taskInstance).orElse(null))
                .buildTaskDefinitionRelatedInfo(request.getTaskDefinition())
                .buildProcessInstanceRelatedInfo(request.getWorkflowInstance())
                .buildResourceParameters(getResourceParameters(taskInstance))
                .buildBusinessParams(getBusinessParams(workflowInstance))
                .buildPrepareParams(getPrepareParams(taskInstance, workflowInstance, workflowDefinition, project))
                .buildK8sTaskRelatedInfo(getK8sTaskExecutionContext(taskInstance))
                .create();
    }

    private ResourceParametersHelper getResourceParameters(final TaskInstance taskInstance) {
        final ResourceParametersHelper resourceParameters = TaskPluginManager.getTaskChannel(taskInstance.getTaskType())
                .parseParameters(taskInstance.getTaskParams())
                .getResources();
        if (resourceParameters != null) {
            // todo: add DataSourceParametersAssembler to assemble DataSourceParameters
            resourceParameters.getResourceMap().forEach((type, map) -> {
                switch (type) {
                    case DATASOURCE:
                        assembleDataSourceParameters(map);
                        break;
                    default:
                        break;
                }
            });
        }
        return resourceParameters;
    }

    private void assembleDataSourceParameters(Map<Integer, AbstractResourceParameters> map) {
        if (MapUtils.isEmpty(map)) {
            return;
        }

        map.forEach((code, parameters) -> {
            DataSource datasource = processService.findDataSourceById(code);
            if (Objects.isNull(datasource)) {
                return;
            }
            DataSourceParameters dataSourceParameters = new DataSourceParameters();
            dataSourceParameters.setType(datasource.getType());
            dataSourceParameters.setConnectionParams(datasource.getConnectionParams());
            map.put(code, dataSourceParameters);
        });
    }

    private K8sTaskExecutionContext getK8sTaskExecutionContext(final TaskInstance taskInstance) {
        K8sTaskExecutionContext k8sTaskExecutionContext = null;
        String namespace = "";
        switch (taskInstance.getTaskType()) {
            case "K8S":
            case "KUBEFLOW":
                K8sTaskParameters k8sTaskParameters =
                        JSONUtils.parseObject(taskInstance.getTaskParams(), K8sTaskParameters.class);
                namespace = k8sTaskParameters.getNamespace();
                break;
            default:
                break;
        }

        if (StringUtils.isNotEmpty(namespace)) {
            String clusterName = JSONUtils.toMap(namespace).get(CLUSTER);
            String configYaml = processService.findConfigYamlByName(clusterName);
            if (configYaml != null) {
                k8sTaskExecutionContext =
                        new K8sTaskExecutionContext(configYaml, JSONUtils.toMap(namespace).get(NAMESPACE_NAME));
            }
        }
        return k8sTaskExecutionContext;
    }

    private Map<String, Property> getBusinessParams(final WorkflowInstance workflowInstance) {
        return curingParamsService.preBuildBusinessParams(workflowInstance);
    }

    private Map<String, Property> getPrepareParams(final TaskInstance taskInstance,
                                                   final WorkflowInstance workflowInstance,
                                                   final WorkflowDefinition workflowDefinition,
                                                   final Project project) {
        final AbstractParameters baseParam = TaskPluginManager.parseTaskParameters(
                taskInstance.getTaskType(),
                taskInstance.getTaskParams());

        return curingParamsService.paramParsingPreparation(
                taskInstance,
                baseParam,
                workflowInstance,
                project.getName(),
                workflowDefinition.getName());
    }

    private Optional<String> getEnvironmentConfigFromDB(final TaskInstance taskInstance) {
        if (EnvironmentUtils.isEnvironmentCodeEmpty(taskInstance.getEnvironmentCode())) {
            return Optional.empty();
        }
        final Optional<Environment> environmentOptional =
                environmentDao.queryByEnvironmentCode(taskInstance.getEnvironmentCode());
        if (!environmentOptional.isPresent()) {
            throw new IllegalArgumentException("Cannot find the environment: " + taskInstance.getEnvironmentCode());
        }
        return Optional.ofNullable(environmentOptional.get().getConfig());
    }

}
