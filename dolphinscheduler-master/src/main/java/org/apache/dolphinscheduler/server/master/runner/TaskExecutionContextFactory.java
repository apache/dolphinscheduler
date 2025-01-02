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
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
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

    public TaskExecutionContext createTaskExecutionContext(TaskExecutionContextCreateRequest request) {
        TaskInstance taskInstance = request.getTaskInstance();
        WorkflowInstance workflowInstance = request.getWorkflowInstance();
        WorkflowDefinition workflowDefinition = request.getWorkflowDefinition();
        Project project = request.getProject();

        ResourceParametersHelper resources = TaskPluginManager.getTaskChannel(taskInstance.getTaskType())
                .parseParameters(taskInstance.getTaskParams())
                .getResources();
        setTaskResourceInfo(resources);

        Map<String, Property> businessParamsMap = curingParamsService.preBuildBusinessParams(workflowInstance);

        AbstractParameters baseParam =
                TaskPluginManager.parseTaskParameters(taskInstance.getTaskType(), taskInstance.getTaskParams());

        Map<String, Property> propertyMap =
                curingParamsService.paramParsingPreparation(taskInstance, baseParam, workflowInstance,
                        project.getName(), workflowDefinition.getName());
        TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                .buildWorkflowInstanceHost(masterConfig.getMasterAddress())
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildTaskDefinitionRelatedInfo(request.getTaskDefinition())
                .buildProcessInstanceRelatedInfo(request.getWorkflowInstance())
                .buildResourceParametersInfo(resources)
                .buildBusinessParamsMap(businessParamsMap)
                .buildParamInfo(propertyMap)
                .create();

        setK8sTaskRelatedInfo(taskExecutionContext, taskInstance);
        return taskExecutionContext;
    }

    public void setK8sTaskRelatedInfo(TaskExecutionContext taskExecutionContext, TaskInstance taskInstance) {
        K8sTaskExecutionContext k8sTaskExecutionContext = setK8sTaskRelation(taskInstance);
        taskExecutionContext.setK8sTaskExecutionContext(k8sTaskExecutionContext);
    }

    private void setTaskResourceInfo(ResourceParametersHelper resourceParametersHelper) {
        if (Objects.isNull(resourceParametersHelper)) {
            return;
        }
        resourceParametersHelper.getResourceMap().forEach((type, map) -> {
            switch (type) {
                case DATASOURCE:
                    setTaskDataSourceResourceInfo(map);
                    break;
                default:
                    break;
            }
        });
    }

    private void setTaskDataSourceResourceInfo(Map<Integer, AbstractResourceParameters> map) {
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

    private K8sTaskExecutionContext setK8sTaskRelation(TaskInstance taskInstance) {
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

}
