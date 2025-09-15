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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LOG_LINES;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.k8s.KubernetesClientPool;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

@Slf4j
public class K8sUtils {

    public void createJob(String configYaml, String namespace, Job job) {
        String clusterId = KubernetesClientPool.getInstance().getClusterId(configYaml);
        KubernetesClient client = null;
        try {
            client = KubernetesClientPool.getInstance().getClient(clusterId, configYaml);
            client.batch()
                    .v1()
                    .jobs()
                    .inNamespace(namespace)
                    .create(job);
        } catch (Exception e) {
            throw new TaskException("fail to create job", e);
        } finally {
            if (client != null) {
                KubernetesClientPool.getInstance().returnClient(clusterId, client);
            }
        }
    }

    public void deleteJob(String configYaml, String jobName, String namespace) {
        String clusterId = KubernetesClientPool.getInstance().getClusterId(configYaml);
        KubernetesClient client = null;
        try {
            client = KubernetesClientPool.getInstance().getClient(clusterId, configYaml);
            client.batch()
                    .v1()
                    .jobs()
                    .inNamespace(namespace)
                    .withName(jobName)
                    .delete();
        } catch (Exception e) {
            throw new TaskException("fail to delete job", e);
        } finally {
            if (client != null) {
                KubernetesClientPool.getInstance().returnClient(clusterId, client);
            }
        }
    }

    public Boolean jobExist(String configYaml, String jobName, String namespace) {
        String clusterId = KubernetesClientPool.getInstance().getClusterId(configYaml);
        KubernetesClient client = null;
        try {
            client = KubernetesClientPool.getInstance().getClient(clusterId, configYaml);
            Job job = client.batch().v1().jobs().inNamespace(namespace).withName(jobName).get();
            return job != null;
        } catch (Exception e) {
            throw new TaskException("fail to check job: ", e);
        } finally {
            if (client != null) {
                KubernetesClientPool.getInstance().returnClient(clusterId, client);
            }
        }
    }

    public Watch createBatchJobWatcher(String configYaml, String jobName, Watcher<Job> watcher) {
        String clusterId = KubernetesClientPool.getInstance().getClusterId(configYaml);
        KubernetesClient client = null;
        try {
            client = KubernetesClientPool.getInstance().getClient(clusterId, configYaml);
            return client.batch()
                    .v1()
                    .jobs()
                    .withName(jobName)
                    .watch(watcher);
        } catch (Exception e) {
            throw new TaskException("fail to register batch job watcher", e);
        } finally {
            if (client != null) {
                log.debug("createBatchJobWatcher does not return client immediately, caller should manage client lifecycle");
            }
        }
    }

    public String getPodLog(String configYaml, String jobName, String namespace) {
        String clusterId = KubernetesClientPool.getInstance().getClusterId(configYaml);
        KubernetesClient client = null;
        try {
            client = KubernetesClientPool.getInstance().getClient(clusterId, configYaml);
            List<Pod> podList = client.pods().inNamespace(namespace).list().getItems();
            String podName = null;
            for (Pod pod : podList) {
                podName = pod.getMetadata().getName();
                if (podName.contains("-") && jobName.equals(podName.substring(0, podName.lastIndexOf("-")))) {
                    break;
                }
            }
            return client.pods().inNamespace(namespace)
                    .withName(podName)
                    .tailingLines(LOG_LINES)
                    .getLog(Boolean.TRUE);
        } catch (Exception e) {
            log.error("fail to getPodLog", e);
            log.error("response bodies : {}", e.getMessage());
        } finally {
            if (client != null) {
                KubernetesClientPool.getInstance().returnClient(clusterId, client);
            }
        }
        return null;
    }

}
