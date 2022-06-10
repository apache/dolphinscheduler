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
import org.apache.dolphinscheduler.api.k8s.K8sManager;
import org.apache.dolphinscheduler.api.service.impl.ClusterServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * cluster service test
 */
@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void setUp(){
    }

    @After
    public void after(){
    }

    @Test
    public void testCreateCluster() {
        User loginUser = getGeneralUser();
        Map<String, Object> result = clusterService.createCluster(loginUser,clusterName,getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        loginUser = getAdminUser();
        result = clusterService.createCluster(loginUser,clusterName,"",getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.CLUSTER_CONFIG_IS_NULL, result.get(Constants.STATUS));

        result = clusterService.createCluster(loginUser,"",getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.CLUSTER_NAME_IS_NULL, result.get(Constants.STATUS));

        Mockito.when(clusterMapper.queryByClusterName(clusterName)).thenReturn(getCluster());
        result = clusterService.createCluster(loginUser,clusterName,getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.CLUSTER_NAME_EXISTS, result.get(Constants.STATUS));

        Mockito.when(clusterMapper.insert(Mockito.any(Cluster.class))).thenReturn(1);
        result = clusterService.createCluster(loginUser,"testName","testConfig","testDesc");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testCheckParams() {
        Map<String, Object> result = clusterService.checkParams(clusterName,getConfig());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        result = clusterService.checkParams("",getConfig());
        Assert.assertEquals(Status.CLUSTER_NAME_IS_NULL, result.get(Constants.STATUS));
        result = clusterService.checkParams(clusterName,"");
        Assert.assertEquals(Status.CLUSTER_CONFIG_IS_NULL, result.get(Constants.STATUS));
    }

    @Test
    public void testUpdateClusterByCode() throws RemotingException {
        User loginUser = getGeneralUser();
        Map<String, Object> result = clusterService.updateClusterByCode(loginUser,1L,clusterName,getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        loginUser = getAdminUser();
        result = clusterService.updateClusterByCode(loginUser,1L,clusterName,"",getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.CLUSTER_CONFIG_IS_NULL, result.get(Constants.STATUS));

        result = clusterService.updateClusterByCode(loginUser,1L,"",getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.CLUSTER_NAME_IS_NULL, result.get(Constants.STATUS));

        result = clusterService.updateClusterByCode(loginUser,2L,clusterName,getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.CLUSTER_NOT_EXISTS, result.get(Constants.STATUS));

        Mockito.when(clusterMapper.queryByClusterName(clusterName)).thenReturn(getCluster());
        result = clusterService.updateClusterByCode(loginUser,2L,clusterName,getConfig(),getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.CLUSTER_NAME_EXISTS, result.get(Constants.STATUS));

        Mockito.when(clusterMapper.updateById(Mockito.any(Cluster.class))).thenReturn(1);
        Mockito.when(clusterMapper.queryByClusterCode(1L)).thenReturn(getCluster());

        result = clusterService.updateClusterByCode(loginUser,1L,"testName",getConfig(),"test");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

    }

    @Test
    public void testQueryAllClusterList() {
        Mockito.when(clusterMapper.queryAllClusterList()).thenReturn(Lists.newArrayList(getCluster()));
        Map<String, Object> result  = clusterService.queryAllClusterList();
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

        List<Cluster> list = (List<Cluster>)(result.get(Constants.DATA_LIST));
        Assert.assertEquals(1,list.size());
    }

    @Test
    public void testQueryClusterListPaging() {
        IPage<Cluster> page = new Page<>(1, 10);
        page.setRecords(getList());
        page.setTotal(1L);
        Mockito.when(clusterMapper.queryClusterListPaging(Mockito.any(Page.class), Mockito.eq(clusterName))).thenReturn(page);

        Result result = clusterService.queryClusterListPaging(1, 10, clusterName);
        logger.info(result.toString());
        PageInfo<Cluster> pageInfo = (PageInfo<Cluster>) result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));
    }

    @Test
    public void testQueryClusterByName() {
        Mockito.when(clusterMapper.queryByClusterName(clusterName)).thenReturn(null);
        Map<String, Object> result = clusterService.queryClusterByName(clusterName);
        logger.info(result.toString());
        Assert.assertEquals(Status.QUERY_CLUSTER_BY_NAME_ERROR,result.get(Constants.STATUS));

        Mockito.when(clusterMapper.queryByClusterName(clusterName)).thenReturn(getCluster());
        result = clusterService.queryClusterByName(clusterName);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }

    @Test
    public void testQueryClusterByCode() {
        Mockito.when(clusterMapper.queryByClusterCode(1L)).thenReturn(null);
        Map<String, Object> result = clusterService.queryClusterByCode(1L);
        logger.info(result.toString());
        Assert.assertEquals(Status.QUERY_CLUSTER_BY_CODE_ERROR,result.get(Constants.STATUS));

        Mockito.when(clusterMapper.queryByClusterCode(1L)).thenReturn(getCluster());
        result = clusterService.queryClusterByCode(1L);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }

    @Test
    public void testDeleteClusterByCode() {
        User loginUser = getGeneralUser();
        Map<String, Object> result = clusterService.deleteClusterByCode(loginUser,1L);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, result.get(Constants.STATUS));

        loginUser = getAdminUser();
        Mockito.when(clusterMapper.deleteByCode(1L)).thenReturn(1);
        result = clusterService.deleteClusterByCode(loginUser,1L);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testVerifyCluster() {
        Map<String, Object> result = clusterService.verifyCluster("");
        logger.info(result.toString());
        Assert.assertEquals(Status.CLUSTER_NAME_IS_NULL, result.get(Constants.STATUS));

        Mockito.when(clusterMapper.queryByClusterName(clusterName)).thenReturn(getCluster());
        result = clusterService.verifyCluster(clusterName);
        logger.info(result.toString());
        Assert.assertEquals(Status.CLUSTER_NAME_EXISTS, result.get(Constants.STATUS));
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
