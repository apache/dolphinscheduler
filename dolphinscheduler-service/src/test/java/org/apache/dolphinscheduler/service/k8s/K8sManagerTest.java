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

import org.apache.dolphinscheduler.dao.entity.K8s;
import org.apache.dolphinscheduler.dao.mapper.K8sMapper;

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
    private K8sMapper k8sMapper;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getK8sClient() {
        Mockito.when(k8sMapper.selectList(Mockito.any())).thenReturn(getK8sList());

        KubernetesClient result = k8sManager.getK8sClient("must null");
        Assert.assertNull(result);
        result = k8sManager.getK8sClient(null);
        Assert.assertNull(result);
    }

    private K8s getK8s() {
        K8s k8s = new K8s();
        k8s.setId(1);
        k8s.setK8sName("default");
        k8s.setK8sConfig("k8s config");
        return k8s;
    }

    private List<K8s> getK8sList() {
        List<K8s> k8sList = new ArrayList<>();
        k8sList.add(getK8s());
        return k8sList;
    }
}