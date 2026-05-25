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
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.DataSourceDao;
import org.apache.dolphinscheduler.dao.repository.DataSourceUserDao;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
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

@Service
@Slf4j
public class DataSourceServiceImpl extends BaseServiceImpl implements DataSourceService {

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private DataSourceUserDao datasourceUserDao;

    private static final String TABLE = "TABLE";
    private static final String VIEW = "VIEW";
    private static final String[] TABLE_TYPES = new String[]{TABLE, VIEW};
    private static final String TABLE_NAME = "TABLE_NAME";
    private static final String COLUMN_NAME = "COLUMN_NAME";

    @Override
    public DataSource createDataSource(User loginUser, BaseDataSourceParamDTO datasourceParam) {
        DataSourceUtils.checkDatasourceParam(datasourceParam);
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.DATASOURCE,
                ApiFuncIdentificationConstant.DATASOURCE_CREATE_DATASOURCE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        // check name can use or not
        if (checkName(datasourceParam.getName())) {
            throw new ServiceException(Status.DATASOURCE_EXIST);
        }
        if (checkDescriptionLength(datasourceParam.getNote())) {
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
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
            dataSourceDao.insert(dataSource);
            return dataSource;
        } catch (DuplicateKeyException ex) {
            throw new ServiceException(Status.DATASOURCE_EXIST);
        }
    }

