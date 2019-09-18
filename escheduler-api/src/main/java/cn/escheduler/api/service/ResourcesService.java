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
package cn.escheduler.api.service;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.PageInfo;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.ResourceType;
import cn.escheduler.common.enums.UserType;
import cn.escheduler.common.utils.FileUtils;
import cn.escheduler.common.utils.HadoopUtils;
import cn.escheduler.common.utils.PropertyUtils;
import cn.escheduler.dao.mapper.*;
import cn.escheduler.dao.model.Resource;
import cn.escheduler.dao.model.Tenant;
import cn.escheduler.dao.model.UdfFunc;
import cn.escheduler.dao.model.User;
import org.apache.commons.collections.BeanMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.MessageFormat;
import java.util.*;

import static cn.escheduler.api.enums.Status.UPDATE_RESOURCE_ERROR;
import static cn.escheduler.common.Constants.*;

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
    private ResourcesUserMapper resourcesUserMapper;

    /**
     * create resource
     *
     * @param loginUser
     * @param type
     * @param name
     * @param desc
     * @param file
     * @return
     */
    @Transactional(value = "TransactionManager",rollbackFor = Exception.class)
    public Result createResource(User loginUser,
                                 String name,
                                 String desc,
                                 ResourceType type,
                                 MultipartFile file) {
        Result result = new Result();

        // if hdfs not startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
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
        if (!StringUtils.equals(fileSuffix, nameSuffix)) {
            /**
             * rename file suffix and original suffix must be consistent
             * 重命名的后缀必须与原文件后缀一致
             */
            logger.error("rename file suffix and original suffix must be consistent: {}", file.getOriginalFilename());
            putMsg(result, Status.RESOURCE_SUFFIX_FORBID_CHANGE);
            return result;
        }
        //
        //If resource type is UDF, only jar packages are allowed to be uploaded, and the suffix must be .jar
        if (Constants.UDF.equals(type.name())) {
            if (!JAR.equalsIgnoreCase(fileSuffix)) {
                logger.error(Status.UDF_RESOURCE_SUFFIX_NOT_JAR.getMsg());
                putMsg(result, Status.UDF_RESOURCE_SUFFIX_NOT_JAR);
                return result;
            }
        }
        if (file.getSize() > Constants.maxFileSize) {
            logger.error("file size is too large: {}", file.getOriginalFilename());
            putMsg(result, Status.RESOURCE_SIZE_EXCEED_LIMIT);
            return result;
        }

        // check resoure name exists
        Resource resource = resourcesMapper.queryResourceByNameAndType(name, type.ordinal());
        if (resource != null) {
            logger.error("resource {} has exist, can't recreate", name);
            putMsg(result, Status.RESOURCE_EXIST);
            return result;
        }

        Date now = new Date();

        resource = new Resource(name,file.getOriginalFilename(),desc,loginUser.getId(),type,file.getSize(),now,now);

        try {
            resourcesMapper.insert(resource);

            putMsg(result, Status.SUCCESS);
            Map dataMap = new BeanMap(resource);
            Map<String, Object> resultMap = new HashMap<String, Object>();
            for (Object key : dataMap.keySet()) {
                if (!"class".equalsIgnoreCase(key.toString())) {
                    resultMap.put(key.toString(), dataMap.get(key));
                }
            }
            result.setData(resultMap);
        } catch (Exception e) {
            logger.error("resource already exists, can't recreate ", e);
            putMsg(result, Status.CREATE_RESOURCE_ERROR);
            return result;
        }

        // fail upload
        if (!upload(loginUser, name, file, type)) {
            logger.error("upload resource: {} file: {} failed.", name, file.getOriginalFilename());
            putMsg(result, Status.HDFS_OPERATION_ERROR);
            throw new RuntimeException(String.format("upload resource: %s file: %s failed.", name, file.getOriginalFilename()));
        }
        return result;
    }



    /**
     * update resource
     *
     * @param loginUser
     * @param type
     * @param name
     * @param desc
     * @return
     */
    @Transactional(value = "TransactionManager",rollbackFor = Exception.class)
    public Result updateResource(User loginUser,
                                 int resourceId,
                                 String name,
                                 String desc,
                                 ResourceType type) {
        Result result = new Result();

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        Resource resource = resourcesMapper.queryResourceById(resourceId);
        String originResourceName = resource.getAlias();
        if (resource == null) {
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        if (loginUser.getId() != resource.getUserId()) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }


        if (name.equals(resource.getAlias()) && desc.equals(resource.getDesc())) {
            putMsg(result, Status.SUCCESS);
            return result;
        }

        //check resource aleady exists
        if (!resource.getAlias().equals(name)) {
            Resource needUpdateResource = resourcesMapper.queryResourceByNameAndType(name, type.ordinal());
            if (needUpdateResource != null) {
                logger.error("resource {} already exists, can't recreate", name);
                putMsg(result, Status.RESOURCE_EXIST);
                return result;
            }
        }

        // updateProcessInstance data
        Date now = new Date();
        resource.setAlias(name);
        resource.setDesc(desc);
        resource.setUpdateTime(now);

        try {
            resourcesMapper.update(resource);

            putMsg(result, Status.SUCCESS);
            Map dataMap = new BeanMap(resource);
            Map<String, Object> resultMap = new HashMap<>(5);
            for (Object key : dataMap.keySet()) {
                if (!Constants.CLASS.equalsIgnoreCase(key.toString())) {
                    resultMap.put(key.toString(), dataMap.get(key));
                }
            }
            result.setData(resultMap);
        } catch (Exception e) {
            logger.error(UPDATE_RESOURCE_ERROR.getMsg(), e);
            putMsg(result, Status.UPDATE_RESOURCE_ERROR);
            return result;
        }
        // if name unchanged, return directly without moving on HDFS
        if (originResourceName.equals(name)) {
            return result;
        }

        // hdfs move
        // query tenant by user id
        User user = userMapper.queryDetailsById(resource.getUserId());
        String tenantCode = tenantMapper.queryById(user.getTenantId()).getTenantCode();
        // get file hdfs path
        // delete hdfs file by type
        String originHdfsFileName = "";
        String destHdfsFileName = "";
        if (resource.getType().equals(ResourceType.FILE)) {
            originHdfsFileName = HadoopUtils.getHdfsFilename(tenantCode, originResourceName);
            destHdfsFileName = HadoopUtils.getHdfsFilename(tenantCode, name);
        } else if (resource.getType().equals(ResourceType.UDF)) {
            originHdfsFileName = HadoopUtils.getHdfsUdfFilename(tenantCode, originResourceName);
            destHdfsFileName = HadoopUtils.getHdfsUdfFilename(tenantCode, name);
        }
        try {
            if (HadoopUtils.getInstance().exists(originHdfsFileName)) {
                logger.info("hdfs copy {} -> {}", originHdfsFileName, destHdfsFileName);
                HadoopUtils.getInstance().copy(originHdfsFileName, destHdfsFileName, true, true);
            } else {
                logger.error("{} not exist", originHdfsFileName);
                putMsg(result,Status.RESOURCE_NOT_EXIST);
            }
        } catch (Exception e) {
            logger.error(MessageFormat.format("hdfs copy {0} -> {1} fail", originHdfsFileName, destHdfsFileName), e);
            putMsg(result,Status.HDFS_COPY_FAIL);
        }

        return result;

    }

    /**
     * query resources list paging
     *
     * @param loginUser
     * @param type
     * @param searchVal
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String, Object> queryResourceListPaging(User loginUser, ResourceType type, String searchVal, Integer pageNo, Integer pageSize) {

        HashMap<String, Object> result = new HashMap<>(5);
        Integer count = 0;
        List<Resource> resourceList = new ArrayList<>();
        PageInfo pageInfo = new PageInfo<Resource>(pageNo, pageSize);
        if (isAdmin(loginUser)) {
            count = resourcesMapper.countAllResourceNumberByType(type.ordinal());
            resourceList = resourcesMapper.queryAllResourceListPaging(type.ordinal(), searchVal,
                    pageInfo.getStart(), pageSize);
        } else {
            count = resourcesMapper.countResourceNumberByType(loginUser.getId(), type.ordinal());
            resourceList = resourcesMapper.queryResourceAuthoredPaging(loginUser.getId(), type.ordinal(), searchVal,
                    pageInfo.getStart(), pageSize);
        }

        pageInfo.setTotalCount(count);
        pageInfo.setLists(resourceList);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result,Status.SUCCESS);
        return result;
    }

    /**
     * upload file to hdfs
     *
     * @param loginUser
     * @param name
     * @param file
     */
    private boolean upload(User loginUser, String name, MultipartFile file, ResourceType type) {
        // save to local
        String fileSuffix = FileUtils.suffix(file.getOriginalFilename());
        String nameSuffix = FileUtils.suffix(name);

        // determine file suffix
        if (!StringUtils.equals(fileSuffix, nameSuffix)) {
            return false;
        }
        // query tenant
        String tenantCode = tenantMapper.queryById(loginUser.getTenantId()).getTenantCode();
        // random file name
        String localFilename = FileUtils.getUploadFilename(tenantCode, UUID.randomUUID().toString());


        // save file to hdfs, and delete original file
        String hdfsFilename = "";
        String resourcePath = "";
        if (type.equals(ResourceType.FILE)) {
            hdfsFilename = HadoopUtils.getHdfsFilename(tenantCode, name);
            resourcePath = HadoopUtils.getHdfsResDir(tenantCode);
        } else if (type.equals(ResourceType.UDF)) {
            hdfsFilename = HadoopUtils.getHdfsUdfFilename(tenantCode, name);
            resourcePath = HadoopUtils.getHdfsUdfDir(tenantCode);
        }
        try {
            // if tenant dir not exists
            if (!HadoopUtils.getInstance().exists(resourcePath)) {
                createTenantDirIfNotExists(tenantCode);
            }
            cn.escheduler.api.utils.FileUtils.copyFile(file, localFilename);
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
     * @param loginUser
     * @param type
     * @return
     */
    public Map<String, Object> queryResourceList(User loginUser, ResourceType type) {

        Map<String, Object> result = new HashMap<>(5);
        List<Resource> resourceList;
        if(isAdmin(loginUser)){
            resourceList = resourcesMapper.listAllResourceByType(type.ordinal());
        }else{
            resourceList = resourcesMapper.queryResourceListAuthored(loginUser.getId(), type.ordinal());
        }
        result.put(Constants.DATA_LIST, resourceList);
        putMsg(result,Status.SUCCESS);

        return result;
    }

    /**
     * delete resource
     *
     * @param loginUser
     * @param resourceId
     */
    @Transactional(value = "TransactionManager",rollbackFor = Exception.class)
    public Result delete(User loginUser, int resourceId) throws Exception {
        Result result = new Result();

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        //get resource and  hdfs path
        Resource resource = resourcesMapper.queryResourceById(resourceId);
        if (resource == null) {
            logger.error("resource file not exist,  resource id {}", resourceId);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        if (loginUser.getId() != resource.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        String tenantCode = tenantMapper.queryById(loginUser.getTenantId()).getTenantCode();
        String hdfsFilename = "";

        // delete hdfs file by type
        hdfsFilename = getHdfsFileName(resource, tenantCode, hdfsFilename);

        //delete data in database
        resourcesMapper.delete(resourceId);
        resourcesUserMapper.deleteByResourceId(resourceId);
        //delete file on hdfs
        HadoopUtils.getInstance().delete(hdfsFilename, false);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * verify resource by name and type
     * @param name
     * @param type
     * @param loginUser
     * @return
     */
    public Result verifyResourceName(String name, ResourceType type,User loginUser) {
        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        Resource resource = resourcesMapper.queryResourceByNameAndType(name, type.ordinal());
        if (resource != null) {
            logger.error("resource type:{} name:{} has exist, can't create again.", type, name);
            putMsg(result, Status.RESOURCE_EXIST);
        } else {
            // query tenant
            Tenant tenant = tenantMapper.queryById(loginUser.getTenantId());
            if(tenant != null){
                String tenantCode = tenant.getTenantCode();

                try {
                    String hdfsFilename = getHdfsFileName(type,tenantCode,name);
                    if(HadoopUtils.getInstance().exists(hdfsFilename)){
                        logger.error("resource type:{} name:{} has exist in hdfs {}, can't create again.", type, name,hdfsFilename);
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
     * verify resource by name and type
     *
     * @param name
     * @return
     */
    public Result verifyResourceName(String name, ResourceType type) {
        Result result = new Result();
        Resource resource = resourcesMapper.queryResourceByNameAndType(name, type.ordinal());
        if (resource != null) {
            logger.error("resource type:{} name:{} has exist, can't create again.", type, name);
            putMsg(result, Status.RESOURCE_EXIST);
        } else {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * view resource file online
     *
     * @param resourceId
     * @return
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
        Resource resource = resourcesMapper.queryResourceById(resourceId);
        if (resource == null) {
            logger.error("resouce file not exist,  resource id {}", resourceId);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        //check preview or not by file suffix
        String nameSuffix = FileUtils.suffix(resource.getAlias());
        String resourceViewSuffixs = FileUtils.getResourceViewSuffixs();
        if (StringUtils.isNotEmpty(resourceViewSuffixs)) {
            List<String> strList = Arrays.asList(resourceViewSuffixs.split(","));
            if (!strList.contains(nameSuffix)) {
                logger.error("resouce suffix {} not support view,  resource id {}", nameSuffix, resourceId);
                putMsg(result, Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW);
                return result;
            }
        }

        User user = userMapper.queryDetailsById(resource.getUserId());
        String tenantCode = tenantMapper.queryById(user.getTenantId()).getTenantCode();
        // hdfs path
        String hdfsFileName = HadoopUtils.getHdfsFilename(tenantCode, resource.getAlias());
        logger.info("resource hdfs path is {} ", hdfsFileName);
        try {
            if(HadoopUtils.getInstance().exists(hdfsFileName)){
                List<String> content = HadoopUtils.getInstance().catFile(hdfsFileName, skipLineNum, limit);

                putMsg(result, Status.SUCCESS);
                Map<String, Object> map = new HashMap<>();
                map.put(ALIAS, resource.getAlias());
                map.put(CONTENT, StringUtils.join(content.toArray(), "\n"));
                result.setData(map);
            }else{
                logger.error("read file {} not exist in hdfs", hdfsFileName);
                putMsg(result, Status.RESOURCE_FILE_NOT_EXIST,hdfsFileName);
            }

        } catch (Exception e) {
            logger.error(String.format("Resource %s read failed", hdfsFileName), e);
            putMsg(result, Status.HDFS_OPERATION_ERROR);
        }

        return result;
    }

    /**
     * create resource file online
     *
     * @param loginUser
     * @param type
     * @param fileName
     * @param fileSuffix
     * @param desc
     * @param content
     * @return
     */
    @Transactional(value = "TransactionManager",rollbackFor = Exception.class)
    public Result onlineCreateResource(User loginUser, ResourceType type, String fileName, String fileSuffix, String desc, String content) {
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

        result = verifyResourceName(name,type,loginUser);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            return result;
        }

        // save data
        Date now = new Date();
        Resource resource = new Resource(name,name,desc,loginUser.getId(),type,content.getBytes().length,now,now);

        resourcesMapper.insert(resource);

        putMsg(result, Status.SUCCESS);
        Map dataMap = new BeanMap(resource);
        Map<String, Object> resultMap = new HashMap<>(5);
        for (Object key : dataMap.keySet()) {
            if (!Constants.CLASS.equalsIgnoreCase(key.toString())) {
                resultMap.put(key.toString(), dataMap.get(key));
            }
        }
        result.setData(resultMap);

        String tenantCode = tenantMapper.queryById(loginUser.getTenantId()).getTenantCode();

        result = uploadContentToHdfs(name, tenantCode, content);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            throw new RuntimeException(result.getMsg());
        }
        return result;
    }

    /**
     * updateProcessInstance resource
     *
     * @param resourceId
     * @return
     */
    @Transactional(value = "TransactionManager",rollbackFor = Exception.class)
    public Result updateResourceContent(int resourceId, String content) {
        Result result = new Result();

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        Resource resource = resourcesMapper.queryResourceById(resourceId);
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
                logger.error("resouce suffix {} not support updateProcessInstance,  resource id {}", nameSuffix, resourceId);
                putMsg(result, Status.RESOURCE_SUFFIX_NOT_SUPPORT_VIEW);
                return result;
            }
        }

        resource.setSize(content.getBytes().length);
        resource.setUpdateTime(new Date());
        resourcesMapper.update(resource);

        User user = userMapper.queryDetailsById(resource.getUserId());
        String tenantCode = tenantMapper.queryById(user.getTenantId()).getTenantCode();

        result = uploadContentToHdfs(resource.getAlias(), tenantCode, content);
        if (!result.getCode().equals(Status.SUCCESS.getCode())) {
            throw new RuntimeException(result.getMsg());
        }
        return result;
    }

    /**
     * @param resourceName
     * @param tenantCode
     * @param content
     * @return
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

            // get file hdfs path
            hdfsFileName = HadoopUtils.getHdfsFilename(tenantCode, resourceName);
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
     * @param resourceId
     * @return
     */
    public org.springframework.core.io.Resource downloadResource(int resourceId) throws Exception {
        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            throw new RuntimeException("hdfs not startup");
        }

        Resource resource = resourcesMapper.queryResourceById(resourceId);
        if (resource == null) {
            logger.error("download file not exist,  resource id {}", resourceId);
            return null;
        }
        User user = userMapper.queryDetailsById(resource.getUserId());
        String tenantCode = tenantMapper.queryById(user.getTenantId()).getTenantCode();

        String hdfsFileName = "";
        hdfsFileName = getHdfsFileName(resource, tenantCode, hdfsFileName);

        String localFileName = FileUtils.getDownloadFilename(resource.getAlias());
        logger.info("resource hdfs path is {} ", hdfsFileName);

        HadoopUtils.getInstance().copyHdfsToLocal(hdfsFileName, localFileName, false, true);
        org.springframework.core.io.Resource file = cn.escheduler.api.utils.FileUtils.file2Resource(localFileName);
        return file;
    }


    /**
     * unauthorized file
     *
     * @param loginUser
     * @param userId
     * @return
     */
    public Map<String, Object> unauthorizedFile(User loginUser, Integer userId) {

        Map<String, Object> result = new HashMap<>();
        if (checkAdmin(loginUser, result)) {
            return result;
        }
        List<Resource> resourceList = resourcesMapper.queryResourceExceptUserId(userId);
        List<Object> list ;
        if (resourceList != null && resourceList.size() > 0) {
            Set<Resource> resourceSet = new HashSet<>(resourceList);
            List<Resource> authedResourceList = resourcesMapper.queryAuthorizedResourceList(userId);

            getAuthorizedResourceList(resourceSet, authedResourceList);
            list = new ArrayList<>(resourceSet);
        }else {
            list = new ArrayList<>(0);
        }

        result.put(Constants.DATA_LIST, list);
        putMsg(result,Status.SUCCESS);
        return result;
    }




    /**
     * unauthorized udf function
     *
     * @param loginUser
     * @param userId
     * @return
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
        if (udfFuncList != null && udfFuncList.size() > 0) {
            udfFuncSet = new HashSet<>(udfFuncList);

            List<UdfFunc> authedUDFFuncList = udfFunctionMapper.authedUdfFunc(userId);

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
     * @param loginUser
     * @param userId
     * @return
     */
    public Map<String, Object> authorizedUDFFunction(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        if (checkAdmin(loginUser, result)) {
            return result;
        }
        List<UdfFunc> udfFuncs = udfFunctionMapper.authedUdfFunc(userId);
        result.put(Constants.DATA_LIST, udfFuncs);
        putMsg(result,Status.SUCCESS);
        return result;
    }


    /**
     * authorized file
     *
     * @param loginUser
     * @param userId
     * @return
     */
    public Map<String, Object> authorizedFile(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>(5);
        if (checkAdmin(loginUser, result)){
            return result;
        }
        List<Resource> authedResources = resourcesMapper.queryAuthorizedResourceList(userId);

        result.put(Constants.DATA_LIST, authedResources);
        putMsg(result,Status.SUCCESS);
        return result;
    }

    /**
     * get hdfs file name
     *
     * @param resource
     * @param tenantCode
     * @param hdfsFileName
     * @return
     */
    private String getHdfsFileName(Resource resource, String tenantCode, String hdfsFileName) {
        if (resource.getType().equals(ResourceType.FILE)) {
            hdfsFileName = HadoopUtils.getHdfsFilename(tenantCode, resource.getAlias());
        } else if (resource.getType().equals(ResourceType.UDF)) {
            hdfsFileName = HadoopUtils.getHdfsUdfFilename(tenantCode, resource.getAlias());
        }
        return hdfsFileName;
    }

    /**
     * get hdfs file name
     *
     * @param resourceType
     * @param tenantCode
     * @param hdfsFileName
     * @return
     */
    private String getHdfsFileName(ResourceType resourceType, String tenantCode, String hdfsFileName) {
        if (resourceType.equals(ResourceType.FILE)) {
            hdfsFileName = HadoopUtils.getHdfsFilename(tenantCode, hdfsFileName);
        } else if (resourceType.equals(ResourceType.UDF)) {
            hdfsFileName = HadoopUtils.getHdfsUdfFilename(tenantCode, hdfsFileName);
        }
        return hdfsFileName;
    }

    /**
     * get authorized resource list
     *
     * @param resourceSet
     * @param authedResourceList
     */
    private void getAuthorizedResourceList(Set<?> resourceSet, List<?> authedResourceList) {
        Set<?> authedResourceSet = null;
        if (authedResourceList != null && authedResourceList.size() > 0) {
            authedResourceSet = new HashSet<>(authedResourceList);
            resourceSet.removeAll(authedResourceSet);

        }
    }

}