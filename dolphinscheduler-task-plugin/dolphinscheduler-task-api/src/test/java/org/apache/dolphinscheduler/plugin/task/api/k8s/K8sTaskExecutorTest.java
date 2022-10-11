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

package org.apache.dolphinscheduler.plugin.task.api.k8s;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.CLUSTER;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.NAMESPACE_NAME;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.k8s.impl.K8sTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;

public class K8sTaskExecutorTest {

    private K8sTaskExecutor k8sTaskExecutor = null;
    private K8sTaskMainParameters k8sTaskMainParameters = null;
    private final String image = "ds-dev";
    private final String namespace = "{\"name\":\"default\",\"cluster\":\"lab\"}";
    private final double minCpuCores = 2;
    private final double minMemorySpace = 10;
    private final int taskInstanceId = 1000;
    private final String taskName = "k8s_task_test";
    private Job job;
    @BeforeEach
    public void before() {
        TaskExecutionContext taskRequest = new TaskExecutionContext();
        taskRequest.setTaskInstanceId(taskInstanceId);
        taskRequest.setTaskName(taskName);
        Map<String, String> namespace = JSONUtils.toMap(this.namespace);
        String namespaceName = namespace.get(NAMESPACE_NAME);
        String clusterName = namespace.get(CLUSTER);
        k8sTaskExecutor = new K8sTaskExecutor(null, taskRequest);
        k8sTaskMainParameters = new K8sTaskMainParameters();
        k8sTaskMainParameters.setImage(image);
        k8sTaskMainParameters.setNamespaceName(namespaceName);
        k8sTaskMainParameters.setClusterName(clusterName);
        k8sTaskMainParameters.setMinCpuCores(minCpuCores);
        k8sTaskMainParameters.setMinMemorySpace(minMemorySpace);
        job = k8sTaskExecutor.buildK8sJob(k8sTaskMainParameters);
    }
    @Test
    public void testGetK8sJobStatusNormal() {
        JobStatus jobStatus = new JobStatus();
        jobStatus.setSucceeded(1);
        job.setStatus(jobStatus);
        Assertions.assertEquals(0, Integer.compare(0, k8sTaskExecutor.getK8sJobStatus(job)));
    }
    @Test
    public void testSetTaskStatusNormal() {
        int jobStatus = 0;
        TaskResponse taskResponse = new TaskResponse();
        K8sTaskMainParameters k8STaskMainParameters = new K8sTaskMainParameters();
        k8sTaskExecutor.setJob(job);
        k8sTaskExecutor.setTaskStatus(jobStatus, String.valueOf(taskInstanceId), taskResponse, k8STaskMainParameters);
        Assertions.assertEquals(0, Integer.compare(EXIT_CODE_KILL, taskResponse.getExitStatusCode()));
    }
    @Test
    public void testWaitTimeoutNormal() {
        try {
            k8sTaskExecutor.waitTimeout(true);
        } catch (TaskException e) {
            Assertions.assertEquals(e.getMessage(), "K8sTask is timeout");
        }
    }
}
