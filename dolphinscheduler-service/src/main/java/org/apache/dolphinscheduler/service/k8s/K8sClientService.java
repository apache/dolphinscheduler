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

package org.apache.dolphinscheduler.service.k8s;

import org.apache.dolphinscheduler.dao.entity.K8sNamespace;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Encapsulates all client-related operations, not involving the db
 */
@Component
public class K8sClientService {

    private static Yaml yaml = new Yaml();
    @Autowired
    private K8sManager k8sManager;

    public ResourceQuota upsertNamespaceAndResourceToK8s(K8sNamespace k8sNamespace, String yamlStr) {
        upsertNamespaceToK8s(k8sNamespace.getNamespace(), k8sNamespace.getK8s());
        return upsertNamespacedResourceToK8s(k8sNamespace, yamlStr);
    }

    public Optional<Namespace> deleteNamespaceToK8s(String name, String k8s) {
        Optional<Namespace> result = getNamespaceFromK8s(name, k8s);
        if (result.isPresent()) {
            KubernetesClient client = k8sManager.getK8sClient(k8s);
            Namespace body = new Namespace();
            ObjectMeta meta = new ObjectMeta();
            meta.setNamespace(name);
            meta.setName(name);
            body.setMetadata(meta);
            client.namespaces().delete(body);
        }
        return getNamespaceFromK8s(name, k8s);
    }

    private ResourceQuota upsertNamespacedResourceToK8s(K8sNamespace k8sNamespace, String yamlStr) {

        KubernetesClient client = k8sManager.getK8sClient(k8sNamespace.getK8s());

        //创建资源
        ResourceQuota queryExist = client.resourceQuotas()
                .inNamespace(k8sNamespace.getNamespace())
                .withName(k8sNamespace.getNamespace())
                .get();

        ResourceQuota body = yaml.loadAs(yamlStr, ResourceQuota.class);

        if (queryExist != null) {
            if (k8sNamespace.getLimitsCpu() == null && k8sNamespace.getLimitsMemory() == null) {
                client.resourceQuotas().inNamespace(k8sNamespace.getNamespace())
                        .withName(k8sNamespace.getNamespace())
                        .delete();
                return null;
            }
        }

        return client.resourceQuotas().inNamespace(k8sNamespace.getNamespace())
                .withName(k8sNamespace.getNamespace())
                .createOrReplace(body);
    }

    private Optional<Namespace> getNamespaceFromK8s(String name, String k8s) {
        NamespaceList listNamespace =
                k8sManager.getK8sClient(k8s).namespaces().list();

        Optional<Namespace> list =
                listNamespace.getItems().stream()
                        .filter((Namespace namespace) ->
                                namespace.getMetadata().getName().equals(name))
                        .findFirst();

        return list;
    }

    private Namespace upsertNamespaceToK8s(String name, String k8s) {
        Optional<Namespace> result = getNamespaceFromK8s(name, k8s);
        //if not exist create
        if (!result.isPresent()) {
            KubernetesClient client = k8sManager.getK8sClient(k8s);
            Namespace body = new Namespace();
            ObjectMeta meta = new ObjectMeta();
            meta.setNamespace(name);
            meta.setName(name);
            body.setMetadata(meta);
            return client.namespaces().create(body);
        }
        return result.get();
    }

}
