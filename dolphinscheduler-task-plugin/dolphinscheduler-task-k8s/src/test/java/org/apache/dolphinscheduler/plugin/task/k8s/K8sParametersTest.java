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

import org.apache.dolphinscheduler.plugin.task.api.model.Label;
import org.apache.dolphinscheduler.plugin.task.api.model.NodeSelectorExpression;
import org.apache.dolphinscheduler.plugin.task.api.parameters.K8sTaskParameters;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class K8sParametersTest {

    private K8sTaskParameters k8sTaskParameters = null;
    private final String image = "ds-dev";
    private final String imagePullPolicy = "IfNotPresent";
    private final String namespace = "{\"name\":\"default\",\"cluster\":\"lab\"}";
    private final double minCpuCores = 2;
    private final double minMemorySpace = 10;
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
    }

    @Test
    public void testCheckParameterNormal() {
        Assertions.assertTrue(k8sTaskParameters.checkParameters());
    }

    @Test
    public void testGetResourceFilesListNormal() {
        Assertions.assertNotNull(k8sTaskParameters.getResourceFilesList());
        Assertions.assertEquals(0, k8sTaskParameters.getResourceFilesList().size());
    }

    @Test
    public void testK8sParameters() {
        Assertions.assertEquals(image, k8sTaskParameters.getImage());
        Assertions.assertEquals(imagePullPolicy, k8sTaskParameters.getImagePullPolicy());
        Assertions.assertEquals(namespace, k8sTaskParameters.getNamespace());
        Assertions.assertEquals(0, Double.compare(minCpuCores, k8sTaskParameters.getMinCpuCores()));
        Assertions.assertEquals(0, Double.compare(minMemorySpace, k8sTaskParameters.getMinMemorySpace()));
        Assertions.assertEquals(command, k8sTaskParameters.getCommand());
        Assertions.assertEquals(args, k8sTaskParameters.getArgs());
        Assertions.assertEquals(labels, k8sTaskParameters.getCustomizedLabels());
        Assertions.assertEquals(nodeSelectorExpressions, k8sTaskParameters.getNodeSelectors());
    }

}
