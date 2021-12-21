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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.enums.TransferDataType;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.UdfFuncService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.ResourcesUser;
import org.apache.dolphinscheduler.dao.entity.UDFUser;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UDFUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * udf func service impl
 */
@Service
public class UdfFuncServiceImpl extends BaseServiceImpl implements UdfFuncService {

    private static final Logger logger = LoggerFactory.getLogger(UdfFuncServiceImpl.class);

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private ResourceUserMapper resourceUserMapper;

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    @Autowired
    private UDFUserMapper udfUserMapper;

    /**
     * create udf function
     *
     * @param loginUser login user
     * @param type udf type
     * @param funcName function name
     * @param argTypes argument types
     * @param database database
     * @param desc description
     * @param resourceId resource id
     * @param className class name
     * @return create result code
     */
    @Override
    public Result<Object> createUdfFunction(User loginUser,
                                            String funcName,
                                            String className,
                                            String argTypes,
                                            String database,
                                            String desc,
                                            UdfType type,
                                            int resourceId) {
        Result<Object> result = new Result<>();

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()) {
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        // verify udf func name exist
        if (checkUdfFuncNameExists(funcName)) {
            putMsg(result, Status.UDF_FUNCTION_EXISTS);
            return result;
        }

        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            logger.error("resourceId {} is not exist", resourceId);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }

        //save data
        UdfFunc udf = new UdfFunc();
        Date now = new Date();
        udf.setUserId(loginUser.getId());
        udf.setFuncName(funcName);
        udf.setClassName(className);
        if (!StringUtils.isEmpty(argTypes)) {
            udf.setArgTypes(argTypes);
        }
        if (!StringUtils.isEmpty(database)) {
            udf.setDatabase(database);
        }
        udf.setDescription(desc);
        udf.setResourceId(resourceId);
        udf.setResourceName(resource.getFullName());
        udf.setType(type);

        udf.setCreateTime(now);
        udf.setUpdateTime(now);

        udfFuncMapper.insert(udf);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     *
     * @param name name
     * @return check result code
     */
    private boolean checkUdfFuncNameExists(String name) {
        List<UdfFunc> resource = udfFuncMapper.queryUdfByIdStr(null, name);
        return resource != null && !resource.isEmpty();
    }

