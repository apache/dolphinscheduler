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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.fabric8.kubernetes.client.KubernetesClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class K8sManagerTest {

    @InjectMocks
    private K8sManager k8sManager;

    @Mock
    private ClusterMapper clusterMapper;

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void getK8sClient() {
        Mockito.when(clusterMapper.selectList(Mockito.any())).thenReturn(getClusterList());

        KubernetesClient result = k8sManager.getK8sClient(1L);
        Assertions.assertNull(result);
        result = k8sManager.getK8sClient(null);
        Assertions.assertNull(result);
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
