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

import static org.apache.dolphinscheduler.common.constants.Constants.ALIAS;
import static org.apache.dolphinscheduler.common.constants.Constants.CONTENT;
import static org.apache.dolphinscheduler.common.constants.Constants.EMPTY_STRING;
import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_SS;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.apache.dolphinscheduler.common.constants.Constants.JAR;
import static org.apache.dolphinscheduler.common.constants.Constants.PERIOD;

import org.apache.dolphinscheduler.api.dto.resources.DeleteDataTransferResponse;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.api.dto.resources.filter.ResourceFilter;
import org.apache.dolphinscheduler.api.dto.resources.visitor.ResourceTreeVisitor;
import org.apache.dolphinscheduler.api.dto.resources.visitor.Visitor;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.RegexUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.ResourcesTask;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceTaskMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.storage.StorageEntity;
import org.apache.dolphinscheduler.service.storage.StorageOperate;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.rmi.ServerException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

/**
 * resources service impl
 */
@Service
public class ResourcesServiceImpl extends BaseServiceImpl implements ResourcesService {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesServiceImpl.class);

    @Autowired
    private ResourceMapper resourcesMapper;

    @Autowired
    private ResourceTaskMapper resourceTaskMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private UdfFuncMapper udfFunctionMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ResourceUserMapper resourceUserMapper;

    @Autowired
    private ProcessDefinitionLogMapper processDefinitionLogMapper;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired(required = false)
    private StorageOperate storageOperate;

    /**
     * create directory
     *
     * @param loginUser   login user
     * @param name        alias
     * @param description description
     * @param type        type
     * @param pid         parent id
     * @param currentDir  current directory
     * @return create directory result
     */
    @Override
    @Transactional
    public Result<Object> createDirectory(User loginUser,
                                          String name,
                                          String description,
                                          ResourceType type,
                                          int pid,
                                          String currentDir) {
        Result<Object> result = new Result<>();

        result = checkResourceUploadStartupState();
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }
        if (FileUtils.directoryTraversal(name)) {
            logger.warn("Parameter name is invalid, name:{}.", RegexUtils.escapeNRT(name));
            putMsg(result, Status.VERIFY_PARAMETER_NAME_FAILED);
            return result;
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return result;
        }

        String tenantCode = tenant.getTenantCode();

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, "")) {
            logger.error("current user does not have permission");
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        if (checkDescriptionLength(description)) {
            logger.warn("Parameter description is too long.");
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }

        String userResRootPath = ResourceType.UDF.equals(type) ? storageOperate.getUdfDir(tenantCode)
                : storageOperate.getResDir(tenantCode);
        String fullName = !currentDir.contains(userResRootPath) ? userResRootPath + name : currentDir + name;

        try {
            if (checkResourceExists(fullName)) {
                logger.error("resource directory {} has exist, can't recreate", fullName);
                putMsg(result, Status.RESOURCE_EXIST);
                return result;
            }
        } catch (Exception e) {
            logger.warn("Resource exists, can not create again, fullName:{}.", fullName, e);
            throw new ServiceException("resource already exists, can't recreate");
        }

        // create directory in storage
        createDirectory(loginUser, fullName, type, result);
        return result;
    }

    private String getFullName(String currentDir, String name) {
        return currentDir.equals(FOLDER_SEPARATOR) ? String.format(FORMAT_SS, currentDir, name)
                : String.format(FORMAT_S_S, currentDir, name);
    }

    /**
     * create resource
     *
     * @param loginUser  login user
     * @param name       alias
     * @param desc       description
     * @param type       type
     * @param file       file
     * @param currentDir current directory
     * @return create result code
     */
    @Override
    @Transactional
    public Result<Object> createResource(User loginUser,
                                         String name,
                                         String desc,
                                         ResourceType type,
                                         MultipartFile file,
                                         String currentDir) {
        Result<Object> result = new Result<>();

        result = checkResourceUploadStartupState();
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return result;
        }

        String tenantCode = tenant.getTenantCode();

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, "")) {
            logger.error("current user does not have permission");
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        result = verifyFile(name, type, file);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }

        // check resource name exists
        String userResRootPath = ResourceType.UDF.equals(type) ? storageOperate.getUdfDir(tenantCode)
                : storageOperate.getResDir(tenantCode);
        String currDirNFileName = !currentDir.contains(userResRootPath) ? userResRootPath + name : currentDir + name;

        try {
            if (checkResourceExists(currDirNFileName)) {
                logger.error("resource {} has exist, can't recreate", RegexUtils.escapeNRT(name));
                putMsg(result, Status.RESOURCE_EXIST);
                return result;
            }
        } catch (Exception e) {
            throw new ServiceException("resource already exists, can't recreate");
        }
        if (currDirNFileName.length() > Constants.RESOURCE_FULL_NAME_MAX_LENGTH) {
            logger.error(
                    "Resource file's name is longer than max full name length, fullName:{}, " +
                            "fullNameSize:{}, maxFullNameSize:{}",
                    RegexUtils.escapeNRT(name), currDirNFileName.length(), Constants.RESOURCE_FULL_NAME_MAX_LENGTH);
            putMsg(result, Status.RESOURCE_FULL_NAME_TOO_LONG_ERROR);
            return result;
        }

        // fail upload
        if (!upload(loginUser, currDirNFileName, file, type)) {
            logger.error("upload resource: {} file: {} failed.", RegexUtils.escapeNRT(name),
                    RegexUtils.escapeNRT(file.getOriginalFilename()));
            putMsg(result, Status.STORE_OPERATE_CREATE_ERROR);
            throw new ServiceException(
                    String.format("upload resource: %s file: %s failed.", name, file.getOriginalFilename()));
        } else
            logger.info("Upload resource file complete, resourceName:{}, fileName:{}.",
                    RegexUtils.escapeNRT(name), RegexUtils.escapeNRT(file.getOriginalFilename()));
        return result;
    }

    /**
     * update the folder's size of the resource
     *
     * @param resource the current resource
     * @param size size
     */
    private void updateParentResourceSize(Resource resource, long size) {
        if (resource.getSize() > 0) {
            String[] splits = resource.getFullName().split("/");
            for (int i = 1; i < splits.length; i++) {
                String parentFullName = Joiner.on("/").join(Arrays.copyOfRange(splits, 0, i));
                if (StringUtils.isNotBlank(parentFullName)) {
                    List<Resource> resources =
                            resourcesMapper.queryResource(parentFullName, resource.getType().ordinal());
                    if (CollectionUtils.isNotEmpty(resources)) {
                        Resource parentResource = resources.get(0);
                        if (parentResource.getSize() + size >= 0) {
                            parentResource.setSize(parentResource.getSize() + size);
                        } else {
                            parentResource.setSize(0L);
                        }
                        resourcesMapper.updateById(parentResource);
                        logger.info("Resource size update complete, resourceFullName:{}, newSize:{}.",
                                parentResource.getFullName(), parentResource.getSize());
                    }
                }
            }
        }
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
            logger.error("error occurred when checking resource: " + fullName, e);
        }
        return Boolean.TRUE.equals(existResource);
    }

    /**
     * update resource
     *
     * @param loginUser  login user
     * @param resourceFullName resource full name
     * @param resTenantCode tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                      can be different from the login user in the case of logging in as admin users.
     * @param name       name
     * @param desc       description
     * @param type       resource type
     * @param file       resource file
     * @return update result code
     */
    @Override
    @Transactional
    public Result<Object> updateResource(User loginUser,
                                         String resourceFullName,
                                         String resTenantCode,
                                         String name,
                                         String desc,
                                         ResourceType type,
                                         MultipartFile file) {
        Result<Object> result = new Result<>();

        result = checkResourceUploadStartupState();
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return result;
        }

        String tenantCode = tenant.getTenantCode();

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            logger.error("current user does not have permission");
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        String defaultPath = storageOperate.getResDir(tenantCode);

        StorageEntity resource;
        try {
            resource = storageOperate.getFileStatus(resourceFullName, defaultPath, resTenantCode, type);
        } catch (Exception e) {
            logger.error("Get file status fail, resource path: {}", resourceFullName, e);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            throw new ServiceException((String.format("Get file status fail, resource path: %s", resourceFullName)));
        }

        if (!PropertyUtils.getResUploadStartupState()) {
            logger.error("Storage does not start up, resource upload startup state: {}.",
                    PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.STORAGE_NOT_STARTUP);
            return result;
        }

        // TODO: deal with OSS
        if (resource.isDirectory() && storageOperate.returnStorageType().equals(ResUploadType.S3)
                && !resource.getFileName().equals(name)) {
            logger.warn("Directory in S3 storage can not be renamed.");
            putMsg(result, Status.S3_CANNOT_RENAME);
            return result;
        }

        if (file == null && name.equals(resource.getAlias()) && desc.equals(resource.getDescription())) {
            logger.info("Resource does not need to be updated due to no change, resource full name:{}.",
                    resourceFullName);
            putMsg(result, Status.SUCCESS);
            return result;
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
                    logger.error("resource {} already exists, can't recreate", fullName);
                    putMsg(result, Status.RESOURCE_EXIST);
                    return result;
                }
            } catch (Exception e) {
                throw new ServiceException(String.format("error occurs while querying resource: %s", fullName));
            }

        }

        result = verifyFile(name, type, file);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }

        Date now = new Date();
        long originFileSize = resource.getSize();

        resource.setAlias(name);
        resource.setFileName(name);
        resource.setFullName(fullName);
        resource.setDescription(desc);
        resource.setUpdateTime(now);
        if (file != null) {
            resource.setSize(file.getSize());
        }

        // if name unchanged, return directly without moving on HDFS
        if (originResourceName.equals(name) && file == null) {
            return result;
        }

        List<ResourcesTask> existResourcesList;
        if (resource.isDirectory()) {
            existResourcesList = resourceTaskMapper.selectSubfoldersFullNames(originFullName + FOLDER_SEPARATOR);
        } else {
            existResourcesList = resourceTaskMapper.selectByMap(
                    Collections.singletonMap("full_name", originFullName));
        }

        if (existResourcesList.size() > 0 && !fullName.equals(originFullName)) {
            // check if any related task is online. If it is, it can not be updated.
            for (ResourcesTask existResource : existResourcesList) {
                int taskId = existResource.getTaskId();
                if (processService.isTaskOnline(taskDefinitionMapper.selectById(taskId).getCode())) {
                    logger.error("can't be updated,because it is used of process definition that's online");
                    logger.error("resource task relation id:{} is used of task code {}", existResource.getId(),
                            taskDefinitionMapper.selectById(taskId).getCode());
                    putMsg(result, Status.RESOURCE_IS_USED);
                    return result;
                }
            }

            for (ResourcesTask existResource : existResourcesList) {
                int taskId = existResource.getTaskId();
                long taskCode = taskDefinitionMapper.selectById(taskId).getCode();

                List<ProcessTaskRelation> processTaskRelation = processTaskRelationMapper.selectByMap(
                        Collections.singletonMap("post_task_code", taskCode));
                if (processTaskRelation.size() > 0) {
                    long processDefinitionCode = processTaskRelation.get(0).getProcessDefinitionCode();
                    int processDefinitionVersion = processTaskRelation.get(0).getProcessDefinitionVersion();
                    List<ProcessTaskRelation> taskRelationList = processTaskRelationMapper.queryByProcessCode(
                            processTaskRelation.get(0).getProjectCode(),
                            processDefinitionCode);

                    List<TaskDefinition> taskDefinitionLogList = new ArrayList<>();

                    if (taskRelationList.size() > 0) {
                        ProcessDefinitionLog processDefinition =
                                processDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                                        processDefinitionCode, processDefinitionVersion);
                        for (ProcessTaskRelation taskRelation : taskRelationList) {
                            long taskCodeInProcess = taskRelation.getPostTaskCode();
                            TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCodeInProcess);
                            if (taskCodeInProcess == taskCode) {
                                // originFullName is a prefix if isDirectory is true
                                taskDefinition.setTaskParams(RemoveResourceFromResourceList(originFullName,
                                        taskDefinition.getTaskParams(),
                                        resource.isDirectory()));
                                // if isDirectory is true, fullName is the new prefix. we replace old prefix
                                // of resource fullname with the new prefix.
                                // if isDirectory is false, fullName is the new path.
                                taskDefinition.setTaskParams(AddResourceToResourceList(originFullName,
                                        fullName,
                                        existResource.getFullName(),
                                        taskDefinition.getTaskParams(),
                                        resource.isDirectory()));
                            }
                            taskDefinitionLogList.add(taskDefinition);
                        }

                        // update workflow & task definition associated to the resource
                        if (processDefinition != null) {
                            processDefinitionService.updateProcessDefinition(loginUser,
                                    processDefinition.getProjectCode(),
                                    processDefinition.getName(),
                                    processDefinition.getCode(),
                                    processDefinition.getDescription(),
                                    processDefinition.getGlobalParams(),
                                    processDefinition.getLocations(),
                                    processDefinition.getTimeout(),
                                    tenantCode,
                                    JSONUtils.toJsonString(taskRelationList.toArray()),
                                    JSONUtils.toJsonString(taskDefinitionLogList.toArray()),
                                    "",
                                    processDefinition.getExecutionType());
                        }
                    }
                }
            }
        }

        if (file != null) {
            // fail upload
            if (!upload(loginUser, fullName, file, type)) {
                logger.error("Storage operation error, resourceName:{}, originFileName:{}.",
                        name, RegexUtils.escapeNRT(file.getOriginalFilename()));
                putMsg(result, Status.HDFS_OPERATION_ERROR);
                throw new ServiceException(
                        String.format("upload resource: %s file: %s failed.", name, file.getOriginalFilename()));
            }
            if (!fullName.equals(originFullName)) {
                try {
                    storageOperate.delete(originFullName, false);
                } catch (IOException e) {
                    logger.error("Resource delete error, resourceFullName:{}.", originFullName, e);
                    throw new ServiceException(String.format("delete resource: %s failed.", originFullName));
                }
            }

            return result;
        }

        // get the path of dest file in hdfs
        String destHdfsFileName = fullName;
        try {
            logger.info("start  copy {} -> {}", originFullName, destHdfsFileName);
            storageOperate.copy(originFullName, destHdfsFileName, true, true);
        } catch (Exception e) {
            logger.error(MessageFormat.format(" copy {0} -> {1} fail", originFullName, destHdfsFileName), e);
            putMsg(result, Status.HDFS_COPY_FAIL);
            throw new ServiceException(MessageFormat.format(
                    Status.HDFS_COPY_FAIL.getMsg(), originFullName, destHdfsFileName));
        }

        return result;
    }

    private Result<Object> verifyFile(String name, ResourceType type, MultipartFile file) {
        Result<Object> result = new Result<>();
        putMsg(result, Status.SUCCESS);

        if (FileUtils.directoryTraversal(name)) {
            logger.warn("Parameter file alias name verify failed, fileAliasName:{}.", RegexUtils.escapeNRT(name));
            putMsg(result, Status.VERIFY_PARAMETER_NAME_FAILED);
            return result;
        }

        if (file != null && FileUtils.directoryTraversal(Objects.requireNonNull(file.getOriginalFilename()))) {
            logger.warn("File original name verify failed, fileOriginalName:{}.",
                    RegexUtils.escapeNRT(file.getOriginalFilename()));
            putMsg(result, Status.VERIFY_PARAMETER_NAME_FAILED);
            return result;
        }

        if (file != null) {
            // file is empty
            if (file.isEmpty()) {
                logger.warn("Parameter file is empty, fileOriginalName:{}.",
                        RegexUtils.escapeNRT(file.getOriginalFilename()));
                putMsg(result, Status.RESOURCE_FILE_IS_EMPTY);
                return result;
            }

            // file suffix
            String fileSuffix = Files.getFileExtension(file.getOriginalFilename());
            String nameSuffix = Files.getFileExtension(name);

            // determine file suffix
            if (!fileSuffix.equalsIgnoreCase(nameSuffix)) {
                // rename file suffix and original suffix must be consistent
                logger.warn("Rename file suffix and original suffix must be consistent, fileOriginalName:{}.",
                        RegexUtils.escapeNRT(file.getOriginalFilename()));
                putMsg(result, Status.RESOURCE_SUFFIX_FORBID_CHANGE);
                return result;
            }

            // If resource type is UDF, only jar packages are allowed to be uploaded, and the suffix must be .jar
            if (Constants.UDF.equals(type.name()) && !JAR.equalsIgnoreCase(fileSuffix)) {
                logger.warn(Status.UDF_RESOURCE_SUFFIX_NOT_JAR.getMsg());
                putMsg(result, Status.UDF_RESOURCE_SUFFIX_NOT_JAR);
                return result;
            }
            if (file.getSize() > Constants.MAX_FILE_SIZE) {
                logger.warn(
                        "Resource file size is larger than max file size, fileOriginalName:{}, fileSize:{}, maxFileSize:{}.",
                        RegexUtils.escapeNRT(file.getOriginalFilename()), file.getSize(), Constants.MAX_FILE_SIZE);
                putMsg(result, Status.RESOURCE_SIZE_EXCEED_LIMIT);
                return result;
            }
        }
        return result;
    }

    /**
     * query resources list paging
     *
     * @param loginUser login user
     * @param fullName resource full name
     * @param resTenantCode tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                      can be different from the login user in the case of logging in as admin users.
     * @param type      resource type
     * @param searchVal search value
     * @param pageNo    page number
     * @param pageSize  page size
     * @return resource list page
     */
    @Override
    public Result queryResourceListPaging(User loginUser, String fullName, String resTenantCode,
                                          ResourceType type, String searchVal, Integer pageNo, Integer pageSize) {
        Result<Object> result = new Result<>();
        PageInfo<StorageEntity> pageInfo = new PageInfo<>(pageNo, pageSize);

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return result;
        }

        String tenantCode = tenant.getTenantCode();

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            logger.error("current user does not have permission");
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        String defaultPath = "";
        List<StorageEntity> resourcesList = new ArrayList<>();

        if (isAdmin(loginUser) && StringUtils.isBlank(fullName)) {
            // list all tenants' resources to admin users in the root directory
            List<User> userList = userMapper.selectList(null);
            Set<String> visitedTenantEntityCode = new HashSet<>();
            for (User userEntity : userList) {
                String tenantEntityCode = tenantMapper.queryById(userEntity.getTenantId()).getTenantCode();
                if (!visitedTenantEntityCode.contains(tenantEntityCode)) {
                    defaultPath = storageOperate.getResDir(tenantEntityCode);
                    if (type.equals(ResourceType.UDF)) {
                        defaultPath = storageOperate.getUdfDir(tenantEntityCode);
                    }
                    try {
                        resourcesList.addAll(storageOperate.listFilesStatus(defaultPath, defaultPath,
                                tenantEntityCode, type));

                        visitedTenantEntityCode.add(tenantEntityCode);
                    } catch (Exception e) {
                        logger.error(e.getMessage() + " Resource path: {}", defaultPath, e);
                        putMsg(result, Status.RESOURCE_NOT_EXIST);
                        throw new ServiceException(String.format(e.getMessage() +
                                " make sure resource path: %s exists in hdfs", defaultPath));
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
                    resourcesList = storageOperate.listFilesStatus(defaultPath, defaultPath, tenantCode, type);
                } else {
                    resourcesList = storageOperate.listFilesStatus(fullName, defaultPath, tenantCode, type);
                }
            } catch (Exception e) {
                logger.error(e.getMessage() + " Resource path: {}", fullName, e);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                throw new ServiceException(String.format(e.getMessage() +
                        " make sure resource path: %s exists in hdfs", defaultPath));
            }
        }

        // remove leading and trailing spaces in searchVal
        String trimmedSearchVal = searchVal != null ? searchVal.trim() : "";
        // filter based on trimmed searchVal
        List<StorageEntity> filteredResourceList = resourcesList.stream()
                .filter(x -> x.getFileName().matches("(.*)" + trimmedSearchVal + "(.*)")).collect(Collectors.toList());
        // inefficient pagination
        List<StorageEntity> slicedResourcesList = filteredResourceList.stream().skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize).collect(Collectors.toList());

        pageInfo.setTotal(resourcesList.size());
        pageInfo.setTotalList(slicedResourcesList);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * create directory
     * xxx The steps to verify resources are cumbersome and can be optimized
     *
     * @param loginUser login user
     * @param fullName  full name
     * @param type      resource type
     * @param result    Result
     */
    private void createDirectory(User loginUser, String fullName, ResourceType type, Result<Object> result) {
        String tenantCode = tenantMapper.queryById(loginUser.getTenantId()).getTenantCode();
        // String directoryName = storageOperate.getFileName(type, tenantCode, fullName);
        String resourceRootPath = storageOperate.getDir(type, tenantCode);
        try {
            if (!storageOperate.exists(resourceRootPath)) {
                storageOperate.createTenantDirIfNotExists(tenantCode);
            }

            if (!storageOperate.mkdir(tenantCode, fullName)) {
                logger.error("create resource directory {} failed", fullName);
                putMsg(result, Status.STORE_OPERATE_CREATE_ERROR);
                // throw new ServiceException(String.format("create resource directory: %s failed.", fullName));
            }
        } catch (Exception e) {
            logger.error("create resource directory {} failed", fullName);
            putMsg(result, Status.STORE_OPERATE_CREATE_ERROR);
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
        String tenantCode = tenantMapper.queryById(loginUser.getTenantId()).getTenantCode();
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
        } catch (Exception e) {
            FileUtils.deleteFile(localFilename);
            logger.error(e.getMessage(), e);
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
    public Map<String, Object> queryResourceList(User loginUser, ResourceType type, String fullName) {
        Map<String, Object> result = new HashMap<>();

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return null;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return null;
        }
        String tenantCode = tenant.getTenantCode();

        String defaultPath = "";
        List<StorageEntity> resourcesList = new ArrayList<>();

        if (StringUtils.isBlank(fullName)) {
            if (isAdmin(loginUser)) {
                List<User> userList = userMapper.selectList(null);
                Set<String> visitedTenantEntityCode = new HashSet<>();
                for (User userEntity : userList) {
                    Tenant tt = tenantMapper.queryById(userEntity.getTenantId());
                    String tenantEntityCode = tenantMapper.queryById(userEntity.getTenantId()).getTenantCode();
                    if (!visitedTenantEntityCode.contains(tenantEntityCode)) {
                        defaultPath = storageOperate.getResDir(tenantEntityCode);
                        if (type.equals(ResourceType.UDF)) {
                            defaultPath = storageOperate.getUdfDir(tenantEntityCode);
                        }
                        resourcesList.addAll(storageOperate.listFilesStatusRecursively(defaultPath, defaultPath,
                                tenantEntityCode, type));
                        visitedTenantEntityCode.add(tenantEntityCode);
                    }
                }
            } else {
                defaultPath = storageOperate.getResDir(tenantCode);
                if (type.equals(ResourceType.UDF)) {
                    defaultPath = storageOperate.getUdfDir(tenantCode);
                }

                resourcesList = storageOperate.listFilesStatusRecursively(defaultPath, defaultPath, tenantCode, type);
            }
        } else {
            defaultPath = storageOperate.getResDir(tenantCode);
            if (type.equals(ResourceType.UDF)) {
                defaultPath = storageOperate.getUdfDir(tenantCode);
            }

            resourcesList = storageOperate.listFilesStatusRecursively(fullName, defaultPath, tenantCode, type);
        }

        Visitor resourceTreeVisitor = new ResourceTreeVisitor(resourcesList);
        result.put(Constants.DATA_LIST, resourceTreeVisitor.visit(defaultPath).getChildren());
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * query resource list by program type
     *
     * @param loginUser login user
     * @param type      resource type
     * @return resource list
     */
    @Override
    public Result<Object> queryResourceByProgramType(User loginUser, ResourceType type, ProgramType programType) {
        Result<Object> result = new Result<>();

        Set<Integer> resourceIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(checkResourceType(type), loginUser.getId(), logger);
        if (resourceIds.isEmpty()) {
            result.setData(Collections.emptyList());
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<Resource> allResourceList = resourcesMapper.selectBatchIds(resourceIds);

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
        List<Resource> resources = new ResourceFilter(suffix, new ArrayList<>(allResourceList)).filter();
        // Transform into StorageEntity for compatibility
        List<StorageEntity> transformedResourceList = resources.stream()
                .map(this::createStorageEntityBasedOnResource)
                .collect(Collectors.toList());
        Visitor visitor = new ResourceTreeVisitor(transformedResourceList);
        result.setData(visitor.visit("").getChildren());
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * transform resource object into StorageEntity object
     *
     * @param resource  a resource object
     * @return a storageEntity object
     */
    private StorageEntity createStorageEntityBasedOnResource(Resource resource) {
        StorageEntity entity = new StorageEntity();
        entity.setFullName(resource.getFullName());
        entity.setPfullName(resource.getPid() == -1 ? ""
                : resourcesMapper.selectById(resource.getPid()).getFullName());
        entity.setDirectory(resource.isDirectory());
        entity.setAlias(resource.getAlias());
        entity.setId(resource.getId());
        entity.setType(resource.getType());
        entity.setDescription(resource.getDescription());

        return entity;
    }

    /**
     * delete resource
     *
     * @param loginUser  login user
     * @param fullName resource full name
     * @param resTenantCode tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                      can be different from the login user in the case of logging in as admin users.
     * @return delete result code
     * @throws IOException exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> delete(User loginUser, String fullName,
                                 String resTenantCode) throws IOException {
        Result<Object> result = new Result<>();

        result = checkResourceUploadStartupState();
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return result;
        }

        String tenantCode = tenant.getTenantCode();

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            logger.error("current user does not have permission");
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        String defaultPath = storageOperate.getResDir(tenantCode);
        StorageEntity resource;
        try {
            resource = storageOperate.getFileStatus(fullName, defaultPath, resTenantCode, null);
        } catch (Exception e) {
            logger.error(e.getMessage() + " Resource path: {}", fullName, e);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            throw new ServiceException(String.format(e.getMessage() + " Resource path: %s", fullName));
        }

        if (resource == null) {
            logger.error("Resource does not exist, resource full name:{}.", fullName);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }

        // recursively delete a folder
        List<String> allChildren = storageOperate.listFilesStatusRecursively(fullName, defaultPath,
                resTenantCode, resource.getType()).stream().map(storageEntity -> storageEntity.getFullName())
                .collect(Collectors.toList());

        Set<ResourcesTask> resourcesNeedToDeleteSet = new HashSet<>();
        String[] allChildrenFullNameArray = allChildren.stream().toArray(String[]::new);

        // check before using allChildrenFullNameArray to query full names.
        if (allChildrenFullNameArray.length != 0) {
            resourcesNeedToDeleteSet.addAll(
                    resourceTaskMapper.selectBatchFullNames(allChildrenFullNameArray));
        }

        // if resource type is UDF,need check whether it is bound by UDF function
        if (resource.getType() == (ResourceType.UDF)) {
            List<UdfFunc> udfFuncs = udfFunctionMapper.listUdfByResourceFullName(allChildrenFullNameArray);
            if (CollectionUtils.isNotEmpty(udfFuncs)) {
                logger.warn("Resource can not be deleted because it is bound by UDF functions, udfFuncIds:{}",
                        udfFuncs);
                putMsg(result, Status.UDF_RESOURCE_IS_BOUND, udfFuncs.get(0).getFuncName());
                return result;
            }
        }

        // delete data in database
        if (resourcesNeedToDeleteSet.size() > 0) {
            for (ResourcesTask resourcesTask : resourcesNeedToDeleteSet) {
                int taskId = resourcesTask.getTaskId();
                if (processService.isTaskOnline(taskDefinitionMapper.selectById(taskId).getCode())) {
                    logger.error("can't be deleted,because it is used of process definition that's online");
                    logger.error("resource task relation id:{} is used of task code {}", resourcesTask.getId(),
                            taskDefinitionMapper.selectById(taskId).getCode());
                    putMsg(result, Status.RESOURCE_IS_USED);
                    return result;
                }
            }

            for (ResourcesTask existResource : resourcesNeedToDeleteSet) {
                int taskId = existResource.getTaskId();
                long taskCode = taskDefinitionMapper.selectById(taskId).getCode();

                // use taskCode to get processDefinitionCode, then get a list of processDefinitionLog.
                List<ProcessTaskRelation> processTaskRelation = processTaskRelationMapper.selectByMap(
                        Collections.singletonMap("post_task_code", taskCode));
                if (processTaskRelation.size() > 0) {
                    long processDefinitionCode = processTaskRelation.get(0).getProcessDefinitionCode();
                    int processDefinitionVersion = processTaskRelation.get(0).getProcessDefinitionVersion();
                    List<ProcessTaskRelation> taskRelationList = processTaskRelationMapper.queryByProcessCode(
                            processTaskRelation.get(0).getProjectCode(),
                            processDefinitionCode);

                    List<TaskDefinition> taskDefinitionLogList = new ArrayList<>();

                    if (taskRelationList.size() > 0) {
                        ProcessDefinitionLog processDefinition =
                                processDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                                        processDefinitionCode, processDefinitionVersion);
                        for (ProcessTaskRelation taskRelation : taskRelationList) {
                            long taskCodeInProcess = taskRelation.getPostTaskCode();
                            TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCodeInProcess);
                            if (taskCodeInProcess == taskCode) {
                                taskDefinition.setTaskParams(RemoveResourceFromResourceList(existResource.getFullName(),
                                        taskDefinition.getTaskParams(), false));
                            }
                            taskDefinitionLogList.add(taskDefinition);
                        }

                        // update workflow & task definition associated to the resource
                        if (processDefinition != null) {
                            processDefinitionService.updateProcessDefinition(loginUser,
                                    processDefinition.getProjectCode(),
                                    processDefinition.getName(),
                                    processDefinition.getCode(),
                                    processDefinition.getDescription(),
                                    processDefinition.getGlobalParams(),
                                    processDefinition.getLocations(),
                                    processDefinition.getTimeout(),
                                    tenantCode,
                                    JSONUtils.toJsonString(taskRelationList.toArray()),
                                    JSONUtils.toJsonString(taskDefinitionLogList.toArray()),
                                    "",
                                    processDefinition.getExecutionType());
                        }
                    }
                }
            }
        }

        // delete file on hdfs,S3
        storageOperate.delete(fullName, allChildren, true);

        putMsg(result, Status.SUCCESS);

        return result;
    }

    private String RemoveResourceFromResourceList(String stringToDelete, String taskParameter, boolean isDir) {
        Map<String, Object> taskParameters = JSONUtils.parseObject(
                taskParameter,
                new TypeReference<Map<String, Object>>() {
                });
        if (taskParameters.containsKey("resourceList")) {
            String resourceListStr = JSONUtils.toJsonString(taskParameters.get("resourceList"));
            List<ResourceInfo> resourceInfoList = JSONUtils.toList(resourceListStr, ResourceInfo.class);
            List<ResourceInfo> updatedResourceInfoList;
            if (isDir) {
                String stringToDeleteWSeparator = stringToDelete + FOLDER_SEPARATOR;
                // use start with to identify any prefix matching folder path
                updatedResourceInfoList = resourceInfoList.stream()
                        .filter(Objects::nonNull)
                        .filter(resourceInfo -> !resourceInfo.getResourceName().startsWith(stringToDeleteWSeparator))
                        .collect(Collectors.toList());
            } else {
                updatedResourceInfoList = resourceInfoList.stream()
                        .filter(Objects::nonNull)
                        .filter(resourceInfo -> !resourceInfo.getResourceName().equals(stringToDelete))
                        .collect(Collectors.toList());
            }
            taskParameters.put("resourceList", updatedResourceInfoList);
            return JSONUtils.toJsonString(taskParameters);
        }
        return taskParameter;
    }

    private String AddResourceToResourceList(String oldPrefix, String newPrefix, String resFullName,
                                             String taskParameter, boolean isDir) {
        Map<String, Object> taskParameters = JSONUtils.parseObject(
                taskParameter,
                new TypeReference<Map<String, Object>>() {
                });

        if (taskParameters.containsKey("resourceList")) {
            String resourceListStr = JSONUtils.toJsonString(taskParameters.get("resourceList"));
            List<ResourceInfo> resourceInfos = JSONUtils.toList(resourceListStr, ResourceInfo.class);

            // add updated resource to replace the original resource.
            ResourceInfo newResource = new ResourceInfo();
            if (isDir) {
                // we add spearator here because we dont want rare cases like
                // oldFullName: .../folderToDelete and a resource path: .../folderToDeleteAnotherFolder
                // Therefore, we make sure the oldFullName has a format of .../folderToDelete/ when
                // modifying resourceFullNames in taskDefinition.
                String oldFullNameWSeparator = oldPrefix + FOLDER_SEPARATOR;
                String newFullNameWSpearator = newPrefix + FOLDER_SEPARATOR;

                newResource.setResourceName(resFullName.replace(oldFullNameWSeparator, newFullNameWSpearator));
            } else {
                newResource.setResourceName(newPrefix);
            }
            resourceInfos.add(newResource);

            taskParameters.put("resourceList", resourceInfos);

            return JSONUtils.toJsonString(taskParameters);
        }
        return taskParameter;
    }

    private String RemoveResourceFromIdsNew(int idToDelete, String idNews) {

        String[] resourceIds = idNews.split(",");
        Set<Integer> resourceIdSet = Arrays.stream(resourceIds)
                .map(Integer::parseInt)
                .filter(integerId -> !integerId.equals(idToDelete))
                .collect(Collectors.toSet());
        return Joiner.on(",").join(resourceIdSet);
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
    public Result<Object> verifyResourceName(String fullName, ResourceType type, User loginUser) {
        Result<Object> result = new Result<>();
        putMsg(result, Status.SUCCESS);
        if (checkResourceExists(fullName)) {
            logger.error("Resource with same name exists so can not create again, resourceType:{}, resourceName:{}.",
                    type, RegexUtils.escapeNRT(fullName));
            putMsg(result, Status.RESOURCE_EXIST);
        }

        return result;
    }

    /**
     * verify resource by full name or pid and type
     *
     * @param fileName resource file name
     * @param type     resource type
     * @param resTenantCode tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                      can be different from the login user in the case of logging in as admin users.
     * @return true if the resource full name or pid not exists, otherwise return false
     */
    @Override
    public Result<Object> queryResourceByFileName(User loginUser, String fileName, ResourceType type,
                                                  String resTenantCode) {
        Result<Object> result = new Result<>();
        if (StringUtils.isBlank(fileName)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            return result;
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return result;
        }

        String tenantCode = tenant.getTenantCode();

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            logger.error("current user does not have permission");
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        String defaultPath = storageOperate.getResDir(resTenantCode);
        if (type.equals(ResourceType.UDF)) {
            defaultPath = storageOperate.getUdfDir(resTenantCode);
        }

        StorageEntity file;
        try {
            file = storageOperate.getFileStatus(defaultPath + fileName, defaultPath, resTenantCode, type);
        } catch (Exception e) {
            logger.error(e.getMessage() + " Resource path: {}", defaultPath + fileName, e);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }

        putMsg(result, Status.SUCCESS);
        result.setData(file);
        return result;
    }

    /**
     * get resource by id
     * @param fullName resource full name
     * @param resTenantCode tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                      can be different from the login user in the case of logging in as admin users.
     * @return resource
     */
    @Override
    public Result<Object> queryResourceByFullName(User loginUser, String fullName, String resTenantCode,
                                                  ResourceType type) throws IOException {
        Result<Object> result = new Result<>();

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return result;
        }

        String tenantCode = tenant.getTenantCode();

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            logger.error("current user does not have permission");
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        String defaultPath = storageOperate.getResDir(resTenantCode);
        if (type.equals(ResourceType.UDF)) {
            defaultPath = storageOperate.getUdfDir(resTenantCode);
        }

        StorageEntity file;
        try {
            file = storageOperate.getFileStatus(fullName, defaultPath, resTenantCode, type);
        } catch (Exception e) {
            logger.error(e.getMessage() + " Resource path: {}", fullName, e);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            throw new ServiceException(String.format(e.getMessage() + " Resource path: %s", fullName));
        }

        putMsg(result, Status.SUCCESS);
        result.setData(file);
        return result;
    }

    /**
     * view resource file online
     *
     * @param fullName  resource fullName
     * @param resTenantCode  owner's tenant code of the resource
     * @param skipLineNum skip line number
     * @param limit       limit
     * @return resource content
     */
    @Override
    public Result<Object> readResource(User loginUser, String fullName, String resTenantCode,
                                       int skipLineNum, int limit) {
        Result<Object> result = checkResourceUploadStartupState();
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return result;
        }

        String tenantCode = tenant.getTenantCode();

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            logger.error("current user does not have permission");
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        // check preview or not by file suffix
        String nameSuffix = Files.getFileExtension(fullName);
        String resourceViewSuffixes = FileUtils.getResourceViewSuffixes();
        if (StringUtils.isNotEmpty(resourceViewSuffixes)) {
            List<String> strList = Arrays.asList(resourceViewSuffixes.split(","));
            if (!strList.contains(nameSuffix)) {
                logger.error("Resource suffix does not support view,resourceFullName:{}, suffix:{}.", fullName,
                        nameSuffix);
                putMsg(result, Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW);
                return result;
            }
        }

        List<String> content = new ArrayList<>();
        try {
            if (storageOperate.exists(fullName)) {
                content = storageOperate.vimFile(tenantCode, fullName, skipLineNum, limit);
            } else {
                logger.error("read file {} not exist in storage", fullName);
                putMsg(result, Status.RESOURCE_FILE_NOT_EXIST, fullName);
                return result;
            }

        } catch (Exception e) {
            logger.error("Resource {} read failed", fullName, e);
            putMsg(result, Status.HDFS_OPERATION_ERROR);
            return result;
        }

        putMsg(result, Status.SUCCESS);
        Map<String, Object> map = new HashMap<>();
        map.put(ALIAS, fullName);
        map.put(CONTENT, String.join("\n", content));
        result.setData(map);

        return result;
    }

    /**
     * create resource file online
     *
     * @param loginUser  login user
     * @param type       resource type
     * @param fileName   file name
     * @param fileSuffix file suffix
     * @param desc       description
     * @param content    content
     * @param currentDir current directory
     * @return create result code
     */
    @Override
    @Transactional
    public Result<Object> onlineCreateResource(User loginUser, ResourceType type, String fileName, String fileSuffix,
                                               String desc, String content, String currentDir) {
        Result<Object> result = new Result<>();

        result = checkResourceUploadStartupState();
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return result;
        }

        String tenantCode = tenant.getTenantCode();

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, "")) {
            logger.error("current user does not have permission");
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        if (FileUtils.directoryTraversal(fileName)) {
            logger.warn("File name verify failed, fileName:{}.", RegexUtils.escapeNRT(fileName));
            putMsg(result, Status.VERIFY_PARAMETER_NAME_FAILED);
            return result;
        }

        // check file suffix
        String nameSuffix = fileSuffix.trim();
        String resourceViewSuffixes = FileUtils.getResourceViewSuffixes();
        if (StringUtils.isNotEmpty(resourceViewSuffixes)) {
            List<String> strList = Arrays.asList(resourceViewSuffixes.split(","));
            if (!strList.contains(nameSuffix)) {
                logger.warn("Resource suffix does not support view, suffix:{}.", nameSuffix);
                putMsg(result, Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW);
                return result;
            }
        }

        String name = fileName.trim() + "." + nameSuffix;

        String fullName = "";
        String userResRootPath = storageOperate.getResDir(tenantCode);
        if (!currentDir.contains(userResRootPath)) {
            fullName = userResRootPath + name;
        } else {
            fullName = currentDir + name;
        }

        result = verifyResourceName(fullName, type, loginUser);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }

        result = uploadContentToStorage(loginUser, fullName, tenantCode, content);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            throw new ServiceException(result.getMsg());
        }
        return result;
    }

    /**
     * create or update resource.
     * If the folder is not already created, it will be
     *
     * @param loginUser user who create or update resource
     * @param fileFullName The full name of resource.Includes path and suffix.
     * @param desc description of resource
     * @param content content of resource
     * @return create result code
     */
    @Override
    @Transactional
    public Result<Object> onlineCreateOrUpdateResourceWithDir(User loginUser, String fileFullName, String desc,
                                                              String content) {
        // TODO: need update to third party service
        if (checkResourceExists(fileFullName)) {
            Resource resource = resourcesMapper.queryResource(fileFullName, ResourceType.FILE.ordinal()).get(0);
            Result<Object> result = this.updateResourceContent(loginUser, fileFullName,
                    resource.getUserName(), content);
            if (result.getCode() == Status.SUCCESS.getCode()) {
                resource.setDescription(desc);
                Map<String, Object> resultMap = new HashMap<>();
                for (Map.Entry<Object, Object> entry : new BeanMap(resource).entrySet()) {
                    if (!Constants.CLASS.equalsIgnoreCase(entry.getKey().toString())) {
                        resultMap.put(entry.getKey().toString(), entry.getValue());
                    }
                }
                result.setData(resultMap);
            }
            return result;
        } else {
            String resourceSuffix = fileFullName.substring(fileFullName.indexOf(PERIOD) + 1);
            String fileNameWithSuffix = fileFullName.substring(fileFullName.lastIndexOf(FOLDER_SEPARATOR) + 1);
            String resourceDir = fileFullName.replace(fileNameWithSuffix, EMPTY_STRING);
            String resourceName = fileNameWithSuffix.replace(PERIOD + resourceSuffix, EMPTY_STRING);
            String[] dirNames = resourceDir.split(FOLDER_SEPARATOR);
            int pid = -1;
            StringBuilder currDirPath = new StringBuilder();
            for (String dirName : dirNames) {
                if (StringUtils.isNotEmpty(dirName)) {
                    pid = queryOrCreateDirId(loginUser, pid, currDirPath.toString(), dirName);
                    currDirPath.append(FOLDER_SEPARATOR).append(dirName);
                }
            }
            return this.onlineCreateResource(
                    loginUser, ResourceType.FILE, resourceName, resourceSuffix, desc, content,
                    currDirPath.toString());
        }
    }

    @Override
    @Transactional
    public Integer createOrUpdateResource(String userName, String fullName, String description,
                                          String resourceContent) {
        User user = userMapper.queryByUserNameAccurately(userName);
        int suffixLabelIndex = fullName.indexOf(PERIOD);
        if (suffixLabelIndex == -1) {
            String msg = String.format("The suffix of file can not be empty, fullName:%s.", fullName);
            logger.warn(msg);
            throw new IllegalArgumentException(msg);
        }
        if (!fullName.startsWith(FOLDER_SEPARATOR)) {
            fullName = FOLDER_SEPARATOR + fullName;
        }
        Result<Object> createResult = onlineCreateOrUpdateResourceWithDir(
                user, fullName, description, resourceContent);
        if (createResult.getCode() == Status.SUCCESS.getCode()) {
            Map<String, Object> resultMap = (Map<String, Object>) createResult.getData();
            return (int) resultMap.get("id");
        }
        String msg = String.format("Create or update resource error, resourceName:%s.", fullName);
        logger.error(msg);
        throw new IllegalArgumentException(msg);
    }

    private int queryOrCreateDirId(User user, int pid, String currentDir, String dirName) {
        String dirFullName = currentDir + FOLDER_SEPARATOR + dirName;
        if (checkResourceExists(dirFullName)) {
            List<Resource> resourceList = resourcesMapper.queryResource(dirFullName, ResourceType.FILE.ordinal());
            return resourceList.get(0).getId();
        } else {
            // create dir
            Result<Object> createDirResult = this.createDirectory(
                    user, dirName, EMPTY_STRING, ResourceType.FILE, pid, currentDir);
            if (createDirResult.getCode() == Status.SUCCESS.getCode()) {
                // Map<String, Object> resultMap = (Map<String, Object>) createDirResult.getData();
                // return resultMap.get("id") == null ? -1 : (Integer) resultMap.get("id");

                // Since resource is kept in third party services, its id will always be -1.
                return -1;

            } else {
                String msg = String.format("Create dir error,  dirFullName:%s.", dirFullName);
                logger.error(msg);
                throw new IllegalArgumentException(msg);
            }
        }
    }

    private void permissionPostHandle(ResourceType resourceType, User loginUser, Integer resourceId) {
        AuthorizationType authorizationType =
                resourceType.equals(ResourceType.FILE) ? AuthorizationType.RESOURCE_FILE_ID
                        : AuthorizationType.UDF_FILE;
        permissionPostHandle(authorizationType, loginUser.getId(), Collections.singletonList(resourceId), logger);
    }

    private Result<Object> checkResourceUploadStartupState() {
        Result<Object> result = new Result<>();
        putMsg(result, Status.SUCCESS);
        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()) {
            logger.error("Storage does not start up, resource upload startup state: {}.",
                    PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.STORAGE_NOT_STARTUP);
            return result;
        }
        return result;
    }

    private Result<Object> verifyResource(User loginUser, ResourceType type, String fullName, int pid) {
        Result<Object> result = verifyResourceName(fullName, type, loginUser);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }
        return verifyPid(loginUser, pid);
    }

    private Result<Object> verifyPid(User loginUser, int pid) {
        Result<Object> result = new Result<>();
        putMsg(result, Status.SUCCESS);
        if (pid != -1) {
            Resource parentResource = resourcesMapper.selectById(pid);
            if (parentResource == null) {
                logger.error("Parent resource does not exist, parentResourceId:{}.", pid);
                putMsg(result, Status.PARENT_RESOURCE_NOT_EXIST);
                return result;
            }
            if (!canOperator(loginUser, parentResource.getUserId())) {
                logger.warn("User does not have operation privilege, loginUserName:{}.", loginUser.getUserName());
                putMsg(result, Status.USER_NO_OPERATION_PERM);
                return result;
            }
        }
        return result;
    }

    /**
     * updateProcessInstance resource
     *
     * @param fullName resource full name
     * @param resTenantCode tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                      can be different from the login user in the case of logging in as admin users.
     * @param content    content
     * @return update result cod
     */
    @Override
    @Transactional
    public Result<Object> updateResourceContent(User loginUser, String fullName, String resTenantCode,
                                                String content) {
        Result<Object> result = checkResourceUploadStartupState();
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return result;
        }

        String tenantCode = tenant.getTenantCode();

        if (!isUserTenantValid(isAdmin(loginUser), tenantCode, resTenantCode)) {
            logger.error("current user does not have permission");
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        StorageEntity resource;
        try {
            resource = storageOperate.getFileStatus(fullName, "", resTenantCode, ResourceType.FILE);
        } catch (Exception e) {
            logger.error("error occurred when fetching resource information ,  resource full name {}", fullName);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }

        if (resource == null) {
            logger.error("Resource does not exist, resource full name:{}.", fullName);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }

        // check can edit by file suffix
        String nameSuffix = Files.getFileExtension(resource.getAlias());
        String resourceViewSuffixes = FileUtils.getResourceViewSuffixes();
        if (StringUtils.isNotEmpty(resourceViewSuffixes)) {
            List<String> strList = Arrays.asList(resourceViewSuffixes.split(","));
            if (!strList.contains(nameSuffix)) {
                logger.warn("Resource suffix does not support view, resource full name:{}, suffix:{}.",
                        fullName, nameSuffix);
                putMsg(result, Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW);
                return result;
            }
        }

        result = uploadContentToStorage(loginUser, resource.getFullName(), resTenantCode, content);

        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            throw new ServiceException(result.getMsg());
        } else
            logger.info("Update resource content complete, resource full name:{}.", fullName);
        return result;
    }

    /**
     * @param fullName resource full name
     * @param tenantCode   tenant code
     * @param content      content
     * @return result
     */
    private Result<Object> uploadContentToStorage(User loginUser, String fullName, String tenantCode, String content) {
        Result<Object> result = new Result<>();
        String localFilename = "";
        try {
            localFilename = FileUtils.getUploadFilename(tenantCode, UUID.randomUUID().toString());

            if (!FileUtils.writeContent2File(content, localFilename)) {
                // write file fail
                logger.error("Write file error, fileName:{}, content:{}.", localFilename,
                        RegexUtils.escapeNRT(content));
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }

            // get resource file path
            String resourcePath = storageOperate.getResDir(tenantCode);
            logger.info("resource  path is {}, resource dir is {}", fullName, resourcePath);

            if (!storageOperate.exists(resourcePath)) {
                // create if tenant dir not exists
                storageOperate.createTenantDirIfNotExists(tenantCode);
                logger.info("Create tenant dir because path {} does not exist, tenantCode:{}.", resourcePath,
                        tenantCode);
            }
            if (storageOperate.exists(fullName)) {
                storageOperate.delete(fullName, false);
            }

            storageOperate.upload(tenantCode, localFilename, fullName, true, true);
        } catch (Exception e) {
            logger.error("Upload content to storage error, tenantCode:{}, destFileName:{}.", tenantCode, localFilename,
                    e);
            result.setCode(Status.HDFS_OPERATION_ERROR.getCode());
            result.setMsg(String.format("copy %s to hdfs %s fail", localFilename, fullName));
            return result;
        }
        logger.info("Upload content to storage complete, tenantCode:{}, destFileName:{}.", tenantCode, localFilename);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * download file
     * @return resource content
     * @throws IOException exception
     */
    @Override
    public org.springframework.core.io.Resource downloadResource(User loginUser,
                                                                 String fullName) throws IOException {
        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()) {
            logger.warn("Storage does not start up, resource upload startup state: {}.",
                    PropertyUtils.getResUploadStartupState());
            throw new ServiceException("hdfs not startup");
        }

        if (fullName.endsWith("/")) {
            logger.error("resource id {} is directory,can't download it", fullName);
            throw new ServiceException("can't download directory");
        }

        int userId = loginUser.getId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            logger.error("User does not exits, userId:{}.", userId);
            throw new ServiceException(String.format("Resource owner id %d does not exist", userId));
        }

        String tenantCode = "";

        if (user.getTenantId() != 0) {
            Tenant tenant = tenantMapper.queryById(user.getTenantId());
            if (tenant == null) {
                logger.error("Tenant id {} not exists", user.getTenantId());
                throw new ServiceException(
                        String.format("The tenant id %d of resource owner not exist", user.getTenantId()));
            }
            tenantCode = tenant.getTenantCode();
        }

        String[] aliasArr = fullName.split("/");
        String alias = aliasArr[aliasArr.length - 1];
        String localFileName = FileUtils.getDownloadFilename(alias);
        logger.info("Resource path is {}, download local filename is {}", alias, localFileName);

        try {
            storageOperate.download(tenantCode, fullName, localFileName, false, true);
            return org.apache.dolphinscheduler.api.utils.FileUtils.file2Resource(localFileName);
        } catch (IOException e) {
            logger.error("Download resource error, the path is {}, and local filename is {}, the error message is {}",
                    fullName, localFileName, e.getMessage());
            throw new ServerException("Download the resource file failed ,it may be related to your storage");
        }
    }

    /**
     * list all file
     *
     * @param loginUser login user
     * @param userId    user id
     * @return unauthorized result code
     */
    @Override
    public Map<String, Object> authorizeResourceTree(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }

        List<Resource> resourceList;
        if (isAdmin(loginUser)) {
            // admin gets all resources except userId
            resourceList = resourcesMapper.queryResourceExceptUserId(userId);
        } else {
            // non-admins users get their own resources
            resourceList = resourcesMapper.queryResourceListAuthored(loginUser.getId(), -1);
        }
        List<ResourceComponent> list;
        if (CollectionUtils.isNotEmpty(resourceList)) {
            // Transform into StorageEntity for compatibility
            List<StorageEntity> transformedResourceList = resourceList.stream()
                    .map(this::createStorageEntityBasedOnResource)
                    .collect(Collectors.toList());
            Visitor visitor = new ResourceTreeVisitor(transformedResourceList);
            list = visitor.visit("").getChildren();
        } else {
            list = new ArrayList<>(0);
        }

        result.put(Constants.DATA_LIST, list);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Resource queryResourcesFileInfo(String userName, String fileName) {
        // TODO: It is used in PythonGateway, should be revised
        User user = userMapper.queryByUserNameAccurately(userName);
        Result<Object> resourceResponse = this.queryResourceByFileName(user, fileName, ResourceType.FILE, "");
        if (resourceResponse.getCode() != Status.SUCCESS.getCode()) {
            String msg =
                    String.format("Query resource by fullName failed, userName:%s, fullName:%s", userName, fileName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return (Resource) resourceResponse.getData();
    }

    @Override
    public DeleteDataTransferResponse deleteDataTransferData(User loginUser, Integer days) {
        DeleteDataTransferResponse result = new DeleteDataTransferResponse();

        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            logger.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null) {
            logger.error("tenant not exists");
            putMsg(result, Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST);
            return result;
        }

        String tenantCode = tenant.getTenantCode();

        String baseFolder = storageOperate.getResourceFileName(tenantCode, "DATA_TRANSFER");

        LocalDateTime now = LocalDateTime.now();
        now = now.minus(days, ChronoUnit.DAYS);
        String deleteDate = now.toLocalDate().toString().replace("-", "");
        List<StorageEntity> storageEntities;
        try {
            storageEntities = new ArrayList<>(
                    storageOperate.listFilesStatus(baseFolder, baseFolder, tenantCode, ResourceType.FILE));
        } catch (Exception e) {
            logger.error("delete data transfer data error", e);
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
                    logger.error("delete data transfer data {} error, please delete it manually", date, ex);
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
     * unauthorized file
     *
     * @param loginUser login user
     * @param userId    user id
     * @return unauthorized result code
     */
    @Override
    public Map<String, Object> unauthorizedFile(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        List<Resource> resourceList;
        if (isAdmin(loginUser)) {
            // admin gets all resources except userId
            resourceList = resourcesMapper.queryResourceExceptUserId(userId);
        } else {
            // non-admins users get their own resources
            resourceList = resourcesMapper.queryResourceListAuthored(loginUser.getId(), -1);
        }
        List<Resource> list;
        if (resourceList != null && !resourceList.isEmpty()) {
            Set<Resource> resourceSet = new HashSet<>(resourceList);
            List<Resource> authedResourceList = queryResourceList(userId, Constants.AUTHORIZE_WRITABLE_PERM);
            getAuthorizedResourceList(resourceSet, authedResourceList);
            list = new ArrayList<>(resourceSet);
        } else {
            list = new ArrayList<>(0);
        }
        // Transform into StorageEntity for compatibility
        List<StorageEntity> transformedResourceList = list.stream()
                .map(this::createStorageEntityBasedOnResource)
                .collect(Collectors.toList());
        Visitor visitor = new ResourceTreeVisitor(transformedResourceList);
        result.put(Constants.DATA_LIST, visitor.visit("").getChildren());
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * unauthorized udf function
     *
     * @param loginUser login user
     * @param userId    user id
     * @return unauthorized result code
     */
    @Override
    public Map<String, Object> unauthorizedUDFFunction(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }

        List<UdfFunc> udfFuncList;
        if (isAdmin(loginUser)) {
            // admin gets all udfs except userId
            udfFuncList = udfFunctionMapper.queryUdfFuncExceptUserId(userId);
        } else {
            // non-admins users get their own udfs
            udfFuncList = udfFunctionMapper.selectByMap(Collections.singletonMap("user_id", loginUser.getId()));
        }
        List<UdfFunc> resultList = new ArrayList<>();
        Set<UdfFunc> udfFuncSet;
        if (CollectionUtils.isNotEmpty(udfFuncList)) {
            udfFuncSet = new HashSet<>(udfFuncList);

            List<UdfFunc> authedUDFFuncList = udfFunctionMapper.queryAuthedUdfFunc(userId);

            getAuthorizedResourceList(udfFuncSet, authedUDFFuncList);
            resultList = new ArrayList<>(udfFuncSet);
        }
        result.put(Constants.DATA_LIST, resultList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * authorized udf function
     *
     * @param loginUser login user
     * @param userId    user id
     * @return authorized result code
     */
    @Override
    public Map<String, Object> authorizedUDFFunction(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        List<UdfFunc> udfFuncs = udfFunctionMapper.queryAuthedUdfFunc(userId);
        result.put(Constants.DATA_LIST, udfFuncs);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * authorized file
     *
     * @param loginUser login user
     * @param userId    user id
     * @return authorized result
     */
    @Override
    public Map<String, Object> authorizedFile(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }

        List<Resource> authedResources = queryResourceList(userId, Constants.AUTHORIZE_WRITABLE_PERM);
        // Transform into StorageEntity for compatibility
        List<StorageEntity> transformedResourceList = authedResources.stream()
                .map(this::createStorageEntityBasedOnResource)
                .collect(Collectors.toList());
        Visitor visitor = new ResourceTreeVisitor(transformedResourceList);
        String visit = JSONUtils.toJsonString(visitor.visit(""), SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        logger.info(visit);
        String jsonTreeStr =
                JSONUtils.toJsonString(visitor.visit("").getChildren(), SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        logger.info(jsonTreeStr);
        result.put(Constants.DATA_LIST, visitor.visit("").getChildren());
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * get authorized resource list
     *
     * @param resourceSet        resource set
     * @param authedResourceList authorized resource list
     */
    private void getAuthorizedResourceList(Set<?> resourceSet, List<?> authedResourceList) {
        Set<?> authedResourceSet;
        if (CollectionUtils.isNotEmpty(authedResourceList)) {
            authedResourceSet = new HashSet<>(authedResourceList);
            resourceSet.removeAll(authedResourceSet);
        }
    }

    /**
     * list all children id
     *
     * @param resource    resource
     * @param containSelf whether add self to children list
     * @return all children id
     */
    List<Integer> listAllChildren(Resource resource, boolean containSelf) {
        List<Integer> childList = new ArrayList<>();
        if (resource.getId() != null && containSelf) {
            childList.add(resource.getId());
        }

        if (resource.isDirectory()) {
            listAllChildren(resource.getId(), childList);
        }
        return childList;
    }

    /**
     * list all children id
     *
     * @param resourceId resource id
     * @param childList  child list
     */
    void listAllChildren(int resourceId, List<Integer> childList) {
        List<Integer> children = resourcesMapper.listChildren(resourceId);
        for (int childId : children) {
            childList.add(childId);
            listAllChildren(childId, childList);
        }
    }

    /**
     * query authored resource list (own and authorized)
     *
     * @param loginUser login user
     * @param type      ResourceType
     * @return all authored resource list
     */
    private List<Resource> queryAuthoredResourceList(User loginUser, ResourceType type) {
        Set<Integer> resourceIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(checkResourceType(type), loginUser.getId(), logger);
        if (resourceIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Resource> resources = resourcesMapper.selectBatchIds(resourceIds);
        resources = resources.stream().filter(rs -> rs.getType() == type).collect(Collectors.toList());
        return resources;
    }

    /**
     * query resource list by userId and perm
     *
     * @param userId userId
     * @param perm   perm
     * @return resource list
     */
    private List<Resource> queryResourceList(Integer userId, int perm) {
        List<Integer> resIds = resourceUserMapper.queryResourcesIdListByUserIdAndPerm(userId, perm);
        return CollectionUtils.isEmpty(resIds) ? new ArrayList<>() : resourcesMapper.queryResourceListById(resIds);
    }

    private AuthorizationType checkResourceType(ResourceType type) {
        return type.equals(ResourceType.FILE) ? AuthorizationType.RESOURCE_FILE_ID : AuthorizationType.UDF_FILE;
    }

    /**
     * check permission by comparing login user's tenantCode with tenantCode in the request
     *
     * @param isAdmin is the login user admin
     * @param userTenantCode loginUser's tenantCode
     * @param resTenantCode tenantCode in the request field "resTenantCode" for tenant code owning the resource,
     *                      can be different from the login user in the case of logging in as admin users.
     * @return isValid
     */
    private boolean isUserTenantValid(boolean isAdmin, String userTenantCode,
                                      String resTenantCode) throws ServiceException {
        if (!isAdmin) {
            resTenantCode = resTenantCode == null ? "" : resTenantCode;
            if (!StringUtils.isBlank(resTenantCode) && !resTenantCode.equals(userTenantCode)) {
                // if an ordinary user directly send a query API with a different tenantCode and fullName "",
                // still he/she does not have read permission.
                return false;
            }
        }

        return true;
    }
}
