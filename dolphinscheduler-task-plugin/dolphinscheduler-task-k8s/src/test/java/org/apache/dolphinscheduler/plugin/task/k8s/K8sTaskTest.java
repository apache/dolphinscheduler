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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class K8sTaskTest {
    private K8sTaskParameters k8sTaskParameters = null;

    private K8sTask k8sTask = null;
    private final String image = "ds-dev";

    private final String namespace = "{\"name\":\"default\",\"cluster\":\"lab\"}";

    private final double minCpuCores = 2;

    private final double minMemorySpace = 10;
    private final int taskInstanceId = 1000;
    private final String taskName = "k8s_task_test";

    private final String DAY = "day";
    private final String date = "20220507";
    @Before
    public void before() {
        k8sTaskParameters = new K8sTaskParameters();
        k8sTaskParameters.setImage(image);
        k8sTaskParameters.setNamespace(namespace);
        k8sTaskParameters.setMinCpuCores(minCpuCores);
        k8sTaskParameters.setMinMemorySpace(minMemorySpace);
        TaskExecutionContext taskRequest = new TaskExecutionContext();
        taskRequest.setTaskInstanceId(taskInstanceId);
        taskRequest.setTaskName(taskName);
        taskRequest.setTaskParams(JSONUtils.toJsonString(k8sTaskParameters));
        Property property = new Property();
        property.setProp(DAY);
        property.setDirect(Direct.IN);
        property.setType(DataType.VARCHAR);
        property.setValue(date);
        Map<String, Property> paramsMap = new HashMap<>();
        paramsMap.put(DAY,property);
        taskRequest.setParamsMap(paramsMap);
        k8sTask = new K8sTask(taskRequest);
    }

    @Test
    public void testBuildCommandNormal() {
        String expectedStr = "{\"image\":\"ds-dev\",\"namespaceName\":\"default\",\"clusterName\":\"lab\",\"minCpuCores\":2.0,\"minMemorySpace\":10.0,\"paramsMap\":{\"day\":\"20220507\"}}";
        String commandStr = k8sTask.buildCommand();
        Assert.assertEquals(expectedStr, commandStr);
    }

    @Test
    public void testGetParametersNormal() {
        String expectedStr = "K8sTaskParameters{image='ds-dev', namespace='{\"name\":\"default\",\"cluster\":\"lab\"}', minCpuCores=2.0, minMemorySpace=10.0}";
        String result = k8sTask.getParameters().toString();
        Assert.assertEquals(expectedStr, result);
    }

}