    @Override
    public DataSource updateDataSource(User loginUser, BaseDataSourceParamDTO dataSourceParam) {
        DataSourceUtils.checkDatasourceParam(dataSourceParam);
        // determine whether the data source exists
        DataSource dataSource = dataSourceDao.queryById(dataSourceParam.getId());
        if (dataSource == null) {
            throw new ServiceException(Status.RESOURCE_NOT_EXIST);
        }

        if (!canOperatorPermissions(loginUser, new Object[]{dataSource.getId()}, AuthorizationType.DATASOURCE,
                DATASOURCE_UPDATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        // check name can use or not
        if (!dataSourceParam.getName().trim().equals(dataSource.getName()) && checkName(dataSourceParam.getName())) {
            throw new ServiceException(Status.DATASOURCE_EXIST);
        }
        if (checkDescriptionLength(dataSourceParam.getNote())) {
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
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
            dataSourceDao.updateById(dataSource);
            return dataSource;
        } catch (DuplicateKeyException ex) {
            throw new ServiceException(Status.DATASOURCE_EXIST);
        }
    }

    private boolean checkName(String name) {
        List<DataSource> queryDataSource = dataSourceDao.queryDataSourceByName(name.trim());
        return queryDataSource != null && !queryDataSource.isEmpty();
    }

    @Override
    public BaseDataSourceParamDTO queryDataSource(int id, User loginUser) {
        DataSource dataSource = dataSourceDao.queryById(id);
        if (dataSource == null) {
            log.error("Datasource does not exist, id:{}.", id);
            throw new ServiceException(Status.RESOURCE_NOT_EXIST);
        }

        if (!canOperatorPermissions(loginUser, new Object[]{id}, AuthorizationType.DATASOURCE,
                ApiFuncIdentificationConstant.DATASOURCE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        // type
        BaseDataSourceParamDTO baseDataSourceParamDTO = DataSourceUtils.buildDatasourceParamDTO(
                dataSource.getType(), dataSource.getConnectionParams());
        baseDataSourceParamDTO.setId(dataSource.getId());
        baseDataSourceParamDTO.setName(dataSource.getName());
        baseDataSourceParamDTO.setNote(dataSource.getNote());
        baseDataSourceParamDTO.setPassword(getHiddenPassword());

        return baseDataSourceParamDTO;
    }

    @Override
    public PageInfo<DataSource> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo,
                                                          Integer pageSize) {
        IPage<DataSource> dataSourceList;
        Page<DataSource> dataSourcePage = new Page<>(pageNo, pageSize);
        PageInfo<DataSource> pageInfo = new PageInfo<>(pageNo, pageSize);
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            dataSourceList = dataSourceDao.queryDataSourcePaging(dataSourcePage, 0, searchVal);
        } else {
            Set<Integer> ids = resourcePermissionCheckService
                    .userOwnedResourceIdsAcquisition(AuthorizationType.DATASOURCE, loginUser.getId(), log);
            if (ids.isEmpty()) {
                return pageInfo;
            }
            dataSourceList = dataSourceDao.queryDataSourcePagingByIds(dataSourcePage, new ArrayList<>(ids), searchVal);
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

    @Override
    public List<DataSource> queryDataSourceList(User loginUser, Integer type) {

        List<DataSource> datasourceList;
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            datasourceList = dataSourceDao.queryDataSourceByType(0, type);
        } else {
            Set<Integer> ids = resourcePermissionCheckService
                    .userOwnedResourceIdsAcquisition(AuthorizationType.DATASOURCE, loginUser.getId(), log);
            if (ids.isEmpty()) {
                return Collections.emptyList();
            }
            datasourceList = dataSourceDao.queryByIds(ids).stream()
                    .filter(dataSource -> dataSource.getType().getCode() == type).collect(Collectors.toList());
        }

        return datasourceList;
    }

    @Override
    public void verifyDataSourceName(String name) {
        List<DataSource> dataSourceList = dataSourceDao.queryDataSourceByName(name);
        if (dataSourceList != null && !dataSourceList.isEmpty()) {
            throw new ServiceException(Status.DATASOURCE_EXIST);
        }
    }

    @Override
    public void checkConnection(DbType type, ConnectionParam connectionParam) {
        DataSourceProcessor sshDataSourceProcessor = DataSourceUtils.getDatasourceProcessor(type);
        boolean connectivity = sshDataSourceProcessor.checkDataSourceConnectivity(connectionParam);
        if (connectivity) {
            return;
        }
        throw new ServiceException(Status.CONNECTION_TEST_FAILURE);
    }

    @Override
    public void connectionTest(User loginUser, int id) {
        DataSource dataSource = dataSourceDao.queryById(id);

        if (dataSource == null) {
            throw new ServiceException(Status.RESOURCE_NOT_EXIST);
        }

        if (!canOperatorPermissions(loginUser, new Object[]{id}, AuthorizationType.DATASOURCE,
                ApiFuncIdentificationConstant.DATASOURCE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        checkConnection(dataSource.getType(),
                DataSourceUtils.buildConnectionParams(dataSource.getType(), dataSource.getConnectionParams()));
    }

    @Override
    @Transactional
    public void delete(User loginUser, int datasourceId) {
        // query datasource by id
        DataSource dataSource = dataSourceDao.queryById(datasourceId);

        if (dataSource == null) {
            throw new ServiceException(Status.RESOURCE_NOT_EXIST);
        }

        if (!canOperatorPermissions(loginUser, new Object[]{datasourceId}, AuthorizationType.DATASOURCE,
                DATASOURCE_DELETE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        dataSourceDao.deleteById(datasourceId);
        datasourceUserDao.deleteByDatasourceId(datasourceId);
    }

    @Override
    public List<DataSource> unAuthDatasource(User loginUser, Integer userId) {
        List<DataSource> datasourceList;
        if (canOperatorPermissions(loginUser, null, AuthorizationType.DATASOURCE, null)) {
            // admin gets all data sources except userId
            datasourceList = dataSourceDao.queryDatasourceExceptUserId(userId);
        } else {
            // non-admins users get their own data sources
            datasourceList = dataSourceDao.queryByUserId(loginUser.getId());
        }
        List<DataSource> resultList = new ArrayList<>();
        Set<DataSource> datasourceSet;
        if (datasourceList != null && !datasourceList.isEmpty()) {
            datasourceSet = new HashSet<>(datasourceList);

            List<DataSource> authedDataSourceList = dataSourceDao.queryAuthedDatasource(userId);

            Set<DataSource> authedDataSourceSet;
            if (authedDataSourceList != null && !authedDataSourceList.isEmpty()) {
                authedDataSourceSet = new HashSet<>(authedDataSourceList);
                datasourceSet.removeAll(authedDataSourceSet);
            }
            resultList = new ArrayList<>(datasourceSet);
        }
        return resultList;
    }

    @Override
    public List<DataSource> authedDatasource(User loginUser, Integer userId) {
        List<DataSource> authedDatasourceList = dataSourceDao.queryAuthedDatasource(userId);
        return authedDatasourceList;
    }

    @Override
    public List<ParamsOptions> getTables(User loginUser, Integer datasourceId, String database) {
        DataSource dataSource = dataSourceDao.queryById(datasourceId);

        if (dataSource == null) {
            throw new ServiceException(Status.QUERY_DATASOURCE_ERROR);
        }

        if (!canOperatorPermissions(loginUser, new Object[]{datasourceId}, AuthorizationType.DATASOURCE,
                ApiFuncIdentificationConstant.DATASOURCE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
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
    public List<ParamsOptions> getTableColumns(User loginUser, Integer datasourceId, String database,
                                               String tableName) {
        DataSource dataSource = dataSourceDao.queryById(datasourceId);

        if (dataSource == null) {
            throw new ServiceException(Status.QUERY_DATASOURCE_ERROR);
        }

        if (!canOperatorPermissions(loginUser, new Object[]{datasourceId}, AuthorizationType.DATASOURCE,
                ApiFuncIdentificationConstant.DATASOURCE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

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
    public List<ParamsOptions> getDatabases(User loginUser, Integer datasourceId) {

        DataSource dataSource = dataSourceDao.queryById(datasourceId);

        if (dataSource == null) {
            throw new ServiceException(Status.QUERY_DATASOURCE_ERROR);
        }

        if (!canOperatorPermissions(loginUser, new Object[]{datasourceId}, AuthorizationType.DATASOURCE,
                ApiFuncIdentificationConstant.DATASOURCE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
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
