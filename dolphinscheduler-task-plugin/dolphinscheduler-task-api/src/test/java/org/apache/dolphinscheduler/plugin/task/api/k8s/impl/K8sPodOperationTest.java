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

package org.apache.dolphinscheduler.plugin.task.api.k8s.impl;

import org.apache.dolphinscheduler.common.utils.YamlUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.K8sPodPhaseConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.K8sYamlType;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.K8sUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import lombok.SneakyThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.type.TypeReference;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;

public class K8sPodOperationTest {

    private static final KubernetesClient mockClient = Mockito.mock(KubernetesClient.class);
    private static final K8sPodOperation k8sPodOperation = new K8sPodOperation(mockClient);

    private static final String simplePodYaml = "apiVersion: v1\n" +
            "kind: Pod\n" +
            "metadata:\n" +
            "  name: hello-mock-world\n" +
            "  namespace: default\n" +
            "spec:\n" +
            "  containers:\n" +
            "    - name: hello-world-container\n" +
            "      image: hello-world\n";

    private static final Pod pod = Objects.requireNonNull(
            YamlUtils.load(simplePodYaml, new TypeReference<Pod>() {
            }));

    private static final String TEST_POD_NAME = pod.getMetadata().getName();
    private static final String TEST_NAMESPACE = K8sUtils.getOrDefaultNamespace(pod.getMetadata().getNamespace());
    private static final int taskInstanceId = 1000;

    @BeforeAll
    public static void init() {

        PodStatus mockStatus = new PodStatus();
        pod.setStatus(mockStatus);

        // BEGIN Mockito Stub for chain: client.pods().resource(anyPodResource).*
        MixedOperation<Pod, PodList, PodResource> mockPodsOperations = Mockito.mock(MixedOperation.class);
        Mockito.when(mockClient.pods()).thenReturn(mockPodsOperations);
        PodResource mockPodResource = Mockito.mock(PodResource.class);
        Mockito.when(mockPodsOperations.resource(Mockito.any())).thenReturn(mockPodResource);

        // Mockito Stub for chain: client.pods().resource(anyPodResource).get() => Pod
        Mockito.when(mockPodResource.get()).thenReturn(pod);

        // Mockito Stub for chain: client.pods().resource(anyPodResource).createOrReplace() => Pod
        Mockito.when(mockPodResource.createOrReplace()).thenReturn(pod);

        // END Mockito Stub for chain: client.pods().resource(anyPodResource).*

        // Mockito Stub for chain: client.pods().inNamespace(anyNamespace).withLabel(anyKey, anyValue) => List<Pod>
        NonNamespaceOperation<Pod, PodList, PodResource> mockNonNamespaceOperation =
                Mockito.mock(NonNamespaceOperation.class);
        FilterWatchListDeletable<Pod, PodList, PodResource> mockWatchList =
                Mockito.mock(FilterWatchListDeletable.class);
        PodList mockPodListEntity = Mockito.mock(PodList.class);
        Mockito.when(mockWatchList.list()).thenReturn(mockPodListEntity);
        List<Pod> mockPodList = new ArrayList<Pod>();
        mockPodList.add(pod);
        Mockito.when(mockPodListEntity.getItems()).thenReturn(mockPodList);
        Mockito.when(mockNonNamespaceOperation.withLabel(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(mockWatchList);

        // BEGIN Mockito Stub for chain: client.pods().inNamespace(anyNamespace).withName(anyName).*
        Mockito.when(mockPodsOperations.inNamespace(Mockito.anyString())).thenReturn(mockNonNamespaceOperation);
        Mockito.when(mockNonNamespaceOperation.withName(Mockito.anyString())).thenReturn(mockPodResource);

        // Mockito Stub for chain: client.pods().inNamespace(anyNamespace).withName(anyName).watchLog()
        LogWatch mockLogWatch = Mockito.mock(LogWatch.class);
        Mockito.when(mockPodResource.watchLog()).thenReturn(mockLogWatch);

        // Mockito Stub for chain: client.pods().inNamespace(anyNamespace).withName(anyName).watch(anyWatcher)
        Watch mockWatch = Mockito.mock(Watch.class);
        Mockito.when(mockPodResource.watch(Mockito.any())).thenReturn(mockWatch);

        // Mockito Stub for chain: client.pods().inNamespace(anyNamespace).withName(anyName).delete()
        List mockDeletionStatusDetails = Mockito.mock(List.class);
        Mockito.when(mockPodResource.delete()).thenReturn(mockDeletionStatusDetails);

        // END Mockito Stub for chain: client.pods().inNamespace(anyNamespace).withName(anyName).*
    }

    @Test
    public void testBuildMetadata() {
        Assertions.assertThrows(TaskException.class, () -> k8sPodOperation.buildMetadata(null));
        Assertions.assertThrows(TaskException.class, () -> k8sPodOperation.buildMetadata(""));
        Assertions.assertNotNull(k8sPodOperation.buildMetadata(simplePodYaml));
        Assertions.assertEquals(K8sYamlType.Pod, K8sYamlType.valueOf(pod.getKind()));
    }

    @Test
    public void testCreateOrReplacePod() {
        // Mockito Stub for chain: client.pods().inNamespace(anyNamespace).withName(anyName).get() => null (just create)
        Mockito.when(mockClient.pods().inNamespace(TEST_NAMESPACE).withName(TEST_POD_NAME).get()).thenReturn(null);
        Assertions.assertDoesNotThrow(() -> k8sPodOperation.createOrReplaceMetadata(pod, taskInstanceId));

        // Mockito Stub for chain: client.pods().inNamespace(anyNamespace).withName(anyName).get() => pod (deletion)
        Mockito.when(mockClient.pods().inNamespace(TEST_NAMESPACE).withName(TEST_POD_NAME).get()).thenReturn(pod);
        Assertions.assertDoesNotThrow(() -> k8sPodOperation.createOrReplaceMetadata(pod, taskInstanceId));
    }

    @Test
    public void testGetState() {
        PodStatus mockStatus = pod.getStatus();
        mockStatus.setPhase(K8sPodPhaseConstants.SUCCEEDED);
        Assertions.assertEquals(TaskConstants.EXIT_CODE_SUCCESS, k8sPodOperation.getState(pod));
        mockStatus.setPhase(K8sPodPhaseConstants.FAILED);
        Assertions.assertEquals(TaskConstants.EXIT_CODE_FAILURE, k8sPodOperation.getState(pod));
        mockStatus.setPhase(K8sPodPhaseConstants.PENDING);
        Assertions.assertEquals(TaskConstants.RUNNING_CODE, k8sPodOperation.getState(pod));
    }

    @Test
    public void testCreateBatchWatcher() {
        CountDownLatch countDownLatch = Mockito.mock(CountDownLatch.class);
        TaskResponse taskResponse = Mockito.mock(TaskResponse.class);
        TaskExecutionContext taskRequest = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskRequest.getTaskInstanceId()).thenReturn(1000);
        Mockito.when(taskRequest.getProcessInstanceId()).thenReturn(2000);
        Assertions.assertNotNull(k8sPodOperation.createBatchWatcher(countDownLatch, taskResponse, pod, taskRequest));
    }

    @Test
    public void testGetLogWatcher() {
        PodStatus mockPodStatus = pod.getStatus();
        mockPodStatus.setPhase(K8sPodPhaseConstants.SUCCEEDED);
        Assertions.assertNotNull(k8sPodOperation.getLogWatcher("<mock-label>", ""));
    }

    @Test
    @SneakyThrows
    public void testStopMetadata() {
        Assertions.assertNotNull(k8sPodOperation.stopMetadata(pod));
    }
}
