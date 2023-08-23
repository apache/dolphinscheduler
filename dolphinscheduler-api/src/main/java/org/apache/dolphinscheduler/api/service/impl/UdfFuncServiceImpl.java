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

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.UdfFuncService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.UDFUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * udf func service impl
 */
@Service
@Slf4j
public class UdfFuncServiceImpl extends BaseServiceImpl implements UdfFuncService {

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    @Autowired
    private UDFUserMapper udfUserMapper;

    @Autowired(required = false)
    private StorageOperate storageOperate;

    /**
     * create udf function
     *
     * @param loginUser login user
     * @param type udf type
     * @param funcName function name
     * @param argTypes argument types
     * @param database database
     * @param desc description
     * @param className class name
     * @return create result code
     */
    @Override
    @Transactional
    public Result<Object> createUdfFunction(User loginUser,
                                            String funcName,
                                            String className,
                                            String fullName,
                                            String argTypes,
                                            String database,
                                            String desc,
                                            UdfType type) {
        Result<Object> result = new Result<>();

        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.UDF,
                ApiFuncIdentificationConstant.UDF_FUNCTION_CREATE);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        if (checkDescriptionLength(desc)) {
            log.warn("Parameter description is too long.");
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        // if resource upload startup
        if (!PropertyUtils.isResourceStorageStartup()) {
            log.error("Storage does not start up, resource upload startup state: {}.",
                    PropertyUtils.isResourceStorageStartup());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        // verify udf func name exist
        if (checkUdfFuncNameExists(funcName)) {
            log.warn("Udf function with the same name already exists.");
            putMsg(result, Status.UDF_FUNCTION_EXISTS);
            return result;
        }

        Boolean existResource = false;
        try {
            existResource = storageOperate.exists(fullName);
        } catch (IOException e) {
            log.error("Check resource error: {}", fullName, e);
        }

        if (!existResource) {
            log.error("resource full name {} is not exist", fullName);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }

        // save data
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
        // set resourceId to -1 because we do not store resource to db anymore, instead we use fullName
        udf.setResourceId(-1);
        udf.setResourceName(fullName);
        udf.setType(type);

        udf.setCreateTime(now);
        udf.setUpdateTime(now);

        udfFuncMapper.insert(udf);
        log.info("UDF function create complete, udfFuncName:{}.", udf.getFuncName());
        putMsg(result, Status.SUCCESS);
        permissionPostHandle(AuthorizationType.UDF, loginUser.getId(), Collections.singletonList(udf.getId()), log);
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
    public Result<Object> queryUdfFuncDetail(User loginUser, int id) {
        Result<Object> result = new Result<>();
        boolean canOperatorPermissions = canOperatorPermissions(loginUser, new Object[]{id}, AuthorizationType.UDF,
                ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        UdfFunc udfFunc = udfFuncMapper.selectById(id);
        if (udfFunc == null) {
            log.error("Resource does not exist, udf func id:{}.", id);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        result.setData(udfFunc);
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
     * @param fullName resource full name
     * @param className class name
     * @return update result code
     */
    @Override
    public Result<Object> updateUdfFunc(User loginUser,
                                        int udfFuncId,
                                        String funcName,
                                        String className,
                                        String argTypes,
                                        String database,
                                        String desc,
                                        UdfType type,
                                        String fullName) {
        Result<Object> result = new Result<>();

        boolean canOperatorPermissions = canOperatorPermissions(loginUser, new Object[]{udfFuncId},
                AuthorizationType.UDF, ApiFuncIdentificationConstant.UDF_FUNCTION_UPDATE);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        if (checkDescriptionLength(desc)) {
            log.warn("Parameter description is too long.");
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        // verify udfFunc is exist
        UdfFunc udf = udfFuncMapper.selectUdfById(udfFuncId);

        if (udf == null) {
            log.error("UDF function does not exist, udfFuncId:{}.", udfFuncId);
            result.setCode(Status.UDF_FUNCTION_NOT_EXIST.getCode());
            result.setMsg(Status.UDF_FUNCTION_NOT_EXIST.getMsg());
            return result;
        }

        // if resource upload startup
        if (!PropertyUtils.isResourceStorageStartup()) {
            log.error("Storage does not start up, resource upload startup state: {}.",
                    PropertyUtils.isResourceStorageStartup());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        // verify udfFuncName is exist
        if (!funcName.equals(udf.getFuncName())) {
            if (checkUdfFuncNameExists(funcName)) {
                log.warn("Udf function exists, can not create again, udfFuncName:{}.", funcName);
                result.setCode(Status.UDF_FUNCTION_EXISTS.getCode());
                result.setMsg(Status.UDF_FUNCTION_EXISTS.getMsg());
                return result;
            }
        }

        Boolean doesResExist = false;
        try {
            doesResExist = storageOperate.exists(fullName);
        } catch (Exception e) {
            log.error("udf resource checking error", fullName);
            result.setCode(Status.RESOURCE_NOT_EXIST.getCode());
            result.setMsg(Status.RESOURCE_NOT_EXIST.getMsg());
            return result;
        }

        if (!doesResExist) {
            log.error("resource full name {} is not exist", fullName);
            result.setCode(Status.RESOURCE_NOT_EXIST.getCode());
            result.setMsg(Status.RESOURCE_NOT_EXIST.getMsg());
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
        // set resourceId to -1 because we do not store resource to db anymore, instead we use fullName
        udf.setResourceId(-1);
        udf.setResourceName(fullName);
        udf.setType(type);

        udf.setUpdateTime(now);

        udfFuncMapper.updateById(udf);
        log.info("UDF function update complete, udfFuncId:{}, udfFuncName:{}.", udfFuncId, funcName);
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
    public Result<Object> queryUdfFuncListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result<Object> result = new Result();
        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.UDF,
                ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        PageInfo<UdfFunc> pageInfo = new PageInfo<>(pageNo, pageSize);
        IPage<UdfFunc> udfFuncList = getUdfFuncsPage(loginUser, searchVal, pageSize, pageNo);
        pageInfo.setTotal((int) udfFuncList.getTotal());
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
        Set<Integer> udfFuncIds = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.UDF,
                loginUser.getId(), log);
        Page<UdfFunc> page = new Page<>(pageNo, pageSize);
        if (udfFuncIds.isEmpty()) {
            return page;
        }
        return udfFuncMapper.queryUdfFuncPaging(page, new ArrayList<>(udfFuncIds), searchVal);
    }

    /**
     * query udf list
     *
     * @param loginUser login user
     * @param type  udf type
     * @return udf func list
     */
    @Override
    public Result<Object> queryUdfFuncList(User loginUser, Integer type) {
        Result<Object> result = new Result<>();

        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.UDF,
                ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        Set<Integer> udfFuncIds = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.UDF,
                loginUser.getId(), log);
        if (udfFuncIds.isEmpty()) {
            result.setData(Collections.emptyList());
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<UdfFunc> udfFuncList = udfFuncMapper.getUdfFuncByType(new ArrayList<>(udfFuncIds), type);

        result.setData(udfFuncList);
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
    @Transactional
    public Result<Object> delete(User loginUser, int id) {
        Result<Object> result = new Result<>();

        boolean canOperatorPermissions = canOperatorPermissions(loginUser, new Object[]{id}, AuthorizationType.UDF,
                ApiFuncIdentificationConstant.UDF_FUNCTION_DELETE);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        udfFuncMapper.deleteById(id);
        udfUserMapper.deleteByUdfFuncId(id);
        log.info("UDF function delete complete, udfFuncId:{}.", id);
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
    public Result<Object> verifyUdfFuncByName(User loginUser, String name) {
        Result<Object> result = new Result<>();
        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.UDF,
                ApiFuncIdentificationConstant.UDF_FUNCTION_VIEW);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }

        if (checkUdfFuncNameExists(name)) {
            log.warn("Udf function with the same already exists.");
            putMsg(result, Status.UDF_FUNCTION_EXISTS);
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }
}
