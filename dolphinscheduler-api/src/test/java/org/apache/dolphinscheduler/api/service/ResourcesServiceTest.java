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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.resources.DeleteDataTransferResponse;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.ResourcesServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import org.springframework.mock.web.MockMultipartFile;

import com.google.common.io.Files;

/**
 * resources service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ResourcesServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesServiceTest.class);

    private static final String tenantCode = "123";

    @InjectMocks
    private ResourcesServiceImpl resourcesService;

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
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private MockedStatic<FileUtils> mockedStaticFileUtils;

    private MockedStatic<Files> mockedStaticFiles;

    private MockedStatic<org.apache.dolphinscheduler.api.utils.FileUtils> mockedStaticDolphinschedulerFileUtils;

    private MockedStatic<PropertyUtils> mockedStaticPropertyUtils;

    private MockedStatic<Paths> mockedStaticPaths;

    private MockedStatic<java.nio.file.Files> filesMockedStatic;

    private Exception exception;

    @BeforeEach
    public void setUp() {
        mockedStaticFileUtils = Mockito.mockStatic(FileUtils.class);
        mockedStaticFiles = Mockito.mockStatic(Files.class);
        mockedStaticDolphinschedulerFileUtils =
                Mockito.mockStatic(org.apache.dolphinscheduler.api.utils.FileUtils.class);

        mockedStaticPropertyUtils = Mockito.mockStatic(PropertyUtils.class);
        mockedStaticPaths = Mockito.mockStatic(Paths.class);
        filesMockedStatic = Mockito.mockStatic(java.nio.file.Files.class);
    }

    @AfterEach
    public void after() {
        mockedStaticFileUtils.close();
        mockedStaticFiles.close();
        mockedStaticDolphinschedulerFileUtils.close();
        mockedStaticPropertyUtils.close();
        mockedStaticPaths.close();
        filesMockedStatic.close();
    }

    @Test
    public void testCreateResource() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.GENERAL_USER);

        // CURRENT_LOGIN_USER_TENANT_NOT_EXIST
        when(userMapper.selectById(user.getId())).thenReturn(getUser());
        when(tenantMapper.queryById(1)).thenReturn(null);
        Assertions.assertThrows(ServiceException.class,
                () -> resourcesService.uploadResource(user, "ResourcesServiceTest", ResourceType.FILE,
                        new MockMultipartFile("test.pdf", "test.pdf", "pdf", "test".getBytes()), "/"));
        // set tenant for user
        user.setTenantId(1);
        when(tenantMapper.queryById(1)).thenReturn(getTenant());

        // RESOURCE_FILE_IS_EMPTY
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test.pdf", "".getBytes());
        Result result = resourcesService.uploadResource(user, "ResourcesServiceTest", ResourceType.FILE,
                mockMultipartFile, "/");
        logger.info(result.toString());
        assertEquals(Status.RESOURCE_FILE_IS_EMPTY.getMsg(), result.getMsg());

        // RESOURCE_SUFFIX_FORBID_CHANGE
        mockMultipartFile = new MockMultipartFile("test.pdf", "test.pdf", "pdf", "test".getBytes());
        when(Files.getFileExtension("test.pdf")).thenReturn("pdf");
        when(Files.getFileExtension("ResourcesServiceTest.jar")).thenReturn("jar");
        result = resourcesService.uploadResource(user, "ResourcesServiceTest.jar", ResourceType.FILE, mockMultipartFile,
                "/");
        logger.info(result.toString());
        assertEquals(Status.RESOURCE_SUFFIX_FORBID_CHANGE.getMsg(), result.getMsg());

        // UDF_RESOURCE_SUFFIX_NOT_JAR
        mockMultipartFile =
                new MockMultipartFile("ResourcesServiceTest.pdf", "ResourcesServiceTest.pdf", "pdf", "test".getBytes());
        when(Files.getFileExtension("ResourcesServiceTest.pdf")).thenReturn("pdf");
        result = resourcesService.uploadResource(user, "ResourcesServiceTest.pdf", ResourceType.UDF, mockMultipartFile,
                "/");
        logger.info(result.toString());
        assertEquals(Status.UDF_RESOURCE_SUFFIX_NOT_JAR.getMsg(), result.getMsg());

        // FULL_FILE_NAME_TOO_LONG
        String tooLongFileName = getRandomStringWithLength(Constants.RESOURCE_FULL_NAME_MAX_LENGTH) + ".pdf";
        mockMultipartFile = new MockMultipartFile(tooLongFileName, tooLongFileName, "pdf", "test".getBytes());
        when(Files.getFileExtension(tooLongFileName)).thenReturn("pdf");
        // '/databasePath/tenantCode/RESOURCE/'
        when(storageOperate.getResDir(tenantCode)).thenReturn("/dolphinscheduler/123/resources/");
        result = resourcesService.uploadResource(user, tooLongFileName, ResourceType.FILE, mockMultipartFile, "/");
        logger.info(result.toString());
        assertEquals(Status.RESOURCE_FULL_NAME_TOO_LONG_ERROR.getMsg(), result.getMsg());
    }

    @Test
    public void testCreateDirecotry() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.GENERAL_USER);

        // RESOURCE_EXIST
        user.setId(1);
        user.setTenantId(1);
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(userMapper.selectById(user.getId())).thenReturn(getUser());
        when(storageOperate.getResDir(tenantCode)).thenReturn("/dolphinscheduler/123/resources/");
        try {
            when(storageOperate.exists("/dolphinscheduler/123/resources/directoryTest")).thenReturn(true);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        Result result = resourcesService.createDirectory(user, "directoryTest", ResourceType.FILE, -1, "/");
        logger.info(result.toString());
        assertEquals(Status.RESOURCE_EXIST.getMsg(), result.getMsg());
    }

    @Test
    public void testUpdateResource() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.GENERAL_USER);
        user.setTenantId(1);

        when(userMapper.selectById(user.getId())).thenReturn(getUser());
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(storageOperate.getResDir(tenantCode)).thenReturn("/dolphinscheduler/123/resources/");

        // USER_NO_OPERATION_PERM
        user.setUserType(UserType.GENERAL_USER);
        // tenant who have access to resource is 123,
        Tenant tenantWNoPermission = new Tenant();
        tenantWNoPermission.setTenantCode("321");
        when(tenantMapper.queryById(1)).thenReturn(tenantWNoPermission);
        Result result = resourcesService.updateResource(user, "/dolphinscheduler/123/resources/ResourcesServiceTest",
                tenantCode, "ResourcesServiceTest", ResourceType.FILE, null);
        logger.info(result.toString());
        assertEquals(Status.NO_CURRENT_OPERATING_PERMISSION.getMsg(), result.getMsg());

        // SUCCESS
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        try {
            when(storageOperate.exists(Mockito.any())).thenReturn(false);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            when(storageOperate.getFileStatus("/dolphinscheduler/123/resources/ResourcesServiceTest",
                    "/dolphinscheduler/123/resources/", tenantCode, ResourceType.FILE))
                            .thenReturn(getStorageEntityResource());
            result = resourcesService.updateResource(user, "/dolphinscheduler/123/resources/ResourcesServiceTest",
                    tenantCode, "ResourcesServiceTest", ResourceType.FILE, null);
            logger.info(result.toString());
            assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
        } catch (Exception e) {
            logger.error(e.getMessage() + " Resource path: {}", "/dolphinscheduler/123/resources/ResourcesServiceTest",
                    e);
        }

        // Tests for udf resources.
        // RESOURCE_EXIST
        try {
            when(storageOperate.exists("/dolphinscheduler/123/resources/ResourcesServiceTest2.jar")).thenReturn(true);
        } catch (IOException e) {
            logger.error("error occurred when checking resource: "
                    + "/dolphinscheduler/123/resources/ResourcesServiceTest2.jar");
        }

        try {
            when(storageOperate.getFileStatus("/dolphinscheduler/123/resources/ResourcesServiceTest1.jar",
                    "/dolphinscheduler/123/resources/", tenantCode, ResourceType.UDF))
                            .thenReturn(getStorageEntityUdfResource());
        } catch (Exception e) {
            logger.error(e.getMessage() + " Resource path: {}",
                    "/dolphinscheduler/123/resources/ResourcesServiceTest1.jar", e);
        }
        result = resourcesService.updateResource(user, "/dolphinscheduler/123/resources/ResourcesServiceTest1.jar",
                tenantCode, "ResourcesServiceTest2.jar", ResourceType.UDF, null);
        logger.info(result.toString());
        assertEquals(Status.RESOURCE_EXIST.getMsg(), result.getMsg());

        // TENANT_NOT_EXIST
        when(tenantMapper.queryById(Mockito.anyInt())).thenReturn(null);
        Assertions.assertThrows(ServiceException.class, () -> resourcesService.updateResource(user,
                "ResourcesServiceTest1.jar", "", "ResourcesServiceTest", ResourceType.UDF, null));

        // SUCCESS
        when(tenantMapper.queryById(1)).thenReturn(getTenant());

        result = resourcesService.updateResource(user, "/dolphinscheduler/123/resources/ResourcesServiceTest1.jar",
                tenantCode, "ResourcesServiceTest1.jar", ResourceType.UDF, null);
        logger.info(result.toString());
        assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testQueryResourceListPaging() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setTenantId(1);
        loginUser.setTenantCode("tenant1");
        loginUser.setUserType(UserType.ADMIN_USER);
        List<StorageEntity> mockResList = new ArrayList<StorageEntity>();
        mockResList.add(getStorageEntityResource());
        List<User> mockUserList = new ArrayList<User>();
        mockUserList.add(getUser());
        when(userMapper.selectList(null)).thenReturn(mockUserList);
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(tenantMapper.queryById(getUser().getTenantId())).thenReturn(getTenant());
        when(storageOperate.getResDir(tenantCode)).thenReturn("/dolphinscheduler/123/resources/");

        try {
            when(storageOperate.listFilesStatus("/dolphinscheduler/123/resources/", "/dolphinscheduler/123/resources/",
                    tenantCode, ResourceType.FILE)).thenReturn(mockResList);
        } catch (Exception e) {
            logger.error("QueryResourceListPaging Error");
        }
        Result result = resourcesService.queryResourceListPaging(loginUser, "", "", ResourceType.FILE, "Test", 1, 10);
        logger.info(result.toString());
        assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        PageInfo pageInfo = (PageInfo) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));

    }

    @Test
    public void testQueryResourceList() {
        User loginUser = getUser();

        when(userMapper.selectList(null)).thenReturn(Collections.singletonList(loginUser));
        when(userMapper.selectById(loginUser.getId())).thenReturn(loginUser);
        when(tenantMapper.queryById(Mockito.anyInt())).thenReturn(getTenant());
        when(storageOperate.getDir(ResourceType.ALL, tenantCode)).thenReturn("/dolphinscheduler");
        when(storageOperate.getDir(ResourceType.FILE, tenantCode)).thenReturn("/dolphinscheduler/123/resources/");
        when(storageOperate.getResDir(tenantCode)).thenReturn("/dolphinscheduler/123/resources/");
        when(storageOperate.listFilesStatusRecursively("/dolphinscheduler/123/resources/",
                "/dolphinscheduler/123/resources/", tenantCode, ResourceType.FILE))
                        .thenReturn(Collections.singletonList(getStorageEntityResource()));
        Map<String, Object> result =
                resourcesService.queryResourceList(loginUser, ResourceType.FILE, "/dolphinscheduler/123/resources/");
        assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<ResourceComponent> resourceList = (List<ResourceComponent>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(resourceList));

        // test udf
        when(storageOperate.getDir(ResourceType.UDF, tenantCode)).thenReturn("/dolphinscheduler/123/udfs/");
        when(storageOperate.getUdfDir(tenantCode)).thenReturn("/dolphinscheduler/123/udfs/");
        when(storageOperate.listFilesStatusRecursively("/dolphinscheduler/123/udfs/", "/dolphinscheduler/123/udfs/",
                tenantCode, ResourceType.UDF)).thenReturn(Arrays.asList(getStorageEntityUdfResource()));
        loginUser.setUserType(UserType.GENERAL_USER);
        result = resourcesService.queryResourceList(loginUser, ResourceType.UDF, "/dolphinscheduler/123/udfs/");
        assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        resourceList = (List<ResourceComponent>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(resourceList));
    }

    @Test
    public void testDelete() throws Exception {

        User loginUser = new User();
        loginUser.setId(0);
        loginUser.setUserType(UserType.GENERAL_USER);

        // TENANT_NOT_EXIST
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setTenantId(2);
        when(userMapper.selectById(loginUser.getId())).thenReturn(loginUser);
        Assertions.assertThrows(ServiceException.class, () -> resourcesService.delete(loginUser, "", ""));

        // RESOURCE_NOT_EXIST
        when(tenantMapper.queryById(Mockito.anyInt())).thenReturn(getTenant());
        when(storageOperate.getDir(ResourceType.ALL, tenantCode)).thenReturn("/dolphinscheduler");
        when(storageOperate.getResDir(getTenant().getTenantCode())).thenReturn("/dolphinscheduler/123/resources/");
        when(storageOperate.getFileStatus("/dolphinscheduler/123/resources/ResourcesServiceTest",
                "/dolphinscheduler/123/resources/", tenantCode, null))
                        .thenReturn(getStorageEntityResource());
        Result result = resourcesService.delete(loginUser, "/dolphinscheduler/123/resources/ResNotExist", tenantCode);
        assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), result.getMsg());

        // SUCCESS
        loginUser.setTenantId(1);
        result = resourcesService.delete(loginUser, "/dolphinscheduler/123/resources/ResourcesServiceTest", tenantCode);
        assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testVerifyResourceName() {

        User user = new User();
        user.setId(1);
        user.setUserType(UserType.GENERAL_USER);
        try {
            when(storageOperate.exists("/ResourcesServiceTest.jar")).thenReturn(true);
        } catch (IOException e) {
            logger.error("error occurred when checking resource: /ResourcesServiceTest.jar\"");
        }
        Result result = resourcesService.verifyResourceName("/ResourcesServiceTest.jar", ResourceType.FILE, user);
        logger.info(result.toString());
        assertEquals(Status.RESOURCE_EXIST.getMsg(), result.getMsg());

        // RESOURCE_FILE_EXIST
        result = resourcesService.verifyResourceName("/ResourcesServiceTest.jar", ResourceType.FILE, user);
        logger.info(result.toString());
        Assertions.assertTrue(Status.RESOURCE_EXIST.getCode() == result.getCode());

        // SUCCESS
        result = resourcesService.verifyResourceName("test2", ResourceType.FILE, user);
        logger.info(result.toString());
        assertEquals(Status.SUCCESS.getMsg(), result.getMsg());

    }

    @Test
    public void testReadResource() throws IOException {
        // RESOURCE_NOT_EXIST
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(tenantMapper.queryById(getUser().getTenantId())).thenReturn(getTenant());
        Result result = resourcesService.readResource(getUser(), "", "", 1, 10);
        assertEquals(Status.RESOURCE_FILE_NOT_EXIST.getCode(), (int) result.getCode());

        // RESOURCE_SUFFIX_NOT_SUPPORT_VIEW
        when(FileUtils.getResourceViewSuffixes()).thenReturn("class");
        result = resourcesService.readResource(getUser(), "", "", 1, 10);
        assertEquals(Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW.getMsg(), result.getMsg());

        // USER_NOT_EXIST
        when(userMapper.selectById(getUser().getId())).thenReturn(null);
        when(FileUtils.getResourceViewSuffixes()).thenReturn("jar");
        when(Files.getFileExtension("ResourcesServiceTest.jar")).thenReturn("jar");
        result = resourcesService.readResource(getUser(), "", "", 1, 10);
        assertEquals(Status.USER_NOT_EXIST.getCode(), (int) result.getCode());

        // TENANT_NOT_EXIST
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(tenantMapper.queryById(getUser().getTenantId())).thenReturn(null);
        Assertions.assertThrows(ServiceException.class, () -> resourcesService.readResource(getUser(), "", "", 1, 10));

        // SUCCESS
        when(FileUtils.getResourceViewSuffixes()).thenReturn("jar,sh");
        when(storageOperate.getDir(ResourceType.ALL, tenantCode)).thenReturn("/dolphinscheduler");
        when(storageOperate.getResDir(getTenant().getTenantCode())).thenReturn("/dolphinscheduler/123/resources/");
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(tenantMapper.queryById(getUser().getTenantId())).thenReturn(getTenant());
        when(storageOperate.exists(Mockito.any())).thenReturn(true);
        when(storageOperate.vimFile(Mockito.any(), Mockito.any(), eq(1), eq(10))).thenReturn(getContent());
        when(Files.getFileExtension("/dolphinscheduler/123/resources/test.jar")).thenReturn("jar");
        result = resourcesService.readResource(getUser(), "/dolphinscheduler/123/resources/test.jar", tenantCode, 1,
                10);
        assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testCreateOrUpdateResource() throws Exception {
        User user = getUser();
        when(userMapper.queryByUserNameAccurately(user.getUserName())).thenReturn(getUser());

        // RESOURCE_SUFFIX_NOT_SUPPORT_VIEW
        exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> resourcesService.createOrUpdateResource(user.getUserName(), "filename", "my-content"));
        Assertions.assertTrue(
                exception.getMessage().contains("Not allow create or update resources without extension name"));

        // SUCCESS
        when(storageOperate.getResDir(user.getTenantCode())).thenReturn("/dolphinscheduler/123/resources/");
        when(FileUtils.getUploadFilename(Mockito.anyString(), Mockito.anyString())).thenReturn("test");
        when(FileUtils.writeContent2File(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        when(storageOperate.getFileStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(getStorageEntityResource());
        StorageEntity storageEntity =
                resourcesService.createOrUpdateResource(user.getUserName(), "filename.txt", "my-content");
        Assertions.assertNotNull(storageEntity);
        assertEquals("/dolphinscheduler/123/resources/ResourcesServiceTest", storageEntity.getFullName());
    }

    @Test
    public void testUpdateResourceContent() throws Exception {
        // RESOURCE_PATH_ILLEGAL
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(storageOperate.getResDir(Mockito.anyString())).thenReturn("/tmp");
        ServiceException serviceException =
                Assertions.assertThrows(ServiceException.class, () -> resourcesService.updateResourceContent(getUser(),
                        "/dolphinscheduler/123/resources/ResourcesServiceTest.jar", tenantCode, "content"));
        assertTrue(serviceException.getMessage()
                .contains("Resource file: /dolphinscheduler/123/resources/ResourcesServiceTest.jar is illegal"));

        // RESOURCE_NOT_EXIST
        when(storageOperate.getDir(ResourceType.ALL, tenantCode)).thenReturn("/dolphinscheduler");
        when(storageOperate.getResDir(Mockito.anyString())).thenReturn("/dolphinscheduler/123/resources");
        when(storageOperate.getFileStatus("/dolphinscheduler/123/resources/ResourcesServiceTest.jar", "", tenantCode,
                ResourceType.FILE)).thenReturn(null);
        Result result = resourcesService.updateResourceContent(getUser(),
                "/dolphinscheduler/123/resources/ResourcesServiceTest.jar", tenantCode, "content");
        assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), result.getMsg());

        // RESOURCE_SUFFIX_NOT_SUPPORT_VIEW
        when(FileUtils.getResourceViewSuffixes()).thenReturn("class");
        when(storageOperate.getFileStatus("/dolphinscheduler/123/resources", "", tenantCode, ResourceType.FILE))
                .thenReturn(getStorageEntityResource());

        result = resourcesService.updateResourceContent(getUser(), "/dolphinscheduler/123/resources", tenantCode,
                "content");
        assertEquals(Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW.getMsg(), result.getMsg());

        // USER_NOT_EXIST
        when(userMapper.selectById(getUser().getId())).thenReturn(null);
        result = resourcesService.updateResourceContent(getUser(), "/dolphinscheduler/123/resources/123.class",
                tenantCode,
                "content");
        Assertions.assertTrue(Status.USER_NOT_EXIST.getCode() == result.getCode());

        // TENANT_NOT_EXIST
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(tenantMapper.queryById(1)).thenReturn(null);
        Assertions.assertThrows(ServiceException.class, () -> resourcesService.updateResourceContent(getUser(),
                "/dolphinscheduler/123/resources/ResourcesServiceTest.jar", tenantCode, "content"));

        // SUCCESS
        when(storageOperate.getFileStatus("/dolphinscheduler/123/resources/ResourcesServiceTest.jar", "", tenantCode,
                ResourceType.FILE)).thenReturn(getStorageEntityResource());

        when(Files.getFileExtension(Mockito.anyString())).thenReturn("jar");
        when(FileUtils.getResourceViewSuffixes()).thenReturn("jar");
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(FileUtils.getUploadFilename(Mockito.anyString(), Mockito.anyString())).thenReturn("test");
        when(FileUtils.writeContent2File(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        result = resourcesService.updateResourceContent(getUser(),
                "/dolphinscheduler/123/resources/ResourcesServiceTest.jar", tenantCode, "content");
        logger.info(result.toString());
        assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testDownloadResource() {
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(userMapper.selectById(1)).thenReturn(getUser());
        org.springframework.core.io.Resource resourceMock = Mockito.mock(org.springframework.core.io.Resource.class);
        Path path = Mockito.mock(Path.class);
        when(Paths.get(Mockito.any())).thenReturn(path);
        try {
            when(java.nio.file.Files.size(Mockito.any())).thenReturn(1L);
            // resource null
            org.springframework.core.io.Resource resource = resourcesService.downloadResource(getUser(), "");
            Assertions.assertNull(resource);

            when(org.apache.dolphinscheduler.api.utils.FileUtils.file2Resource(Mockito.any())).thenReturn(resourceMock);
            resource = resourcesService.downloadResource(getUser(), "");
            Assertions.assertNotNull(resource);
        } catch (Exception e) {
            logger.error("DownloadResource error", e);
            Assertions.assertTrue(false);
        }

    }

    @Test
    public void testDeleteDataTransferData() throws Exception {
        User user = getUser();
        when(userMapper.selectById(user.getId())).thenReturn(getUser());
        when(tenantMapper.queryById(user.getTenantId())).thenReturn(getTenant());

        StorageEntity storageEntity1 = Mockito.mock(StorageEntity.class);
        StorageEntity storageEntity2 = Mockito.mock(StorageEntity.class);
        StorageEntity storageEntity3 = Mockito.mock(StorageEntity.class);
        StorageEntity storageEntity4 = Mockito.mock(StorageEntity.class);
        StorageEntity storageEntity5 = Mockito.mock(StorageEntity.class);

        when(storageEntity1.getFullName()).thenReturn("DATA_TRANSFER/20220101");
        when(storageEntity2.getFullName()).thenReturn("DATA_TRANSFER/20220102");
        when(storageEntity3.getFullName()).thenReturn("DATA_TRANSFER/20220103");
        when(storageEntity4.getFullName()).thenReturn("DATA_TRANSFER/20220104");
        when(storageEntity5.getFullName()).thenReturn("DATA_TRANSFER/20220105");

        List<StorageEntity> storageEntityList = new ArrayList<>();
        storageEntityList.add(storageEntity1);
        storageEntityList.add(storageEntity2);
        storageEntityList.add(storageEntity3);
        storageEntityList.add(storageEntity4);
        storageEntityList.add(storageEntity5);

        when(storageOperate.listFilesStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(storageEntityList);

        LocalDateTime localDateTime = LocalDateTime.of(2022, 1, 5, 0, 0, 0);
        try (MockedStatic<LocalDateTime> mockHook = Mockito.mockStatic(LocalDateTime.class)) {
            mockHook.when(LocalDateTime::now).thenReturn(localDateTime);
            DeleteDataTransferResponse response = resourcesService.deleteDataTransferData(user, 3);

            assertEquals(response.getSuccessList().size(), 2);
            assertEquals(response.getSuccessList().get(0), "DATA_TRANSFER/20220101");
            assertEquals(response.getSuccessList().get(1), "DATA_TRANSFER/20220102");
        }

        try (MockedStatic<LocalDateTime> mockHook = Mockito.mockStatic(LocalDateTime.class)) {
            mockHook.when(LocalDateTime::now).thenReturn(localDateTime);
            DeleteDataTransferResponse response = resourcesService.deleteDataTransferData(user, 0);
            assertEquals(response.getSuccessList().size(), 5);
        }

    }

    @Test
    public void testCatFile() {
        // SUCCESS
        try {
            List<String> list = storageOperate.vimFile(Mockito.any(), Mockito.anyString(), eq(1), eq(10));
            Assertions.assertNotNull(list);

        } catch (IOException e) {
            logger.error("hadoop error", e);
        }
    }

    @Test
    void testQueryBaseDir() {
        User user = getUser();
        when(userMapper.selectById(user.getId())).thenReturn(getUser());
        when(tenantMapper.queryById(user.getTenantId())).thenReturn(getTenant());
        when(storageOperate.getDir(ResourceType.FILE, tenantCode)).thenReturn("/dolphinscheduler/123/resources/");
        try {
            when(storageOperate.getFileStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                    Mockito.any())).thenReturn(getStorageEntityResource());
        } catch (Exception e) {
            logger.error(e.getMessage() + " Resource path: {}", "/dolphinscheduler/123/resources/ResourcesServiceTest",
                    e);
        }
        Result<Object> result = resourcesService.queryResourceBaseDir(user, ResourceType.FILE);
        assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    private Tenant getTenant() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode(tenantCode);
        return tenant;
    }

    private User getUser() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.GENERAL_USER);
        user.setTenantId(1);
        user.setTenantCode(tenantCode);
        return user;
    }

    private StorageEntity getStorageEntityResource() {
        StorageEntity entity = new StorageEntity();
        entity.setAlias("ResourcesServiceTest");
        entity.setFileName("ResourcesServiceTest");
        entity.setDirectory(false);
        entity.setUserName(tenantCode);
        entity.setType(ResourceType.FILE);
        entity.setFullName("/dolphinscheduler/123/resources/ResourcesServiceTest");
        return entity;
    }

    private StorageEntity getStorageEntityUdfResource() {
        StorageEntity entity = new StorageEntity();
        entity.setAlias("ResourcesServiceTest1.jar");
        entity.setFileName("ResourcesServiceTest1.jar");
        entity.setDirectory(false);
        entity.setUserName(tenantCode);
        entity.setType(ResourceType.UDF);
        entity.setFullName("/dolphinscheduler/123/resources/ResourcesServiceTest1.jar");

        return entity;
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
