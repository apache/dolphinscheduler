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

package org.apache.dolphinscheduler.plugin.datasource.k8s;

import org.apache.dolphinscheduler.plugin.datasource.k8s.param.K8sConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.k8s.param.K8sDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.k8s.param.K8sDataSourceProcessor;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class K8sDataSourceProcessorTest {

    private K8sDataSourceProcessor k8sDataSourceProcessor;

    private String connectJson =
            "{\"namespace\":\"namespace\",\"kubeConfig\":\"kubeConfig\"}";

    @BeforeEach
    public void init() {
        k8sDataSourceProcessor = new K8sDataSourceProcessor();
    }

    @Test
    void testCheckDatasourceParam() {
        K8sDataSourceParamDTO k8sDataSourceParamDTO = new K8sDataSourceParamDTO();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> k8sDataSourceProcessor.checkDatasourceParam(k8sDataSourceParamDTO));
        k8sDataSourceParamDTO.setNamespace("namespace");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> k8sDataSourceProcessor.checkDatasourceParam(k8sDataSourceParamDTO));
        k8sDataSourceParamDTO.setKubeConfig("kubeConfig");
        Assertions
                .assertDoesNotThrow(
                        () -> k8sDataSourceProcessor.checkDatasourceParam(k8sDataSourceParamDTO));
    }

    @Test
    void testGetDatasourceUniqueId() {
        K8sConnectionParam k8sConnectionParam = new K8sConnectionParam();
        k8sConnectionParam.setNamespace("namespace");
        k8sConnectionParam.setKubeConfig("kubeConfig");
        Assertions.assertEquals("k8s@kubeConfig@namespace",
                k8sDataSourceProcessor.getDatasourceUniqueId(k8sConnectionParam, DbType.K8S));

    }

    @Test
    void testCreateDatasourceParamDTO() {
        K8sDataSourceParamDTO k8sDataSourceParamDTO =
                (K8sDataSourceParamDTO) k8sDataSourceProcessor.createDatasourceParamDTO(connectJson);
        Assertions.assertEquals("namespace", k8sDataSourceParamDTO.getNamespace());
        Assertions.assertEquals("kubeConfig", k8sDataSourceParamDTO.getKubeConfig());
    }

    @Test
    void testCreateConnectionParams() {
        K8sDataSourceParamDTO k8sDataSourceParamDTO =
                (K8sDataSourceParamDTO) k8sDataSourceProcessor.createDatasourceParamDTO(connectJson);
        K8sConnectionParam k8sConnectionParam =
                k8sDataSourceProcessor.createConnectionParams(k8sDataSourceParamDTO);
        Assertions.assertEquals("namespace", k8sConnectionParam.getNamespace());
        Assertions.assertEquals("kubeConfig", k8sConnectionParam.getKubeConfig());
    }

    @Test
    void testTestConnection() {
        K8sDataSourceParamDTO k8sDataSourceParamDTO =
                (K8sDataSourceParamDTO) k8sDataSourceProcessor.createDatasourceParamDTO(connectJson);
        K8sConnectionParam connectionParam =
                k8sDataSourceProcessor.createConnectionParams(k8sDataSourceParamDTO);
        Assertions.assertFalse(k8sDataSourceProcessor.checkDataSourceConnectivity(connectionParam));

        try (
                MockedConstruction<K8sClientWrapper> k8sClientWrapperMockedConstruction =
                        Mockito.mockConstruction(K8sClientWrapper.class, (mock, context) -> {
                            Mockito.when(
                                    mock.checkConnect(connectionParam.getKubeConfig(), connectionParam.getNamespace()))
                                    .thenReturn(true);
                        })) {
            Assertions.assertTrue(k8sDataSourceProcessor.checkDataSourceConnectivity(connectionParam));
        }

    }
}
