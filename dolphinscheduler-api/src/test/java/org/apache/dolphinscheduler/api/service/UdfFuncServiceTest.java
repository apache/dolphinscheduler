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

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.UdfFuncServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.UDFUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * udf func service test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PropertyUtils.class)
public class UdfFuncServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(UdfFuncServiceTest.class);

    @InjectMocks
    private UdfFuncServiceImpl udfFuncService;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private UdfFuncMapper udfFuncMapper;

    @Mock
    private UDFUserMapper udfUserMapper;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(PropertyUtils.class);
    }

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private static final Logger serviceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);
    private static final Logger udfLogger = LoggerFactory.getLogger(UdfFuncServiceImpl.class);

    @Test
    public  void testCreateUdfFunction() {

        PowerMockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1, ApiFuncIdentificationConstant.UDF_FUNCTION_CREATE, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, null, 0, serviceLogger)).thenReturn(true);
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);
        //hdfs not start
        Result result = udfFuncService.createUdfFunction(getLoginUser(), "UdfFuncServiceTest", "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, Integer.MAX_VALUE);
        logger.info(result.toString());
        Assert.assertEquals(Status.HDFS_NOT_STARTUP.getMsg(),result.getMsg());
        //resource not exist
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        result = udfFuncService.createUdfFunction(getLoginUser(), "UdfFuncServiceTest", "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, Integer.MAX_VALUE);
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(),result.getMsg());
        // success
        PowerMockito.when(resourceMapper.selectById(1)).thenReturn(getResource());
        result = udfFuncService.createUdfFunction(getLoginUser(), "UdfFuncServiceTest", "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, 1);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),result.getMsg());
    }

    @Test
    public  void testQueryUdfFuncDetail() {

        PowerMockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1, ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{2}, 0, serviceLogger)).thenReturn(true);
        PowerMockito.when(udfFuncMapper.selectById(1)).thenReturn(getUdfFunc());
        //resource not exist
        Result<Object> result = udfFuncService.queryUdfFuncDetail(getLoginUser(), 2);
        logger.info(result.toString());
        Assert.assertTrue(Status.RESOURCE_NOT_EXIST.getCode() == result.getCode());
        // success
        PowerMockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1, ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{1}, 0, serviceLogger)).thenReturn(true);
        result = udfFuncService.queryUdfFuncDetail(getLoginUser(), 1);
        logger.info(result.toString());
        Assert.assertTrue(Status.SUCCESS.getCode() == result.getCode());
    }

    @Test
    public  void testUpdateUdfFunc() {

        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);
        PowerMockito.when(udfFuncMapper.selectUdfById(1)).thenReturn(getUdfFunc());
        PowerMockito.when(resourceMapper.selectById(1)).thenReturn(getResource());

        //UDF_FUNCTION_NOT_EXIST
        PowerMockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1, ApiFuncIdentificationConstant.UDF_FUNCTION_UPDATE, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{12}, 0, serviceLogger)).thenReturn(true);
        Result<Object> result = udfFuncService.updateUdfFunc(getLoginUser(), 12, "UdfFuncServiceTest", "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, 1);
        logger.info(result.toString());
        Assert.assertTrue(Status.UDF_FUNCTION_NOT_EXIST.getCode() == result.getCode());

        //HDFS_NOT_STARTUP
        PowerMockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{1}, 0, serviceLogger)).thenReturn(true);
        result = udfFuncService.updateUdfFunc(getLoginUser(), 1, "UdfFuncServiceTest", "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, 1);
        logger.info(result.toString());
        Assert.assertTrue(Status.HDFS_NOT_STARTUP.getCode() == result.getCode());

        //RESOURCE_NOT_EXIST
        PowerMockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1, ApiFuncIdentificationConstant.UDF_FUNCTION_UPDATE, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{11}, 0, serviceLogger)).thenReturn(true);
        PowerMockito.when(udfFuncMapper.selectUdfById(11)).thenReturn(getUdfFunc());
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        result = udfFuncService.updateUdfFunc(getLoginUser(), 11, "UdfFuncServiceTest", "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, 12);
        logger.info(result.toString());
        Assert.assertTrue(Status.RESOURCE_NOT_EXIST.getCode() == result.getCode());

        //success
        PowerMockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1, ApiFuncIdentificationConstant.UDF_FUNCTION_UPDATE, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{1}, 0, serviceLogger)).thenReturn(true);
        result = udfFuncService.updateUdfFunc(getLoginUser(), 11, "UdfFuncServiceTest", "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, 1);
        logger.info(result.toString());
        Assert.assertTrue(Status.SUCCESS.getCode() == result.getCode());

    }

    @Test
    public  void testQueryUdfFuncListPaging() {

        PowerMockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1, ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, null, 0, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.UDF, 1, udfLogger)).thenReturn(getSetIds());
        IPage<UdfFunc> page = new Page<>(1,10);
        page.setTotal(1L);
        page.setRecords(getList());
        Mockito.when(udfFuncMapper.queryUdfFuncPaging(Mockito.any(Page.class), Mockito.anyList(),Mockito.eq("test"))).thenReturn(page);
        Result result = udfFuncService.queryUdfFuncListPaging(getLoginUser(),"test",1,10);
        logger.info(result.toString());
        PageInfo pageInfo = (PageInfo) result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));
    }

    @Test
    public  void testQueryUdfFuncList() {
        PowerMockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1, ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, null, 1, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.UDF, 1, udfLogger)).thenReturn(getSetIds());

        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        user.setId(1);
        Mockito.when(udfFuncMapper.getUdfFuncByType(Collections.singletonList(1), UdfType.HIVE.ordinal())).thenReturn(getList());
        Result<Object> result = udfFuncService.queryUdfFuncList(user,UdfType.HIVE.ordinal());
        logger.info(result.toString());
        Assert.assertTrue(Status.SUCCESS.getCode() == result.getCode());
        List<UdfFunc> udfFuncList = (List<UdfFunc>) result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(udfFuncList));
    }

    @Test
    public  void testDelete() {
        PowerMockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1, ApiFuncIdentificationConstant.UDF_FUNCTION_DELETE, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{122}, 0, serviceLogger)).thenReturn(true);

        Mockito.when(udfFuncMapper.deleteById(Mockito.anyInt())).thenReturn(1);
        Mockito.when(udfUserMapper.deleteByUdfFuncId(Mockito.anyInt())).thenReturn(1);
        Result result = udfFuncService.delete(getLoginUser(), 122);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),result.getMsg());
    }

    @Test
    public  void testVerifyUdfFuncByName() {

        PowerMockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1, ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW, serviceLogger)).thenReturn(true);
        PowerMockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, null, 0, serviceLogger)).thenReturn(true);
        //success
        Mockito.when(udfFuncMapper.queryUdfByIdStr(null, "UdfFuncServiceTest")).thenReturn(getList());
        Result result = udfFuncService.verifyUdfFuncByName(getLoginUser(), "test");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),result.getMsg());
        //exist
        result = udfFuncService.verifyUdfFuncByName(getLoginUser(), "UdfFuncServiceTest");
        logger.info(result.toString());
        Assert.assertEquals(Status.UDF_FUNCTION_EXISTS.getMsg(),result.getMsg());
    }

    private Set<Integer> getSetIds(){
        Set<Integer> set = new HashSet();
        set.add(1);
        return set;
    }

    /**
     * create admin user
     * @return
     */
    private User getLoginUser() {

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setId(1);
        return loginUser;
    }

    /**
     * get resourceId
     */
    private Resource getResource() {

        Resource resource = new Resource();
        resource.setId(1);
        resource.setAlias("test");
        return resource;
    }

    private List<UdfFunc> getList() {
        List<UdfFunc> udfFuncList = new ArrayList<>();
        udfFuncList.add(getUdfFunc());
        return  udfFuncList;
    }

    /**
     *  get UdfFuncRequest id
     */
    private UdfFunc getUdfFunc() {
        UdfFunc udfFunc = new UdfFunc();
        udfFunc.setFuncName("UdfFuncServiceTest");
        udfFunc.setClassName("org.apache.dolphinscheduler.api.service.UdfFuncServiceTest");
        udfFunc.setResourceId(0);
        udfFunc.setResourceName("UdfFuncServiceTest");
        udfFunc.setCreateTime(new Date());
        udfFunc.setDatabase("database");
        udfFunc.setUpdateTime(new Date());
        udfFunc.setType(UdfType.HIVE);
        return udfFunc;
    }
}
