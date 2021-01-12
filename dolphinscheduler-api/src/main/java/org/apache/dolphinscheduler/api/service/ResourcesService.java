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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections.BeanMap;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.api.dto.resources.filter.ResourceFilter;
import org.apache.dolphinscheduler.api.dto.resources.visitor.ResourceTreeVisitor;
import org.apache.dolphinscheduler.api.dto.resources.visitor.Visitor;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.mapper.*;
import org.apache.dolphinscheduler.dao.utils.ResourceProcessDefinitionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.apache.dolphinscheduler.common.Constants.*;

/**
 * resources service
 */
@Service
public class ResourcesService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesService.class);

    @Autowired
    private ResourceMapper resourcesMapper;

    @Autowired
    private UdfFuncMapper udfFunctionMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ResourceUserMapper resourceUserMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    /**
     * create directory
     *
     * @param loginUser login user
     * @param name alias
     * @param description description
     * @param type type
     * @param pid parent id
     * @param currentDir current directory
     * @return create directory result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result createDirectory(User loginUser,
                                 String name,
                                 String description,
                                 ResourceType type,
                                 int pid,
                                 String currentDir) {
        Result result = new Result();
        // if hdfs not startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }
        String fullName = currentDir.equals("/") ? String.format("%s%s",currentDir,name):String.format("%s/%s",currentDir,name);
        result = verifyResourceName(fullName,type,loginUser);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }
        if (pid != -1) {
            Resource parentResource = resourcesMapper.selectById(pid);

            if (parentResource == null) {
                putMsg(result, Status.PARENT_RESOURCE_NOT_EXIST);
                return result;
            }

            if (!hasPerm(loginUser, parentResource.getUserId())) {
                putMsg(result, Status.USER_NO_OPERATION_PERM);
                return result;
            }
        }

        Date now = new Date();

        Resource resource = new Resource(pid,name,fullName,true,description,name,loginUser.getId(),type,0,now,now);

        try {
            resourcesMapper.insert(resource);

            putMsg(result, Status.SUCCESS);
            Map<Object, Object> dataMap = new BeanMap(resource);
            Map<String, Object> resultMap = new HashMap<String, Object>();
            for (Map.Entry<Object, Object> entry: dataMap.entrySet()) {
                if (!"class".equalsIgnoreCase(entry.getKey().toString())) {
                    resultMap.put(entry.getKey().toString(), entry.getValue());
                }
            }
            result.setData(resultMap);
        } catch (DuplicateKeyException e) {
            logger.error("resource directory {} has exist, can't recreate", fullName);
            putMsg(result, Status.RESOURCE_EXIST);
            return result;
        } catch (Exception e) {
            logger.error("resource already exists, can't recreate ", e);
            throw new RuntimeException("resource already exists, can't recreate");
        }
        //create directory in hdfs
        createDirecotry(loginUser,fullName,type,result);
        return result;
    }

    /**
     * create resource
     *
     * @param loginUser login user
     * @param name alias
     * @param desc description
     * @param file file
     * @param type type
     * @param pid parent id
     * @param currentDir current directory
     * @return create result code
     */
    @Transactional(rollbackFor = Exception.class)
    public Result createResource(User loginUser,
                                 String name,
                                 String desc,
                                 ResourceType type,
                                 MultipartFile file,
                                 int pid,
                                 String currentDir) {
        Result result = new Result();

        // if hdfs not startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        if (pid != -1) {
            Resource parentResource = resourcesMapper.selectById(pid);

            if (parentResource == null) {
                putMsg(result, Status.PARENT_RESOURCE_NOT_EXIST);
                return result;
            }

            if (!hasPerm(loginUser, parentResource.getUserId())) {
                putMsg(result, Status.USER_NO_OPERATION_PERM);
                return result;
            }
        }

        // file is empty
        if (file.isEmpty()) {
            logger.error("file is empty: {}", file.getOriginalFilename());
            putMsg(result, Status.RESOURCE_FILE_IS_EMPTY);
            return result;
        }

        // file suffix
        String fileSuffix = FileUtils.suffix(file.getOriginalFilename());
        String nameSuffix = FileUtils.suffix(name);

        // determine file suffix
        if (!(StringUtils.isNotEmpty(fileSuffix) && fileSuffix.equalsIgnoreCase(nameSuffix))) {
            /**
             * rename file suffix and original suffix must be consistent
             */
            logger.error("rename file suffix and original suffix must be consistent: {}", file.getOriginalFilename());
            putMsg(result, Status.RESOURCE_SUFFIX_FORBID_CHANGE);
            return result;
        }

        //If resource type is UDF, only jar packages are allowed to be uploaded, and the suffix must be .jar
        if (Constants.UDF.equals(type.name()) && !JAR.equalsIgnoreCase(fileSuffix)) {
            logger.error(Status.UDF_RESOURCE_SUFFIX_NOT_JAR.getMsg());
            putMsg(result, Status.UDF_RESOURCE_SUFFIX_NOT_JAR);
            return result;
        }
        if (file.getSize() > Constants.MAX_FILE_SIZE) {
            logger.error("file size is too large: {}", file.getOriginalFilename());
            putMsg(result, Status.RESOURCE_SIZE_EXCEED_LIMIT);
            return result;
        }

        // check resoure name exists
        String fullName = currentDir.equals("/") ? String.format("%s%s",currentDir,name):String.format("%s/%s",currentDir,name);
        if (checkResourceExists(fullName, 0, type.ordinal())) {
            logger.error("resource {} has exist, can't recreate", name);
            putMsg(result, Status.RESOURCE_EXIST);
            return result;
        }

        Date now = new Date();
        Resource resource = new Resource(pid,name,fullName,false,desc,file.getOriginalFilename(),loginUser.getId(),type,file.getSize(),now,now);

        try {
            resourcesMapper.insert(resource);

            putMsg(result, Status.SUCCESS);
            Map<Object, Object> dataMap = new BeanMap(resource);
            Map<String, Object> resultMap = new HashMap<>();
            for (Map.Entry<Object, Object> entry: dataMap.entrySet()) {
                if (!"class".equalsIgnoreCase(entry.getKey().toString())) {
                    resultMap.put(entry.getKey().toString(), entry.getValue());
                }
            }
            result.setData(resultMap);
        } catch (Exception e) {
            logger.error("resource already exists, can't recreate ", e);
            throw new RuntimeException("resource already exists, can't recreate");
        }

        // fail upload
        if (!upload(loginUser, fullName, file, type)) {
            logger.error("upload resource: {} file: {} failed.", name, file.getOriginalFilename());
            putMsg(result, Status.HDFS_OPERATION_ERROR);
            throw new RuntimeException(String.format("upload resource: %s file: %s failed.", name, file.getOriginalFilename()));
        }
        return result;
    }

    /**
     * check resource is exists
     *
     * @param fullName  fullName
     * @param userId    user id
     * @param type      type
     * @return true if resource exists
     */
    private boolean checkResourceExists(String fullName, int userId, int type ){

        List<Resource> resources = resourcesMapper.queryResourceList(fullName, userId, type);
        if (resources != null && resources.size() > 0) {
            return true;
        }
        return false;
    }


    /**
     * update resource
     * @param loginUser     login user
     * @param resourceId    resource id
     * @param name          name
     * @param desc          description
     * @param type          resource type
     * @param file          resource file
     * @return  update result code
     */
    @Transactional(rollbackFor = Exception.class)
    public Result updateResource(User loginUser,
                                 int resourceId,
                                 String name,
                                 String desc,
                                 ResourceType type,
                                 MultipartFile file) {
        Result result = new Result();

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        Resource resource = resourcesMapper.selectById(resourceId);
        if (resource == null) {
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        if (!hasPerm(loginUser, resource.getUserId())) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        if (file == null && name.equals(resource.getAlias()) && desc.equals(resource.getDescription())) {
            putMsg(result, Status.SUCCESS);
            return result;
        }

        //check resource aleady exists
        String originFullName = resource.getFullName();
        String originResourceName = resource.getAlias();

        String fullName = String.format("%s%s",originFullName.substring(0,originFullName.lastIndexOf("/")+1),name);
        if (!originResourceName.equals(name) && checkResourceExists(fullName, 0, type.ordinal())) {
            logger.error("resource {} already exists, can't recreate", name);
            putMsg(result, Status.RESOURCE_EXIST);
            return result;
        }

        if (file != null) {

            // file is empty
            if (file.isEmpty()) {
                logger.error("file is empty: {}", file.getOriginalFilename());
                putMsg(result, Status.RESOURCE_FILE_IS_EMPTY);
                return result;
            }

            // file suffix
            String fileSuffix = FileUtils.suffix(file.getOriginalFilename());
            String nameSuffix = FileUtils.suffix(name);

            // determine file suffix
            if (!(StringUtils.isNotEmpty(fileSuffix) && fileSuffix.equalsIgnoreCase(nameSuffix))) {
                /**
                 * rename file suffix and original suffix must be consistent
                 */
                logger.error("rename file suffix and original suffix must be consistent: {}", file.getOriginalFilename());
                putMsg(result, Status.RESOURCE_SUFFIX_FORBID_CHANGE);
                return result;
            }

            //If resource type is UDF, only jar packages are allowed to be uploaded, and the suffix must be .jar
            if (Constants.UDF.equals(type.name()) && !JAR.equalsIgnoreCase(FileUtils.suffix(originFullName))) {
                logger.error(Status.UDF_RESOURCE_SUFFIX_NOT_JAR.getMsg());
                putMsg(result, Status.UDF_RESOURCE_SUFFIX_NOT_JAR);
                return result;
            }
            if (file.getSize() > Constants.MAX_FILE_SIZE) {
                logger.error("file size is too large: {}", file.getOriginalFilename());
                putMsg(result, Status.RESOURCE_SIZE_EXCEED_LIMIT);
                return result;
            }
        }

        // query tenant by user id
        String tenantCode = getTenantCode(resource.getUserId(),result);
        if (StringUtils.isEmpty(tenantCode)){
            return result;
        }
        // verify whether the resource exists in storage
        // get the path of origin file in storage
        String originHdfsFileName = HadoopUtils.getHdfsFileName(resource.getType(),tenantCode,originFullName);
        try {
            if (!HadoopUtils.getInstance().exists(originHdfsFileName)) {
                logger.error("{} not exist", originHdfsFileName);
                putMsg(result,Status.RESOURCE_NOT_EXIST);
                return result;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            throw new ServiceException(Status.HDFS_OPERATION_ERROR);
        }

        if (!resource.isDirectory()) {
            //get the origin file suffix
            String originSuffix = FileUtils.suffix(originFullName);
            String suffix = FileUtils.suffix(fullName);
            boolean suffixIsChanged = false;
            if (StringUtils.isBlank(suffix) && StringUtils.isNotBlank(originSuffix)) {
                suffixIsChanged = true;
            }
            if (StringUtils.isNotBlank(suffix) && !suffix.equals(originSuffix)) {
                suffixIsChanged = true;
            }
            //verify whether suffix is changed
            if (suffixIsChanged) {
                //need verify whether this resource is authorized to other users
                Map<String, Object> columnMap = new HashMap<>();
                columnMap.put("resources_id", resourceId);

                List<ResourcesUser> resourcesUsers = resourceUserMapper.selectByMap(columnMap);
                if (CollectionUtils.isNotEmpty(resourcesUsers)) {
                    List<Integer> userIds = resourcesUsers.stream().map(ResourcesUser::getUserId).collect(Collectors.toList());
                    List<User> users = userMapper.selectBatchIds(userIds);
                    String userNames = users.stream().map(User::getUserName).collect(Collectors.toList()).toString();
                    logger.error("resource is authorized to user {},suffix not allowed to be modified", userNames);
                    putMsg(result,Status.RESOURCE_IS_AUTHORIZED,userNames);
                    return result;
                }
            }
        }

        // updateResource data

        Date now = new Date();

        resource.setAlias(name);
        resource.setFullName(fullName);
        resource.setDescription(desc);
        resource.setUpdateTime(now);
        if (file != null) {
            resource.setFileName(file.getOriginalFilename());
            resource.setSize(file.getSize());
        }

        try {
            resourcesMapper.updateById(resource);
            if (resource.isDirectory()) {
                List<Integer> childrenResource = listAllChildren(resource,false);
                if (CollectionUtils.isNotEmpty(childrenResource)) {
                    String matcherFullName = Matcher.quoteReplacement(fullName);
                    List<Resource> childResourceList = new ArrayList<>();
                    Integer[] childResIdArray = childrenResource.toArray(new Integer[childrenResource.size()]);
                    List<Resource> resourceList = resourcesMapper.listResourceByIds(childResIdArray);
                    childResourceList = resourceList.stream().map(t -> {
                        t.setFullName(t.getFullName().replaceFirst(originFullName, matcherFullName));
                        t.setUpdateTime(now);
                        return t;
                    }).collect(Collectors.toList());
                    resourcesMapper.batchUpdateResource(childResourceList);

                    if (ResourceType.UDF.equals(resource.getType())) {
                        List<UdfFunc> udfFuncs = udfFunctionMapper.listUdfByResourceId(childResIdArray);
                        if (CollectionUtils.isNotEmpty(udfFuncs)) {
                            udfFuncs = udfFuncs.stream().map(t -> {
                                t.setResourceName(t.getResourceName().replaceFirst(originFullName, matcherFullName));
                                t.setUpdateTime(now);
                                return t;
                            }).collect(Collectors.toList());
                            udfFunctionMapper.batchUpdateUdfFunc(udfFuncs);
                        }
                    }
                }
            } else if (ResourceType.UDF.equals(resource.getType())) {
                List<UdfFunc> udfFuncs = udfFunctionMapper.listUdfByResourceId(new Integer[]{resourceId});
                if (CollectionUtils.isNotEmpty(udfFuncs)) {
                    udfFuncs = udfFuncs.stream().map(t -> {
                        t.setResourceName(fullName);
                        t.setUpdateTime(now);
                        return t;
                    }).collect(Collectors.toList());
                    udfFunctionMapper.batchUpdateUdfFunc(udfFuncs);
                }

            }

            putMsg(result, Status.SUCCESS);
            Map<Object, Object> dataMap = new BeanMap(resource);
            Map<String, Object> resultMap = new HashMap<>(5);
            for (Map.Entry<Object, Object> entry: dataMap.entrySet()) {
                if (!Constants.CLASS.equalsIgnoreCase(entry.getKey().toString())) {
                    resultMap.put(entry.getKey().toString(), entry.getValue());
                }
            }
            result.setData(resultMap);
        } catch (Exception e) {
            logger.error(Status.UPDATE_RESOURCE_ERROR.getMsg(), e);
            throw new ServiceException(Status.UPDATE_RESOURCE_ERROR);
        }

        // if name unchanged, return directly without moving on HDFS
        if (originResourceName.equals(name) && file == null) {
            return result;
        }

        if (file != null) {
            // fail upload
            if (!upload(loginUser, fullName, file, type)) {
                logger.error("upload resource: {} file: {} failed.", name, file.getOriginalFilename());
                putMsg(result, Status.HDFS_OPERATION_ERROR);
                throw new RuntimeException(String.format("upload resource: %s file: %s failed.", name, file.getOriginalFilename()));
            }
            if (!fullName.equals(originFullName)) {
                try {
                    HadoopUtils.getInstance().delete(originHdfsFileName,false);
                } catch (IOException e) {
                    logger.error(e.getMessage(),e);
                    throw new RuntimeException(String.format("delete resource: %s failed.", originFullName));
                }
            }
            return result;
        }


        // get the path of dest file in hdfs
        String destHdfsFileName = HadoopUtils.getHdfsFileName(resource.getType(),tenantCode,fullName);


        try {
            logger.info("start hdfs copy {} -> {}", originHdfsFileName, destHdfsFileName);
            HadoopUtils.getInstance().copy(originHdfsFileName, destHdfsFileName, true, true);
        } catch (Exception e) {
            logger.error(MessageFormat.format("hdfs copy {0} -> {1} fail", originHdfsFileName, destHdfsFileName), e);
            putMsg(result,Status.HDFS_COPY_FAIL);
            throw new ServiceException(Status.HDFS_COPY_FAIL);
        }

        return result;

    }

    /**
     * query resources list paging
     *
     * @param loginUser login user
     * @param type resource type
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return resource list page
     */
    public Map<String, Object> queryResourceListPaging(User loginUser, int direcotryId, ResourceType type, String searchVal, Integer pageNo, Integer pageSize) {

        HashMap<String, Object> result = new HashMap<>(5);
        Page<Resource> page = new Page(pageNo, pageSize);
        int userId = loginUser.getId();
        if (isAdmin(loginUser)) {
            userId= 0;
        }
        if (direcotryId != -1) {
            Resource directory = resourcesMapper.selectById(direcotryId);
            if (directory == null) {
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }
        }

        IPage<Resource> resourceIPage = resourcesMapper.queryResourcePaging(page,
                userId,direcotryId, type.ordinal(), searchVal);
        PageInfo pageInfo = new PageInfo<Resource>(pageNo, pageSize);
        pageInfo.setTotalCount((int)resourceIPage.getTotal());
        pageInfo.setLists(resourceIPage.getRecords());
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result,Status.SUCCESS);
        return result;
    }

    /**
     * create direcoty
     * @param loginUser login user
     * @param fullName  full name
     * @param type      resource type
     * @param result    Result
     */
    private void createDirecotry(User loginUser,String fullName,ResourceType type,Result result){
        // query tenant
        String tenantCode = tenantMapper.queryById(loginUser.getTenantId()).getTenantCode();
        String directoryName = HadoopUtils.getHdfsFileName(type,tenantCode,fullName);
        String resourceRootPath = HadoopUtils.getHdfsDir(type,tenantCode);
        try {
            if (!HadoopUtils.getInstance().exists(resourceRootPath)) {
                createTenantDirIfNotExists(tenantCode);
            }

            if (!HadoopUtils.getInstance().mkdir(directoryName)) {
                logger.error("create resource directory {} of hdfs failed",directoryName);
                putMsg(result,Status.HDFS_OPERATION_ERROR);
                throw new RuntimeException(String.format("create resource directory: %s failed.", directoryName));
            }
        } catch (Exception e) {
            logger.error("create resource directory {} of hdfs failed",directoryName);
            putMsg(result,Status.HDFS_OPERATION_ERROR);
            throw new RuntimeException(String.format("create resource directory: %s failed.", directoryName));
        }
    }

    /**
     * upload file to hdfs
     *
     * @param loginUser login user
     * @param fullName  full name
     * @param file      file
     */
    private boolean upload(User loginUser, String fullName, MultipartFile file, ResourceType type) {
        // save to local
        String fileSuffix = FileUtils.suffix(file.getOriginalFilename());
        String nameSuffix = FileUtils.suffix(fullName);

        // determine file suffix
        if (!(StringUtils.isNotEmpty(fileSuffix) && fileSuffix.equalsIgnoreCase(nameSuffix))) {
            return false;
        }
        // query tenant
        String tenantCode = tenantMapper.queryById(loginUser.getTenantId()).getTenantCode();
        // random file name
        String localFilename = FileUtils.getUploadFilename(tenantCode, UUID.randomUUID().toString());


        // save file to hdfs, and delete original file
        String hdfsFilename = HadoopUtils.getHdfsFileName(type,tenantCode,fullName);
        String resourcePath = HadoopUtils.getHdfsDir(type,tenantCode);
        try {
            // if tenant dir not exists
            if (!HadoopUtils.getInstance().exists(resourcePath)) {
                createTenantDirIfNotExists(tenantCode);
            }
            org.apache.dolphinscheduler.api.utils.FileUtils.copyFile(file, localFilename);
            HadoopUtils.getInstance().copyLocalToHdfs(localFilename, hdfsFilename, true, true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * query resource list
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    public Map<String, Object> queryResourceList(User loginUser, ResourceType type) {

        Map<String, Object> result = new HashMap<>(5);

        int userId = loginUser.getId();
        if(isAdmin(loginUser)){
            userId = 0;
        }
        List<Resource> allResourceList = resourcesMapper.queryResourceListAuthored(userId, type.ordinal(),0);
        Visitor resourceTreeVisitor = new ResourceTreeVisitor(allResourceList);
        //JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(resourceTreeVisitor.visit().getChildren(), SerializerFeature.SortField));
        result.put(Constants.DATA_LIST, resourceTreeVisitor.visit().getChildren());
        putMsg(result,Status.SUCCESS);

        return result;
    }

    /**
     * query resource list by program type
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    public Map<String, Object> queryResourceByProgramType(User loginUser, ResourceType type, ProgramType programType) {

        Map<String, Object> result = new HashMap<>(5);
        String suffix = ".jar";
        int userId = loginUser.getId();
        if(isAdmin(loginUser)){
            userId = 0;
        }
        if (programType != null) {
            switch (programType) {
                case JAVA:
                    break;
                case SCALA:
                    break;
                case PYTHON:
                    suffix = ".py";
                    break;
            }
        }
        List<Resource> allResourceList = resourcesMapper.queryResourceListAuthored(userId, type.ordinal(),0);
        List<Resource> resources = new ResourceFilter(suffix,new ArrayList<>(allResourceList)).filter();
        Visitor resourceTreeVisitor = new ResourceTreeVisitor(resources);
        result.put(Constants.DATA_LIST, resourceTreeVisitor.visit().getChildren());
        putMsg(result,Status.SUCCESS);

        return result;
    }

    /**
     * delete resource
     *
     * @param loginUser login user
     * @param resourceId resource id
     * @return delete result code
     * @throws Exception exception
     */
    @Transactional(rollbackFor = Exception.class)
    public Result delete(User loginUser, int resourceId) throws Exception {
        Result result = new Result();

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        //get resource and  hdfs path
        Resource resource = resourcesMapper.selectById(resourceId);
        if (resource == null) {
            logger.error("resource file not exist,  resource id {}", resourceId);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        if (!hasPerm(loginUser, resource.getUserId())) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        String tenantCode = getTenantCode(resource.getUserId(),result);
        if (StringUtils.isEmpty(tenantCode)){
            return  result;
        }

        // get all resource id of process definitions those is released
        List<Map<String, Object>> list = processDefinitionMapper.listResources();
        Map<Integer, Set<Integer>> resourceProcessMap = ResourceProcessDefinitionUtils.getResourceProcessDefinitionMap(list);
        Set<Integer> resourceIdSet = resourceProcessMap.keySet();
        // get all children of the resource
        List<Integer> allChildren = listAllChildren(resource,true);
        Integer[] needDeleteResourceIdArray = allChildren.toArray(new Integer[allChildren.size()]);

        //if resource type is UDF,need check whether it is bound by UDF functon
        if (resource.getType() == (ResourceType.UDF)) {
            List<UdfFunc> udfFuncs = udfFunctionMapper.listUdfByResourceId(needDeleteResourceIdArray);
            if (CollectionUtils.isNotEmpty(udfFuncs)) {
                logger.error("can't be deleted,because it is bound by UDF functions:{}",udfFuncs.toString());
                putMsg(result,Status.UDF_RESOURCE_IS_BOUND,udfFuncs.get(0).getFuncName());
                return result;
            }
        }

        if (resourceIdSet.contains(resource.getPid())) {
            logger.error("can't be deleted,because it is used of process definition");
            putMsg(result, Status.RESOURCE_IS_USED);
            return result;
        }
        resourceIdSet.retainAll(allChildren);
        if (CollectionUtils.isNotEmpty(resourceIdSet)) {
            logger.error("can't be deleted,because it is used of process definition");
            for (Integer resId : resourceIdSet) {
                logger.error("resource id:{} is used of process definition {}",resId,resourceProcessMap.get(resId));
            }
            putMsg(result, Status.RESOURCE_IS_USED);
            return result;
        }

        // get hdfs file by type
        String hdfsFilename = HadoopUtils.getHdfsFileName(resource.getType(), tenantCode, resource.getFullName());

        //delete data in database
        resourcesMapper.deleteIds(needDeleteResourceIdArray);
        resourceUserMapper.deleteResourceUserArray(0, needDeleteResourceIdArray);

        //delete file on hdfs
        HadoopUtils.getInstance().delete(hdfsFilename, true);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * verify resource by name and type
     * @param loginUser login user
     * @param fullName  resource full name
     * @param type      resource type
     * @return true if the resource name not exists, otherwise return false
     */
    public Result verifyResourceName(String fullName, ResourceType type,User loginUser) {
        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        if (checkResourceExists(fullName, 0, type.ordinal())) {
            logger.error("resource type:{} name:{} has exist, can't create again.", type, fullName);
            putMsg(result, Status.RESOURCE_EXIST);
        } else {
            // query tenant
            Tenant tenant = tenantMapper.queryById(loginUser.getTenantId());
            if(tenant != null){
                String tenantCode = tenant.getTenantCode();

                try {
                    String hdfsFilename = HadoopUtils.getHdfsFileName(type,tenantCode,fullName);
                    if(HadoopUtils.getInstance().exists(hdfsFilename)){
                        logger.error("resource type:{} name:{} has exist in hdfs {}, can't create again.", type, fullName,hdfsFilename);
                        putMsg(result, Status.RESOURCE_FILE_EXIST,hdfsFilename);
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                    putMsg(result,Status.HDFS_OPERATION_ERROR);
                }
            }else{
                putMsg(result,Status.TENANT_NOT_EXIST);
            }
        }


        return result;
    }

    /**
     * verify resource by full name or pid and type
     * @param fullName  resource full name
     * @param id        resource id
     * @param type      resource type
     * @return true if the resource full name or pid not exists, otherwise return false
     */
    public Result queryResource(String fullName,Integer id,ResourceType type) {
        Result result = new Result();
        if (StringUtils.isBlank(fullName) && id == null) {
            logger.error("You must input one of fullName and pid");
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            return result;
        }
        if (StringUtils.isNotBlank(fullName)) {
            List<Resource> resourceList = resourcesMapper.queryResource(fullName,type.ordinal());
            if (CollectionUtils.isEmpty(resourceList)) {
                logger.error("resource file not exist,  resource full name {} ", fullName);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }
            putMsg(result, Status.SUCCESS);
            result.setData(resourceList.get(0));
        } else {
            Resource resource = resourcesMapper.selectById(id);
            if (resource == null) {
                logger.error("resource file not exist,  resource id {}", id);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }
            Resource parentResource = resourcesMapper.selectById(resource.getPid());
            if (parentResource == null) {
                logger.error("parent resource file not exist,  resource id {}", id);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }
            putMsg(result, Status.SUCCESS);
            result.setData(parentResource);
        }
        return result;
    }

    /**
     * view resource file online
     *
     * @param resourceId resource id
     * @param skipLineNum skip line number
     * @param limit limit
     * @return resource content
     */
    public Result readResource(int resourceId, int skipLineNum, int limit) {
        Result result = new Result();

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        // get resource by id
        Resource resource = resourcesMapper.selectById(resourceId);
        if (resource == null) {
            logger.error("resource file not exist,  resource id {}", resourceId);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        //check preview or not by file suffix
        String nameSuffix = FileUtils.suffix(resource.getAlias());
        String resourceViewSuffixs = FileUtils.getResourceViewSuffixs();
        if (StringUtils.isNotEmpty(resourceViewSuffixs)) {
            List<String> strList = Arrays.asList(resourceViewSuffixs.split(","));
            if (!strList.contains(nameSuffix)) {
                logger.error("resource suffix {} not support view,  resource id {}", nameSuffix, resourceId);
                putMsg(result, Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW);
                return result;
            }
        }

        String tenantCode = getTenantCode(resource.getUserId(),result);
        if (StringUtils.isEmpty(tenantCode)){
            return  result;
        }

        // hdfs path
        String hdfsFileName = HadoopUtils.getHdfsResourceFileName(tenantCode, resource.getFullName());
        logger.info("resource hdfs path is {} ", hdfsFileName);
        try {
            if(HadoopUtils.getInstance().exists(hdfsFileName)){
                List<String> content = HadoopUtils.getInstance().catFile(hdfsFileName, skipLineNum, limit);

                putMsg(result, Status.SUCCESS);
                Map<String, Object> map = new HashMap<>();
                map.put(ALIAS, resource.getAlias());
                map.put(CONTENT, String.join("\n", content));
                result.setData(map);
            }else{
                logger.error("read file {} not exist in hdfs", hdfsFileName);
                putMsg(result, Status.RESOURCE_FILE_NOT_EXIST,hdfsFileName);
            }

        } catch (Exception e) {
            logger.error("Resource {} read failed", hdfsFileName, e);
            putMsg(result, Status.HDFS_OPERATION_ERROR);
        }

        return result;
    }

    /**
     * create resource file online
     *
     * @param loginUser login user
     * @param type resource type
     * @param fileName file name
     * @param fileSuffix file suffix
     * @param desc description
     * @param content content
     * @return create result code
     */
    @Transactional(rollbackFor = Exception.class)
    public Result onlineCreateResource(User loginUser, ResourceType type, String fileName, String fileSuffix, String desc, String content,int pid,String currentDirectory) {
        Result result = new Result();
        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        //check file suffix
        String nameSuffix = fileSuffix.trim();
        String resourceViewSuffixs = FileUtils.getResourceViewSuffixs();
        if (StringUtils.isNotEmpty(resourceViewSuffixs)) {
            List<String> strList = Arrays.asList(resourceViewSuffixs.split(","));
            if (!strList.contains(nameSuffix)) {
                logger.error("resouce suffix {} not support create", nameSuffix);
                putMsg(result, Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW);
                return result;
            }
        }

        String name = fileName.trim() + "." + nameSuffix;
        String fullName = currentDirectory.equals("/") ? String.format("%s%s",currentDirectory,name):String.format("%s/%s",currentDirectory,name);

        result = verifyResourceName(fullName,type,loginUser);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }
        if (pid != -1) {
            Resource parentResource = resourcesMapper.selectById(pid);

            if (parentResource == null) {
                putMsg(result, Status.PARENT_RESOURCE_NOT_EXIST);
                return result;
            }

            if (!hasPerm(loginUser, parentResource.getUserId())) {
                putMsg(result, Status.USER_NO_OPERATION_PERM);
                return result;
            }
        }

        // save data
        Date now = new Date();
        Resource resource = new Resource(pid,name,fullName,false,desc,name,loginUser.getId(),type,content.getBytes().length,now,now);

        resourcesMapper.insert(resource);

        putMsg(result, Status.SUCCESS);
        Map<Object, Object> dataMap = new BeanMap(resource);
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry: dataMap.entrySet()) {
            if (!Constants.CLASS.equalsIgnoreCase(entry.getKey().toString())) {
                resultMap.put(entry.getKey().toString(), entry.getValue());
            }
        }
        result.setData(resultMap);

        String tenantCode = tenantMapper.queryById(loginUser.getTenantId()).getTenantCode();

        result = uploadContentToHdfs(fullName, tenantCode, content);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            throw new RuntimeException(result.getMsg());
        }
        return result;
    }

    /**
     * updateProcessInstance resource
     *
     * @param resourceId resource id
     * @param content content
     * @return update result cod
     */
    @Transactional(rollbackFor = Exception.class)
    public Result updateResourceContent(int resourceId, String content) {
        Result result = new Result();

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        Resource resource = resourcesMapper.selectById(resourceId);
        if (resource == null) {
            logger.error("read file not exist,  resource id {}", resourceId);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        //check can edit by file suffix
        String nameSuffix = FileUtils.suffix(resource.getAlias());
        String resourceViewSuffixs = FileUtils.getResourceViewSuffixs();
        if (StringUtils.isNotEmpty(resourceViewSuffixs)) {
            List<String> strList = Arrays.asList(resourceViewSuffixs.split(","));
            if (!strList.contains(nameSuffix)) {
                logger.error("resource suffix {} not support updateProcessInstance,  resource id {}", nameSuffix, resourceId);
                putMsg(result, Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW);
                return result;
            }
        }

        String tenantCode = getTenantCode(resource.getUserId(),result);
        if (StringUtils.isEmpty(tenantCode)){
            return  result;
        }
        resource.setSize(content.getBytes().length);
        resource.setUpdateTime(new Date());
        resourcesMapper.updateById(resource);


        result = uploadContentToHdfs(resource.getFullName(), tenantCode, content);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            throw new RuntimeException(result.getMsg());
        }
        return result;
    }

    /**
     * @param resourceName  resource name
     * @param tenantCode    tenant code
     * @param content       content
     * @return result
     */
    private Result uploadContentToHdfs(String resourceName, String tenantCode, String content) {
        Result result = new Result();
        String localFilename = "";
        String hdfsFileName = "";
        try {
            localFilename = FileUtils.getUploadFilename(tenantCode, UUID.randomUUID().toString());

            if (!FileUtils.writeContent2File(content, localFilename)) {
                // write file fail
                logger.error("file {} fail, content is {}", localFilename, content);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }

            // get resource file hdfs path
            hdfsFileName = HadoopUtils.getHdfsResourceFileName(tenantCode, resourceName);
            String resourcePath = HadoopUtils.getHdfsResDir(tenantCode);
            logger.info("resource hdfs path is {} ", hdfsFileName);

            HadoopUtils hadoopUtils = HadoopUtils.getInstance();
            if (!hadoopUtils.exists(resourcePath)) {
                // create if tenant dir not exists
                createTenantDirIfNotExists(tenantCode);
            }
            if (hadoopUtils.exists(hdfsFileName)) {
                hadoopUtils.delete(hdfsFileName, false);
            }

            hadoopUtils.copyLocalToHdfs(localFilename, hdfsFileName, true, true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setCode(Status.HDFS_OPERATION_ERROR.getCode());
            result.setMsg(String.format("copy %s to hdfs %s fail", localFilename, hdfsFileName));
            return result;
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * download file
     *
     * @param resourceId resource id
     * @return resource content
     * @throws Exception exception
     */
    public org.springframework.core.io.Resource downloadResource(int resourceId) throws Exception {
        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            throw new RuntimeException("hdfs not startup");
        }

        Resource resource = resourcesMapper.selectById(resourceId);
        if (resource == null) {
            logger.error("download file not exist,  resource id {}", resourceId);
            return null;
        }
        if (resource.isDirectory()) {
            logger.error("resource id {} is directory,can't download it", resourceId);
            throw new RuntimeException("cant't download directory");
        }

        int userId = resource.getUserId();
        User user = userMapper.selectById(userId);
        if(user == null){
            logger.error("user id {} not exists", userId);
            throw new RuntimeException(String.format("resource owner id %d not exist",userId));
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if(tenant == null){
            logger.error("tenant id {} not exists", user.getTenantId());
            throw new RuntimeException(String.format("The tenant id %d of resource owner not exist",user.getTenantId()));
        }

        String tenantCode = tenant.getTenantCode();

        String hdfsFileName = HadoopUtils.getHdfsFileName(resource.getType(), tenantCode, resource.getFullName());

        String localFileName = FileUtils.getDownloadFilename(resource.getAlias());
        logger.info("resource hdfs path is {} ", hdfsFileName);

        HadoopUtils.getInstance().copyHdfsToLocal(hdfsFileName, localFileName, false, true);
        return org.apache.dolphinscheduler.api.utils.FileUtils.file2Resource(localFileName);
    }


    /**
     * list all file
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    public Map<String, Object> authorizeResourceTree(User loginUser, Integer userId) {

        Map<String, Object> result = new HashMap<>();
        if (checkAdmin(loginUser, result)) {
            return result;
        }
        List<Resource> resourceList = resourcesMapper.queryResourceExceptUserId(userId);
        List<ResourceComponent> list ;
        if (CollectionUtils.isNotEmpty(resourceList)) {
            Visitor visitor = new ResourceTreeVisitor(resourceList);
            list = visitor.visit().getChildren();
        }else {
            list = new ArrayList<>(0);
        }

        result.put(Constants.DATA_LIST, list);
        putMsg(result,Status.SUCCESS);
        return result;
    }

    /**
     * unauthorized file
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    public Map<String, Object> unauthorizedFile(User loginUser, Integer userId) {

        Map<String, Object> result = new HashMap<>();
        if (checkAdmin(loginUser, result)) {
            return result;
        }
        List<Resource> resourceList = resourcesMapper.queryResourceExceptUserId(userId);
        List<Resource> list ;
        if (resourceList != null && resourceList.size() > 0) {
            Set<Resource> resourceSet = new HashSet<>(resourceList);
            List<Resource> authedResourceList = resourcesMapper.queryAuthorizedResourceList(userId);

            getAuthorizedResourceList(resourceSet, authedResourceList);
            list = new ArrayList<>(resourceSet);
        }else {
            list = new ArrayList<>(0);
        }
        Visitor visitor = new ResourceTreeVisitor(list);
        result.put(Constants.DATA_LIST, visitor.visit().getChildren());
        putMsg(result,Status.SUCCESS);
        return result;
    }

    /**
     * unauthorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    public Map<String, Object> unauthorizedUDFFunction(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>(5);
        //only admin can operate
        if (checkAdmin(loginUser, result)) {
            return result;
        }

        List<UdfFunc> udfFuncList = udfFunctionMapper.queryUdfFuncExceptUserId(userId);
        List<UdfFunc> resultList = new ArrayList<>();
        Set<UdfFunc> udfFuncSet = null;
        if (CollectionUtils.isNotEmpty(udfFuncList)) {
            udfFuncSet = new HashSet<>(udfFuncList);

            List<UdfFunc> authedUDFFuncList = udfFunctionMapper.queryAuthedUdfFunc(userId);

            getAuthorizedResourceList(udfFuncSet, authedUDFFuncList);
            resultList = new ArrayList<>(udfFuncSet);
        }
        result.put(Constants.DATA_LIST, resultList);
        putMsg(result,Status.SUCCESS);
        return result;
    }




    /**
     * authorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result code
     */
    public Map<String, Object> authorizedUDFFunction(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (checkAdmin(loginUser, result)) {
            return result;
        }
        List<UdfFunc> udfFuncs = udfFunctionMapper.queryAuthedUdfFunc(userId);
        result.put(Constants.DATA_LIST, udfFuncs);
        putMsg(result,Status.SUCCESS);
        return result;
    }


    /**
     * authorized file
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result
     */
    public Map<String, Object> authorizedFile(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>(5);
        if (checkAdmin(loginUser, result)){
            return result;
        }
        List<Resource> authedResources = resourcesMapper.queryAuthorizedResourceList(userId);
        Visitor visitor = new ResourceTreeVisitor(authedResources);
        logger.info(JSON.toJSONString(visitor.visit(), SerializerFeature.SortField));
        String jsonTreeStr = JSON.toJSONString(visitor.visit().getChildren(), SerializerFeature.SortField);
        logger.info(jsonTreeStr);
        result.put(Constants.DATA_LIST, visitor.visit().getChildren());
        putMsg(result,Status.SUCCESS);
        return result;
    }

    /**
     * get authorized resource list
     *
     * @param resourceSet resource set
     * @param authedResourceList authorized resource list
     */
    private void getAuthorizedResourceList(Set<?> resourceSet, List<?> authedResourceList) {
        Set<?> authedResourceSet = null;
        if (CollectionUtils.isNotEmpty(authedResourceList)) {
            authedResourceSet = new HashSet<>(authedResourceList);
            resourceSet.removeAll(authedResourceSet);
        }
    }

    /**
     * get tenantCode by UserId
     *
     * @param userId user id
     * @param result return result
     * @return
     */
    private String getTenantCode(int userId,Result result){

        User user = userMapper.selectById(userId);
        if (user == null) {
            logger.error("user {} not exists", userId);
            putMsg(result, Status.USER_NOT_EXIST,userId);
            return null;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null){
            logger.error("tenant not exists");
            putMsg(result, Status.TENANT_NOT_EXIST);
            return null;
        }
        return tenant.getTenantCode();
    }

    /**
     * list all children id
     * @param resource    resource
     * @param containSelf whether add self to children list
     * @return all children id
     */
    List<Integer> listAllChildren(Resource resource,boolean containSelf){
        List<Integer> childList = new ArrayList<>();
        if (resource.getId() != -1 && containSelf) {
            childList.add(resource.getId());
        }

        if(resource.isDirectory()){
            listAllChildren(resource.getId(),childList);
        }
        return childList;
    }

    /**
     * list all children id
     * @param resourceId    resource id
     * @param childList     child list
     */
    void listAllChildren(int resourceId,List<Integer> childList){

        List<Integer> children = resourcesMapper.listChildren(resourceId);
        for(int chlidId:children){
            childList.add(chlidId);
            listAllChildren(chlidId,childList);
        }
    }

}
