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

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;

public class K8sExecutor {

    private static String clusterServer;

    public void createJob(String namespace, Job job) {

        KubernetesClient client;

        try {
            client = K8sClientPool.getClient(clusterServer);

            client.batch()
                    .v1()
                    .jobs()
                    .inNamespace(namespace)
                    .create(job);

        } catch (Exception e) {
            throw new TaskException("fail to create job", e);
        }
    }

    public boolean jobExists(String namespace, String jobName) {

        KubernetesClient client;

        try {
            client = K8sClientPool.getClient(clusterServer);

            Job job = client.batch()
                    .v1()
                    .jobs()
                    .inNamespace(namespace)
                    .withName(jobName)
                    .get();

            return job != null;
        } catch (Exception e) {
            throw new TaskException("fail to check job: ", e);
        }
    }

    public void deleteJob(String namespace, String jobName) {

        KubernetesClient client;

        try {
            client = K8sClientPool.getClient(clusterServer);

            client.batch()
                    .v1()
                    .jobs()
                    .inNamespace(namespace)
                    .withName(jobName)
                    .delete();
        } catch (Exception e) {
            throw new TaskException("fail to delete job", e);
        }
    }

    public void buildClient(String configYml) {
        clusterServer = getMasterUrl(configYml);
    }

    private static String getMasterUrl(String configYml) {
        return Config.fromKubeconfig(configYml).getMasterUrl();
    }

}