    /**
     * query udf function
     *
     * @param id  udf function id
     * @return udf function detail
     */
    @Override
    public Map<String, Object> queryUdfFuncDetail(int id) {
        Map<String, Object> result = new HashMap<>();
        UdfFunc udfFunc = udfFuncMapper.selectById(id);
        if (udfFunc == null) {
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        result.put(Constants.DATA_LIST, udfFunc);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * updateProcessInstance udf function
     *
     * @param udfFuncId udf function id
     * @param type  resource type
     * @param funcName function name
     * @param argTypes argument types
     * @param database data base
     * @param desc description
     * @param resourceId resource id
     * @param className class name
     * @return update result code
     */
    @Override
    public Map<String, Object> updateUdfFunc(int udfFuncId,
                                             String funcName,
                                             String className,
                                             String argTypes,
                                             String database,
                                             String desc,
                                             UdfType type,
                                             int resourceId) {
        Map<String, Object> result = new HashMap<>();
        // verify udfFunc is exist
        UdfFunc udf = udfFuncMapper.selectUdfById(udfFuncId);

        if (udf == null) {
            result.put(Constants.STATUS, Status.UDF_FUNCTION_NOT_EXIST);
            result.put(Constants.MSG, Status.UDF_FUNCTION_NOT_EXIST.getMsg());
            return result;
        }

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()) {
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        // verify udfFuncName is exist
        if (!funcName.equals(udf.getFuncName())) {
            if (checkUdfFuncNameExists(funcName)) {
                logger.error("UdfFuncRequest {} has exist, can't create again.", funcName);
                result.put(Constants.STATUS, Status.UDF_FUNCTION_EXISTS);
                result.put(Constants.MSG, Status.UDF_FUNCTION_EXISTS.getMsg());
                return result;
            }
        }

        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            logger.error("resourceId {} is not exist", resourceId);
            result.put(Constants.STATUS, Status.RESOURCE_NOT_EXIST);
            result.put(Constants.MSG, Status.RESOURCE_NOT_EXIST.getMsg());
            return result;
        }
        Date now = new Date();
        udf.setFuncName(funcName);
        udf.setClassName(className);
        udf.setArgTypes(argTypes);
        if (!StringUtils.isEmpty(database)) {
            udf.setDatabase(database);
        }
        udf.setDescription(desc);
        udf.setResourceId(resourceId);
        udf.setResourceName(resource.getFullName());
        udf.setType(type);

        udf.setUpdateTime(now);

        udfFuncMapper.updateById(udf);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query udf function list paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param pageSize page size
     * @param searchVal search value
     * @return udf function list page
     */
    @Override
    public Result queryUdfFuncListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result result = new Result();
        PageInfo<UdfFunc> pageInfo = new PageInfo<>(pageNo, pageSize);
        IPage<UdfFunc> udfFuncList = getUdfFuncsPage(loginUser, searchVal, pageSize, pageNo);
        pageInfo.setTotal((int)udfFuncList.getTotal());
        pageInfo.setTotalList(udfFuncList.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * get udf functions
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize page size
     * @param pageNo page number
     * @return udf function list page
     */
    private IPage<UdfFunc> getUdfFuncsPage(User loginUser, String searchVal, Integer pageSize, int pageNo) {
        int userId = loginUser.getId();
        if (isAdmin(loginUser)) {
            userId = 0;
        }
        Page<UdfFunc> page = new Page<>(pageNo, pageSize);
        return udfFuncMapper.queryUdfFuncPaging(page, userId, searchVal);
    }

    /**
     * query udf list
     *
     * @param loginUser login user
     * @param type  udf type
     * @return udf func list
     */
    @Override
    public Map<String, Object> queryUdfFuncList(User loginUser, Integer type) {
        Map<String, Object> result = new HashMap<>();
        int userId = loginUser.getId();
        if (isAdmin(loginUser)) {
            userId = 0;
        }
        List<UdfFunc> udfFuncList = udfFuncMapper.getUdfFuncByType(userId, type);

        result.put(Constants.DATA_LIST, udfFuncList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete udf function
     *
     * @param id udf function id
     * @return delete result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> delete(int id) {
        Result<Object> result = new Result<>();
        udfFuncMapper.deleteById(id);
        udfUserMapper.deleteByUdfFuncId(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * verify udf function by name
     *
     * @param name name
     * @return true if the name can user, otherwise return false
     */
    @Override
    public Result<Object> verifyUdfFuncByName(String name) {
        Result<Object> result = new Result<>();
        if (checkUdfFuncNameExists(name)) {
            putMsg(result, Status.UDF_FUNCTION_EXISTS);
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * query all udf function list created by user
     *
     * @param userId user id
     * @return udf function list
     */
    @Override
    public List<UdfFunc> queryCreatedByUser(int userId) {
        return udfFuncMapper.queryUdfFuncCreatedByUser(userId);
    }

    /**
     * transfer udf function list owned by the user
     *
     * @param transferredUserId transferred user id
     * @param receivedUserId received user id
     * @param transferredIds transferred ids
     * @return transfer result code
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> transferOwnedData(int transferredUserId, int receivedUserId, List<Integer> transferredIds) {
        Map<String, Object> result = new HashMap<>();

        List<UdfFunc> udfFuncList = udfFuncMapper.selectList(Wrappers.<UdfFunc>lambdaQuery()
                .eq(UdfFunc::getUserId, transferredUserId)
                .in(UdfFunc::getId, transferredIds)
        );
        Set<Integer> udfResourceIds = udfFuncList.stream().map(UdfFunc::getResourceId).collect(Collectors.toSet());
        // check received user is visible to the udf resources or not
        Set<Integer> visibleResourceIds = Stream.concat(
                resourceMapper.listAuthorizedResourceById(
                        receivedUserId, udfResourceIds.toArray(new Integer[0])
                ).stream().map(Resource::getId),
                resourceUserMapper.selectList(
                        Wrappers.<ResourcesUser>lambdaQuery()
                                .eq(ResourcesUser::getUserId, receivedUserId)
                                .in(ResourcesUser::getResourcesId, udfResourceIds)
                ).stream().map(ResourcesUser::getResourcesId)
        ).collect(Collectors.toSet());
        if (!CollectionUtils.isEqualCollection(udfResourceIds, visibleResourceIds)) {
            throw new ServiceException(Status.UDF_RESOURCE_IS_BOUND, udfFuncList.stream().collect(Collectors.toMap(UdfFunc::getFuncName, UdfFunc::getResourceName)));
        }

        Set<Integer> realUdfFuncIds = udfFuncList.stream().map(UdfFunc::getId).collect(Collectors.toSet());
        // update udf function owner
        int updatedUdfFuncNum = udfFuncMapper.update(null, Wrappers.<UdfFunc>lambdaUpdate()
                .set(UdfFunc::getUserId, receivedUserId)
                .set(UdfFunc::getUpdateTime, new Date())
                .in(UdfFunc::getId, realUdfFuncIds)
        );
        if (updatedUdfFuncNum != realUdfFuncIds.size()) {
            putMsg(result, Status.TRANSFER_UDF_FUNCTION_ERROR);
            return result;
        }
        // delete project user relation if exist
        udfUserMapper.delete(Wrappers.<UDFUser>lambdaQuery()
                .eq(UDFUser::getUserId, receivedUserId)
                .in(UDFUser::getUdfId, realUdfFuncIds)
        );

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * return the udf function type
     *
     * @return transfer data type
     */
    @Override
    public TransferDataType transferDataType() {
        return TransferDataType.UDF_FUNCTION;
    }

}
