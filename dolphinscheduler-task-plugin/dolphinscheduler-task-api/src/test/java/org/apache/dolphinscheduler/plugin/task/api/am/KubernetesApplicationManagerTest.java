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

package org.apache.dolphinscheduler.plugin.task.api.am;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.UNIQUE_LABEL_NAME;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceManagerType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ContainerResource;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;

@ExtendWith(MockitoExtension.class)
public class KubernetesApplicationManagerTest {

    @Spy
    private KubernetesApplicationManager kubernetesApplicationManager = new KubernetesApplicationManager();

    @Mock
    private KubernetesClientPool kubernetesClientPoolInstance;

    @Mock
    private MockedStatic<KubernetesClientPool> mockedKubernetesClientPool;

    private KubernetesClient mockClient;
    private K8sTaskExecutionContext mockK8sTaskExecutionContext;
    private KubernetesApplicationManagerContext mockContext;
    private FilterWatchListDeletable<Pod, PodList, PodResource> mockWatchList;

    private final String mockLabelValue = "test-label-value";
    private final String mockNamespace = "test-namespace";
    private final String mockKubeConfig = "test-kube-config";
    private final String mockClusterId = "k8s-cluster-123";

    @BeforeEach
    public void setUp() {
        mockContext = mock(KubernetesApplicationManagerContext.class);
        mockK8sTaskExecutionContext = mock(K8sTaskExecutionContext.class);
        mockClient = mock(KubernetesClient.class);
        mockWatchList = mock(FilterWatchListDeletable.class);
    }

    @AfterEach
    public void tearDown() {
        mockedKubernetesClientPool.close();
    }
    @Test
    public void testGetClusterId() {
        KubernetesClientPool mockPool = Mockito.mock(KubernetesClientPool.class);
        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(mockPool);
        Mockito.when(mockPool.getClusterId(Mockito.anyString())).thenReturn(mockClusterId);

        mockK8sTaskExecutionContext = mock(K8sTaskExecutionContext.class);
        when(mockK8sTaskExecutionContext.getConfigYaml()).thenReturn(mockKubeConfig);
        String clusterId = new KubernetesApplicationManager().getClusterId(mockK8sTaskExecutionContext);
        Assertions.assertSame(clusterId,mockClusterId, "ClusterId Same");
    }

    @Test
    public void testGetResourceManagerType() {
        KubernetesApplicationManager manager = new KubernetesApplicationManager();
        ResourceManagerType resourceManagerType = manager.getResourceManagerType();
        Assertions.assertEquals(ResourceManagerType.KUBERNETES, resourceManagerType);
    }

    @Test
    public void testReturnClientSuccess() throws NoSuchFieldException, IllegalAccessException {
        when(KubernetesClientPool.getInstance()).thenReturn(kubernetesClientPoolInstance);
        KubernetesClientPool kubernetesClientPool = Mockito.mock(KubernetesClientPool.class);

        Field field = KubernetesApplicationManager.class.getDeclaredField("clientPool");
        field.setAccessible(true);
        field.set(kubernetesApplicationManager, kubernetesClientPool);

        kubernetesApplicationManager.returnClient(mockClusterId, mockClient);
        verify(kubernetesClientPool).returnClient(mockClusterId, mockClient); // just verify use it
    }

    @Test
    public void testKillApplicationSuccess() {
        when(mockContext.getLabelValue()).thenReturn(mockLabelValue);
        when(mockContext.getK8sTaskExecutionContext()).thenReturn(mockK8sTaskExecutionContext);
        when(mockK8sTaskExecutionContext.getNamespace()).thenReturn(mockNamespace);

        doReturn(mockWatchList).when(kubernetesApplicationManager).getListenPod(mockContext);
        doReturn(TaskExecutionStatus.SUCCESS).when(kubernetesApplicationManager).getApplicationStatus(mockContext,
                mockWatchList);
        doReturn(mockClusterId).when(kubernetesApplicationManager).getClusterId(mockK8sTaskExecutionContext);
        doReturn(mockClient).when(kubernetesApplicationManager).getClient(mockContext);

        MixedOperation<Pod, PodList, PodResource> mockPodsOperation = mock(MixedOperation.class);
        NonNamespaceOperation<Pod, PodList, PodResource> mockNamespaceOperation = mock(NonNamespaceOperation.class);
        FilterWatchListDeletable<Pod, PodList, PodResource> mockResult = mock(FilterWatchListDeletable.class);

        when(mockClient.pods()).thenReturn(mockPodsOperation);
        when(mockPodsOperation.inNamespace(mockNamespace)).thenReturn(mockNamespaceOperation);
        when(mockNamespaceOperation.withLabel(UNIQUE_LABEL_NAME, mockLabelValue)).thenReturn(mockResult);

        boolean flag = kubernetesApplicationManager.killApplication(mockContext);

        verify(mockResult).delete();
        verify(kubernetesApplicationManager).returnClient(mockClusterId, mockClient);
        Assertions.assertTrue(flag);
    }

