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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertDoesNotThrow;
import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowsServiceException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.ClusterDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.k8s.K8sManager;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * cluster service test
 */
@ExtendWith(MockitoExtension.class)
public class ClusterServiceTest {

    public static final Logger logger = LoggerFactory.getLogger(ClusterServiceTest.class);

    @InjectMocks
    private ClusterServiceImpl clusterService;

    @Mock
    private ClusterMapper clusterMapper;

    @Mock
    private K8sNamespaceMapper k8sNamespaceMapper;

    @Mock
    private K8sManager k8sManager;

    public static final String testUserName = "clusterServerTest";

    public static final String clusterName = "Env1";

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void after() {
    }

    @Test
    public void testCreateCluster() {
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM, () -> {
            User loginUser = getGeneralUser();
            clusterService.createCluster(loginUser, clusterName, getConfig(), getDesc());
        });

        assertThrowsServiceException(Status.CLUSTER_CONFIG_IS_NULL, () -> {
            User loginUser = getAdminUser();
            clusterService.createCluster(loginUser, clusterName, "", getDesc());
        });

        final User loginUser = getAdminUser();
        assertThrowsServiceException(Status.CLUSTER_NAME_IS_NULL,
                () -> clusterService.createCluster(loginUser, "", getConfig(), getDesc()));

        when(clusterMapper.queryByClusterName(clusterName)).thenReturn(getCluster());
        assertThrowsServiceException(Status.CLUSTER_NAME_EXISTS,
                () -> clusterService.createCluster(loginUser, clusterName, getConfig(), getDesc()));

