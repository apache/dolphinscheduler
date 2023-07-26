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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.DATASOURCE_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.DATASOURCE_UPDATE;

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.DataSourceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class DataSourceServiceImpl extends BaseServiceImpl implements DataSourceService {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private DataSourceUserMapper datasourceUserMapper;

    private static final String TABLE = "TABLE";
    private static final String VIEW = "VIEW";
    private static final String[] TABLE_TYPES = new String[]{TABLE, VIEW};
    private static final String TABLE_NAME = "TABLE_NAME";
    private static final String COLUMN_NAME = "COLUMN_NAME";

    /**
     * create data source
     *
     * @param loginUser login user
     * @param datasourceParam datasource parameters
     * @return create result code
     */
    @Override
    @Transactional
    public Result<Object> createDataSource(User loginUser, BaseDataSourceParamDTO datasourceParam) {
        DataSourceUtils.checkDatasourceParam(datasourceParam);
        Result<Object> result = new Result<>();
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.DATASOURCE,
                ApiFuncIdentificationConstant.DATASOURCE_CREATE_DATASOURCE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        // check name can use or not
        if (checkName(datasourceParam.getName())) {
            log.warn("Datasource with the same name already exists, name:{}.", datasourceParam.getName());
            putMsg(result, Status.DATASOURCE_EXIST);
            return result;
        }
        if (checkDescriptionLength(datasourceParam.getNote())) {
            log.warn("Parameter description is too long, description:{}.", datasourceParam.getNote());
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(datasourceParam);

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
            permissionPostHandle(AuthorizationType.DATASOURCE, loginUser.getId(),
                    Collections.singletonList(dataSource.getId()), log);
            log.info("Datasource create complete, dbType:{}, datasourceName:{}.", dataSource.getType().getDescp(),
                    dataSource.getName());
        } catch (DuplicateKeyException ex) {
            log.error("Datasource create error.", ex);
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
        DataSourceUtils.checkDatasourceParam(dataSourceParam);
        Result<Object> result = new Result<>();
        // determine whether the data source exists
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            log.error("Datasource does not exist, id:{}.", id);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }

        if (!canOperatorPermissions(loginUser, new Object[]{dataSource.getId()}, AuthorizationType.DATASOURCE,
                DATASOURCE_UPDATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // check name can use or not
        if (!dataSourceParam.getName().trim().equals(dataSource.getName()) && checkName(dataSourceParam.getName())) {
            log.warn("Datasource with the same name already exists, name:{}.", dataSource.getName());
            putMsg(result, Status.DATASOURCE_EXIST);
            return result;
        }
        if (checkDescriptionLength(dataSourceParam.getNote())) {
            log.warn("Parameter description is too long, description:{}.", dataSourceParam.getNote());
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        // check password，if the password is not updated, set to the old password.
        ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(dataSourceParam);

        String password = connectionParam.getPassword();

        if (StringUtils.isBlank(password)) {
            String oldConnectionParams = dataSource.getConnectionParams();
            ObjectNode oldParams = JSONUtils.parseObject(oldConnectionParams);
            connectionParam.setPassword(oldParams.path(Constants.PASSWORD).asText());
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
            log.info("Update datasource complete, datasourceId:{}, datasourceName:{}.", dataSource.getId(),
                    dataSource.getName());
            putMsg(result, Status.SUCCESS);
        } catch (DuplicateKeyException ex) {
            log.error("Update datasource error, datasourceId:{}, datasourceName:{}.", dataSource.getId(),
                    dataSource.getName());
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
    public BaseDataSourceParamDTO queryDataSource(int id, User loginUser) {
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            log.error("Datasource does not exist, id:{}.", id);
            throw new ServiceException(Status.RESOURCE_NOT_EXIST);
        }

        if (!canOperatorPermissions(loginUser, new Object[]{dataSource.getId()}, AuthorizationType.DATASOURCE,
                ApiFuncIdentificationConstant.DATASOURCE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        // type
        BaseDataSourceParamDTO baseDataSourceParamDTO = DataSourceUtils.buildDatasourceParamDTO(
                dataSource.getType(), dataSource.getConnectionParams());
        baseDataSourceParamDTO.setId(dataSource.getId());
        baseDataSourceParamDTO.setName(dataSource.getName());
        baseDataSourceParamDTO.setNote(dataSource.getNote());

        return baseDataSourceParamDTO;
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
    public PageInfo<DataSource> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo,
                                                          Integer pageSize) {
        IPage<DataSource> dataSourceList = null;
        Page<DataSource> dataSourcePage = new Page<>(pageNo, pageSize);
        PageInfo<DataSource> pageInfo = new PageInfo<>(pageNo, pageSize);
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            dataSourceList = dataSourceMapper.selectPaging(dataSourcePage, 0, searchVal);
        } else {
            Set<Integer> ids = resourcePermissionCheckService
                    .userOwnedResourceIdsAcquisition(AuthorizationType.DATASOURCE, loginUser.getId(), log);
            if (ids.isEmpty()) {
                return pageInfo;
            }
            dataSourceList = dataSourceMapper.selectPagingByIds(dataSourcePage, new ArrayList<>(ids), searchVal);
        }

        List<DataSource> dataSources = dataSourceList != null ? dataSourceList.getRecords() : new ArrayList<>();
        handlePasswd(dataSources);
        pageInfo.setTotal((int) (dataSourceList != null ? dataSourceList.getTotal() : 0L));
        pageInfo.setTotalList(dataSources);
        return pageInfo;
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
    public List<DataSource> queryDataSourceList(User loginUser, Integer type) {

        List<DataSource> datasourceList = null;
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            datasourceList = dataSourceMapper.queryDataSourceByType(0, type);
        } else {
            Set<Integer> ids = resourcePermissionCheckService
                    .userOwnedResourceIdsAcquisition(AuthorizationType.DATASOURCE, loginUser.getId(), log);
            if (ids.isEmpty()) {
                return Collections.emptyList();
            }
            datasourceList = dataSourceMapper.selectBatchIds(ids).stream()
                    .filter(dataSource -> dataSource.getType().getCode() == type).collect(Collectors.toList());
        }

        return datasourceList;
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
            log.warn("Datasource with the same name already exists, dataSourceName:{}.",
                    dataSourceList.get(0).getName());
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
     * @param connectionParam connectionParam
     * @return true if connect successfully, otherwise false
     * @return true if connect successfully, otherwise false
     */
    @Override
    public Result<Object> checkConnection(DbType type, ConnectionParam connectionParam) {
        Result<Object> result = new Result<>();
        if (type == DbType.SSH) {
            DataSourceProcessor sshDataSourceProcessor = DataSourceUtils.getDatasourceProcessor(type);
            if (sshDataSourceProcessor.testConnection(connectionParam)) {
                putMsg(result, Status.SUCCESS);
            } else {
                putMsg(result, Status.CONNECT_DATASOURCE_FAILURE);
            }
            return result;
        }
        try (Connection connection = DataSourceClientProvider.getAdHocConnection(type, connectionParam)) {
            if (connection == null) {
                log.error("Connection test to {} datasource failed, connectionParam:{}.", type.getDescp(),
                        connectionParam);
                putMsg(result, Status.CONNECTION_TEST_FAILURE);
                return result;
            }
            log.info("Connection test to {} datasource success, connectionParam:{}", type.getDescp(),
                    connectionParam);
            putMsg(result, Status.SUCCESS);
            return result;
        } catch (Exception e) {
            String message = Optional.of(e).map(Throwable::getCause)
                    .map(Throwable::getMessage)
                    .orElse(e.getMessage());
            log.error("Datasource test connection error, dbType:{}, connectionParam:{}, message:{}.", type,
                    connectionParam, message);
            return new Result<>(Status.CONNECTION_TEST_FAILURE);
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
            log.error("Datasource does not exist, datasourceId:{}.", id);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        return checkConnection(dataSource.getType(),
                DataSourceUtils.buildConnectionParams(dataSource.getType(), dataSource.getConnectionParams()));
    }

    /**
     * delete datasource
     *
     * @param loginUser login user
     * @param datasourceId data source id
     * @return delete result code
     */
    @Override
    @Transactional
    public Result<Object> delete(User loginUser, int datasourceId) {
        Result<Object> result = new Result<>();
        try {
            // query datasource by id
            DataSource dataSource = dataSourceMapper.selectById(datasourceId);
            if (dataSource == null) {
                log.warn("Datasource does not exist, datasourceId:{}.", datasourceId);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }
            if (!canOperatorPermissions(loginUser, new Object[]{dataSource.getId()}, AuthorizationType.DATASOURCE,
                    DATASOURCE_DELETE)) {
                putMsg(result, Status.USER_NO_OPERATION_PERM);
                return result;
            }
            dataSourceMapper.deleteById(datasourceId);
            datasourceUserMapper.deleteByDatasourceId(datasourceId);
            log.info("Delete datasource complete, datasourceId:{}.", datasourceId);
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            log.error("Delete datasource complete, datasourceId:{}.", datasourceId, e);
            throw new ServiceException(Status.DELETE_DATA_SOURCE_FAILURE);
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
    public List<DataSource> unAuthDatasource(User loginUser, Integer userId) {
        List<DataSource> datasourceList;
        if (canOperatorPermissions(loginUser, null, AuthorizationType.DATASOURCE, null)) {
            // admin gets all data sources except userId
            datasourceList = dataSourceMapper.queryDatasourceExceptUserId(userId);
        } else {
            // non-admins users get their own data sources
            datasourceList = dataSourceMapper.selectByMap(Collections.singletonMap("user_id", loginUser.getId()));
        }
        List<DataSource> resultList = new ArrayList<>();
        Set<DataSource> datasourceSet;
        if (datasourceList != null && !datasourceList.isEmpty()) {
            datasourceSet = new HashSet<>(datasourceList);

            List<DataSource> authedDataSourceList = dataSourceMapper.queryAuthedDatasource(userId);

            Set<DataSource> authedDataSourceSet;
            if (authedDataSourceList != null && !authedDataSourceList.isEmpty()) {
                authedDataSourceSet = new HashSet<>(authedDataSourceList);
                datasourceSet.removeAll(authedDataSourceSet);
            }
            resultList = new ArrayList<>(datasourceSet);
        }
        return resultList;
    }

    /**
     * authorized datasource
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result code
     */
    @Override
    public List<DataSource> authedDatasource(User loginUser, Integer userId) {
        List<DataSource> authedDatasourceList = dataSourceMapper.queryAuthedDatasource(userId);
        return authedDatasourceList;
    }

    @Override
    public List<ParamsOptions> getTables(Integer datasourceId, String database) {
        DataSource dataSource = dataSourceMapper.selectById(datasourceId);

        List<String> tableList = null;
        BaseConnectionParam connectionParam =
                (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                        dataSource.getType(),
                        dataSource.getConnectionParams());

        if (null == connectionParam) {
            throw new ServiceException(Status.DATASOURCE_CONNECT_FAILED);
        }

        Connection connection =
                DataSourceUtils.getConnection(dataSource.getType(), connectionParam);
        ResultSet tables = null;

        try {

            if (null == connection) {
                throw new ServiceException(Status.DATASOURCE_CONNECT_FAILED);
            }

            DatabaseMetaData metaData = connection.getMetaData();
            String schema = null;
            try {
                schema = metaData.getConnection().getSchema();
            } catch (SQLException e) {
                log.error("Cant not get the schema, datasourceId:{}.", datasourceId, e);
                throw new ServiceException(Status.GET_DATASOURCE_TABLES_ERROR);
            }

            tables = metaData.getTables(
                    database,
                    getDbSchemaPattern(dataSource.getType(), schema, connectionParam),
                    "%", TABLE_TYPES);
            if (null == tables) {
                log.error("Get datasource tables error, datasourceId:{}.", datasourceId);
                throw new ServiceException(Status.GET_DATASOURCE_TABLES_ERROR);
            }

            tableList = new ArrayList<>();
            while (tables.next()) {
                String name = tables.getString(TABLE_NAME);
                tableList.add(name);
            }

        } catch (Exception e) {
            log.error("Get datasource tables error, datasourceId:{}.", datasourceId, e);
            throw new ServiceException(Status.GET_DATASOURCE_TABLES_ERROR);
        } finally {
            closeResult(tables);
            releaseConnection(connection);
        }

        List<ParamsOptions> options = getParamsOptions(tableList);
        return options;
    }

    @Override
    public List<ParamsOptions> getTableColumns(Integer datasourceId, String database, String tableName) {
        DataSource dataSource = dataSourceMapper.selectById(datasourceId);
        BaseConnectionParam connectionParam =
                (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                        dataSource.getType(),
                        dataSource.getConnectionParams());

        if (null == connectionParam) {
            throw new ServiceException(Status.DATASOURCE_CONNECT_FAILED);
        }

        Connection connection =
                DataSourceUtils.getConnection(dataSource.getType(), connectionParam);
        List<String> columnList = new ArrayList<>();
        ResultSet rs = null;

        try {
            if (null == connection) {
                throw new ServiceException(Status.DATASOURCE_CONNECT_FAILED);
            }

            DatabaseMetaData metaData = connection.getMetaData();

            if (dataSource.getType() == DbType.ORACLE) {
                database = null;
            }
            rs = metaData.getColumns(database, null, tableName, "%");
            if (rs == null) {
                throw new ServiceException(Status.DATASOURCE_CONNECT_FAILED);
            }
            while (rs.next()) {
                columnList.add(rs.getString(COLUMN_NAME));
            }
        } catch (Exception e) {
            log.error("Get datasource table columns error, datasourceId:{}.", dataSource.getId(), e);
            throw new ServiceException(Status.DATASOURCE_CONNECT_FAILED);
        } finally {
            closeResult(rs);
            releaseConnection(connection);
        }

        List<ParamsOptions> options = getParamsOptions(columnList);
        return options;
    }

    @Override
    public List<ParamsOptions> getDatabases(Integer datasourceId) {

        DataSource dataSource = dataSourceMapper.selectById(datasourceId);

        if (dataSource == null) {
            throw new ServiceException(Status.QUERY_DATASOURCE_ERROR);
        }

        List<String> tableList;
        BaseConnectionParam connectionParam =
                (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                        dataSource.getType(),
                        dataSource.getConnectionParams());

        if (null == connectionParam) {
            throw new ServiceException(Status.DATASOURCE_CONNECT_FAILED);
        }

        Connection connection =
                DataSourceUtils.getConnection(dataSource.getType(), connectionParam);
        ResultSet rs = null;

        try {
            if (null == connection) {
                throw new ServiceException(Status.DATASOURCE_CONNECT_FAILED);
            }
            if (dataSource.getType() == DbType.POSTGRESQL) {
                rs = connection.createStatement().executeQuery(Constants.DATABASES_QUERY_PG);
            } else {
                rs = connection.createStatement().executeQuery(Constants.DATABASES_QUERY);
            }
            tableList = new ArrayList<>();
            while (rs.next()) {
                String name = rs.getString(1);
                tableList.add(name);
            }
        } catch (Exception e) {
            log.error("Get databases error, datasourceId:{}.", datasourceId, e);
            throw new ServiceException(Status.GET_DATASOURCE_TABLES_ERROR);
        } finally {
            closeResult(rs);
            releaseConnection(connection);
        }

        List<ParamsOptions> options = getParamsOptions(tableList);
        return options;
    }

    private List<ParamsOptions> getParamsOptions(List<String> columnList) {
        List<ParamsOptions> options = null;
        if (CollectionUtils.isNotEmpty(columnList)) {
            options = new ArrayList<>();

            for (String column : columnList) {
                ParamsOptions childrenOption =
                        new ParamsOptions(column, column, false);
                options.add(childrenOption);
            }
        }
        return options;
    }

    private String getDbSchemaPattern(DbType dbType, String schema, BaseConnectionParam connectionParam) {
        if (dbType == null) {
            return null;
        }
        String schemaPattern = null;
        switch (dbType) {
            case HIVE:
                schemaPattern = connectionParam.getDatabase();
                break;
            case ORACLE:
                schemaPattern = connectionParam.getUser();
                if (null != schemaPattern) {
                    schemaPattern = schemaPattern.toUpperCase();
                }
                break;
            case SQLSERVER:
                schemaPattern = "dbo";
                break;
            case CLICKHOUSE:
            case DATABEND:
            case PRESTO:
                if (!StringUtils.isEmpty(schema)) {
                    schemaPattern = schema;
                }
                break;
            default:
                break;
        }
        return schemaPattern;
    }

    private static void releaseConnection(Connection connection) {
        if (null != connection) {
            try {
                connection.close();
            } catch (Exception e) {
                log.error("Connection release error", e);
            }
        }
    }

    private static void closeResult(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                log.error("ResultSet close error", e);
            }
        }
    }

}
