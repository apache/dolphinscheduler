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

package org.apache.dolphinscheduler.plugin.task.k8s;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.CLUSTER;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.NAMESPACE_NAME;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.k8s.AbstractK8sTask;
import org.apache.dolphinscheduler.plugin.task.api.k8s.K8sTaskMainParameters;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

public class K8sTask extends AbstractK8sTask {

    /**
     * taskExecutionContext
     */
    private final TaskExecutionContext taskExecutionContext;

    /**
     * task parameters
     */
    private final K8sTaskParameters k8sTaskParameters;

    /**
     * @param taskRequest taskRequest
     */
    public K8sTask(TaskExecutionContext taskRequest) {
        super(taskRequest);
        this.taskExecutionContext = taskRequest;
        this.k8sTaskParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), K8sTaskParameters.class);
        if (!k8sTaskParameters.checkParameters()) {
            throw new TaskException("K8S task params is not valid");
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return k8sTaskParameters;
    }

    @Override
    protected String buildCommand() {
        K8sTaskMainParameters k8sTaskMainParameters = new K8sTaskMainParameters();
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext,getParameters());
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            paramsMap.putAll(taskExecutionContext.getParamsMap());
        }
        Map<String,String> namespace = JSONUtils.toMap(k8sTaskParameters.getNamespace());
        String namespaceName = namespace.get(NAMESPACE_NAME);
        String clusterName = namespace.get(CLUSTER);
        k8sTaskMainParameters.setImage(k8sTaskParameters.getImage());
        k8sTaskMainParameters.setNamespaceName(namespaceName);
        k8sTaskMainParameters.setClusterName(clusterName);
        k8sTaskMainParameters.setMinCpuCores(k8sTaskParameters.getMinCpuCores());
        k8sTaskMainParameters.setMinMemorySpace(k8sTaskParameters.getMinMemorySpace());
        k8sTaskMainParameters.setParamsMap(ParamUtils.convert(paramsMap));
        return JSONUtils.toJsonString(k8sTaskMainParameters);
    }

}
