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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KubeflowHelperTest {

    private String clusterConfigPath = "/tmp/dolphinscheduler/.kube/config";

    private KubeflowHelper kubeflowHelper;

    @BeforeEach
    public void init() {
        kubeflowHelper = new KubeflowHelper(clusterConfigPath);
    }

    @Test
    public void testBuildSubmitCommand() {
        String yamlFilePATH = "/tmp/dolphinscheduler/test.yaml";
        String command = kubeflowHelper.buildSubmitCommand(yamlFilePATH);
        String expectCommand = String.format("export KUBECONFIG=%s\n", clusterConfigPath) +
                String.format("kubectl apply -f %s", yamlFilePATH);
        Assertions.assertEquals(expectCommand, command);
    }

    @Test
    public void testBuildGetCommand() {
        String yamlFilePATH = "/tmp/dolphinscheduler/test.yaml";
        String command = kubeflowHelper.buildGetCommand(yamlFilePATH);
        String expectCommand = String.format("export KUBECONFIG=%s\n", clusterConfigPath) +
                String.format("kubectl get -f %s -o json", yamlFilePATH);
        Assertions.assertEquals(expectCommand, command);
    }

    @Test
    public void testBuildDeleteCommand() {
        String yamlFilePATH = "/tmp/dolphinscheduler/test.yaml";
        String command = kubeflowHelper.buildDeleteCommand(yamlFilePATH);
        String expectCommand = String.format("export KUBECONFIG=%s\n", clusterConfigPath) +
                String.format("kubectl delete -f %s", yamlFilePATH);
        Assertions.assertEquals(expectCommand, command);
    }

    @Test
    public void testParseGetMessage() {
        String message = "{\n" +
                "    \"apiVersion\": \"kubeflow.org/v1\",\n" +
                "    \"kind\": \"PyTorchJob\",\n" +
                "    \"status\": {\n" +
                "        \"conditions\": [\n" +
                "            {\n" +
                "                \"key\": \"value\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"key\": \"value\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"phase\": \"Succeeded\"\n" +
                "    }\n" +
                "}\n";
        Assertions.assertEquals("Succeeded", kubeflowHelper.parseGetMessage(message));

        String messageError1 = "{\n" +
                "    \"apiVersion\": \"kubeflow.org/v1\",\n" +
                "    \"kind\": \"PyTorchJob\"\n" +
                "}\n";

        Assertions.assertDoesNotThrow(() -> kubeflowHelper.parseGetMessage(messageError1));

        String messageError2 = "{\n" +
                "    \"apiVersion\": \"kubeflow.org/v1\",\n" +
                "    \"kind\": \"PyTorchJob\",\n" +
                "    \"status\": {\n" +
                "        \"phase\": \"Failed\"\n" +
                "    }\n" +
                "}\n";

        Assertions.assertEquals("Failed", kubeflowHelper.parseGetMessage(messageError2));

    }
}
