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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.k8s.K8sClientService;
import org.apache.dolphinscheduler.api.service.impl.K8SNamespaceServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class K8SNamespaceServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(K8SNamespaceServiceTest.class);

    @InjectMocks
    private K8SNamespaceServiceImpl k8sNamespaceService;

    @Mock
    private K8sNamespaceMapper k8sNamespaceMapper;

    @Mock
    private K8sClientService k8sClientService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ClusterMapper clusterMapper;

    private String namespace = "default";
    private Long clusterCode = 100L;

    @BeforeEach
    public void setUp() throws Exception {
        Mockito.when(
                k8sClientService.upsertNamespaceAndResourceToK8s(Mockito.any(K8sNamespace.class), Mockito.anyString()))
                .thenReturn(null);
        Mockito.when(k8sClientService.deleteNamespaceToK8s(Mockito.anyString(), Mockito.anyLong())).thenReturn(null);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void queryListPaging() {
        IPage<K8sNamespace> page = new Page<>(1, 10);
        page.setTotal(1L);
        page.setRecords(getNamespaceList());
        Mockito.when(k8sNamespaceMapper.queryK8sNamespacePaging(Mockito.any(Page.class), Mockito.eq(namespace)))
                .thenReturn(page);
        Result result = k8sNamespaceService.queryListPaging(getLoginUser(), namespace, 1, 10);
        logger.info(result.toString());
        PageInfo<K8sNamespace> pageInfo = (PageInfo<K8sNamespace>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));
    }

    @Test
    public void createK8sNamespace() {
        // namespace is null
        Map<String, Object> result =
                k8sNamespaceService.createK8sNamespace(getLoginUser(), null, clusterCode, 10.0, 100);
        logger.info(result.toString());
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));
        // k8s is null
        result = k8sNamespaceService.createK8sNamespace(getLoginUser(), namespace, null, 10.0, 100);
        logger.info(result.toString());
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));
        // correct
        Mockito.when(clusterMapper.queryByClusterCode(Mockito.anyLong())).thenReturn(getCluster());
        result = k8sNamespaceService.createK8sNamespace(getLoginUser(), namespace, clusterCode, 10.0, 100);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        // null limit cpu and mem
        result = k8sNamespaceService.createK8sNamespace(getLoginUser(), namespace, clusterCode, null, null);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void updateK8sNamespace() {
        Mockito.when(k8sNamespaceMapper.selectById(1)).thenReturn(getNamespace());

        Map<String, Object> result = k8sNamespaceService.updateK8sNamespace(getLoginUser(), 1, null, null, null);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        result = k8sNamespaceService.updateK8sNamespace(getLoginUser(), 1, null, -1.0, 100);
        logger.info(result.toString());
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, result.get(Constants.STATUS));

        result = k8sNamespaceService.updateK8sNamespace(getLoginUser(), 1, null, 1.0, 100);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void verifyNamespaceK8s() {

        Mockito.when(k8sNamespaceMapper.existNamespace(namespace, clusterCode)).thenReturn(true);

        // namespace null
        Result result = k8sNamespaceService.verifyNamespaceK8s(null, clusterCode);
        logger.info(result.toString());
        Assertions.assertEquals(result.getCode().intValue(), Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode());

        // k8s null
        result = k8sNamespaceService.verifyNamespaceK8s(namespace, null);
        logger.info(result.toString());
        Assertions.assertEquals(result.getCode().intValue(), Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode());

        // exist
        result = k8sNamespaceService.verifyNamespaceK8s(namespace, clusterCode);
        logger.info(result.toString());
        Assertions.assertEquals(result.getCode().intValue(), Status.K8S_NAMESPACE_EXIST.getCode());

        // not exist
        result = k8sNamespaceService.verifyNamespaceK8s(namespace, 9999L);
        logger.info(result.toString());
        Assertions.assertEquals(result.getCode().intValue(), Status.SUCCESS.getCode());
    }

    @Test
    public void deleteNamespaceById() {
        Mockito.when(k8sNamespaceMapper.deleteById(Mockito.<Serializable>any())).thenReturn(1);
        Mockito.when(k8sNamespaceMapper.selectById(1)).thenReturn(getNamespace());

        Map<String, Object> result = k8sNamespaceService.deleteNamespaceById(getLoginUser(), 1);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testQueryAuthorizedNamespace() {
        Mockito.when(k8sNamespaceMapper.queryAuthedNamespaceListByUserId(2)).thenReturn(getNamespaceList());

        User loginUser = getLoginUser();

        // test admin user
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = k8sNamespaceService.queryAuthorizedNamespace(loginUser, 2);
        logger.info(result.toString());
        List<K8sNamespace> namespaces = (List<K8sNamespace>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(namespaces));

        // test non-admin user
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setId(3);
        result = k8sNamespaceService.queryAuthorizedNamespace(loginUser, 2);
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        namespaces = (List<K8sNamespace>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isEmpty(namespaces));
    }

    @Test
    public void testQueryUnAuthorizedNamespace() {
        Mockito.when(k8sNamespaceMapper.queryAuthedNamespaceListByUserId(2)).thenReturn(new ArrayList<>());
        Mockito.when(k8sNamespaceMapper.selectList(Mockito.any())).thenReturn(getNamespaceList());

        // test admin user
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = k8sNamespaceService.queryUnauthorizedNamespace(loginUser, 2);
        logger.info(result.toString());
        List<K8sNamespace> namespaces = (List<K8sNamespace>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(namespaces));

        // test non-admin user
        loginUser.setId(2);
        loginUser.setUserType(UserType.GENERAL_USER);
        result = k8sNamespaceService.queryUnauthorizedNamespace(loginUser, 3);
        logger.info(result.toString());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));
        namespaces = (List<K8sNamespace>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isEmpty(namespaces));
    }

    @Test
    public void testQueryNamespaceAvailable() {
        List<K8sNamespace> k8sNamespaces = new ArrayList<>();
        K8sNamespace k8sNamespace = new K8sNamespace();
        k8sNamespace.setClusterCode(1L);
        k8sNamespaces.add(k8sNamespace);

        List<Cluster> clusters = new ArrayList<>();
        Cluster cluster = new Cluster();
        cluster.setCode(1L);
        cluster.setName("test");
        clusters.add(cluster);

        Mockito.when(k8sNamespaceMapper.selectList(Mockito.any())).thenReturn(k8sNamespaces);
        Mockito.when(clusterMapper.queryAllClusterList()).thenReturn(clusters);
        List<K8sNamespace> result = k8sNamespaceService.queryNamespaceAvailable(getLoginUser());
        Assertions.assertEquals(result.get(0).getClusterName(), cluster.getName());
    }

    private User getLoginUser() {

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setId(99999999);
        return loginUser;
    }

    private K8sNamespace getNamespace() {
        K8sNamespace k8sNamespace = new K8sNamespace();
        k8sNamespace.setId(1);
        k8sNamespace.setClusterCode(clusterCode);
        k8sNamespace.setNamespace(namespace);
        return k8sNamespace;
    }

    private List<K8sNamespace> getNamespaceList() {
        List<K8sNamespace> k8sNamespaceList = new ArrayList<>();
        k8sNamespaceList.add(getNamespace());
        return k8sNamespaceList;
    }

    private Cluster getCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(1);
        cluster.setCode(1L);
        cluster.setName("clusterName");
        cluster.setConfig("{}");
        cluster.setOperator(1);
        return cluster;
    }
}
