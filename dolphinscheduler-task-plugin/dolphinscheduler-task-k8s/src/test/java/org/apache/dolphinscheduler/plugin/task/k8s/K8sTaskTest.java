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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Label;
import org.apache.dolphinscheduler.plugin.task.api.model.NodeSelectorExpression;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.K8sTaskParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.NodeSelectorRequirement;

public class K8sTaskTest {

    private K8sTaskParameters k8sTaskParameters = null;

    private K8sTask k8sTask = null;
    private final String image = "ds-dev";
    private final String imagePullPolicy = "IfNotPresent";

    private final String namespace = "{\"name\":\"default\",\"cluster\":\"lab\"}";

    private final double minCpuCores = 2;

    private final double minMemorySpace = 10;
    private final int taskInstanceId = 1000;
    private final String taskName = "k8s_task_test";

    private final String DAY = "day";
    private final String date = "20220507";
    private final String command = "[\"/bin/bash\", \"-c\"]";
    private final String args = "[\"echo hello world\"]";
    private final List<Label> labels = Arrays.asList(new Label("test", "1234"));
    private final List<NodeSelectorExpression> nodeSelectorExpressions =
            Arrays.asList(new NodeSelectorExpression("node-label", "In", "1234,12345"));

    @BeforeEach
    public void before() {
        k8sTaskParameters = new K8sTaskParameters();
        k8sTaskParameters.setImage(image);
        k8sTaskParameters.setImagePullPolicy(imagePullPolicy);
        k8sTaskParameters.setNamespace(namespace);
        k8sTaskParameters.setMinCpuCores(minCpuCores);
        k8sTaskParameters.setMinMemorySpace(minMemorySpace);
        k8sTaskParameters.setCommand(command);
        k8sTaskParameters.setArgs(args);
        k8sTaskParameters.setCustomizedLabels(labels);
        k8sTaskParameters.setNodeSelectors(nodeSelectorExpressions);
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
        paramsMap.put(DAY, property);
        taskRequest.setParamsMap(paramsMap);

        Map<String, Property> prepareParamsMap = new HashMap<>();
        Property property1 = new Property();
        property1.setProp("day");
        property1.setValue("20220507");
        prepareParamsMap.put("day", property1);
        taskRequest.setPrepareParamsMap(prepareParamsMap);
        k8sTask = new K8sTask(taskRequest);
    }

    @Test
    public void testBuildCommandNormal() {
        String expectedStr =
                "{\"image\":\"ds-dev\",\"command\":\"[\\\"/bin/bash\\\", \\\"-c\\\"]\",\"args\":\"[\\\"echo hello world\\\"]\",\"namespaceName\":\"default\",\"clusterName\":\"lab\",\"imagePullPolicy\":\"IfNotPresent\",\"minCpuCores\":2.0,\"minMemorySpace\":10.0,\"paramsMap\":{\"day\":\"20220507\"},\"labelMap\":{\"test\":\"1234\"},\"nodeSelectorRequirements\":[{\"key\":\"node-label\",\"operator\":\"In\",\"values\":[\"1234\",\"12345\"]}]}";
        String commandStr = k8sTask.buildCommand();
        Assertions.assertEquals(expectedStr, commandStr);
    }

    @Test
    public void testGetParametersNormal() {
        String expectedStr =
                "K8sTaskParameters(image=ds-dev, namespace={\"name\":\"default\",\"cluster\":\"lab\"}, command=[\"/bin/bash\", \"-c\"], args=[\"echo hello world\"], imagePullPolicy=IfNotPresent, minCpuCores=2.0, minMemorySpace=10.0, customizedLabels=[Label(label=test, value=1234)], nodeSelectors=[NodeSelectorExpression(key=node-label, operator=In, values=1234,12345)])";
        String result = k8sTask.getParameters().toString();
        Assertions.assertEquals(expectedStr, result);
    }

    @Test
    public void testConvertToNodeSelectorRequirements() {
        NodeSelectorExpression expression = new NodeSelectorExpression();
        expression.setKey("key");
        expression.setOperator("In");
        expression.setValues("123, 1234");
        List<NodeSelectorRequirement> nodeSelectorRequirements =
                k8sTask.convertToNodeSelectorRequirements(Arrays.asList(expression));
        Assertions.assertEquals(1, nodeSelectorRequirements.size());
        List<String> expectedList = new ArrayList<>();
        expectedList.add("123");
        expectedList.add("1234");
        Assertions.assertEquals(expectedList, nodeSelectorRequirements.get(0).getValues());
    }

}
