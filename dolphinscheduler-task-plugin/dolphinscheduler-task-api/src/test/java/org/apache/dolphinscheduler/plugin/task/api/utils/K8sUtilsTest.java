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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.BatchAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.PrettyLoggable;
import io.fabric8.kubernetes.client.dsl.ScalableResource;
import io.fabric8.kubernetes.client.dsl.V1BatchAPIGroupDSL;

public class K8sUtilsTest {

    private K8sUtils k8sUtils;
    private MockedStatic<KubernetesClientPool> mockedKubernetesClientPool;
    private KubernetesClient mockClient;
    private final String mockClusterId = "mock-cluster-id";
    private final String mockKubeConfig =
            "apiVersion: v1\nclusters:\n- cluster:\n    server: https://kubernetes.default.svc\n  name: mock-cluster\ncontexts:\n- context:\n    cluster: mock-cluster\n    namespace: default\n    user: mock-user\n  name: mock-context\ncurrent-context: mock-context\nkind: Config\npreferences: {}\nusers:\n- name: mock-user\n  user: {}\n";
    private final String mockNamespace = "default";
    private final String mockJobName = "test-job-123";

    private BatchAPIGroupDSL mockBatch;
    private V1BatchAPIGroupDSL mockV1;
    private MixedOperation<Job, JobList, ScalableResource<Job>> mockJobs;
    private MixedOperation<Job, JobList, ScalableResource<Job>> mockInNamespace;
    private ScalableResource<Job> mockWithName;
    private ScalableResource<Job> mockResource;
    private Job mockJob;
    private Watch mockWatch;
    private Watcher<Job> mockWatcher;

    private final String expectedLog = "Pod log content";

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {
        k8sUtils = new K8sUtils();
        mockedKubernetesClientPool = Mockito.mockStatic(KubernetesClientPool.class); // 拦截所有使用静态方法的请求
        KubernetesClientPool mockPool = Mockito.mock(KubernetesClientPool.class);
        mockClient = Mockito.mock(KubernetesClient.class);

        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(mockPool);
        Mockito.when(mockPool.getClusterId(mockKubeConfig)).thenReturn(mockClusterId);
        Mockito.when(mockPool.getClient(mockClusterId, mockKubeConfig)).thenReturn(mockClient);

        mockJob = Mockito.mock(Job.class);

        mockBatch = Mockito.mock(BatchAPIGroupDSL.class);
        mockV1 = Mockito.mock(V1BatchAPIGroupDSL.class);
        mockJobs = Mockito.mock(MixedOperation.class);
        mockInNamespace = Mockito.mock(MixedOperation.class);
        mockResource = Mockito.mock(ScalableResource.class);
        mockWatch = Mockito.mock(Watch.class);
        mockWatcher = Mockito.mock(Watcher.class);
        mockWithName = Mockito.mock(ScalableResource.class);
        List<StatusDetails> mockStatusDetails = Mockito.mock(List.class);
        Pod mockPod = Mockito.mock(Pod.class);
        List<Pod> pods = Collections.singletonList(mockPod); // it must have a pod
        PodResource mockPodResource = Mockito.mock(PodResource.class);
        MixedOperation<Pod, PodList, PodResource> mockPods = Mockito.mock(MixedOperation.class);
        MixedOperation<Pod, PodList, PodResource> mockPodsInNamespace = Mockito.mock(MixedOperation.class);
        PodList podList = Mockito.mock(PodList.class);
        ObjectMeta mockMetadata = Mockito.mock(ObjectMeta.class);
        PrettyLoggable prettyLoggable = Mockito.mock(PrettyLoggable.class);

        Mockito.when(mockClient.batch()).thenReturn(mockBatch);
        Mockito.when(mockBatch.v1()).thenReturn(mockV1);
        Mockito.when(mockV1.jobs()).thenReturn(mockJobs);

        Mockito.when(mockJobs.inNamespace(mockNamespace)).thenReturn(mockInNamespace);
        Mockito.when(mockJobs.withName(mockJobName)).thenReturn(mockWithName);

        Mockito.when(mockInNamespace.resource(mockJob)).thenReturn(mockResource);
        Mockito.when(mockInNamespace.withName(mockJobName)).thenReturn(mockWithName);

        Mockito.when(mockResource.create()).thenReturn(mockJob);

        Mockito.when(mockWithName.delete()).thenReturn(mockStatusDetails);
        Mockito.when(mockWithName.get()).thenReturn(mockJob);

        Mockito.when(mockWithName.watch(mockWatcher)).thenReturn(mockWatch);

        Mockito.when(mockClient.pods()).thenReturn(mockPods);
        Mockito.when(mockPods.inNamespace(mockNamespace)).thenReturn(mockPodsInNamespace);
        Mockito.when(mockPodsInNamespace.list()).thenReturn(podList);
        Mockito.when(podList.getItems()).thenReturn(pods);

        Mockito.when(mockPod.getMetadata()).thenReturn(mockMetadata);
        Mockito.when(mockMetadata.getName()).thenReturn(mockJobName + "-pod-123");

        Mockito.when(mockPodsInNamespace.withName(mockJobName + "-pod-123")).thenReturn(mockPodResource);
        Mockito.when(mockPodResource.tailingLines(Mockito.anyInt())).thenReturn(prettyLoggable);
        Mockito.when(prettyLoggable.getLog(Mockito.anyBoolean())).thenReturn(expectedLog);
    }

