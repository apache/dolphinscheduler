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

package org.apache.dolphinscheduler.plugin.datasource.k8s;

import lombok.extern.slf4j.Slf4j;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

@Slf4j
public class K8sClientWrapper implements AutoCloseable {

    private KubernetesClient client;

    public K8sClientWrapper() {
    }

    public boolean checkConnect(String kubeConfigYaml, String namespace) {
        try {
            Config config = Config.fromKubeconfig(kubeConfigYaml);
            client = new KubernetesClientBuilder().withConfig(config).build();
            NamespaceList namespaceList = client.namespaces().list();
            if (!namespaceList.getItems().stream().anyMatch(ns -> ns.getMetadata().getName().equals(namespace))) {
                log.info("failed to connect to the K8S cluster, namespace not found\n");
                return false;
            }
            log.info("successfully connected to the K8S cluster");
            return true;
        } catch (Exception e) {
            log.info("failed to connect to the K8S cluster\n");
            return false;
        }
    }

    @Override
    public void close() throws Exception {

    }
}