    @Test
    public void testKillApplicationFail() {
        when(mockContext.getLabelValue()).thenReturn(mockLabelValue);
        doReturn(mockWatchList).when(kubernetesApplicationManager).getListenPod(mockContext);
        doReturn(TaskExecutionStatus.FAILURE).when(kubernetesApplicationManager).getApplicationStatus(mockContext,
                mockWatchList);

        boolean flag = kubernetesApplicationManager.killApplication(mockContext);
        Assertions.assertFalse(flag);
    }

    @Test
    public void testGetListenPod() {
        when(mockContext.getK8sTaskExecutionContext()).thenReturn(mockK8sTaskExecutionContext);
        when(mockK8sTaskExecutionContext.getNamespace()).thenReturn(mockNamespace);
        when(mockContext.getLabelValue()).thenReturn(mockLabelValue);

        doReturn(mockClusterId).when(kubernetesApplicationManager).getClusterId(mockK8sTaskExecutionContext);
        MixedOperation<Pod, PodList, PodResource> mockPodsOperation = mock(MixedOperation.class);
        NonNamespaceOperation<Pod, PodList, PodResource> mockNamespaceOperation = mock(NonNamespaceOperation.class);
        FilterWatchListDeletable<Pod, PodList, PodResource> mockResult = mock(FilterWatchListDeletable.class);

        doReturn(mockClient).when(kubernetesApplicationManager).getClient(mockContext);

        when(mockClient.pods()).thenReturn(mockPodsOperation);
        when(mockPodsOperation.inNamespace(mockNamespace)).thenReturn(mockNamespaceOperation);
        when(mockNamespaceOperation.withLabel(UNIQUE_LABEL_NAME, mockLabelValue)).thenReturn(mockResult);
        PodList pods = mock(PodList.class);
        Pod mockPod = Mockito.mock(Pod.class);
        List<Pod> podList = Collections.singletonList(mockPod);
        when(mockResult.list()).thenReturn(pods);
        when(pods.getItems()).thenReturn(podList);

        FilterWatchListDeletable<Pod, PodList, PodResource> result =
                kubernetesApplicationManager.getListenPod(mockContext);
        Assertions.assertEquals(mockResult, result);
        verify(kubernetesApplicationManager).returnClient(mockClusterId, mockClient);
    }

    @Test
    public void testGetClientSuccess() throws NoSuchFieldException, IllegalAccessException {
        when(mockContext.getK8sTaskExecutionContext()).thenReturn(mockK8sTaskExecutionContext);
        doReturn(mockClusterId).when(kubernetesApplicationManager).getClusterId(mockK8sTaskExecutionContext);
        when(mockK8sTaskExecutionContext.getConfigYaml()).thenReturn(mockKubeConfig);
        KubernetesClientPool mockKubernetesClientPool = mock(KubernetesClientPool.class);

        Field field = KubernetesApplicationManager.class.getDeclaredField("clientPool");
        field.setAccessible(true);
        field.set(kubernetesApplicationManager, mockKubernetesClientPool);

        kubernetesApplicationManager.getClient(mockContext);
        verify(mockKubernetesClientPool).getClient(mockClusterId, mockKubeConfig);
    }

    @Test
    public void testGetApplicationStatusSuccess() {
        doReturn(mockWatchList).when(kubernetesApplicationManager).getListenPod(mockContext);
        when(mockContext.getK8sTaskExecutionContext()).thenReturn(mockK8sTaskExecutionContext);
        doReturn(mockClusterId).when(kubernetesApplicationManager).getClusterId(mockK8sTaskExecutionContext);
        doReturn(mockClient).when(kubernetesApplicationManager).getClient(mockContext);
        when(mockContext.getLabelValue()).thenReturn(mockLabelValue);
        when(mockK8sTaskExecutionContext.getNamespace()).thenReturn(mockNamespace);

        MixedOperation<Pod, PodList, PodResource> mockPodsOperation = mock(MixedOperation.class);
        NonNamespaceOperation<Pod, PodList, PodResource> mockNamespaceOperation = mock(NonNamespaceOperation.class);
        FilterWatchListDeletable<Pod, PodList, PodResource> mockResult = mock(FilterWatchListDeletable.class);

        when(mockClient.pods()).thenReturn(mockPodsOperation);
        when(mockPodsOperation.inNamespace(mockNamespace)).thenReturn(mockNamespaceOperation);
        when(mockNamespaceOperation.withLabel(UNIQUE_LABEL_NAME, mockLabelValue)).thenReturn(mockResult);
        PodStatus mockPodStatus = mock(PodStatus.class);
        PodList pods = mock(PodList.class);
        Pod mockPod = Mockito.mock(Pod.class);
        List<Pod> podList = Collections.singletonList(mockPod);
        when(mockResult.list()).thenReturn(pods);
        when(pods.getItems()).thenReturn(podList);
        when(mockPod.getStatus()).thenReturn(mockPodStatus);
        when(mockPodStatus.getPhase()).thenReturn("Succeeded");

        TaskExecutionStatus applicationStatus = kubernetesApplicationManager.getApplicationStatus(mockContext);
        Assertions.assertEquals(TaskExecutionStatus.SUCCESS, applicationStatus);
    }

