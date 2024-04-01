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
import org.springframework.mock.web.MockMultipartFile;

import com.google.common.io.Files;

/**
 * resources service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ResourcesServiceTest {

    private static final String basePath = "/dolphinscheduler";
    private static final String tenantCode = "123";
    private static final String tenantFileResourceDir = "/dolphinscheduler/123/resources/";
    private static final String tenantUdfResourceDir = "/dolphinscheduler/123/udfs/";

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
        ServiceException serviceException = Assertions.assertThrows(ServiceException.class,
                () -> resourcesService.uploadResource(user, "ResourcesServiceTest", ResourceType.FILE,
                        new MockMultipartFile("test.pdf", "test.pdf", "pdf", "test".getBytes()), "/"));
        assertEquals(Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST.getMsg(), serviceException.getMessage());

        // set tenant for user
        user.setTenantId(1);
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(storageOperate.getDir(ResourceType.ALL, tenantCode)).thenReturn(basePath);

        // ILLEGAL_RESOURCE_FILE
        String illegal_path = "/dolphinscheduler/123/../";
        serviceException = Assertions.assertThrows(ServiceException.class,
                () -> {
                    MockMultipartFile mockMultipartFile = new MockMultipartFile("test.pdf", "".getBytes());
                    resourcesService.uploadResource(user, "ResourcesServiceTest", ResourceType.FILE,
                            mockMultipartFile, illegal_path);
                });
        assertEquals(new ServiceException(Status.ILLEGAL_RESOURCE_PATH, illegal_path), serviceException);

        // RESOURCE_FILE_IS_EMPTY
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test.pdf", "".getBytes());
        Result result = resourcesService.uploadResource(user, "ResourcesServiceTest", ResourceType.FILE,
                mockMultipartFile, tenantFileResourceDir);
        assertEquals(Status.RESOURCE_FILE_IS_EMPTY.getMsg(), result.getMsg());

        // RESOURCE_SUFFIX_FORBID_CHANGE
        mockMultipartFile = new MockMultipartFile("test.pdf", "test.pdf", "pdf", "test".getBytes());
        when(Files.getFileExtension("test.pdf")).thenReturn("pdf");
        when(Files.getFileExtension("ResourcesServiceTest.jar")).thenReturn("jar");
        result = resourcesService.uploadResource(user, "ResourcesServiceTest.jar", ResourceType.FILE, mockMultipartFile,
                tenantFileResourceDir);
        assertEquals(Status.RESOURCE_SUFFIX_FORBID_CHANGE.getMsg(), result.getMsg());

        // UDF_RESOURCE_SUFFIX_NOT_JAR
        mockMultipartFile =
                new MockMultipartFile("ResourcesServiceTest.pdf", "ResourcesServiceTest.pdf", "pdf", "test".getBytes());
        when(Files.getFileExtension("ResourcesServiceTest.pdf")).thenReturn("pdf");
        result = resourcesService.uploadResource(user, "ResourcesServiceTest.pdf", ResourceType.UDF, mockMultipartFile,
                tenantUdfResourceDir);
        assertEquals(Status.UDF_RESOURCE_SUFFIX_NOT_JAR.getMsg(), result.getMsg());

        // FULL_FILE_NAME_TOO_LONG
        String tooLongFileName = getRandomStringWithLength(Constants.RESOURCE_FULL_NAME_MAX_LENGTH) + ".pdf";
        mockMultipartFile = new MockMultipartFile(tooLongFileName, tooLongFileName, "pdf", "test".getBytes());
        when(Files.getFileExtension(tooLongFileName)).thenReturn("pdf");

        // '/databasePath/tenantCode/RESOURCE/'
        when(storageOperate.getResDir(tenantCode)).thenReturn(tenantFileResourceDir);
        result = resourcesService.uploadResource(user, tooLongFileName, ResourceType.FILE, mockMultipartFile,
                tenantFileResourceDir);
        assertEquals(Status.RESOURCE_FULL_NAME_TOO_LONG_ERROR.getMsg(), result.getMsg());
    }

    @Test
    public void testCreateDirecotry() throws IOException {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.GENERAL_USER);

        String fileName = "directoryTest";
        // RESOURCE_EXIST
        user.setId(1);
        user.setTenantId(1);
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(userMapper.selectById(user.getId())).thenReturn(getUser());
        when(storageOperate.getDir(ResourceType.ALL, tenantCode)).thenReturn(basePath);
        when(storageOperate.getResDir(tenantCode)).thenReturn(tenantFileResourceDir);
        when(storageOperate.exists(tenantFileResourceDir + fileName)).thenReturn(true);
        Result result = resourcesService.createDirectory(user, fileName, ResourceType.FILE, -1, tenantFileResourceDir);
        assertEquals(Status.RESOURCE_EXIST.getMsg(), result.getMsg());
    }

    @Test
    public void testUpdateResource() throws Exception {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.GENERAL_USER);
        user.setTenantId(1);

        when(userMapper.selectById(user.getId())).thenReturn(getUser());
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(storageOperate.getDir(ResourceType.ALL, tenantCode)).thenReturn(basePath);
        when(storageOperate.getResDir(tenantCode)).thenReturn(tenantFileResourceDir);

        // TENANT_NOT_EXIST
        when(tenantMapper.queryById(Mockito.anyInt())).thenReturn(null);
        Assertions.assertThrows(ServiceException.class, () -> resourcesService.updateResource(user,
                "ResourcesServiceTest1.jar", "", "ResourcesServiceTest", ResourceType.UDF, null));

        // USER_NO_OPERATION_PERM
        user.setUserType(UserType.GENERAL_USER);
        // tenant who have access to resource is 123,
        Tenant tenantWNoPermission = new Tenant();
        tenantWNoPermission.setTenantCode("321");
        when(tenantMapper.queryById(1)).thenReturn(tenantWNoPermission);
        when(storageOperate.getDir(ResourceType.ALL, "321")).thenReturn(basePath);

        String fileName = "ResourcesServiceTest";
        Result result = resourcesService.updateResource(user, tenantFileResourceDir + fileName,
                tenantCode, fileName, ResourceType.FILE, null);
        assertEquals(Status.NO_CURRENT_OPERATING_PERMISSION.getMsg(), result.getMsg());

        // SUCCESS
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(storageOperate.exists(Mockito.any())).thenReturn(false);

        when(storageOperate.getDir(ResourceType.FILE, tenantCode)).thenReturn(tenantFileResourceDir);
        when(storageOperate.getFileStatus(tenantFileResourceDir + fileName,
                tenantFileResourceDir, tenantCode, ResourceType.FILE))
                        .thenReturn(getStorageEntityResource(fileName));
        result = resourcesService.updateResource(user, tenantFileResourceDir + fileName,
                tenantCode, fileName, ResourceType.FILE, null);
        assertEquals(Status.SUCCESS.getMsg(), result.getMsg());

        // Tests for udf resources.
        fileName = "ResourcesServiceTest.jar";
        when(storageOperate.getDir(ResourceType.UDF, tenantCode)).thenReturn(tenantUdfResourceDir);
        when(storageOperate.exists(tenantUdfResourceDir + fileName)).thenReturn(true);
        when(storageOperate.getFileStatus(tenantUdfResourceDir + fileName, tenantUdfResourceDir, tenantCode,
                ResourceType.UDF))
                        .thenReturn(getStorageEntityUdfResource(fileName));
        result = resourcesService.updateResource(user, tenantUdfResourceDir + fileName,
                tenantCode, fileName, ResourceType.UDF, null);
        assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testQueryResourceListPaging() throws Exception {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setTenantId(1);
        loginUser.setTenantCode("tenant1");
        loginUser.setUserType(UserType.ADMIN_USER);

        String fileName = "ResourcesServiceTest";
        List<StorageEntity> mockResList = new ArrayList<>();
        mockResList.add(getStorageEntityResource(fileName));
        List<User> mockUserList = new ArrayList<>();
        mockUserList.add(getUser());
        when(userMapper.selectList(null)).thenReturn(mockUserList);
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(tenantMapper.queryById(getUser().getTenantId())).thenReturn(getTenant());
        when(storageOperate.getResDir(tenantCode)).thenReturn(tenantFileResourceDir);
        when(storageOperate.listFilesStatus(tenantFileResourceDir, tenantFileResourceDir,
                tenantCode, ResourceType.FILE)).thenReturn(mockResList);

        Result result = resourcesService.queryResourceListPaging(loginUser, "", "", ResourceType.FILE, "Test", 1, 10);
        assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        PageInfo pageInfo = (PageInfo) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));

    }

    @Test
    public void testQueryResourceList() {
        User loginUser = getUser();
        String fileName = "ResourcesServiceTest";

        when(userMapper.selectList(null)).thenReturn(Collections.singletonList(loginUser));
        when(userMapper.selectById(loginUser.getId())).thenReturn(loginUser);
        when(tenantMapper.queryById(Mockito.anyInt())).thenReturn(getTenant());
        when(storageOperate.getDir(ResourceType.ALL, tenantCode)).thenReturn(basePath);
        when(storageOperate.getDir(ResourceType.FILE, tenantCode)).thenReturn(tenantFileResourceDir);
        when(storageOperate.getResDir(tenantCode)).thenReturn(tenantFileResourceDir);
        when(storageOperate.listFilesStatusRecursively(tenantFileResourceDir,
                tenantFileResourceDir, tenantCode, ResourceType.FILE))
                        .thenReturn(Collections.singletonList(getStorageEntityResource(fileName)));
        Map<String, Object> result =
                resourcesService.queryResourceList(loginUser, ResourceType.FILE, tenantFileResourceDir);
        assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        List<ResourceComponent> resourceList = (List<ResourceComponent>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(resourceList));

        // test udf
        when(storageOperate.getDir(ResourceType.UDF, tenantCode)).thenReturn(tenantUdfResourceDir);
        when(storageOperate.getUdfDir(tenantCode)).thenReturn(tenantUdfResourceDir);
        when(storageOperate.listFilesStatusRecursively(tenantUdfResourceDir, tenantUdfResourceDir,
                tenantCode, ResourceType.UDF)).thenReturn(Arrays.asList(getStorageEntityUdfResource("test.jar")));
        loginUser.setUserType(UserType.GENERAL_USER);
        result = resourcesService.queryResourceList(loginUser, ResourceType.UDF, tenantUdfResourceDir);
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
        String fileName = "ResourcesServiceTest";
        when(tenantMapper.queryById(Mockito.anyInt())).thenReturn(getTenant());
        when(storageOperate.getDir(ResourceType.ALL, tenantCode)).thenReturn(basePath);
        when(storageOperate.getResDir(getTenant().getTenantCode())).thenReturn(tenantFileResourceDir);
        when(storageOperate.getFileStatus(tenantFileResourceDir + fileName, tenantFileResourceDir, tenantCode, null))
                .thenReturn(getStorageEntityResource(fileName));
        Result result = resourcesService.delete(loginUser, tenantFileResourceDir + "ResNotExist", tenantCode);
        assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), result.getMsg());

        // SUCCESS
        loginUser.setTenantId(1);
        result = resourcesService.delete(loginUser, tenantFileResourceDir + fileName, tenantCode);
        assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testVerifyResourceName() throws IOException {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.GENERAL_USER);

        String fileName = "ResourcesServiceTest";
        when(storageOperate.exists(tenantFileResourceDir + fileName)).thenReturn(true);

        Result result = resourcesService.verifyResourceName(tenantFileResourceDir + fileName, ResourceType.FILE, user);
        assertEquals(Status.RESOURCE_EXIST.getMsg(), result.getMsg());

        // RESOURCE_FILE_EXIST
        result = resourcesService.verifyResourceName(tenantFileResourceDir + fileName, ResourceType.FILE, user);
        Assertions.assertTrue(Status.RESOURCE_EXIST.getCode() == result.getCode());

        // SUCCESS
        result = resourcesService.verifyResourceName("test2", ResourceType.FILE, user);
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
        when(storageOperate.getDir(ResourceType.ALL, tenantCode)).thenReturn(basePath);
        when(storageOperate.getResDir(getTenant().getTenantCode())).thenReturn(tenantFileResourceDir);
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
        String fileName = "ResourcesServiceTest";
        when(storageOperate.getResDir(user.getTenantCode())).thenReturn(tenantFileResourceDir);
        when(FileUtils.getUploadFilename(Mockito.anyString(), Mockito.anyString())).thenReturn("test");
        when(FileUtils.writeContent2File(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        when(storageOperate.getFileStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(getStorageEntityResource(fileName));
        StorageEntity storageEntity =
                resourcesService.createOrUpdateResource(user.getUserName(), "filename.txt", "my-content");
        Assertions.assertNotNull(storageEntity);
        assertEquals(tenantFileResourceDir + fileName, storageEntity.getFullName());
    }

    @Test
    public void testUpdateResourceContent() throws Exception {
        // RESOURCE_PATH_ILLEGAL
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(storageOperate.getResDir(Mockito.anyString())).thenReturn("/tmp");

        String fileName = "ResourcesServiceTest.jar";
        ServiceException serviceException =
                Assertions.assertThrows(ServiceException.class, () -> resourcesService.updateResourceContent(getUser(),
                        tenantFileResourceDir + fileName, tenantCode, "content"));
        assertEquals(new ServiceException(Status.ILLEGAL_RESOURCE_PATH, tenantFileResourceDir + fileName),
                serviceException);

        // RESOURCE_NOT_EXIST
        when(storageOperate.getDir(ResourceType.ALL, tenantCode)).thenReturn(basePath);
        when(storageOperate.getResDir(Mockito.anyString())).thenReturn(tenantFileResourceDir);
        when(storageOperate.getFileStatus(tenantFileResourceDir + fileName, "", tenantCode, ResourceType.FILE))
                .thenReturn(null);
        Result result = resourcesService.updateResourceContent(getUser(), tenantFileResourceDir + fileName, tenantCode,
                "content");
        assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(), result.getMsg());

        // RESOURCE_SUFFIX_NOT_SUPPORT_VIEW
        when(FileUtils.getResourceViewSuffixes()).thenReturn("class");
        when(storageOperate.getFileStatus(tenantFileResourceDir, "", tenantCode, ResourceType.FILE))
                .thenReturn(getStorageEntityResource(fileName));

        result = resourcesService.updateResourceContent(getUser(), tenantFileResourceDir, tenantCode,
                "content");
        assertEquals(Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW.getMsg(), result.getMsg());

        // USER_NOT_EXIST
        when(userMapper.selectById(getUser().getId())).thenReturn(null);
        result = resourcesService.updateResourceContent(getUser(), tenantFileResourceDir + "123.class",
                tenantCode,
                "content");
        Assertions.assertTrue(Status.USER_NOT_EXIST.getCode() == result.getCode());

        // TENANT_NOT_EXIST
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(tenantMapper.queryById(1)).thenReturn(null);
        Assertions.assertThrows(ServiceException.class, () -> resourcesService.updateResourceContent(getUser(),
                tenantFileResourceDir + fileName, tenantCode, "content"));

        // SUCCESS
        when(storageOperate.getFileStatus(tenantFileResourceDir + fileName, "", tenantCode,
                ResourceType.FILE)).thenReturn(getStorageEntityResource(fileName));

        when(Files.getFileExtension(Mockito.anyString())).thenReturn("jar");
        when(FileUtils.getResourceViewSuffixes()).thenReturn("jar");
        when(userMapper.selectById(getUser().getId())).thenReturn(getUser());
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(FileUtils.getUploadFilename(Mockito.anyString(), Mockito.anyString())).thenReturn("test");
        when(FileUtils.writeContent2File(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        result = resourcesService.updateResourceContent(getUser(),
                tenantFileResourceDir + fileName, tenantCode, "content");
        assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testDownloadResource() throws IOException {
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(userMapper.selectById(1)).thenReturn(getUser());
        org.springframework.core.io.Resource resourceMock = Mockito.mock(org.springframework.core.io.Resource.class);
        Path path = Mockito.mock(Path.class);
        when(Paths.get(Mockito.any())).thenReturn(path);
        when(java.nio.file.Files.size(Mockito.any())).thenReturn(1L);
        // resource null
        org.springframework.core.io.Resource resource = resourcesService.downloadResource(getUser(), "");
        Assertions.assertNull(resource);

        when(org.apache.dolphinscheduler.api.utils.FileUtils.file2Resource(Mockito.any())).thenReturn(resourceMock);
        resource = resourcesService.downloadResource(getUser(), "");
        Assertions.assertNotNull(resource);
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
    public void testCatFile() throws IOException {
        // SUCCESS
        List<String> list = storageOperate.vimFile(Mockito.any(), Mockito.anyString(), eq(1), eq(10));
        Assertions.assertNotNull(list);
    }

    @Test
    void testQueryBaseDir() throws Exception {
        User user = getUser();
        String fileName = "ResourcesServiceTest.jar";
        when(userMapper.selectById(user.getId())).thenReturn(getUser());
        when(tenantMapper.queryById(user.getTenantId())).thenReturn(getTenant());
        when(storageOperate.getDir(ResourceType.FILE, tenantCode)).thenReturn(tenantFileResourceDir);
        when(storageOperate.getFileStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any())).thenReturn(getStorageEntityResource(fileName));
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

    private StorageEntity getStorageEntityResource(String fileName) {
        StorageEntity entity = new StorageEntity();
        entity.setAlias(fileName);
        entity.setFileName(fileName);
        entity.setDirectory(false);
        entity.setUserName(tenantCode);
        entity.setType(ResourceType.FILE);
        entity.setFullName(tenantFileResourceDir + fileName);
        return entity;
    }

    private StorageEntity getStorageEntityUdfResource(String fileName) {
        StorageEntity entity = new StorageEntity();
        entity.setAlias(fileName);
        entity.setFileName(fileName);
        entity.setDirectory(false);
        entity.setUserName(tenantCode);
        entity.setType(ResourceType.UDF);
        entity.setFullName(tenantUdfResourceDir + fileName);

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