    @AfterEach
    void tearDown() {
        if (mockedKubernetesClientPool != null) {
            mockedKubernetesClientPool.close();
        }
    }

    @Test
    public void testCreateJobSuccess() {
        k8sUtils.createJob(mockKubeConfig, mockNamespace, mockJob);
        Mockito.verify(mockClient).batch();
        Mockito.verify(mockBatch).v1();
        Mockito.verify(mockV1).jobs();
        Mockito.verify(mockJobs).inNamespace(mockNamespace);
        Mockito.verify(mockInNamespace).resource(mockJob);
        Mockito.verify(mockResource).create();
    }

    @Test
    public void testDeleteJobSuccess() {
        k8sUtils.deleteJob(mockKubeConfig, mockJobName, mockNamespace);
        Mockito.verify(mockClient).batch();
        Mockito.verify(mockBatch).v1();
        Mockito.verify(mockV1).jobs();
        Mockito.verify(mockJobs).inNamespace(mockNamespace);
        Mockito.verify(mockInNamespace).withName(mockJobName);
        Mockito.verify(mockWithName).delete();
    }

    @Test
    public void testJobExistSuccess() {
        Boolean result = k8sUtils.jobExist(mockKubeConfig, mockJobName, mockNamespace);
        Assertions.assertTrue(result);
    }

    @Test
    public void testJobExistFailure() {
        RuntimeException expectedException = new RuntimeException("Check job failed");
        Mockito.doThrow(expectedException).when(mockClient).batch();

        TaskException exception = Assertions.assertThrows(TaskException.class,
                () -> k8sUtils.jobExist(mockKubeConfig, mockJobName, mockNamespace));

        Assertions.assertEquals("fail to check job", exception.getMessage());
        Assertions.assertEquals(expectedException, exception.getCause());
    }

    @Test
    public void testCreateBatchJobWatcherSuccess() {
        Watch result = k8sUtils.createBatchJobWatcher(mockKubeConfig, mockJobName, mockWatcher);
        Assertions.assertEquals(mockWatch, result);
    }

    @Test
    public void testCreateBatchJobWatcherFailure() {
        RuntimeException expectedException = new RuntimeException("Create watcher failed");
        Mockito.doThrow(expectedException).when(mockClient).batch();

        TaskException exception = Assertions.assertThrows(TaskException.class,
                () -> k8sUtils.createBatchJobWatcher(mockKubeConfig, mockJobName, mockWatcher));

        Assertions.assertEquals("fail to register batch job watcher", exception.getMessage());
        Assertions.assertEquals(expectedException, exception.getCause());
    }

    @Test
    public void testGetPodLogSuccess() {
        String result = k8sUtils.getPodLog(mockKubeConfig, mockJobName, mockNamespace);
        Assertions.assertEquals(expectedLog, result);
    }

    @Test
    public void testGetPodLogWithEmptyPodList() {
        PodList podList = Mockito.mock(PodList.class);
        List<Pod> emptyPodList = Collections.emptyList();
        Mockito.when(mockClient.pods().inNamespace(mockNamespace).list()).thenReturn(podList);
        Mockito.when(podList.getItems()).thenReturn(emptyPodList);

        String result = k8sUtils.getPodLog(mockKubeConfig, mockJobName, mockNamespace);
        Assertions.assertNull(result);
    }

    @Test
    public void testGetPodLogFailure() {
        RuntimeException expectedException = new RuntimeException("Get pod log failed");
        Mockito.doThrow(expectedException).when(mockClient).pods();

        String result = k8sUtils.getPodLog(mockKubeConfig, mockJobName, mockNamespace);
        Assertions.assertNull(result);
    }
}
