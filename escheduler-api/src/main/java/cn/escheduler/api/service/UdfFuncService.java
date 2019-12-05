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
import cn.escheduler.common.enums.UdfType;
import cn.escheduler.common.utils.PropertyUtils;
import cn.escheduler.dao.mapper.ResourceMapper;
import cn.escheduler.dao.mapper.UDFUserMapper;
import cn.escheduler.dao.mapper.UdfFuncMapper;
import cn.escheduler.dao.model.Resource;
import cn.escheduler.dao.model.UdfFunc;
import cn.escheduler.dao.model.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * udf function service
 */
@Service
public class UdfFuncService extends BaseService{

    private static final Logger logger = LoggerFactory.getLogger(UdfFuncService.class);

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    @Autowired
    private UDFUserMapper udfUserMapper;


    /**
     * create udf function
     *
     * @param loginUser
     * @param funcName
     * @param argTypes
     * @param database
     * @param desc
     * @param type
     * @param resourceId
     * @return
     */
    public Result createUdfFunction(User loginUser,
                                    String funcName,
                                    String className,
                                    String argTypes,
                                    String database,
                                    String desc,
                                    UdfType type,
                                    int resourceId) {
        Result result = new Result();

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        // verify udf func name exist
        UdfFunc udfFunc = udfFuncMapper.queryUdfFuncByName(funcName);
        if (udfFunc != null) {
            logger.error("udf func {} has exist, can't recreate", funcName);
            putMsg(result, Status.UDF_FUNCTION_EXISTS);
            return result;
        }

        Resource resource = resourceMapper.queryResourceById(resourceId);
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
        if (StringUtils.isNotEmpty(argTypes)) {
            udf.setArgTypes(argTypes);
        }
        if (StringUtils.isNotEmpty(argTypes)) {
            udf.setDatabase(database);
        }
        udf.setDesc(desc);
        udf.setResourceId(resourceId);
        udf.setResourceName(resource.getAlias());
        udf.setType(type);

        udf.setCreateTime(now);
        udf.setUpdateTime(now);

        udfFuncMapper.insert(udf);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * query udf function
     */
    public Map<String, Object> queryUdfFuncDetail(int id) {

        Map<String, Object> result = new HashMap<>(5);
        UdfFunc udfFunc = udfFuncMapper.queryUdfById(id);
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
     * @param funcName
     * @param argTypes
     * @param database
     * @param desc
     * @param type
     * @param resourceId
     * @return
     */
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
        UdfFunc udf = udfFuncMapper.queryUdfById(udfFuncId);

        // if resource upload startup
        if (!PropertyUtils.getResUploadStartupState()){
            logger.error("resource upload startup state: {}", PropertyUtils.getResUploadStartupState());
            putMsg(result, Status.HDFS_NOT_STARTUP);
            return result;
        }

        if (udf == null) {
            result.put(Constants.STATUS, Status.UDF_FUNCTION_NOT_EXIST);
            result.put(Constants.MSG, Status.UDF_FUNCTION_NOT_EXIST.getMsg());
            return result;
        }

        // verify udfFuncName is exist
        if (!funcName.equals(udf.getFuncName())) {
            UdfFunc udfFunc = udfFuncMapper.queryUdfFuncByName(funcName);
            if (udfFunc != null) {
                logger.error("UdfFunc {} has exist, can't create again.", funcName);
                result.put(Constants.STATUS, Status.UDF_FUNCTION_EXISTS);
                result.put(Constants.MSG, Status.UDF_FUNCTION_EXISTS.getMsg());
                return result;
            }
        }

        Resource resource = resourceMapper.queryResourceById(resourceId);
        if (resource == null) {
            logger.error("resourceId {} is not exist", resourceId);
            result.put(Constants.STATUS, Status.RESOURCE_NOT_EXIST);
            result.put(Constants.MSG, Status.RESOURCE_NOT_EXIST.getMsg());
            return result;
        }
        Date now = new Date();
        udf.setFuncName(funcName);
        udf.setClassName(className);
        if (StringUtils.isNotEmpty(argTypes)) {
            udf.setArgTypes(argTypes);
        }
        if (StringUtils.isNotEmpty(argTypes)) {
            udf.setDatabase(database);
        }
        udf.setDesc(desc);
        udf.setResourceId(resourceId);
        udf.setResourceName(resource.getAlias());
        udf.setType(type);


        udf.setCreateTime(now);
        udf.setUpdateTime(now);

        udfFuncMapper.update(udf);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * query udf function list paging
     *
     * @param loginUser
     * @param searchVal
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String, Object> queryUdfFuncListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>(5);

        Integer count = getTotalCount(loginUser);

        PageInfo pageInfo = new PageInfo<Resource>(pageNo, pageSize);
        pageInfo.setTotalCount(count);
        List<UdfFunc> udfFuncList = getUdfFuncs(loginUser, searchVal, pageSize, pageInfo);

        pageInfo.setLists(udfFuncList);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * get udf functions
     *
     * @param loginUser
     * @param searchVal
     * @param pageSize
     * @param pageInfo
     * @return
     */
    private List<UdfFunc> getUdfFuncs(User loginUser, String searchVal, Integer pageSize, PageInfo pageInfo) {
        if (isAdmin(loginUser)) {
            return udfFuncMapper.queryAllUdfFuncPaging(searchVal, pageInfo.getStart(), pageSize);
        }
        return udfFuncMapper.queryUdfFuncPaging(loginUser.getId(), searchVal,
                pageInfo.getStart(), pageSize);
    }

    /**
     * udf function total
     *
     * @param loginUser
     * @return
     */
    private Integer getTotalCount(User loginUser) {
        if (isAdmin(loginUser)) {
            return udfFuncMapper.countAllUdfFunc();
        }
        return udfFuncMapper.countUserUdfFunc(loginUser.getId());
    }

    /**
     * query data resource by type
     *
     * @param loginUser
     * @param type
     * @return
     */
    public Map<String, Object> queryResourceList(User loginUser, Integer type) {
        Map<String, Object> result = new HashMap<>(5);
        List<UdfFunc> udfFuncList = udfFuncMapper.getUdfFuncByType(loginUser.getId(), type);

        result.put(Constants.DATA_LIST, udfFuncList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete udf function
     *
     * @param id
     */
    @Transactional(value = "TransactionManager", rollbackFor = Exception.class)
    public Result delete(int id) {
        Result result = new Result();

        udfFuncMapper.delete(id);
        udfUserMapper.deleteByUdfFuncId(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * verify udf function by name
     *
     * @param name
     * @return
     */
    public Result verifyUdfFuncByName(String name) {
        Result result = new Result();
        UdfFunc udfFunc = udfFuncMapper.queryUdfFuncByName(name);
        if (udfFunc != null) {
            logger.error("UDF function name:{} has exist, can't create again.", name);
            putMsg(result, Status.UDF_FUNCTION_EXISTS);
        } else {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

}