    @Test
    public void testGetApplicationStatusFailed() {
        doReturn(mockWatchList).when(kubernetesApplicationManager).getListenPod(mockContext);
        when(mockContext.getK8sTaskExecutionContext()).thenReturn(mockK8sTaskExecutionContext);
        doReturn(mockClusterId).when(kubernetesApplicationManager).getClusterId(mockK8sTaskExecutionContext);
        doReturn(mockClient).when(kubernetesApplicationManager).getClient(mockContext);
        when(mockContext.getLabelValue()).thenReturn(mockLabelValue);
        when(mockK8sTaskExecutionContext.getNamespace()).thenReturn(mockNamespace);

        MixedOperation<Pod, PodList, PodResource> mockPodsOperation = mock(MixedOperation.class);
        NonNamespaceOperation<Pod, PodList, PodResource> mockNamespaceOperation = mock(NonNamespaceOperation.class);
        FilterWatchListDeletable<Pod, PodList, PodResource> mockResult = mock(FilterWatchListDeletable.class);

        when(mockClient.pods()).thenReturn(mockPodsOperation);
        when(mockPodsOperation.inNamespace(mockNamespace)).thenReturn(mockNamespaceOperation);
        when(mockNamespaceOperation.withLabel(UNIQUE_LABEL_NAME, mockLabelValue)).thenReturn(mockResult);
        PodStatus mockPodStatus = mock(PodStatus.class);
        PodList pods = mock(PodList.class);
        Pod mockPod = Mockito.mock(Pod.class);
        List<Pod> podList = Collections.singletonList(mockPod);
        when(mockResult.list()).thenReturn(pods);
        when(pods.getItems()).thenReturn(podList);
        when(mockPod.getStatus()).thenReturn(mockPodStatus);
        when(mockPodStatus.getPhase()).thenReturn("Failed");

        TaskExecutionStatus applicationStatus = kubernetesApplicationManager.getApplicationStatus(mockContext);
        Assertions.assertEquals(TaskExecutionStatus.FAILURE, applicationStatus);
    }
    @Test
    public void testGetPodLogWatcher() {
        doReturn(mockClient).when(kubernetesApplicationManager).getClient(mockContext);
        doReturn(mockWatchList).when(kubernetesApplicationManager).getListenPod(mockContext);

        KubernetesClientPool mockPool = Mockito.mock(KubernetesClientPool.class);
        Mockito.when(KubernetesClientPool.getInstance()).thenReturn(mockPool);
        mockK8sTaskExecutionContext = mock(K8sTaskExecutionContext.class);
        when(mockContext.getK8sTaskExecutionContext()).thenReturn(mockK8sTaskExecutionContext);
        when(mockK8sTaskExecutionContext.getConfigYaml()).thenReturn(mockKubeConfig);

        MixedOperation<Pod, PodList, PodResource> mockPodsOperation = mock(MixedOperation.class);
        NonNamespaceOperation<Pod, PodList, PodResource> mockNamespaceOperation = mock(NonNamespaceOperation.class);
        Pod mockPod = Mockito.mock(Pod.class);
        List<Pod> podList = Collections.singletonList(mockPod); // must have one pod
        PodList pods = mock(PodList.class);
        ObjectMeta mockMeta = Mockito.mock(ObjectMeta.class);
        PodStatus mockStatus = Mockito.mock(PodStatus.class);
        PodResource mockPodResource = Mockito.mock(PodResource.class);
        ContainerResource mockContainerResource = Mockito.mock(ContainerResource.class);
        LogWatch mockLogWatch = Mockito.mock(LogWatch.class);

        when(mockWatchList.list()).thenReturn(pods);
        when(pods.getItems()).thenReturn(podList);
        when(mockPod.getStatus()).thenReturn(mockStatus);
        when(mockStatus.getPhase()).thenReturn("Running");

        when(mockPod.getMetadata()).thenReturn(mockMeta);
        when(mockMeta.getNamespace()).thenReturn(mockNamespace);
        when(mockMeta.getName()).thenReturn("mockName");
        when(mockContext.getContainerName()).thenReturn("container-1");

        when(mockClient.pods()).thenReturn(mockPodsOperation);
        when(mockPodsOperation.inNamespace(mockNamespace)).thenReturn(mockNamespaceOperation);
        when(mockNamespaceOperation.withName("mockName")).thenReturn(mockPodResource);
        when(mockPodResource.inContainer("container-1")).thenReturn(mockContainerResource);
        when(mockContainerResource.watchLog()).thenReturn(mockLogWatch);

        LogWatch podLogWatcher = kubernetesApplicationManager.getPodLogWatcher(mockContext);
        Assertions.assertTrue(podLogWatcher instanceof LogWatch);
        Assertions.assertSame(mockLogWatch.getOutput(), podLogWatcher.getOutput());

    }
}
