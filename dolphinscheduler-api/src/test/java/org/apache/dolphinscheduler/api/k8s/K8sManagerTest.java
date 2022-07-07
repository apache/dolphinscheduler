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
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import io.fabric8.kubernetes.client.KubernetesClient;

@RunWith(MockitoJUnitRunner.Silent.class)
public class K8sManagerTest {

    @InjectMocks
    private K8sManager k8sManager;

    @Mock
    private ClusterMapper clusterMapper;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getK8sClient() throws RemotingException {
        Mockito.when(clusterMapper.selectList(Mockito.any())).thenReturn(getClusterList());

        KubernetesClient result = k8sManager.getK8sClient(1L);
        Assert.assertNull(result);
        result = k8sManager.getK8sClient(null);
        Assert.assertNull(result);
    }

    private Cluster getCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(1);
        cluster.setCode(1L);
        cluster.setName("cluster");
        cluster.setConfig("{\"k8s\":\"k8s config yaml\"}");
        return cluster;
    }

    private List<Cluster> getClusterList() {
        List<Cluster> clusterList = new ArrayList<>();
        clusterList.add(getCluster());
        return clusterList;
    }
}