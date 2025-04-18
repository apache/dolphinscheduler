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

package org.apache.dolphinscheduler.plugin.task.api.k8s.pool;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.fabric8.kubernetes.client.Client;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

public class K8sClientPool {

    private static final ConcurrentMap<String, KubernetesClient> clientMap = new ConcurrentHashMap<>();

    public static KubernetesClient getClient(String configYml) {

        String server = getMasterUrl(configYml);

        return clientMap.computeIfAbsent(
                server,
                key -> createClient(configYml));
    }

    public static void removeClient(String server) {
        KubernetesClient client = clientMap.remove(server);
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                throw new RuntimeException("fail to remove client",e);
            }
        }
    }

    public static void shutdown() {
        clientMap.values().forEach(Client::close);
        clientMap.clear();
    }

    private static String getMasterUrl(String configYml) {
        return Config.fromKubeconfig(configYml).getMasterUrl();
    }

    private static KubernetesClient createClient(String configYml) {
        Config config = Config.fromKubeconfig(configYml);

        return new KubernetesClientBuilder()
                .withConfig(config)
                .build();
    }

    private static boolean clientExist(String server) {
        return clientMap.containsKey(server);
    }

}
