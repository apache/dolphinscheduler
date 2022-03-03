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

package org.apache.dolphinscheduler.plugin.task.api.k8s;
import io.fabric8.kubernetes.api.model.batch.v1.Job;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.CLUSTER;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.NAMESPACE_NAME;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.k8s.impl.K8sTaskExecutor;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class K8sTaskExecutorTest {
    private K8sTaskExecutor k8sTaskExecutor = null;
    private K8sTaskMainParameters k8sTaskMainParameters = null;
    private final String image = "ds-dev";
    private final String namespace = "{\"name\":\"default\",\"cluster\":\"lab\"}";
    private final double minCpuCores = 2;
    private final double minMemorySpace = 10;
    private final int taskInstanceId = 1000;
    private final String taskName = "k8s_task_test";

    @Before
    public void before() {
        TaskExecutionContext taskRequest = new TaskExecutionContext();
        taskRequest.setTaskInstanceId(taskInstanceId);
        taskRequest.setTaskName(taskName);
        Map<String,String> namespace = JSONUtils.toMap(this.namespace);
        String namespaceName = namespace.get(NAMESPACE_NAME);
        String clusterName = namespace.get(CLUSTER);
        k8sTaskExecutor = new K8sTaskExecutor(null,taskRequest);
        k8sTaskMainParameters = new K8sTaskMainParameters();
        k8sTaskMainParameters.setImage(image);
        k8sTaskMainParameters.setNamespaceName(namespaceName);
        k8sTaskMainParameters.setClusterName(clusterName);
        k8sTaskMainParameters.setMinCpuCores(minCpuCores);
        k8sTaskMainParameters.setMinMemorySpace(minMemorySpace);
    }

    @Test
    public void testBuildK8sJobNormal() {
        Job job = k8sTaskExecutor.buildK8sJob(k8sTaskMainParameters);
        String jobStr = "Job(apiVersion=batch/v1, kind=Job, metadata=ObjectMeta(annotations=null, clusterName=null, creationTimestamp=null, deletionGracePeriodSeconds=null, deletionTimestamp=null, finalizers=[], generateName=null, generation=null, labels={k8s.cn/layer=batch, k8s.cn/name=k8s_task_test-1000}, managedFields=[], name=k8s_task_test-1000, namespace=default, ownerReferences=[], resourceVersion=null, selfLink=null, uid=null, additionalProperties={}), spec=JobSpec(activeDeadlineSeconds=null, backoffLimit=0, completionMode=null, completions=null, manualSelector=null, parallelism=null, selector=null, suspend=null, template=PodTemplateSpec(metadata=null, spec=PodSpec(activeDeadlineSeconds=null, affinity=null, automountServiceAccountToken=null, containers=[Container(args=[], command=[], env=[EnvVar(name=taskInstanceId, value=1000, valueFrom=null, additionalProperties={})], envFrom=[], image=ds-dev, imagePullPolicy=Always, lifecycle=null, livenessProbe=null, name=k8s_task_test-1000, ports=[], readinessProbe=null, resources=ResourceRequirements(limits={memory=20.0Mi, cpu=4.0}, requests={memory=10.0Mi, cpu=2.0}, additionalProperties={}), securityContext=null, startupProbe=null, stdin=null, stdinOnce=null, terminationMessagePath=null, terminationMessagePolicy=null, tty=null, volumeDevices=[], volumeMounts=[], workingDir=null, additionalProperties={})], dnsConfig=null, dnsPolicy=null, enableServiceLinks=null, ephemeralContainers=[], hostAliases=[], hostIPC=null, hostNetwork=null, hostPID=null, hostname=null, imagePullSecrets=[], initContainers=[], nodeName=null, nodeSelector=null, overhead=null, preemptionPolicy=null, priority=null, priorityClassName=null, readinessGates=[], restartPolicy=Never, runtimeClassName=null, schedulerName=null, securityContext=null, serviceAccount=null, serviceAccountName=null, setHostnameAsFQDN=null, shareProcessNamespace=null, subdomain=null, terminationGracePeriodSeconds=null, tolerations=[], topologySpreadConstraints=[], volumes=[], additionalProperties={}), additionalProperties={}), ttlSecondsAfterFinished=300, additionalProperties={}), status=null, additionalProperties={})";
        Assert.assertEquals(jobStr, job.toString());
    }
}
