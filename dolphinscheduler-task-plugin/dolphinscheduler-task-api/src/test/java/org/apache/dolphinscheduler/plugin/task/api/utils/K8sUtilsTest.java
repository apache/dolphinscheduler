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

import org.apache.dolphinscheduler.common.utils.YamlUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

public class K8sUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(K8sUtilsTest.class);

    @Test
    public void testGetOrDefaultNamespace() {
        String namespace;
        namespace = K8sUtils.getOrDefaultNamespace(null);
        Assertions.assertTrue(StringUtils.isNotBlank(namespace));
        namespace = K8sUtils.getOrDefaultNamespace("");
        Assertions.assertTrue(StringUtils.isNotBlank(namespace));
        namespace = K8sUtils.getOrDefaultNamespace("     ");
        Assertions.assertTrue(StringUtils.isNotBlank(namespace));
        namespace = "my-namespace-name";
        Assertions.assertEquals(namespace, K8sUtils.getOrDefaultNamespace(namespace));
    }

    @Test
    public void testGetOrDefaultNamespacedResource() {
        Assertions.assertThrows(TaskException.class, () -> K8sUtils.getOrDefaultNamespacedResource(null));

        // Load Pod without namespace from YAML file
        String filePathRelative = "k8s-yaml/hello-world-without-namespace.yaml";
        String filePathAbsolute = Objects.requireNonNull(
                getClass().getClassLoader().getResource(filePathRelative)).getFile();
        Pod podNoNamespace = YamlUtils.load(new File(filePathAbsolute), new TypeReference<Pod>() {
        });

        // for pod without namespace, assign a default namespace, e.g., "default", see `K8sUtils.K8S_NAMESPACE_DEFAULT`
        Pod podWithNamespace = (Pod) K8sUtils.getOrDefaultNamespacedResource(podNoNamespace);
        Assertions.assertTrue(StringUtils.isNotBlank(getNamespace(podWithNamespace)));

        // for pod with blank namespace, also assign a default namespace
        setNamespace(podNoNamespace, "    ");
        Pod podWithBlankNamespaceFix = (Pod) K8sUtils.getOrDefaultNamespacedResource(podNoNamespace);
        Assertions.assertTrue(StringUtils.isNotBlank(getNamespace(podWithBlankNamespaceFix)));

        // for a valid namespace, just keep it as it was
        setNamespace(podWithNamespace, "my-namespace");
        Pod podWithValidNamespaceChecked = (Pod) K8sUtils.getOrDefaultNamespacedResource(podWithNamespace);
        Assertions.assertEquals(getNamespace(podWithNamespace), getNamespace(podWithValidNamespaceChecked));
    }

    @Test
    public void testGetClient() {
        // for uninitialized K8sUtils, getClient throws TaskException
        K8sUtils k8sUtilsUninitialized = new K8sUtils();
        Assertions.assertThrows(TaskException.class, k8sUtilsUninitialized::getClient);

        // Mockito Stub
        K8sUtils k8sUtilsMockitoMocked = Mockito.mock(K8sUtils.class);
        KubernetesClient kubernetesClientMocked = Mockito.mock(KubernetesClient.class);
        Mockito.doNothing().when(k8sUtilsMockitoMocked).buildClient(Mockito.anyString());
        Mockito.when(k8sUtilsMockitoMocked.getClient()).thenReturn(kubernetesClientMocked);

        // for initialized with K8sUtils::buildClient, return non-null client
        String yamlK8sClientMocked = "<some-mock-yaml-to-build-k8s-client>";
        k8sUtilsMockitoMocked.buildClient(yamlK8sClientMocked);
        Assertions.assertNotNull(k8sUtilsMockitoMocked.getClient());

        Mockito.verify(k8sUtilsMockitoMocked).buildClient(Mockito.anyString());
        Mockito.verify(k8sUtilsMockitoMocked).getClient();
    }

    private void setNamespace(Pod pod, String namespace) {
        ObjectMeta podMetadata = Objects.requireNonNull(pod).getMetadata();
        podMetadata.setNamespace(namespace);
        pod.setMetadata(podMetadata);
    }

    private String getNamespace(Pod pod) {
        return Objects.requireNonNull(pod).getMetadata().getNamespace();
    }

}
