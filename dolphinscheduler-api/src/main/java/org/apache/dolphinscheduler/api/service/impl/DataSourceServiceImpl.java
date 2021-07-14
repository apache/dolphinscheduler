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
import org.apache.dolphinscheduler.api.service.DataSourceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.ConnectionParam;
import org.apache.dolphinscheduler.common.datasource.DatasourceUtil;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * data source service impl
 */
@Service
public class DataSourceServiceImpl extends BaseServiceImpl implements DataSourceService {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceServiceImpl.class);

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private DataSourceUserMapper datasourceUserMapper;

    /**
     * create data source
     *
     * @param loginUser login user
     * @param datasourceParam datasource parameters
     * @return create result code
     */
    @Override
    public Result<Object> createDataSource(User loginUser, BaseDataSourceParamDTO datasourceParam) {
        DatasourceUtil.checkDatasourceParam(datasourceParam);
        Result<Object> result = new Result<>();
        // check name can use or not
        if (checkName(datasourceParam.getName())) {
            putMsg(result, Status.DATASOURCE_EXIST);
            return result;
        }
        // check connect
        ConnectionParam connectionParam = DatasourceUtil.buildConnectionParams(datasourceParam);
        Result<Object> isConnection = checkConnection(datasourceParam.getType(), connectionParam);
        if (Status.SUCCESS.getCode() != isConnection.getCode()) {
            putMsg(result, Status.DATASOURCE_CONNECT_FAILED);
            return result;
        }

        // build datasource
        DataSource dataSource = new DataSource();
        Date now = new Date();

        dataSource.setName(datasourceParam.getName().trim());
        dataSource.setNote(datasourceParam.getNote());
        dataSource.setUserId(loginUser.getId());
        dataSource.setUserName(loginUser.getUserName());
        dataSource.setType(datasourceParam.getType());
        dataSource.setConnectionParams(JSONUtils.toJsonString(connectionParam));
        dataSource.setCreateTime(now);
        dataSource.setUpdateTime(now);
        try {
            dataSourceMapper.insert(dataSource);
            putMsg(result, Status.SUCCESS);
        } catch (DuplicateKeyException ex) {
            logger.error("Create datasource error.", ex);
            putMsg(result, Status.DATASOURCE_EXIST);
        }

        return result;
    }

    /**
     * updateProcessInstance datasource
     *
     * @param loginUser login user
     * @param id data source id
     * @return update result code
     */
    @Override
    public Result<Object> updateDataSource(int id, User loginUser, BaseDataSourceParamDTO dataSourceParam) {
        DatasourceUtil.checkDatasourceParam(dataSourceParam);
        Result<Object> result = new Result<>();
        // determine whether the data source exists
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }

        if (!hasPerm(loginUser, dataSource.getUserId())) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        //check name can use or not
        if (!dataSource.getName().trim().equals(dataSource.getName()) && checkName(dataSource.getName())) {
            putMsg(result, Status.DATASOURCE_EXIST);
            return result;
        }
        //check password，if the password is not updated, set to the old password.
        BaseConnectionParam connectionParam = (BaseConnectionParam) DatasourceUtil.buildConnectionParams(dataSourceParam);
        String password = connectionParam.getPassword();
        if (StringUtils.isBlank(password)) {
            String oldConnectionParams = dataSource.getConnectionParams();
            ObjectNode oldParams = JSONUtils.parseObject(oldConnectionParams);
            connectionParam.setPassword(oldParams.path(Constants.PASSWORD).asText());
        }

        Result<Object> isConnection = checkConnection(dataSource.getType(), connectionParam);
        if (isConnection.isFailed()) {
            return isConnection;
        }

        Date now = new Date();

        dataSource.setName(dataSourceParam.getName().trim());
        dataSource.setNote(dataSourceParam.getNote());
        dataSource.setUserName(loginUser.getUserName());
        dataSource.setType(dataSource.getType());
        dataSource.setConnectionParams(JSONUtils.toJsonString(connectionParam));
        dataSource.setUpdateTime(now);
        try {
            dataSourceMapper.updateById(dataSource);
            putMsg(result, Status.SUCCESS);
        } catch (DuplicateKeyException ex) {
            logger.error("Update datasource error.", ex);
            putMsg(result, Status.DATASOURCE_EXIST);
        }
        return result;
    }

    private boolean checkName(String name) {
        List<DataSource> queryDataSource = dataSourceMapper.queryDataSourceByName(name.trim());
        return queryDataSource != null && !queryDataSource.isEmpty();
    }

    /**
     * updateProcessInstance datasource
     *
     * @param id datasource id
     * @return data source detail
     */
    @Override
    public Map<String, Object> queryDataSource(int id) {

        Map<String, Object> result = new HashMap<>();
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        // type
        BaseDataSourceParamDTO baseDataSourceParamDTO = DatasourceUtil.buildDatasourceParamDTO(
                dataSource.getType(), dataSource.getConnectionParams());
        baseDataSourceParamDTO.setId(dataSource.getId());
        baseDataSourceParamDTO.setName(dataSource.getName());
        baseDataSourceParamDTO.setNote(dataSource.getNote());

        result.put(Constants.DATA_LIST, baseDataSourceParamDTO);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query datasource list by keyword
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return data source list page
     */
    @Override
    public Map<String, Object> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        IPage<DataSource> dataSourceList;
        Page<DataSource> dataSourcePage = new Page<>(pageNo, pageSize);

        if (isAdmin(loginUser)) {
            dataSourceList = dataSourceMapper.selectPaging(dataSourcePage, 0, searchVal);
        } else {
            dataSourceList = dataSourceMapper.selectPaging(dataSourcePage, loginUser.getId(), searchVal);
        }

        List<DataSource> dataSources = dataSourceList != null ? dataSourceList.getRecords() : new ArrayList<>();
        handlePasswd(dataSources);
        PageInfo<DataSource> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int) (dataSourceList != null ? dataSourceList.getTotal() : 0L));
        pageInfo.setLists(dataSources);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * handle datasource connection password for safety
     */
    private void handlePasswd(List<DataSource> dataSourceList) {
        for (DataSource dataSource : dataSourceList) {
            String connectionParams = dataSource.getConnectionParams();
            ObjectNode object = JSONUtils.parseObject(connectionParams);
            object.put(Constants.PASSWORD, getHiddenPassword());
            dataSource.setConnectionParams(object.toString());
        }
    }

    /**
     * get hidden password (resolve the security hotspot)
     *
     * @return hidden password
     */
    private String getHiddenPassword() {
        return Constants.XXXXXX;
    }

    /**
     * query data resource list
     *
     * @param loginUser login user
     * @param type data source type
     * @return data source list page
     */
    @Override
    public Map<String, Object> queryDataSourceList(User loginUser, Integer type) {
        Map<String, Object> result = new HashMap<>();

        List<DataSource> datasourceList;

        if (isAdmin(loginUser)) {
            datasourceList = dataSourceMapper.listAllDataSourceByType(type);
        } else {
            datasourceList = dataSourceMapper.queryDataSourceByType(loginUser.getId(), type);
        }

        result.put(Constants.DATA_LIST, datasourceList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * verify datasource exists
     *
     * @param name datasource name
     * @return true if data datasource not exists, otherwise return false
     */
    @Override
    public Result<Object> verifyDataSourceName(String name) {
        Result<Object> result = new Result<>();
        List<DataSource> dataSourceList = dataSourceMapper.queryDataSourceByName(name);
        if (dataSourceList != null && !dataSourceList.isEmpty()) {
            putMsg(result, Status.DATASOURCE_EXIST);
        } else {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * check connection
     *
     * @param type data source type
     * @param parameter data source parameters
     * @return true if connect successfully, otherwise false
     */
    @Override
    public Result<Object> checkConnection(DbType type, ConnectionParam connectionParam) {
        Result<Object> result = new Result<>();
        try (Connection connection = DatasourceUtil.getConnection(type, connectionParam)) {
            if (connection == null) {
                putMsg(result, Status.CONNECTION_TEST_FAILURE);
                return result;
            }
            putMsg(result, Status.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("datasource test connection error, dbType:{}, connectionParam:{}, message:{}.", type, connectionParam, e.getMessage());
            return new Result<>(Status.CONNECTION_TEST_FAILURE.getCode(), e.getMessage());
        }
    }

    /**
     * test connection
     *
     * @param id datasource id
     * @return connect result code
     */
    @Override
    public Result<Object> connectionTest(int id) {
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            Result<Object> result = new Result<>();
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        return checkConnection(dataSource.getType(), DatasourceUtil.buildConnectionParams(dataSource.getType(), dataSource.getConnectionParams()));
    }

    /**
     * delete datasource
     *
     * @param loginUser login user
     * @param datasourceId data source id
     * @return delete result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> delete(User loginUser, int datasourceId) {
        Result<Object> result = new Result<>();
        try {
            //query datasource by id
            DataSource dataSource = dataSourceMapper.selectById(datasourceId);
            if (dataSource == null) {
                logger.error("resource id {} not exist", datasourceId);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }
            if (!hasPerm(loginUser, dataSource.getUserId())) {
                putMsg(result, Status.USER_NO_OPERATION_PERM);
                return result;
            }
            dataSourceMapper.deleteById(datasourceId);
            datasourceUserMapper.deleteByDatasourceId(datasourceId);
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            logger.error("delete datasource error", e);
            throw new RuntimeException("delete datasource error");
        }
        return result;
    }

    /**
     * unauthorized datasource
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthed data source result code
     */
    @Override
    public Map<String, Object> unauthDatasource(User loginUser, Integer userId) {

        Map<String, Object> result = new HashMap<>();
        //only admin operate
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        /**
         * query all data sources except userId
         */
        List<DataSource> resultList = new ArrayList<>();
        List<DataSource> datasourceList = dataSourceMapper.queryDatasourceExceptUserId(userId);
        Set<DataSource> datasourceSet = null;
        if (datasourceList != null && !datasourceList.isEmpty()) {
            datasourceSet = new HashSet<>(datasourceList);

            List<DataSource> authedDataSourceList = dataSourceMapper.queryAuthedDatasource(userId);

            Set<DataSource> authedDataSourceSet = null;
            if (authedDataSourceList != null && !authedDataSourceList.isEmpty()) {
                authedDataSourceSet = new HashSet<>(authedDataSourceList);
                datasourceSet.removeAll(authedDataSourceSet);

            }
            resultList = new ArrayList<>(datasourceSet);
        }
        result.put(Constants.DATA_LIST, resultList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * authorized datasource
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result code
     */
    @Override
    public Map<String, Object> authedDatasource(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        List<DataSource> authedDatasourceList = dataSourceMapper.queryAuthedDatasource(userId);
        result.put(Constants.DATA_LIST, authedDatasourceList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

}
