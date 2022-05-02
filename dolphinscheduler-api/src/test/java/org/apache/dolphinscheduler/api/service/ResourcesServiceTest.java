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

import static org.mockito.ArgumentMatchers.eq;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ResourcesServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.storage.StorageOperate;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.io.Files;

/**
 * resources service test
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"sun.security.*", "javax.net.*"})
@PrepareForTest({PropertyUtils.class,
        FileUtils.class, org.apache.dolphinscheduler.api.utils.FileUtils.class,
        Files.class})
public class ResourcesServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesServiceTest.class);

    @InjectMocks
    private ResourcesServiceImpl resourcesService;

    @Mock
    private ResourceMapper resourcesMapper;

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private StorageOperate storageOperate;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UdfFuncMapper udfFunctionMapper;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private ResourceUserMapper resourceUserMapper;

    @Before
    public void setUp() {
//        PowerMockito.mockStatic(HadoopUtils.class);
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(Files.class);
        PowerMockito.mockStatic(org.apache.dolphinscheduler.api.utils.FileUtils.class);
        try {
            // new HadoopUtils
            // PowerMockito.whenNew(HadoopUtils.class).withNoArguments().thenReturn(hadoopUtils);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // PowerMockito.when(HadoopUtils.getInstance()).thenReturn(hadoopUtils);
        PowerMockito.mockStatic(PropertyUtils.class);
    }

    @Test
    public void testCreateResource() {

        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);
        User user = new User();
        //HDFS_NOT_STARTUP
        Result result = resourcesService.createResource(user, "ResourcesServiceTest", "ResourcesServiceTest", ResourceType.FILE, null, -1, "/");
        logger.info(result.toString());
        Assert.assertEquals(Status.STORAGE_NOT_STARTUP.getMsg(), result.getMsg());

        //RESOURCE_FILE_IS_EMPTY
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test.pdf", "".getBytes());
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        result = resourcesService.createResource(user, "ResourcesServiceTest", "ResourcesServiceTest", ResourceType.FILE, mockMultipartFile, -1, "/");
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_FILE_IS_EMPTY.getMsg(), result.getMsg());

        //RESOURCE_SUFFIX_FORBID_CHANGE
        mockMultipartFile = new MockMultipartFile("test.pdf", "test.pdf", "pdf", "test".getBytes());
        PowerMockito.when(Files.getFileExtension("test.pdf")).thenReturn("pdf");
        PowerMockito.when(Files.getFileExtension("ResourcesServiceTest.jar")).thenReturn("jar");
        result = resourcesService.createResource(user, "ResourcesServiceTest.jar", "ResourcesServiceTest", ResourceType.FILE, mockMultipartFile, -1, "/");
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_SUFFIX_FORBID_CHANGE.getMsg(), result.getMsg());

        //UDF_RESOURCE_SUFFIX_NOT_JAR
        mockMultipartFile = new MockMultipartFile("ResourcesServiceTest.pdf", "ResourcesServiceTest.pdf", "pdf", "test".getBytes());
        PowerMockito.when(Files.getFileExtension("ResourcesServiceTest.pdf")).thenReturn("pdf");
        result = resourcesService.createResource(user, "ResourcesServiceTest.pdf", "ResourcesServiceTest", ResourceType.UDF, mockMultipartFile, -1, "/");
        logger.info(result.toString());
        Assert.assertEquals(Status.UDF_RESOURCE_SUFFIX_NOT_JAR.getMsg(), result.getMsg());

        //FULL_FILE_NAME_TOO_LONG
        String tooLongFileName = getRandomStringWithLength(Constants.RESOURCE_FULL_NAME_MAX_LENGTH) + ".pdf";
        mockMultipartFile = new MockMultipartFile(tooLongFileName, tooLongFileName, "pdf", "test".getBytes());
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        PowerMockito.when(Files.getFileExtension(tooLongFileName)).thenReturn("pdf");
        result = resourcesService.createResource(user, tooLongFileName, tooLongFileName, ResourceType.FILE, mockMultipartFile, -1, "/");
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_FULL_NAME_TOO_LONG_ERROR.getMsg(), result.getMsg());
    }

    @Test
    public void testCreateDirecotry() {

        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);
        User user = new User();
        //HDFS_NOT_STARTUP
        Result result = resourcesService.createDirectory(user, "directoryTest", "directory test", ResourceType.FILE, -1, "/");
        logger.info(result.toString());
        Assert.assertEquals(Status.STORAGE_NOT_STARTUP.getMsg(), result.getMsg());

        //PARENT_RESOURCE_NOT_EXIST
        user.setId(1);
        user.setTenantId(1);
        Mockito.when(userMapper.selectById(1)).thenReturn(getUser());
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        Mockito.when(resourcesMapper.selectById(Mockito.anyInt())).thenReturn(null);
        result = resourcesService.createDirectory(user, "directoryTest", "directory test", ResourceType.FILE, 1, "/");
        logger.info(result.toString());
        Assert.assertEquals(Status.PARENT_RESOURCE_NOT_EXIST.getMsg(), result.getMsg());
        //RESOURCE_EXIST
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        Mockito.when(resourcesMapper.existResource("/directoryTest", 0)).thenReturn(true);
        result = resourcesService.createDirectory(user, "directoryTest", "directory test", ResourceType.FILE, -1, "/");
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_EXIST.getMsg(), result.getMsg());

    }

    @Test
    public void testUpdateResource() {

        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);
        User user = new User();
        //HDFS_NOT_STARTUP
        Result result = resourcesService.updateResource(user, 1, "ResourcesServiceTest", "ResourcesServiceTest", ResourceType.FILE, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.STORAGE_NOT_STARTUP.getMsg(), result.getMsg());

        //RESOURCE_NOT_EXIST
        Mockito.when(resourcesMapper.selectById(1)).thenReturn(getResource());
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        result = resourcesService.updateResource(user, 0, "ResourcesServiceTest", "ResourcesServiceTest", ResourceType.FILE, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), result.getMsg());

        //USER_NO_OPERATION_PERM
        result = resourcesService.updateResource(user, 1, "ResourcesServiceTest", "ResourcesServiceTest", ResourceType.FILE, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getMsg(), result.getMsg());

        //RESOURCE_NOT_EXIST
        user.setId(1);
        Mockito.when(userMapper.selectById(1)).thenReturn(getUser());
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        PowerMockito.when(storageOperate.getFileName(Mockito.any(), Mockito.any(), Mockito.anyString())).thenReturn("test1");

        try {
            Mockito.when(storageOperate.exists(Mockito.any(), Mockito.any())).thenReturn(false);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        result = resourcesService.updateResource(user, 1, "ResourcesServiceTest1.jar", "ResourcesServiceTest", ResourceType.UDF, null);
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), result.getMsg());

        //SUCCESS
        user.setId(1);
        Mockito.when(userMapper.queryDetailsById(1)).thenReturn(getUser());
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        try {
            Mockito.when(storageOperate.exists(Mockito.any(), Mockito.any())).thenReturn(true);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        result = resourcesService.updateResource(user, 1, "ResourcesServiceTest.jar", "ResourcesServiceTest", ResourceType.FILE, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());

        //RESOURCE_EXIST
        Mockito.when(resourcesMapper.existResource("/ResourcesServiceTest1.jar", 0)).thenReturn(true);
        result = resourcesService.updateResource(user, 1, "ResourcesServiceTest1.jar", "ResourcesServiceTest", ResourceType.FILE, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_EXIST.getMsg(), result.getMsg());
        //USER_NOT_EXIST
        Mockito.when(userMapper.selectById(Mockito.anyInt())).thenReturn(null);
        result = resourcesService.updateResource(user, 1, "ResourcesServiceTest1.jar", "ResourcesServiceTest", ResourceType.UDF, null);
        logger.info(result.toString());
        Assert.assertTrue(Status.USER_NOT_EXIST.getCode() == result.getCode());

        //TENANT_NOT_EXIST
        Mockito.when(userMapper.selectById(1)).thenReturn(getUser());
        Mockito.when(tenantMapper.queryById(Mockito.anyInt())).thenReturn(null);
        result = resourcesService.updateResource(user, 1, "ResourcesServiceTest1.jar", "ResourcesServiceTest", ResourceType.UDF, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST.getMsg(), result.getMsg());

        //SUCCESS
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        PowerMockito.when(storageOperate.getResourceFileName(Mockito.any(), Mockito.any())).thenReturn("test");
        try {
            // PowerMockito.when(HadoopUtils.getInstance().copy(Mockito.anyString(), Mockito.anyString(), true, true)).thenReturn(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        result = resourcesService.updateResource(user, 1, "ResourcesServiceTest1.jar", "ResourcesServiceTest1.jar", ResourceType.UDF, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());

    }

    @Test
    public void testQueryResourceListPaging() {
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        IPage<Resource> resourcePage = new Page<>(1, 10);
        resourcePage.setTotal(1);
        resourcePage.setRecords(getResourceList());

        Mockito.when(resourcesMapper.queryResourcePaging(Mockito.any(Page.class),
                eq(0), eq(-1), eq(0), eq("test"), Mockito.any())).thenReturn(resourcePage);
        Result result = resourcesService.queryResourceListPaging(loginUser, -1, ResourceType.FILE, "test", 1, 10);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        PageInfo pageInfo = (PageInfo) result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));

    }

    @Test
    public void testQueryResourceList() {
        User loginUser = new User();
        loginUser.setId(0);
        loginUser.setUserType(UserType.ADMIN_USER);
        Mockito.when(resourcesMapper.queryResourceListAuthored(0, 0)).thenReturn(getResourceList());
        Map<String, Object> result = resourcesService.queryResourceList(loginUser, ResourceType.FILE);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<Resource> resourceList = (List<Resource>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(resourceList));

        // test udf
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(resourceUserMapper.queryResourcesIdListByUserIdAndPerm(0, 0))
                .thenReturn(Arrays.asList(Integer.valueOf(10), Integer.valueOf(11)));
        Mockito.when(resourcesMapper.queryResourceListById(Arrays.asList(Integer.valueOf(10), Integer.valueOf(11))))
                .thenReturn(Arrays.asList(getResource(10, ResourceType.FILE), getResource(11, ResourceType.UDF)));
        Mockito.when(resourcesMapper.queryResourceListAuthored(0, 1)).thenReturn(getResourceList());
        result = resourcesService.queryResourceList(loginUser, ResourceType.UDF);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        resourceList = (List<Resource>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(resourceList.size() == 4);
    }

    @Test
    public void testDelete() {

        User loginUser = new User();
        loginUser.setId(0);
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());

        try {
            // HDFS_NOT_STARTUP
            Result result = resourcesService.delete(loginUser, 1);
            logger.info(result.toString());
            Assert.assertEquals(Status.STORAGE_NOT_STARTUP.getMsg(), result.getMsg());

            //RESOURCE_NOT_EXIST
            PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
            Mockito.when(resourcesMapper.selectById(1)).thenReturn(getResource());
            result = resourcesService.delete(loginUser, 2);
            logger.info(result.toString());
            Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), result.getMsg());

            // USER_NO_OPERATION_PERM
            result = resourcesService.delete(loginUser, 2);
            logger.info(result.toString());
            Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), result.getMsg());

            //TENANT_NOT_EXIST
            loginUser.setUserType(UserType.ADMIN_USER);
            loginUser.setTenantId(2);
            Mockito.when(userMapper.selectById(Mockito.anyInt())).thenReturn(loginUser);
            result = resourcesService.delete(loginUser, 1);
            logger.info(result.toString());
            Assert.assertEquals(Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST.getMsg(), result.getMsg());

            //SUCCESS
            loginUser.setTenantId(1);
            Mockito.when(storageOperate.delete(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean())).thenReturn(true);
            Mockito.when(processDefinitionMapper.listResources()).thenReturn(getResources());
            Mockito.when(resourcesMapper.deleteIds(Mockito.any())).thenReturn(1);
            Mockito.when(resourceUserMapper.deleteResourceUserArray(Mockito.anyInt(), Mockito.any())).thenReturn(1);
            result = resourcesService.delete(loginUser, 1);
            logger.info(result.toString());
            Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());

        } catch (Exception e) {
            logger.error("delete error", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testVerifyResourceName() {

        User user = new User();
        user.setId(1);
        Mockito.when(resourcesMapper.existResource("/ResourcesServiceTest.jar", 0)).thenReturn(true);
        Result result = resourcesService.verifyResourceName("/ResourcesServiceTest.jar", ResourceType.FILE, user);
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_EXIST.getMsg(), result.getMsg());

        //TENANT_NOT_EXIST
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        String unExistFullName = "/test.jar";
        try {
            Mockito.when(storageOperate.exists(Mockito.anyString(), eq(unExistFullName))).thenReturn(false);
        } catch (IOException e) {
            logger.error("hadoop error", e);
        }
        result = resourcesService.verifyResourceName("/test.jar", ResourceType.FILE, user);
        logger.info(result.toString());
        Assert.assertEquals(Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST.getMsg(), result.getMsg());

        //RESOURCE_FILE_EXIST
        user.setTenantId(1);
        try {
            Mockito.when(storageOperate.exists(Mockito.any(), eq("test"))).thenReturn(true);
        } catch (IOException e) {
            logger.error("hadoop error", e);
        }
        PowerMockito.when(storageOperate.getResourceFileName("123", "test1")).thenReturn("test");
        result = resourcesService.verifyResourceName("/ResourcesServiceTest.jar", ResourceType.FILE, user);
        logger.info(result.toString());
        Assert.assertTrue(Status.RESOURCE_EXIST.getCode() == result.getCode());

        //SUCCESS
        result = resourcesService.verifyResourceName("test2", ResourceType.FILE, user);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());

    }

    @Test
    public void testReadResource() {

        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);

        //HDFS_NOT_STARTUP
        Result result = resourcesService.readResource(1, 1, 10);
        logger.info(result.toString());
        Assert.assertEquals(Status.STORAGE_NOT_STARTUP.getMsg(), result.getMsg());

        //RESOURCE_NOT_EXIST
        Mockito.when(resourcesMapper.selectById(1)).thenReturn(getResource());
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        result = resourcesService.readResource(2, 1, 10);
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), result.getMsg());

        //RESOURCE_SUFFIX_NOT_SUPPORT_VIEW
        PowerMockito.when(FileUtils.getResourceViewSuffixes()).thenReturn("class");
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        result = resourcesService.readResource(1, 1, 10);
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW.getMsg(), result.getMsg());

        //USER_NOT_EXIST
        PowerMockito.when(FileUtils.getResourceViewSuffixes()).thenReturn("jar");
        PowerMockito.when(Files.getFileExtension("ResourcesServiceTest.jar")).thenReturn("jar");
        result = resourcesService.readResource(1, 1, 10);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NOT_EXIST.getCode(), (int) result.getCode());

        //TENANT_NOT_EXIST
        Mockito.when(userMapper.selectById(1)).thenReturn(getUser());
        result = resourcesService.readResource(1, 1, 10);
        logger.info(result.toString());
        Assert.assertEquals(Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST.getMsg(), result.getMsg());

        //RESOURCE_FILE_NOT_EXIST
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        try {
            Mockito.when(storageOperate.exists(Mockito.any(), Mockito.anyString())).thenReturn(false);
        } catch (IOException e) {
            logger.error("hadoop error", e);
        }
        result = resourcesService.readResource(1, 1, 10);
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_FILE_NOT_EXIST.getCode(), (int) result.getCode());


        //SUCCESS
        try {
            Mockito.when(storageOperate.exists(Mockito.any(), Mockito.any())).thenReturn(true);
            Mockito.when(storageOperate.vimFile(Mockito.any(), Mockito.any(), eq(1), eq(10))).thenReturn(getContent());
        } catch (IOException e) {
            logger.error("storage error", e);
        }
        result = resourcesService.readResource(1, 1, 10);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());

    }

    @Test
    public void testOnlineCreateResource() {

        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);
        PowerMockito.when(storageOperate.getResourceFileName(Mockito.anyString(), eq("hdfsdDir"))).thenReturn("hdfsDir");
        PowerMockito.when(storageOperate.getUdfDir("udfDir")).thenReturn("udfDir");
        User user = getUser();
        //HDFS_NOT_STARTUP
        Result result = resourcesService.onlineCreateResource(user, ResourceType.FILE, "test", "jar", "desc", "content", -1, "/");
        logger.info(result.toString());
        Assert.assertEquals(Status.STORAGE_NOT_STARTUP.getMsg(), result.getMsg());

        //RESOURCE_SUFFIX_NOT_SUPPORT_VIEW
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        PowerMockito.when(FileUtils.getResourceViewSuffixes()).thenReturn("class");
        result = resourcesService.onlineCreateResource(user, ResourceType.FILE, "test", "jar", "desc", "content", -1, "/");
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW.getMsg(), result.getMsg());

        //RuntimeException
        try {
            PowerMockito.when(FileUtils.getResourceViewSuffixes()).thenReturn("jar");
            Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
            result = resourcesService.onlineCreateResource(user, ResourceType.FILE, "test", "jar", "desc", "content", -1, "/");
        } catch (RuntimeException ex) {
            logger.info(result.toString());
            Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), ex.getMessage());
        }

        //SUCCESS
        Mockito.when(FileUtils.getUploadFilename(Mockito.anyString(), Mockito.anyString())).thenReturn("test");
        PowerMockito.when(FileUtils.writeContent2File(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        result = resourcesService.onlineCreateResource(user, ResourceType.FILE, "test", "jar", "desc", "content", -1, "/");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());

    }

    @Test
    public void testUpdateResourceContent() {
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);

        // HDFS_NOT_STARTUP
        Result result = resourcesService.updateResourceContent(1, "content");
        logger.info(result.toString());
        Assert.assertEquals(Status.STORAGE_NOT_STARTUP.getMsg(), result.getMsg());

        //RESOURCE_NOT_EXIST
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        Mockito.when(resourcesMapper.selectById(1)).thenReturn(getResource());
        result = resourcesService.updateResourceContent(2, "content");
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), result.getMsg());

        //RESOURCE_SUFFIX_NOT_SUPPORT_VIEW
        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        PowerMockito.when(FileUtils.getResourceViewSuffixes()).thenReturn("class");
        result = resourcesService.updateResourceContent(1, "content");
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW.getMsg(), result.getMsg());

        //USER_NOT_EXIST
        PowerMockito.when(FileUtils.getResourceViewSuffixes()).thenReturn("jar");
        PowerMockito.when(Files.getFileExtension("ResourcesServiceTest.jar")).thenReturn("jar");
        result = resourcesService.updateResourceContent(1, "content");
        logger.info(result.toString());
        Assert.assertTrue(Status.USER_NOT_EXIST.getCode() == result.getCode());

        //TENANT_NOT_EXIST
        Mockito.when(userMapper.selectById(1)).thenReturn(getUser());
        result = resourcesService.updateResourceContent(1, "content");
        logger.info(result.toString());
        Assert.assertTrue(Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST.getCode() == result.getCode());

        //SUCCESS
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        Mockito.when(FileUtils.getUploadFilename(Mockito.anyString(), Mockito.anyString())).thenReturn("test");
        PowerMockito.when(FileUtils.writeContent2File(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        result = resourcesService.updateResourceContent(1, "content");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testDownloadResource() {

        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(true);
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        Mockito.when(userMapper.selectById(1)).thenReturn(getUser());
        org.springframework.core.io.Resource resourceMock = Mockito.mock(org.springframework.core.io.Resource.class);
        try {
            //resource null
            org.springframework.core.io.Resource resource = resourcesService.downloadResource(1);
            Assert.assertNull(resource);

            Mockito.when(resourcesMapper.selectById(1)).thenReturn(getResource());
            PowerMockito.when(org.apache.dolphinscheduler.api.utils.FileUtils.file2Resource(Mockito.any())).thenReturn(resourceMock);
            resource = resourcesService.downloadResource(1);
            Assert.assertNotNull(resource);
        } catch (Exception e) {
            logger.error("DownloadResource error", e);
            Assert.assertTrue(false);
        }

    }

    @Test
    public void testAuthorizeResourceTree() {
        User user = getUser();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        int userId = 3;

        // test admin user
        List<Integer> resIds = new ArrayList<>();
        resIds.add(1);
        Mockito.when(resourcesMapper.queryResourceExceptUserId(userId)).thenReturn(getResourceList());
        Map<String, Object> result = resourcesService.authorizeResourceTree(user, userId);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<Resource> resources = (List<Resource>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(resources));

        // test non-admin user
        user.setId(2);
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(resourcesMapper.queryResourceListAuthored(user.getId(), -1)).thenReturn(getResourceList());
        result = resourcesService.authorizeResourceTree(user, userId);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        resources = (List<Resource>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(resources));
    }

    @Test
    public void testUnauthorizedFile() {
        User user = getUser();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        int userId = 3;

        // test admin user
        List<Integer> resIds = new ArrayList<>();
        resIds.add(1);
        Mockito.when(resourcesMapper.queryResourceExceptUserId(userId)).thenReturn(getResourceList());
        Mockito.when(resourceUserMapper.queryResourcesIdListByUserIdAndPerm(Mockito.anyInt(), Mockito.anyInt())).thenReturn(resIds);
        Mockito.when(resourcesMapper.queryResourceListById(Mockito.any())).thenReturn(getSingleResourceList());
        Map<String, Object> result = resourcesService.unauthorizedFile(user, userId);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<Resource> resources = (List<Resource>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(resources));

        // test non-admin user
        user.setId(2);
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(resourcesMapper.queryResourceListAuthored(user.getId(), -1)).thenReturn(getResourceList());
        result = resourcesService.unauthorizedFile(user, userId);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        resources = (List<Resource>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(resources));
    }

    @Test
    public void testUnauthorizedUDFFunction() {
        User user = getUser();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        int userId = 3;

        // test admin user
        Mockito.when(udfFunctionMapper.queryUdfFuncExceptUserId(userId)).thenReturn(getUdfFuncList());
        Mockito.when(udfFunctionMapper.queryAuthedUdfFunc(userId)).thenReturn(getSingleUdfFuncList());
        Map<String, Object> result = resourcesService.unauthorizedUDFFunction(user, userId);
        logger.info(result.toString());
        List<UdfFunc> udfFuncs = (List<UdfFunc>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(udfFuncs));

        // test non-admin user
        user.setId(2);
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(udfFunctionMapper.selectByMap(Collections.singletonMap("user_id", user.getId()))).thenReturn(getUdfFuncList());
        result = resourcesService.unauthorizedUDFFunction(user, userId);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        udfFuncs = (List<UdfFunc>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(udfFuncs));
    }

    @Test
    public void testAuthorizedUDFFunction() {
        User user = getUser();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        int userId = 3;

        // test admin user
        Mockito.when(udfFunctionMapper.queryAuthedUdfFunc(userId)).thenReturn(getUdfFuncList());
        Map<String, Object> result = resourcesService.authorizedUDFFunction(user, userId);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<UdfFunc> udfFuncs = (List<UdfFunc>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(udfFuncs));

        // test non-admin user
        user.setUserType(UserType.GENERAL_USER);
        user.setId(2);
        Mockito.when(udfFunctionMapper.queryAuthedUdfFunc(userId)).thenReturn(getUdfFuncList());
        result = resourcesService.authorizedUDFFunction(user, userId);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        udfFuncs = (List<UdfFunc>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(udfFuncs));
    }

    @Test
    public void testAuthorizedFile() {
        User user = getUser();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        int userId = 3;

        // test admin user
        List<Integer> resIds = new ArrayList<>();
        resIds.add(1);
        Mockito.when(resourceUserMapper.queryResourcesIdListByUserIdAndPerm(Mockito.anyInt(), Mockito.anyInt())).thenReturn(resIds);
        Mockito.when(resourcesMapper.queryResourceListById(Mockito.any())).thenReturn(getResourceList());
        Map<String, Object> result = resourcesService.authorizedFile(user, userId);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<Resource> resources = (List<Resource>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(resources));

        // test non-admin user
        user.setId(2);
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(resourceUserMapper.queryResourcesIdListByUserIdAndPerm(Mockito.anyInt(), Mockito.anyInt())).thenReturn(resIds);
        Mockito.when(resourcesMapper.queryResourceListById(Mockito.any())).thenReturn(getResourceList());
        result = resourcesService.authorizedFile(user, userId);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        resources = (List<Resource>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(resources));
    }

    @Test
    public void testCatFile() {

        PowerMockito.when(PropertyUtils.getResUploadStartupState()).thenReturn(false);

        //SUCCESS
        try {
            Mockito.when(storageOperate.exists(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            Mockito.when(storageOperate.vimFile(Mockito.anyString(), Mockito.anyString(), eq(1), eq(10))).thenReturn(getContent());
            List<String> list = storageOperate.vimFile(Mockito.any(), Mockito.anyString(), eq(1), eq(10));
            Assert.assertNotNull(list);

        } catch (IOException e) {
            logger.error("hadoop error", e);
        }
    }

    private List<Resource> getResourceList() {

        List<Resource> resources = new ArrayList<>();
        resources.add(getResource(1));
        resources.add(getResource(2));
        resources.add(getResource(3));
        return resources;
    }

    private List<Resource> getSingleResourceList() {
        return Collections.singletonList(getResource(1));
    }

    private Tenant getTenant() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("123");
        return tenant;
    }

    private Resource getResource() {

        Resource resource = new Resource();
        resource.setPid(-1);
        resource.setUserId(1);
        resource.setDescription("ResourcesServiceTest.jar");
        resource.setAlias("ResourcesServiceTest.jar");
        resource.setFullName("/ResourcesServiceTest.jar");
        resource.setType(ResourceType.FILE);
        return resource;
    }

    private Resource getResource(int resourceId) {

        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setPid(-1);
        resource.setUserId(1);
        resource.setDescription("ResourcesServiceTest.jar");
        resource.setAlias("ResourcesServiceTest.jar");
        resource.setFullName("/ResourcesServiceTest.jar");
        resource.setType(ResourceType.FILE);
        return resource;
    }

    private Resource getResource(int resourceId,ResourceType type) {

        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setPid(-1);
        resource.setUserId(1);
        resource.setDescription("ResourcesServiceTest.jar");
        resource.setAlias("ResourcesServiceTest.jar");
        resource.setFullName("/ResourcesServiceTest.jar");
        resource.setType(type);
        return resource;
    }

    private Resource getUdfResource() {

        Resource resource = new Resource();
        resource.setUserId(1);
        resource.setDescription("udfTest");
        resource.setAlias("udfTest.jar");
        resource.setFullName("/udfTest.jar");
        resource.setType(ResourceType.UDF);
        return resource;
    }

    private UdfFunc getUdfFunc() {

        UdfFunc udfFunc = new UdfFunc();
        udfFunc.setId(1);
        return udfFunc;
    }

    private UdfFunc getUdfFunc(int udfId) {

        UdfFunc udfFunc = new UdfFunc();
        udfFunc.setId(udfId);
        return udfFunc;
    }

    private List<UdfFunc> getUdfFuncList() {

        List<UdfFunc> udfFuncs = new ArrayList<>();
        udfFuncs.add(getUdfFunc(1));
        udfFuncs.add(getUdfFunc(2));
        udfFuncs.add(getUdfFunc(3));
        return udfFuncs;
    }

    private List<UdfFunc> getSingleUdfFuncList() {
        return Collections.singletonList(getUdfFunc(3));
    }

    private User getUser() {
        User user = new User();
        user.setId(1);
        user.setTenantId(1);
        user.setTenantCode("tenantCode");
        return user;
    }

    private List<String> getContent() {
        List<String> contentList = new ArrayList<>();
        contentList.add("test");
        return contentList;
    }

    private List<Map<String, Object>> getResources() {
        List<Map<String, Object>> resources = new ArrayList<>();
        Map<String, Object> resource = new HashMap<>();
        resource.put("id", 1);
        resource.put("resource_ids", "1");
        resources.add(resource);
        return resources;
    }

    private static String getRandomStringWithLength(int length) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            char c = (char) (r.nextInt(26) + 'a');
            sb.append(c);
        }
        return sb.toString();
    }
}
