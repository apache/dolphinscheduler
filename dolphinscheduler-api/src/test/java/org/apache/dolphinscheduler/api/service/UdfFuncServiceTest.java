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
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * udf func service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UdfFuncServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(UdfFuncServiceTest.class);

    private MockedStatic<PropertyUtils> mockedStaticPropertyUtils;

    @InjectMocks
    private UdfFuncServiceImpl udfFuncService;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private UdfFuncMapper udfFuncMapper;

    @Mock
    private UDFUserMapper udfUserMapper;

    @BeforeEach
    public void setUp() {
        mockedStaticPropertyUtils = Mockito.mockStatic(PropertyUtils.class);
    }

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private static final Logger serviceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);
    private static final Logger udfLogger = LoggerFactory.getLogger(UdfFuncServiceImpl.class);

    @Test
    public void testCreateUdfFunction() {

        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1,
                ApiFuncIdentificationConstant.UDF_FUNCTION_CREATE, serviceLogger)).thenReturn(true);
        Mockito.when(
                resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, null, 0, serviceLogger))
                .thenReturn(true);
        Mockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);
        // hdfs not start
        Result result = udfFuncService.createUdfFunction(getLoginUser(), "UdfFuncServiceTest",
                "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, Integer.MAX_VALUE);
        logger.info(result.toString());
        Assertions.assertEquals(Status.HDFS_NOT_STARTUP.getMsg(), result.getMsg());
        // resource not exist
        Mockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        result = udfFuncService.createUdfFunction(getLoginUser(), "UdfFuncServiceTest",
                "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, Integer.MAX_VALUE);
        logger.info(result.toString());
        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), result.getMsg());
        // success
        Mockito.when(resourceMapper.selectById(1)).thenReturn(getResource());
        result = udfFuncService.createUdfFunction(getLoginUser(), "UdfFuncServiceTest",
                "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, 1);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testQueryUdfFuncDetail() {

        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1,
                ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW, serviceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{2}, 0,
                serviceLogger)).thenReturn(true);
        Mockito.when(udfFuncMapper.selectById(1)).thenReturn(getUdfFunc());
        // resource not exist
        Result<Object> result = udfFuncService.queryUdfFuncDetail(getLoginUser(), 2);
        logger.info(result.toString());
        Assertions.assertTrue(Status.RESOURCE_NOT_EXIST.getCode() == result.getCode());
        // success
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1,
                ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW, serviceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{1}, 0,
                serviceLogger)).thenReturn(true);
        result = udfFuncService.queryUdfFuncDetail(getLoginUser(), 1);
        logger.info(result.toString());
        Assertions.assertTrue(Status.SUCCESS.getCode() == result.getCode());
    }

    @Test
    public void testUpdateUdfFunc() {

        Mockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);
        Mockito.when(udfFuncMapper.selectUdfById(1)).thenReturn(getUdfFunc());
        Mockito.when(resourceMapper.selectById(1)).thenReturn(getResource());

        // UDF_FUNCTION_NOT_EXIST
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1,
                ApiFuncIdentificationConstant.UDF_FUNCTION_UPDATE, serviceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{12}, 0,
                serviceLogger)).thenReturn(true);
        Result<Object> result = udfFuncService.updateUdfFunc(getLoginUser(), 12, "UdfFuncServiceTest",
                "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, 1);
        logger.info(result.toString());
        Assertions.assertTrue(Status.UDF_FUNCTION_NOT_EXIST.getCode() == result.getCode());

        // HDFS_NOT_STARTUP
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{1}, 0,
                serviceLogger)).thenReturn(true);
        result = udfFuncService.updateUdfFunc(getLoginUser(), 1, "UdfFuncServiceTest",
                "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, 1);
        logger.info(result.toString());
        Assertions.assertTrue(Status.HDFS_NOT_STARTUP.getCode() == result.getCode());

        // RESOURCE_NOT_EXIST
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1,
                ApiFuncIdentificationConstant.UDF_FUNCTION_UPDATE, serviceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{11}, 0,
                serviceLogger)).thenReturn(true);
        Mockito.when(udfFuncMapper.selectUdfById(11)).thenReturn(getUdfFunc());
        Mockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        result = udfFuncService.updateUdfFunc(getLoginUser(), 11, "UdfFuncServiceTest",
                "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, 12);
        logger.info(result.toString());
        Assertions.assertTrue(Status.RESOURCE_NOT_EXIST.getCode() == result.getCode());

        // success
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1,
                ApiFuncIdentificationConstant.UDF_FUNCTION_UPDATE, serviceLogger)).thenReturn(true);
        result = udfFuncService.updateUdfFunc(getLoginUser(), 11, "UdfFuncServiceTest",
                "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String",
                "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, 1);
        logger.info(result.toString());
        Assertions.assertTrue(Status.SUCCESS.getCode() == result.getCode());

    }

    @Test
    public void testQueryUdfFuncListPaging() {

        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1,
                ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW, serviceLogger)).thenReturn(true);
        Mockito.when(
                resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, null, 0, serviceLogger))
                .thenReturn(true);
        Mockito.when(
                resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.UDF, 1, udfLogger))
                .thenReturn(getSetIds());
        IPage<UdfFunc> page = new Page<>(1, 10);
        page.setTotal(1L);
        page.setRecords(getList());
        Mockito.when(udfFuncMapper.queryUdfFuncPaging(Mockito.any(Page.class), Mockito.anyList(), Mockito.eq("test")))
                .thenReturn(page);
        Result result = udfFuncService.queryUdfFuncListPaging(getLoginUser(), "test", 1, 10);
        logger.info(result.toString());
        PageInfo pageInfo = (PageInfo) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));
    }

    @Test
    public void testQueryUdfFuncList() {
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1,
                ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW, serviceLogger)).thenReturn(true);
        Mockito.when(
                resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, null, 1, serviceLogger))
                .thenReturn(true);
        Mockito.when(
                resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.UDF, 1, udfLogger))
                .thenReturn(getSetIds());

        User user = getLoginUser();
        user.setUserType(UserType.GENERAL_USER);
        user.setId(1);
        Mockito.when(udfFuncMapper.getUdfFuncByType(Collections.singletonList(1), UdfType.HIVE.ordinal()))
                .thenReturn(getList());
        Result<Object> result = udfFuncService.queryUdfFuncList(user, UdfType.HIVE.ordinal());
        logger.info(result.toString());
        Assertions.assertTrue(Status.SUCCESS.getCode() == result.getCode());
        List<UdfFunc> udfFuncList = (List<UdfFunc>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(udfFuncList));
    }

    @Test
    public void testDelete() {
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1,
                ApiFuncIdentificationConstant.UDF_FUNCTION_DELETE, serviceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, new Object[]{122}, 0,
                serviceLogger)).thenReturn(true);

        Mockito.when(udfFuncMapper.deleteById(Mockito.anyInt())).thenReturn(1);
        Mockito.when(udfUserMapper.deleteByUdfFuncId(Mockito.anyInt())).thenReturn(1);
        Result result = udfFuncService.delete(getLoginUser(), 122);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testVerifyUdfFuncByName() {

        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.UDF, null, 1,
                ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW, serviceLogger)).thenReturn(true);
        Mockito.when(
                resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.UDF, null, 0, serviceLogger))
                .thenReturn(true);
        // success
        Mockito.when(udfFuncMapper.queryUdfByIdStr(null, "UdfFuncServiceTest")).thenReturn(getList());
        Result result = udfFuncService.verifyUdfFuncByName(getLoginUser(), "test");
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
        // exist
        result = udfFuncService.verifyUdfFuncByName(getLoginUser(), "UdfFuncServiceTest");
        logger.info(result.toString());
        Assertions.assertEquals(Status.UDF_FUNCTION_EXISTS.getMsg(), result.getMsg());
    }

    private Set<Integer> getSetIds() {
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
        return udfFuncList;
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

    @AfterEach
    public void after() {
        mockedStaticPropertyUtils.close();
    }
}
