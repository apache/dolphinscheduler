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

import static org.apache.dolphinscheduler.api.utils.CheckUtils.checkFilePath;
import static org.apache.dolphinscheduler.common.constants.Constants.ALIAS;
import static org.apache.dolphinscheduler.common.constants.Constants.CONTENT;
import static org.apache.dolphinscheduler.common.constants.Constants.EMPTY_STRING;
import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_SS;
import static org.apache.dolphinscheduler.common.constants.Constants.JAR;
import static org.apache.dolphinscheduler.common.constants.Constants.PERIOD;

import org.apache.dolphinscheduler.api.dto.resources.DeleteDataTransferResponse;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.api.dto.resources.filter.ResourceFilter;
import org.apache.dolphinscheduler.api.dto.resources.visitor.ResourceTreeVisitor;
import org.apache.dolphinscheduler.api.dto.resources.visitor.Visitor;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.metrics.ApiServerMetrics;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.RegexUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.Files;

@Service
@Slf4j
public class ResourcesServiceImpl extends BaseServiceImpl implements ResourcesService {

    @Autowired
    private UdfFuncMapper udfFunctionMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false)
    private StorageOperate storageOperate;

    /**
     * create directory
     *
     * @param loginUser  login user
     * @param name       alias
     * @param type       type
     * @param pid        parent id
     * @param currentDir current directory
     * @return create directory result
     */
    @Override
    @Transactional
    public void createDirectory(User loginUser, String name, ResourceType type, int pid, String currentDir) {
        if (FileUtils.directoryTraversal(name)) {
            log.warn("Parameter name is invalid, name:{}.", RegexUtils.escapeNRT(name));
            throw new ServiceException(Status.VERIFY_PARAMETER_NAME_FAILED);
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            log.error("user {} not exists", loginUser.getId());
            throw new ServiceException(Status.USER_NOT_EXIST);
        }

        String tenantCode = getTenantCode(user);
        checkFullName(tenantCode, currentDir);

        String userResRootPath = ResourceType.UDF.equals(type) ? storageOperate.getUdfDir(tenantCode)
                : storageOperate.getResDir(tenantCode);
        String fullName = !currentDir.contains(userResRootPath) ? userResRootPath + name : currentDir + name;

        try {
            if (checkResourceExists(fullName)) {
                log.error("resource directory {} has exist, can't recreate", fullName);
                throw new ServiceException(Status.RESOURCE_EXIST);
            }
        } catch (Exception e) {
            log.warn("Resource exists, can not create again, fullName:{}.", fullName, e);
            throw new ServiceException("resource already exists, can't recreate");
        }

        // create directory in storage
        createDirectory(loginUser, fullName, type);
    }

    /**
     * create resource
     *
     * @param loginUser  login user
     * @param name       alias
     * @param type       type
     * @param file       file
     * @param currentDir current directory
     * @return create result code
     */
    @Override
    @Transactional
    public void uploadResource(User loginUser, String name, ResourceType type, MultipartFile file,
                               String currentDir) {
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            log.error("user {} not exists", loginUser.getId());
            throw new ServiceException(Status.USER_NOT_EXIST);
        }

        String tenantCode = getTenantCode(user);
        checkFullName(tenantCode, currentDir);

        verifyFile(name, type, file);

        // check resource name exists
        String userResRootPath = ResourceType.UDF.equals(type) ? storageOperate.getUdfDir(tenantCode)
                : storageOperate.getResDir(tenantCode);
        String currDirNFileName = !currentDir.contains(userResRootPath) ? userResRootPath + name : currentDir + name;

        try {
            if (checkResourceExists(currDirNFileName)) {
                log.error("resource {} has exist, can't recreate", RegexUtils.escapeNRT(name));
                throw new ServiceException(Status.RESOURCE_EXIST);
            }
        } catch (Exception e) {
            throw new ServiceException("resource already exists, can't recreate");
        }
        if (currDirNFileName.length() > Constants.RESOURCE_FULL_NAME_MAX_LENGTH) {
            log.error(
                    "Resource file's name is longer than max full name length, fullName:{}, "
                            + "fullNameSize:{}, maxFullNameSize:{}",
                    RegexUtils.escapeNRT(name), currDirNFileName.length(), Constants.RESOURCE_FULL_NAME_MAX_LENGTH);
            throw new ServiceException(Status.RESOURCE_FULL_NAME_TOO_LONG_ERROR);
        }

        boolean uploadSuccess = upload(loginUser, currDirNFileName, file, type);
        if (!uploadSuccess) {
            log.error("upload resource: {} file: {} failed.", RegexUtils.escapeNRT(name),
                    RegexUtils.escapeNRT(file.getOriginalFilename()));
            throw new ServiceException(
                    String.format("upload resource: %s file: %s failed.", name, file.getOriginalFilename()));
        }

        ApiServerMetrics.recordApiResourceUploadSize(file.getSize());
        log.info("Upload resource file complete, resourceName:{}, fileName:{}.", RegexUtils.escapeNRT(name),
                RegexUtils.escapeNRT(file.getOriginalFilename()));
    }

    /**
     * check resource is exists
     *
     * @param fullName fullName
     * @return true if resource exists
     */
    private boolean checkResourceExists(String fullName) {
        Boolean existResource = false;
        try {
            existResource = storageOperate.exists(fullName);
        } catch (IOException e) {
            log.error("error occurred when checking resource: " + fullName, e);
        }
        return Boolean.TRUE.equals(existResource);
    }

    /**
     * update resource
     *
     * @param loginUser        login user
     * @param resourceFullName resource full name
     * @param resTenantCode    tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                         can be different from the login user in the case of logging in as admin users.
     * @param name             name
     * @param type             resource type
     * @param file             resource file
     */
    @Override
    @Transactional
    public void updateResource(User loginUser, String resourceFullName, String resTenantCode, String name,
                               ResourceType type, MultipartFile file) {
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            log.error("user {} not exists", loginUser.getId());
            throw new ServiceException(Status.USER_NOT_EXIST, loginUser.getId());
        }

        String tenantCode = getTenantCode(user);
        checkFullName(tenantCode, resourceFullName);

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            throw new ServiceException(Status.NO_CURRENT_OPERATING_PERMISSION);
        }

        String defaultPath = storageOperate.getDir(type, tenantCode);

        StorageEntity resource;
        try {
            resource = storageOperate.getFileStatus(resourceFullName, defaultPath, resTenantCode, type);
        } catch (Exception e) {
            log.error("Get file status fail, resource path: {}", resourceFullName, e);
            throw new ServiceException((String.format("Get file status fail, resource path: %s", resourceFullName)));
        }

        // TODO: deal with OSS
        if (resource.isDirectory() && storageOperate.returnStorageType().equals(ResUploadType.S3)
                && !resource.getFileName().equals(name)) {
            log.warn("Directory in S3 storage can not be renamed.");
            throw new ServiceException(Status.S3_CANNOT_RENAME);
        }

        // check if updated name of the resource already exists
        String originFullName = resource.getFullName();
        String originResourceName = resource.getAlias();

        // the format of hdfs folders in the implementation has a "/" at the very end, we need to remove it.
        originFullName = originFullName.endsWith("/") ? StringUtils.chop(originFullName) : originFullName;
        name = name.endsWith("/") ? StringUtils.chop(name) : name;
        // updated fullName
        String fullName = String.format(FORMAT_SS,
                originFullName.substring(0, originFullName.lastIndexOf(FOLDER_SEPARATOR) + 1), name);
        if (!originResourceName.equals(name)) {
            try {
                if (checkResourceExists(fullName)) {
                    log.error("resource {} already exists, can't recreate", fullName);
                    throw new ServiceException(Status.RESOURCE_EXIST);
                }
            } catch (Exception e) {
                throw new ServiceException(String.format("error occurs while querying resource: %s", fullName));
            }

        }

        verifyFile(name, type, file);

        Date now = new Date();

        resource.setAlias(name);
        resource.setFileName(name);
        resource.setFullName(fullName);
        resource.setUpdateTime(now);
        if (file != null) {
            resource.setSize(file.getSize());
        }

        // if name unchanged, return directly without moving on HDFS
        if (originResourceName.equals(name) && file == null) {
            return;
        }

        if (file != null) {
            // fail upload
            if (!upload(loginUser, fullName, file, type)) {
                log.error("Storage operation error, resourceName:{}, originFileName:{}.", name,
                        RegexUtils.escapeNRT(file.getOriginalFilename()));
                throw new ServiceException(
                        String.format("upload resource: %s file: %s failed.", name, file.getOriginalFilename()));
            }
            if (!fullName.equals(originFullName)) {
                try {
                    storageOperate.delete(originFullName, false);
                } catch (IOException e) {
                    log.error("Resource delete error, resourceFullName:{}.", originFullName, e);
                    throw new ServiceException(String.format("delete resource: %s failed.", originFullName));
                }
            }

            ApiServerMetrics.recordApiResourceUploadSize(file.getSize());
            return;
        }

        // get the path of dest file in hdfs
        String destHdfsFileName = fullName;
        try {
            log.info("start  copy {} -> {}", originFullName, destHdfsFileName);
            storageOperate.copy(originFullName, destHdfsFileName, true, true);
        } catch (Exception e) {
            log.error(MessageFormat.format(" copy {0} -> {1} fail", originFullName, destHdfsFileName), e);
            throw new ServiceException(
                    MessageFormat.format(Status.HDFS_COPY_FAIL.getMsg(), originFullName, destHdfsFileName));
        }
    }

    private void verifyFile(String name, ResourceType type, MultipartFile file) {
        if (FileUtils.directoryTraversal(name)) {
            log.warn("Parameter file alias name verify failed, fileAliasName:{}.", RegexUtils.escapeNRT(name));
            throw new ServiceException(Status.VERIFY_PARAMETER_NAME_FAILED);
        }

        if (file == null) {
            return;
        }

        // file is empty
        if (file.isEmpty()) {
            log.warn("Parameter file is empty, fileOriginalName:{}.",
                    RegexUtils.escapeNRT(file.getOriginalFilename()));
            throw new ServiceException(Status.RESOURCE_FILE_IS_EMPTY);
        }

        if (FileUtils.directoryTraversal(Objects.requireNonNull(file.getOriginalFilename()))) {
            log.warn("File original name verify failed, fileOriginalName:{}.",
                    RegexUtils.escapeNRT(file.getOriginalFilename()));
            throw new ServiceException(Status.VERIFY_PARAMETER_NAME_FAILED);
        }

        // file suffix
        String fileSuffix = Files.getFileExtension(file.getOriginalFilename());
        String nameSuffix = Files.getFileExtension(name);

        // determine file suffix
        if (!fileSuffix.equalsIgnoreCase(nameSuffix)) {
            // rename file suffix and original suffix must be consistent
            log.warn("Rename file suffix and original suffix must be consistent, fileOriginalName:{}.",
                    RegexUtils.escapeNRT(file.getOriginalFilename()));
            throw new ServiceException(Status.RESOURCE_SUFFIX_FORBID_CHANGE);
        }

        // If resource type is UDF, only jar packages are allowed to be uploaded, and the suffix must be .jar
        if (Constants.UDF.equals(type.name()) && !JAR.equalsIgnoreCase(fileSuffix)) {
            throw new ServiceException(Status.UDF_RESOURCE_SUFFIX_NOT_JAR);
        }
        if (file.getSize() > Constants.MAX_FILE_SIZE) {
            log.warn(
                    "Resource file size is larger than max file size, fileOriginalName:{}, fileSize:{}, maxFileSize:{}.",
                    RegexUtils.escapeNRT(file.getOriginalFilename()), file.getSize(), Constants.MAX_FILE_SIZE);
            throw new ServiceException(Status.RESOURCE_SIZE_EXCEED_LIMIT);
        }
    }

    /**
     * query resources list paging
     *
     * @param loginUser     login user
     * @param fullName      resource full name
     * @param resTenantCode tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                      can be different from the login user in the case of logging in as admin users.
     * @param type          resource type
     * @param searchVal     search value
     * @param pageNo        page number
     * @param pageSize      page size
     * @return resource list page
     */
    @Override
    public PageInfo<StorageEntity> queryResourceListPaging(User loginUser, String fullName,
                                                           String resTenantCode, ResourceType type,
                                                           String searchVal, Integer pageNo, Integer pageSize) {
        Result<PageInfo<StorageEntity>> result = new Result<>();
        PageInfo<StorageEntity> pageInfo = new PageInfo<>(pageNo, pageSize);
        if (storageOperate == null) {
            log.warn("The resource storage is not opened.");
            return pageInfo;
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, loginUser.getId());
        }

        String tenantCode = getTenantCode(user);
        checkFullName(tenantCode, fullName);

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            throw new ServiceException(Status.NO_CURRENT_OPERATING_PERMISSION);
        }

        List<StorageEntity> resourcesList;
        try {
            resourcesList = queryStorageEntityList(loginUser, fullName, type, tenantCode, false);
        } catch (ServiceException e) {
            throw new ServiceException(Status.RESOURCE_NOT_EXIST);
        }

        // remove leading and trailing spaces in searchVal
        String trimmedSearchVal = searchVal != null ? searchVal.trim() : "";
        // filter based on trimmed searchVal
        List<StorageEntity> filteredResourceList = resourcesList.stream()
                .filter(x -> x.getFileName().contains(trimmedSearchVal)).collect(Collectors.toList());
        // inefficient pagination
        List<StorageEntity> slicedResourcesList = filteredResourceList.stream().skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize).collect(Collectors.toList());

        pageInfo.setTotal(filteredResourceList.size());
        pageInfo.setTotalList(slicedResourcesList);
        return pageInfo;
    }

    private List<StorageEntity> queryStorageEntityList(User loginUser, String fullName, ResourceType type,
                                                       String tenantCode, boolean recursive) {
        String defaultPath = "";
        List<StorageEntity> resourcesList = new ArrayList<>();
        String resourceStorageType =
                PropertyUtils.getString(Constants.RESOURCE_STORAGE_TYPE, ResUploadType.LOCAL.name());
        if (isAdmin(loginUser) && StringUtils.isBlank(fullName)) {
            // list all tenants' resources to admin users in the root directory
            List<User> userList = userMapper.selectList(null);
            Set<String> visitedTenantEntityCode = new HashSet<>();
            for (User userEntity : userList) {
                String tenantEntityCode = getTenantCode(userEntity);
                if (!visitedTenantEntityCode.contains(tenantEntityCode)) {
                    defaultPath = storageOperate.getResDir(tenantEntityCode);
                    if (type.equals(ResourceType.UDF)) {
                        defaultPath = storageOperate.getUdfDir(tenantEntityCode);
                    }
                    try {
                        resourcesList.addAll(recursive
                                ? storageOperate.listFilesStatusRecursively(defaultPath, defaultPath, tenantEntityCode,
                                        type)
                                : storageOperate.listFilesStatus(defaultPath, defaultPath, tenantEntityCode, type));

                        visitedTenantEntityCode.add(tenantEntityCode);
                    } catch (Exception e) {
                        log.error(e.getMessage() + " Resource path: {}", defaultPath, e);
                        throw new ServiceException(
                                String.format(e.getMessage() + " make sure resource path: %s exists in %s", defaultPath,
                                        resourceStorageType));
                    }
                }
            }
        } else {
            defaultPath = storageOperate.getResDir(tenantCode);
            if (type.equals(ResourceType.UDF)) {
                defaultPath = storageOperate.getUdfDir(tenantCode);
            }

            try {
                if (StringUtils.isBlank(fullName)) {
                    fullName = defaultPath;
                }
                resourcesList =
                        recursive ? storageOperate.listFilesStatusRecursively(fullName, defaultPath, tenantCode, type)
                                : storageOperate.listFilesStatus(fullName, defaultPath, tenantCode, type);
            } catch (Exception e) {
                log.error(e.getMessage() + " Resource path: {}", fullName, e);
                throw new ServiceException(String.format(e.getMessage() + " make sure resource path: %s exists in %s",
                        defaultPath, resourceStorageType));
            }
        }

        return resourcesList;
    }

    /**
     * create directory
     * xxx The steps to verify resources are cumbersome and can be optimized
     *
     * @param loginUser login user
     * @param fullName  full name
     * @param type      resource type
     */
    private void createDirectory(User loginUser, String fullName, ResourceType type) {
        String tenantCode = tenantMapper.queryById(loginUser.getTenantId()).getTenantCode();
        // String directoryName = storageOperate.getFileName(type, tenantCode, fullName);
        String resourceRootPath = storageOperate.getDir(type, tenantCode);
        try {
            if (!storageOperate.exists(resourceRootPath)) {
                storageOperate.createTenantDirIfNotExists(tenantCode);
            }

            if (!storageOperate.mkdir(tenantCode, fullName)) {
                throw new ServiceException(String.format("Create resource directory: %s failed.", fullName));
            }
        } catch (Exception e) {
            throw new ServiceException(String.format("create resource directory: %s failed.", fullName));
        }
    }

    /**
     * upload file to hdfs
     *
     * @param loginUser login user
     * @param fullName  full name
     * @param file      file
     * @param type      resource type
     * @return upload success return true, otherwise false
     */
    private boolean upload(User loginUser, String fullName, MultipartFile file, ResourceType type) {
        // save to local
        String fileSuffix = Files.getFileExtension(file.getOriginalFilename());
        String nameSuffix = Files.getFileExtension(fullName);

        // determine file suffix
        if (!fileSuffix.equalsIgnoreCase(nameSuffix)) {
            return false;
        }
        // query tenant
        String tenantCode = getTenantCode(loginUser);
        // random file name
        String localFilename = FileUtils.getUploadFilename(tenantCode, UUID.randomUUID().toString());

        // save file to hdfs, and delete original file
        String resourcePath = storageOperate.getDir(type, tenantCode);
        try {
            // if tenant dir not exists
            if (!storageOperate.exists(resourcePath)) {
                storageOperate.createTenantDirIfNotExists(tenantCode);
            }
            org.apache.dolphinscheduler.api.utils.FileUtils.copyInputStreamToFile(file, localFilename);
            storageOperate.upload(tenantCode, localFilename, fullName, true, true);
            FileUtils.deleteFile(localFilename);
        } catch (Exception e) {
            FileUtils.deleteFile(localFilename);
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * query resource list
     *
     * @param loginUser login user
     * @param type      resource type
     * @param fullName  resource full name
     * @return resource list
     */
    @Override
    public List<ResourceComponent> queryResourceList(User loginUser, ResourceType type, String fullName) {
        if (storageOperate == null) {
            return Collections.emptyList();
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, loginUser.getId());
        }

        String tenantCode = getTenantCode(user);
        checkFullName(tenantCode, fullName);

        String baseDir = storageOperate.getDir(type, tenantCode);

        List<StorageEntity> resourcesList = new ArrayList<>();
        if (StringUtils.isBlank(fullName)) {
            if (isAdmin(loginUser)) {
                List<User> userList = userMapper.selectList(null);
                Set<String> visitedTenantEntityCode = new HashSet<>();
                for (User userEntity : userList) {
                    String tenantEntityCode = getTenantCode(userEntity);
                    if (!visitedTenantEntityCode.contains(tenantEntityCode)) {
                        baseDir = storageOperate.getDir(type, tenantEntityCode);
                        resourcesList.addAll(storageOperate.listFilesStatusRecursively(baseDir, baseDir,
                                tenantEntityCode, type));
                        visitedTenantEntityCode.add(tenantEntityCode);
                    }
                }
            } else {
                resourcesList = storageOperate.listFilesStatusRecursively(baseDir, baseDir, tenantCode, type);
            }
        } else {
            resourcesList = storageOperate.listFilesStatusRecursively(fullName, baseDir, tenantCode, type);
        }

        Visitor resourceTreeVisitor = new ResourceTreeVisitor(resourcesList);
        return resourceTreeVisitor.visit(baseDir).getChildren();
    }

    /**
     * query resource list by program type
     *
     * @param loginUser login user
     * @param type      resource type
     * @return resource list
     */
    @Override
    public List<ResourceComponent> queryResourceByProgramType(User loginUser, ResourceType type,
                                                              ProgramType programType) {
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, loginUser.getId());
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            throw new ServiceException(Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
        }

        String tenantCode = tenant.getTenantCode();

        List<StorageEntity> allResourceList = queryStorageEntityList(loginUser, "", type, tenantCode, true);

        String suffix = ".jar";
        if (programType != null) {
            switch (programType) {
                case JAVA:
                case SCALA:
                    break;
                case PYTHON:
                    suffix = ".py";
                    break;
                default:
            }
        }
        List<StorageEntity> resources = new ResourceFilter(suffix, new ArrayList<>(allResourceList)).filter();
        Visitor visitor = new ResourceTreeVisitor(resources);
        return visitor.visit("").getChildren();
    }

    /**
     * delete resource
     *
     * @param loginUser     login user
     * @param fullName      resource full name
     * @param resTenantCode tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                      can be different from the login user in the case of logging in as admin users.
     * @return delete result code
     * @throws IOException exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(User loginUser, String fullName, String resTenantCode) throws IOException {
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, loginUser.getId());
        }

        String tenantCode = getTenantCode(user);
        checkFullName(tenantCode, fullName);

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            throw new ServiceException(Status.NO_CURRENT_OPERATING_PERMISSION);
        }

        String baseDir = storageOperate.getResDir(tenantCode);

        StorageEntity resource;
        try {
            resource = storageOperate.getFileStatus(fullName, baseDir, resTenantCode, null);
        } catch (Exception e) {
            throw new ServiceException(String.format(e.getMessage() + " Resource path: %s", fullName));
        }

        if (resource == null) {
            log.error("Resource does not exist, resource full name:{}.", fullName);
            throw new ServiceException(Status.RESOURCE_NOT_EXIST);
        }

        // recursively delete a folder
        List<String> allChildren =
                storageOperate.listFilesStatusRecursively(fullName, baseDir, resTenantCode, resource.getType())
                        .stream().map(storageEntity -> storageEntity.getFullName()).collect(Collectors.toList());

        String[] allChildrenFullNameArray = allChildren.stream().toArray(String[]::new);

        // if resource type is UDF,need check whether it is bound by UDF function
        if (resource.getType() == (ResourceType.UDF)) {
            List<UdfFunc> udfFuncs = udfFunctionMapper.listUdfByResourceFullName(allChildrenFullNameArray);
            if (CollectionUtils.isNotEmpty(udfFuncs)) {
                log.warn("Resource can not be deleted because it is bound by UDF functions, udfFuncIds:{}", udfFuncs);
                throw new ServiceException(Status.UDF_RESOURCE_IS_BOUND, udfFuncs.get(0).getFuncName());
            }
        }

        // delete file on hdfs,S3
        storageOperate.delete(fullName, allChildren, true);
    }

    /**
     * verify resource by name and type
     *
     * @param loginUser login user
     * @param fullName  resource full name
     * @param type      resource type
     * @return true if the resource name not exists, otherwise return false
     */
    @Override
    public void verifyResourceName(String fullName, ResourceType type, User loginUser) {
        if (checkResourceExists(fullName)) {
            log.error("Resource with same name exists so can not create again, resourceType:{}, resourceName:{}.", type,
                    RegexUtils.escapeNRT(fullName));
            throw new ServiceException(Status.RESOURCE_EXIST);
        }
    }

    /**
     * verify resource by full name or pid and type
     *
     * @param fileName      resource file name
     * @param type          resource type
     * @param resTenantCode tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                      can be different from the login user in the case of logging in as admin users.
     * @return true if the resource full name or pid not exists, otherwise return false
     */
    @Override
    public StorageEntity queryResourceByFileName(User loginUser, String fileName, ResourceType type,
                                                 String resTenantCode) {
        if (StringUtils.isBlank(fileName)) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR);
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, loginUser.getId());
        }

        String tenantCode = getTenantCode(user);

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            throw new ServiceException(Status.NO_CURRENT_OPERATING_PERMISSION);
        }

        String defaultPath = storageOperate.getDir(type, resTenantCode);
        StorageEntity file;
        try {
            file = storageOperate.getFileStatus(defaultPath + fileName, defaultPath, resTenantCode, type);
        } catch (Exception e) {
            log.error(e.getMessage() + " Resource path: {}", defaultPath + fileName, e);
            throw new ServiceException(Status.RESOURCE_NOT_EXIST);
        }

        return file;
    }

    /**
     * view resource file online
     *
     * @param fullName      resource fullName
     * @param resTenantCode owner's tenant code of the resource
     * @param skipLineNum   skip line number
     * @param limit         limit
     * @return resource content
     */
    @Override
    public Map<String, Object> readResource(User loginUser, String fullName, String resTenantCode, int skipLineNum,
                                            int limit) {
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            log.error("user {} not exists", loginUser.getId());
            throw new ServiceException(Status.USER_NOT_EXIST, loginUser.getId());
        }

        String tenantCode = getTenantCode(user);
        checkFullName(tenantCode, fullName);

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            throw new ServiceException(Status.NO_CURRENT_OPERATING_PERMISSION);
        }

        // check preview or not by file suffix
        String nameSuffix = Files.getFileExtension(fullName);
        String resourceViewSuffixes = FileUtils.getResourceViewSuffixes();
        if (StringUtils.isNotEmpty(resourceViewSuffixes)) {
            List<String> strList = Arrays.asList(resourceViewSuffixes.split(","));
            if (!strList.contains(nameSuffix)) {
                log.error("Resource suffix does not support view,resourceFullName:{}, suffix:{}.", fullName,
                        nameSuffix);
                throw new ServiceException(Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW);
            }
        }

        List<String> content;
        try {
            if (storageOperate.exists(fullName)) {
                content = storageOperate.vimFile(tenantCode, fullName, skipLineNum, limit);
                long size = content.stream().mapToLong(String::length).sum();
                ApiServerMetrics.recordApiResourceDownloadSize(size);
            } else {
                throw new ServiceException(Status.RESOURCE_FILE_NOT_EXIST, fullName);
            }

        } catch (Exception e) {
            log.error("Resource {} read failed", fullName, e);
            throw new ServiceException(Status.STORAGE_OPERATION_ERROR);
        }

        Map<String, Object> map = new HashMap<>();
        map.put(ALIAS, fullName);
        map.put(CONTENT, String.join("\n", content));
        return map;
    }

    /**
     * create resource file online
     *
     * @param loginUser  login user
     * @param type       resource type
     * @param fileName   file name
     * @param fileSuffix file suffix
     * @param content    content
     * @param currentDir current directory
     */
    @Override
    @Transactional
    public void createResourceFile(User loginUser, ResourceType type, String fileName, String fileSuffix,
                                   String content, String currentDir) {
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, loginUser.getId());
        }

        String tenantCode = getTenantCode(user);
        checkFullName(tenantCode, currentDir);

        if (FileUtils.directoryTraversal(fileName)) {
            log.warn("File name verify failed, fileName:{}.", RegexUtils.escapeNRT(fileName));
            throw new ServiceException(Status.VERIFY_PARAMETER_NAME_FAILED);
        }

        // check file suffix
        String nameSuffix = fileSuffix.trim();
        String resourceViewSuffixes = FileUtils.getResourceViewSuffixes();
        if (StringUtils.isNotEmpty(resourceViewSuffixes)) {
            List<String> strList = Arrays.asList(resourceViewSuffixes.split(","));
            if (!strList.contains(nameSuffix)) {
                log.warn("Resource suffix does not support view, suffix:{}.", nameSuffix);
                throw new ServiceException(Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW);
            }
        }

        String name = fileName.trim() + "." + nameSuffix;

        String userResRootPath = storageOperate.getResDir(tenantCode);
        String fullName = currentDir.contains(userResRootPath) ? currentDir + name : userResRootPath + name;

        verifyResourceName(fullName, type, loginUser);

        uploadContentToStorage(fullName, tenantCode, content);
    }

    @Override
    @Transactional
    public StorageEntity createOrUpdateResource(String userName, String filepath,
                                                String resourceContent) throws Exception {
        User user = userMapper.queryByUserNameAccurately(userName);
        int suffixLabelIndex = filepath.indexOf(PERIOD);
        if (suffixLabelIndex == -1) {
            throw new IllegalArgumentException(String
                    .format("Not allow create or update resources without extension name, filepath: %s", filepath));
        }

        String defaultPath = storageOperate.getResDir(user.getTenantCode());
        String fullName = defaultPath + filepath;

        uploadContentToStorage(fullName, user.getTenantCode(), resourceContent);
        return storageOperate.getFileStatus(fullName, defaultPath, user.getTenantCode(), ResourceType.FILE);
    }

    /**
     * updateProcessInstance resource
     *
     * @param fullName      resource full name
     * @param resTenantCode tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                      can be different from the login user in the case of logging in as admin users.
     * @param content       content
     * @return update result cod
     */
    @Override
    @Transactional
    public void updateResourceContent(User loginUser, String fullName, String resTenantCode, String content) {
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, loginUser.getId());
        }

        String tenantCode = getTenantCode(user);
        checkFullName(tenantCode, fullName);

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            throw new ServiceException(Status.NO_CURRENT_OPERATING_PERMISSION);
        }

        StorageEntity resource;
        try {
            resource = storageOperate.getFileStatus(fullName, "", resTenantCode, ResourceType.FILE);
        } catch (Exception e) {
            log.error("error occurred when fetching resource information ,  resource full name {}", fullName);
            throw new ServiceException(Status.RESOURCE_NOT_EXIST);
        }

        if (resource == null) {
            log.error("Resource does not exist, resource full name:{}.", fullName);
            throw new ServiceException(Status.RESOURCE_NOT_EXIST);
        }

        // check can edit by file suffix
        String nameSuffix = Files.getFileExtension(resource.getAlias());
        String resourceViewSuffixes = FileUtils.getResourceViewSuffixes();
        if (StringUtils.isNotEmpty(resourceViewSuffixes)) {
            List<String> strList = Arrays.asList(resourceViewSuffixes.split(","));
            if (!strList.contains(nameSuffix)) {
                log.warn("Resource suffix does not support view, resource full name:{}, suffix:{}.", fullName,
                        nameSuffix);
                throw new ServiceException(Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW);
            }
        }

        uploadContentToStorage(resource.getFullName(), resTenantCode, content);

        log.info("Update resource content complete, resource full name:{}.", fullName);
    }

    /**
     * @param fullName   resource full name
     * @param tenantCode tenant code
     * @param content    content
     * @return result
     */
    private void uploadContentToStorage(String fullName, String tenantCode, String content) {
        String localFilename = "";
        try {
            localFilename = FileUtils.getUploadFilename(tenantCode, UUID.randomUUID().toString());

            if (!FileUtils.writeContent2File(content, localFilename)) {
                // write file fail
                log.error("Write file error, fileName:{}, content:{}.", localFilename, RegexUtils.escapeNRT(content));
                throw new ServiceException(Status.RESOURCE_NOT_EXIST);
            }

            // get resource file path
            String resourcePath = storageOperate.getResDir(tenantCode);
            log.info("resource  path is {}, resource dir is {}", fullName, resourcePath);

            if (!storageOperate.exists(resourcePath)) {
                // create if tenant dir not exists
                storageOperate.createTenantDirIfNotExists(tenantCode);
                log.info("Create tenant dir because path {} does not exist, tenantCode:{}.", resourcePath, tenantCode);
            }
            if (storageOperate.exists(fullName)) {
                storageOperate.delete(fullName, false);
            }

            storageOperate.upload(tenantCode, localFilename, fullName, true, true);
        } catch (Exception e) {
            log.error("Upload content to storage error, tenantCode:{}, destFileName:{}.", tenantCode, localFilename, e);
            throw new ServiceException(Status.STORAGE_OPERATION_ERROR);
        } finally {
            FileUtils.deleteFile(localFilename);
        }
        log.info("Upload content to storage complete, tenantCode:{}, destFileName:{}.", tenantCode, localFilename);
    }

    /**
     * download file
     *
     * @return resource content
     */
    @Override
    public org.springframework.core.io.Resource downloadResource(User loginUser, String fullName) {
        if (fullName.endsWith("/")) {
            log.error("resource id {} is directory,can't download it", fullName);
            throw new ServiceException("can't download directory");
        }

        int userId = loginUser.getId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("User does not exits, userId:{}.", userId);
            throw new ServiceException(String.format("Resource owner id %d does not exist", userId));
        }

        String tenantCode = getTenantCode(user);
        checkFullName(tenantCode, fullName);

        String[] aliasArr = fullName.split("/");
        String alias = aliasArr[aliasArr.length - 1];
        String localFileName = FileUtils.getDownloadFilename(alias);
        log.info("Resource path is {}, download local filename is {}", alias, localFileName);

        try {
            storageOperate.download(fullName, localFileName, true);
            ApiServerMetrics.recordApiResourceDownloadSize(java.nio.file.Files.size(Paths.get(localFileName)));
            return org.apache.dolphinscheduler.api.utils.FileUtils.file2Resource(localFileName);
        } catch (IOException e) {
            log.error("Download resource error, the path is {}, and local filename is {}, the error message is {}",
                    fullName, localFileName, e.getMessage());
            throw new ServiceException("Download the resource file failed ,it may be related to your storage");
        }
    }

    @Override
    public StorageEntity queryFileStatus(String userName, String fileName) throws Exception {
        // TODO: It is used in PythonGateway, should be revised
        User user = userMapper.queryByUserNameAccurately(userName);

        String defaultPath = storageOperate.getResDir(user.getTenantCode());
        return storageOperate.getFileStatus(defaultPath + fileName, defaultPath, user.getTenantCode(),
                ResourceType.FILE);
    }

    @Override
    public DeleteDataTransferResponse deleteDataTransferData(User loginUser, Integer days) {
        DeleteDataTransferResponse result = new DeleteDataTransferResponse();

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            log.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        String tenantCode = getTenantCode(user);

        String baseFolder = storageOperate.getResourceFullName(tenantCode, "DATA_TRANSFER");

        LocalDateTime now = LocalDateTime.now();
        now = now.minus(days, ChronoUnit.DAYS);
        String deleteDate = now.toLocalDate().toString().replace("-", "");
        List<StorageEntity> storageEntities;
        try {
            storageEntities = new ArrayList<>(
                    storageOperate.listFilesStatus(baseFolder, baseFolder, tenantCode, ResourceType.FILE));
        } catch (Exception e) {
            log.error("delete data transfer data error", e);
            putMsg(result, Status.DELETE_RESOURCE_ERROR);
            return result;
        }

        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();

        for (StorageEntity storageEntity : storageEntities) {
            File path = new File(storageEntity.getFullName());
            String date = path.getName();
            if (date.compareTo(deleteDate) <= 0) {
                try {
                    storageOperate.delete(storageEntity.getFullName(), true);
                    successList.add(storageEntity.getFullName());
                } catch (Exception ex) {
                    log.error("delete data transfer data {} error, please delete it manually", date, ex);
                    failList.add(storageEntity.getFullName());
                }
            }
        }

        result.setSuccessList(successList);
        result.setFailedList(failList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * get resource base dir
     *
     * @param loginUser login user
     * @param type      resource type
     * @return
     */
    @Override
    public String queryResourceBaseDir(User loginUser, ResourceType type) {
        if (storageOperate == null) {
            return EMPTY_STRING;
        }
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, loginUser.getId());
        }

        String tenantCode = getTenantCode(user);

        return isAdmin(loginUser) ? storageOperate.getDir(ResourceType.ALL, tenantCode)
                : storageOperate.getDir(type, tenantCode);
    }

    /**
     * check permission by comparing login user's tenantCode with tenantCode in the request
     *
     * @param isAdmin        is the login user admin
     * @param userTenantCode loginUser's tenantCode
     * @param resTenantCode  tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                       can be different from the login user in the case of logging in as admin users.
     * @return isValid
     */
    private boolean isUserTenantValid(boolean isAdmin, String userTenantCode,
                                      String resTenantCode) throws ServiceException {
        if (isAdmin) {
            return true;
        }
        if (StringUtils.isEmpty(resTenantCode)) {
            // TODO: resource tenant code will be empty when query resources list, need to be optimized
            return true;
        }
        return resTenantCode.equals(userTenantCode);
    }

    private String getTenantCode(User user) {
        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            throw new ServiceException(Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
        }
        return tenant.getTenantCode();
    }

    private void checkFullName(String userTenantCode, String fullName) {
        if (StringUtils.isEmpty(fullName)) {
            return;
        }
        if (FOLDER_SEPARATOR.equalsIgnoreCase(fullName)) {
            return;
        }
        // abnormal characters check
        if (!checkFilePath(fullName)) {
            throw new ServiceException(Status.ILLEGAL_RESOURCE_PATH);
        }
        // Avoid returning to the parent directory
        if (fullName.contains("../")) {
            throw new ServiceException(Status.ILLEGAL_RESOURCE_PATH, fullName);
        }
        String baseDir = storageOperate.getDir(ResourceType.ALL, userTenantCode);
        if (!StringUtils.startsWith(fullName, baseDir)) {
            throw new ServiceException(Status.ILLEGAL_RESOURCE_PATH, fullName);
        }
    }
}
