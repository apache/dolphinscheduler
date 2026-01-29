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

package org.apache.dolphinscheduler.api.k8s;

import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.service.utils.ClusterConfUtils;

import java.util.Hashtable;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

/**
 * use multiple environment feature
 */
@Component
@Slf4j
public class K8sManager {

    /**
     * cache k8s client
     */
    private static Map<Long, KubernetesClient> clientMap = new Hashtable<>();

    @Autowired
    private ClusterMapper clusterMapper;

    /**
     * get k8s client for api use
     *
     * @param clusterCode
     * @return
     */
    public synchronized KubernetesClient getK8sClient(Long clusterCode) {
        if (null == clusterCode) {
            return null;
        }

        return getAndUpdateK8sClient(clusterCode, false);
    }

    /**
     * @param clusterCode
     * @return new client if need updated
     */
    public synchronized KubernetesClient getAndUpdateK8sClient(Long clusterCode,
                                                               boolean update) {
        if (null == clusterCode) {
            return null;
        }

        if (update) {
            deleteK8sClientInner(clusterCode);
        }

        if (clientMap.containsKey(clusterCode)) {
            return clientMap.get(clusterCode);
        } else {
            createK8sClientInner(clusterCode);
        }
        return clientMap.get(clusterCode);
    }

    private void deleteK8sClientInner(Long clusterCode) {
        if (clusterCode == null) {
            return;
        }
        Cluster cluster = clusterMapper.queryByClusterCode(clusterCode);
        if (cluster == null) {
            return;
        }
        KubernetesClient client = clientMap.get(clusterCode);
        if (client != null) {
            client.close();
        }
    }

    private void createK8sClientInner(Long clusterCode) {
        Cluster cluster = clusterMapper.queryByClusterCode(clusterCode);
        if (cluster == null) {
            return;
        }

        String k8sConfig = ClusterConfUtils.getK8sConfig(cluster.getConfig());
        if (k8sConfig != null) {
            KubernetesClient client = null;
            try {
                client = getClient(k8sConfig);
                clientMap.put(clusterCode, client);
            } catch (Exception e) {
                log.error("cluster code ={},fail to get k8s ApiClient:  {}", clusterCode, e.getMessage());
                throw new RuntimeException("fail to get k8s ApiClient:" + e.getMessage());
            }
        }
    }

    private KubernetesClient getClient(String configYaml) throws RuntimeException {
        try {
            Config config = Config.fromKubeconfig(configYaml);
            return new KubernetesClientBuilder().withConfig(config).build();
        } catch (Exception e) {
            log.error("Fail to get k8s ApiClient", e);
            throw new RuntimeException("fail to get k8s ApiClient:" + e.getMessage());
        }
    }

}
