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

import static org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils.VAR_DELIMITER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.k8s.param.K8sConnectionParam;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Label;
import org.apache.dolphinscheduler.plugin.task.api.model.NodeSelectorExpression;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.K8sTaskParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import io.fabric8.kubernetes.api.model.NodeSelectorRequirement;

public class K8sTaskTest {

    private K8sTaskParameters k8sTaskParameters = null;

    private K8sTask k8sTask = null;
    private final String image = "ds-dev";
    private final String imagePullPolicy = "IfNotPresent";

    private final String pullSecret = "ds-secret";

    private final String namespace = "namespace";

    private final double minCpuCores = 2;

    private final double minMemorySpace = 10;
    private final int taskInstanceId = 1000;
    private final String taskName = "k8s_task_test";

    private final String DAY = "day";
    private final String date = "20220507";
    private final String command = "[\"/bin/bash\", \"-c\"]";
    private final String args = "[\"echo hello world\"]";
    private final String kubeConfig = "{}";
    private final String type = "K8S";

    private final Map<String, Property> prepareParamsMap = new HashMap<String, Property>() {

        {
            put(DAY, new Property() {

                {
                    setProp(DAY);
                    setValue(date);
                }
            });
        }
    };

    private final int datasource = 0;
    private final List<Label> labels = Arrays.asList(new Label("test", "1234"));
    private final List<NodeSelectorExpression> nodeSelectorExpressions =
            Arrays.asList(new NodeSelectorExpression("node-label", "In", "1234,12345"));

    private static MockedStatic<DataSourceUtils> dataSourceUtilsStaticMock = null;

    @BeforeEach
    public void before() {
        String k8sTaskParameters = buildK8sTaskParameters();
        TaskExecutionContext taskExecutionContext = mock(TaskExecutionContext.class);
        ResourceParametersHelper resourceParametersHelper = mock(ResourceParametersHelper.class);
        K8sConnectionParam k8sConnectionParam = mock(K8sConnectionParam.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(k8sTaskParameters);
        when(k8sConnectionParam.getNamespace()).thenReturn(namespace);
        when(k8sConnectionParam.getKubeConfig()).thenReturn(kubeConfig);
        when(taskExecutionContext.getPrepareParamsMap()).thenReturn(prepareParamsMap);
        when(taskExecutionContext.getResourceParametersHelper()).thenReturn(resourceParametersHelper);
        dataSourceUtilsStaticMock = Mockito.mockStatic(DataSourceUtils.class);
        dataSourceUtilsStaticMock.when(() -> DataSourceUtils.buildConnectionParams(Mockito.any(), Mockito.any()))
                .thenReturn(k8sConnectionParam);
        k8sTask = spy(new K8sTask(taskExecutionContext));
        k8sTask.init();
    }

    @AfterEach
    public void afterEach() {
        dataSourceUtilsStaticMock.close();
    }
    private String buildK8sTaskParameters() {
        K8sTaskParameters k8sTaskParameters = new K8sTaskParameters();
        k8sTaskParameters.setImage(image);
        k8sTaskParameters.setImagePullPolicy(imagePullPolicy);
        k8sTaskParameters.setNamespace(namespace);
        k8sTaskParameters.setMinCpuCores(minCpuCores);
        k8sTaskParameters.setMinMemorySpace(minMemorySpace);
        k8sTaskParameters.setCommand(command);
        k8sTaskParameters.setArgs(args);
        k8sTaskParameters.setCustomizedLabels(labels);
        k8sTaskParameters.setNodeSelectors(nodeSelectorExpressions);
        k8sTaskParameters.setLocalParams(new ArrayList<>());
        k8sTaskParameters.setPullSecret(pullSecret);
        k8sTaskParameters.setType(type);
        k8sTaskParameters.setKubeConfig(kubeConfig);
        k8sTaskParameters.setDatasource(datasource);
        return JSONUtils.toJsonString(k8sTaskParameters);
    }
    @Test
    public void testBuildCommandNormal() {
        String expectedStr =
                "{\"image\":\"ds-dev\",\"command\":\"[\\\"/bin/bash\\\", \\\"-c\\\"]\",\"args\":\"[\\\"echo hello world\\\"]\",\"pullSecret\":\"ds-secret\",\"namespaceName\":\"namespace\",\"imagePullPolicy\":\"IfNotPresent\",\"minCpuCores\":2.0,\"minMemorySpace\":10.0,\"paramsMap\":{\"day\":\"20220507\"},\"labelMap\":{\"test\":\"1234\"},\"nodeSelectorRequirements\":[{\"key\":\"node-label\",\"operator\":\"In\",\"values\":[\"1234\",\"12345\"]}]}";
        String commandStr = k8sTask.buildCommand();
        Assertions.assertEquals(expectedStr, commandStr);
    }

    @Test
    public void testGetParametersNormal() {
        String expectedStr =
                "K8sTaskParameters(image=ds-dev, namespace=namespace, command=[\"/bin/bash\", \"-c\"], args=[\"echo hello world\"], pullSecret=ds-secret, imagePullPolicy=IfNotPresent, minCpuCores=2.0, minMemorySpace=10.0, customizedLabels=[Label(label=test, value=1234)], nodeSelectors=[NodeSelectorExpression(key=node-label, operator=In, values=1234,12345)], kubeConfig={}, datasource=0, type=K8S)";
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

    @Test
    public void testDealOutParam() {
        String result = "key=123" + VAR_DELIMITER;
        k8sTask.getParameters().localParams.add(new Property("key", Direct.OUT, DataType.VARCHAR, "value"));
        k8sTask.dealOutParam(result);
        k8sTask.getParameters().getVarPool().forEach(property -> {
            Assertions.assertNotEquals("value", property.getValue());
            Assertions.assertEquals("123", property.getValue());
        });
    }
}
