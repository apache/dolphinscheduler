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

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.k8s.AbstractK8sOperation;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.YamlContent;

import java.util.concurrent.CountDownLatch;

public class ServiceOperation implements AbstractK8sOperation {

    private KubernetesClient client;

    public ServiceOperation(KubernetesClient client) {
        this.client = client;
    }
    @Override
    public HasMetadata buildMetadata(YamlContent yamlContent) {
        return client.services().load(yamlContent.getYaml()).get();
    }

    @Override
    public void createOrReplaceMetadata(HasMetadata metadata) {
        client.services().createOrReplace((Service) metadata);
    }

    @Override
    public int getState(HasMetadata hasMetadata) {
        return 0;
    }

    @Override
    public Watch createBatchWatcher( CountDownLatch countDownLatch, TaskResponse taskResponse, HasMetadata hasMetadata, TaskExecutionContext taskRequest) {
        return null;
    }

    @Override
    public LogWatch getLogWatcher(String labelValue, String namespace) {
        return null;
    }

}
