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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class KubeflowTaskTest {

    public static String clusterConfigName = "clusterConfigYAML.yaml";

    public static String jobConfigName = "jobConfigYAML.yaml";

    public static String readFile(String fileName) throws IOException {
        String path = KubeflowHelperTest.class.getClassLoader().getResource(fileName).getPath();
        String content = Files.lines(Paths.get(path), StandardCharsets.UTF_8)
                .collect(Collectors.joining(System.lineSeparator()));

        return content;
    }

    @Test
    public void testInit() throws IOException {
        KubeflowParameters kubeflowParameters = createKubeflowParameters();
        KubeflowTask task = createTask(kubeflowParameters);
        Assertions.assertEquals(kubeflowParameters.getClusterYAML(), task.getParameters().getClusterYAML());
        Assertions.assertEquals(kubeflowParameters.getYamlContent(), task.getParameters().getYamlContent());

        KubeflowParameters kubeflowParametersError2 = new KubeflowParameters();
        kubeflowParameters.setYamlContent(readFile(clusterConfigName));
        Assertions.assertThrows(TaskException.class, () -> {
            createTask(kubeflowParametersError2);
        });

    }

    @Test
    public void TestSubmit() throws IOException {
        KubeflowParameters kubeflowParameters = createKubeflowParameters();
        KubeflowTask task = Mockito.spy(createTask(kubeflowParameters));
        Mockito.when(task.runCommand(Mockito.anyString())).thenReturn("test_result");
        task.submitApplication();
        Assertions.assertNotEquals(null, task.getAppIds());
        Assertions.assertEquals(task.getExitStatusCode(), TaskConstants.EXIT_CODE_SUCCESS);
    }

    @Test
    public void TestTrack() throws IOException {
        KubeflowParameters kubeflowParameters = createKubeflowParameters();

        TaskExecutionContext taskExecutionContext = createTaskExecutionContext(kubeflowParameters);
        TestTask task = Mockito.spy(new TestTask(taskExecutionContext));
        Mockito.when(task.runCommand(Mockito.anyString())).thenReturn("track_result");
        task.init();

        KubeflowHelper kubeflowHelper = Mockito.mock(KubeflowHelper.class);
        Mockito.when(kubeflowHelper.buildGetCommand(Mockito.anyString())).thenReturn("");
        task.setKubeflowHelper(kubeflowHelper);

        Mockito.when(kubeflowHelper.parseGetMessage(Mockito.anyString())).thenReturn("Succeeded");
        task.trackApplicationStatus();
        Assertions.assertEquals(task.getExitStatusCode(), TaskConstants.EXIT_CODE_SUCCESS);

        Mockito.when(kubeflowHelper.parseGetMessage(Mockito.anyString())).thenReturn("Failed");
        task.trackApplicationStatus();
        Assertions.assertEquals(task.getExitStatusCode(), TaskConstants.EXIT_CODE_FAILURE);

        Mockito.when(kubeflowHelper.parseGetMessage(Mockito.anyString())).thenReturn("", "Succeeded");
        task.trackApplicationStatus();
        Assertions.assertEquals(task.getExitStatusCode(), TaskConstants.EXIT_CODE_SUCCESS);
    }

    @Test
    public void TestCancel() throws IOException {
        KubeflowParameters kubeflowParameters = createKubeflowParameters();
        KubeflowTask task = Mockito.spy(createTask(kubeflowParameters));
        Mockito.when(task.runCommand(Mockito.anyString())).thenReturn("delete_result");
        task.cancelApplication();
        Assertions.assertEquals(task.getExitStatusCode(), TaskConstants.EXIT_CODE_KILL);
    }

    public KubeflowTask createTask(KubeflowParameters kubeflowParameters) {
        TaskExecutionContext taskExecutionContext = createTaskExecutionContext(kubeflowParameters);
        KubeflowTask kubeflowTask = new KubeflowTask(taskExecutionContext);
        kubeflowTask.init();
        return kubeflowTask;
    }

    public TaskExecutionContext createTaskExecutionContext(KubeflowParameters kubeflowParameters) {
        String parameters = JSONUtils.toJsonString(kubeflowParameters);
        TaskExecutionContext taskExecutionContext =
                Mockito.mock(TaskExecutionContext.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp/dolphinscheduler/kubeflow");
        File file = new File("/tmp/dolphinscheduler/kubeflow");
        if (!file.exists()) {
            file.mkdirs();
        }
        Mockito.when(taskExecutionContext.getK8sTaskExecutionContext().getConfigYaml())
                .thenReturn(kubeflowParameters.getClusterYAML());
        TaskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);

        return taskExecutionContext;
    }

    public KubeflowParameters createKubeflowParameters() throws IOException {
        KubeflowParameters kubeflowParameters = new KubeflowParameters();
        kubeflowParameters.setClusterYAML(readFile(clusterConfigName));
        kubeflowParameters.setYamlContent(readFile(jobConfigName));
        return kubeflowParameters;
    }

    public static class TestTask extends KubeflowTask {

        public TestTask(TaskExecutionContext taskExecutionContext) {
            super(taskExecutionContext);
        }

        public void setKubeflowHelper(KubeflowHelper kubeflowHelper) {
            this.kubeflowHelper = kubeflowHelper;
        }
    }

}