        when(clusterMapper.insert(Mockito.any(Cluster.class))).thenReturn(1);
        Assertions.assertDoesNotThrow(
                () -> clusterService.createCluster(loginUser, "testName", "testConfig", "testDesc"));
    }

    @Test
    public void testCheckParams() {
        assertDoesNotThrow(() -> clusterService.checkParams(clusterName, getConfig()));
        assertThrowsServiceException(Status.CLUSTER_NAME_IS_NULL, () -> clusterService.checkParams("", getConfig()));
        assertThrowsServiceException(Status.CLUSTER_CONFIG_IS_NULL, () -> clusterService.checkParams(clusterName, ""));
    }

    @Test
    public void testUpdateClusterByCode() {
        final User loginUser = getGeneralUser();
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM,
                () -> clusterService.updateClusterByCode(loginUser, 1L, clusterName, getConfig(), getDesc()));

        final User adminUser = getAdminUser();
        assertThrowsServiceException(Status.CLUSTER_CONFIG_IS_NULL,
                () -> clusterService.updateClusterByCode(adminUser, 1L, clusterName, "", getDesc()));
        assertThrowsServiceException(Status.CLUSTER_NAME_IS_NULL,
                () -> clusterService.updateClusterByCode(adminUser, 1L, "", getConfig(), getDesc()));
        assertThrowsServiceException(Status.CLUSTER_NOT_EXISTS,
                () -> clusterService.updateClusterByCode(adminUser, 2L, clusterName, getConfig(), getDesc()));

        when(clusterMapper.queryByClusterName(clusterName)).thenReturn(getCluster());
        assertThrowsServiceException(Status.CLUSTER_NAME_EXISTS,
                () -> clusterService.updateClusterByCode(adminUser, 2L, clusterName, getConfig(), getDesc()));

        when(clusterMapper.updateById(Mockito.any(Cluster.class))).thenReturn(1);
        when(clusterMapper.queryByClusterCode(1L)).thenReturn(getCluster());
        Cluster cluster = clusterService.updateClusterByCode(adminUser, 1L, "testName", getConfig(), "test");
        assertNotNull(cluster);
    }

    @Test
    public void testQueryAllClusterList() {
        when(clusterMapper.queryAllClusterList()).thenReturn(Lists.newArrayList(getCluster()));
        List<ClusterDto> clusterDtos = clusterService.queryAllClusterList();
        Assertions.assertEquals(clusterDtos.size(), 1);
    }

    @Test
    public void testQueryClusterListPaging() {
        IPage<Cluster> page = new Page<>(1, 10);
        page.setRecords(getList());
        page.setTotal(1L);
        when(clusterMapper.queryClusterListPaging(Mockito.any(Page.class), Mockito.eq(clusterName))).thenReturn(page);

        PageInfo<ClusterDto> clusterDtoPageInfo = clusterService.queryClusterListPaging(1, 10, clusterName);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(clusterDtoPageInfo.getTotalList()));
    }

    @Test
    public void testQueryClusterByName() {
        when(clusterMapper.queryByClusterName(clusterName)).thenReturn(null);
        assertThrowsServiceException(Status.QUERY_CLUSTER_BY_NAME_ERROR,
                () -> clusterService.queryClusterByName(clusterName));

        when(clusterMapper.queryByClusterName(clusterName)).thenReturn(getCluster());
        ClusterDto clusterDto = clusterService.queryClusterByName(clusterName);
        assertNotNull(clusterDto);
    }

    @Test
    public void testQueryClusterByCode() {
        when(clusterMapper.queryByClusterCode(1L)).thenReturn(null);
        assertThrowsServiceException(Status.QUERY_CLUSTER_BY_CODE_ERROR, () -> clusterService.queryClusterByCode(1L));

        when(clusterMapper.queryByClusterCode(1L)).thenReturn(getCluster());
        ClusterDto clusterDto = clusterService.queryClusterByCode(1L);
        assertNotNull(clusterDto);
    }

    @Test
    public void testDeleteClusterByCode() {
        assertThrowsServiceException(Status.USER_NO_OPERATION_PERM, () -> {
            User loginUser = getGeneralUser();
            clusterService.deleteClusterByCode(loginUser, 1L);
        });

        final User adminUser = getAdminUser();
        when(clusterMapper.deleteByCode(1L)).thenReturn(1);
        assertDoesNotThrow(() -> clusterService.deleteClusterByCode(adminUser, 1L));

        when(k8sNamespaceMapper.selectCount(Mockito.any())).thenReturn(1L);
        assertThrowsServiceException(Status.DELETE_CLUSTER_RELATED_NAMESPACE_EXISTS,
                () -> clusterService.deleteClusterByCode(adminUser, 1L));
    }

    @Test
    public void testVerifyCluster() {
        assertThrowsServiceException(Status.CLUSTER_NAME_IS_NULL, () -> clusterService.verifyCluster(""));

        when(clusterMapper.queryByClusterName(clusterName)).thenReturn(getCluster());
        assertThrowsServiceException(Status.CLUSTER_NAME_EXISTS, () -> clusterService.verifyCluster(clusterName));
    }

    private Cluster getCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(1);
        cluster.setCode(1L);
        cluster.setName(clusterName);
        cluster.setConfig(getConfig());
        cluster.setDescription(getDesc());
        cluster.setOperator(1);
        return cluster;
    }

    /**
     * create an cluster description
     */
    private String getDesc() {
        return "create an cluster to test ";
    }

    /**
     * create an cluster config
     */
    private String getConfig() {
        return "{\"k8s\":\"apiVersion: v1\\nclusters:\\n- cluster:\\n    certificate-authority-data: LS0tLS1CRUdJTiBDRJUSUZJQ0FURS0tLS0tCg==\\n    server: https:\\/\\/127.0.0.1:6443\\n  name: kubernetes\\ncontexts:\\n- context:\\n    cluster: kubernetes\\n    user: kubernetes-admin\\n  name: kubernetes-admin@kubernetes\\ncurrent-context: kubernetes-admin@kubernetes\\nkind: Config\\npreferences: {}\\nusers:\\n- name: kubernetes-admin\\n  user:\\n    client-certificate-data: LS0tLS1CRUdJTiBDRVJJ0cEhYYnBLRVktLS0tLQo=\"}\n";
    }

    /**
     * create general user
     */
    private User getGeneralUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName(testUserName);
        loginUser.setId(1);
        return loginUser;
    }

    /**
     * create admin user
     */
    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setUserName(testUserName);
        loginUser.setId(1);
        return loginUser;
    }

    private List<Cluster> getList() {
        List<Cluster> list = new ArrayList<>();
        list.add(getCluster());
        return list;
    }
}
