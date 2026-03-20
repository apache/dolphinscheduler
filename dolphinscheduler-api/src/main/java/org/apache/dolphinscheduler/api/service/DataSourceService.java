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

import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;

import java.util.List;

/**
 * data source service
 */
public interface DataSourceService {

    /**
     * create data source
     *
     * @param loginUser login user
     * @param datasourceParam datasource parameter
     * @return create result code
     */
    DataSource createDataSource(User loginUser, BaseDataSourceParamDTO datasourceParam);

    /**
     * updateWorkflowInstance datasource
     *
     * @param loginUser login user
     * @param dataSourceParam data source params
     * @return update result code
     */
    DataSource updateDataSource(User loginUser, BaseDataSourceParamDTO dataSourceParam);

    /**
     * updateWorkflowInstance datasource
     *
     * @param id datasource id
     * @return data source detail
     */
    BaseDataSourceParamDTO queryDataSource(int id, User loginUser);

    /**
     * query datasource list by keyword
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo    page number
     * @param pageSize  page size
     * @return data source list page
     */
    PageInfo<DataSource> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * query data resource list
     *
     * @param loginUser login user
     * @param type      data source type
     * @return data source list page
     */
    List<DataSource> queryDataSourceList(User loginUser, Integer type);

    /**
     * verify datasource exists
     *
     * @param name      datasource name
     * @return true if data datasource not exists, otherwise return false
     */
    void verifyDataSourceName(String name);

    /**
     * check connection
     *
     * @param type      data source type
     * @param parameter data source parameters
     * @return true if connect successfully, otherwise false
     */
    void checkConnection(DbType type, ConnectionParam parameter);

    /**
     * Tests the connectivity of a specific data source.
     *
     * @param loginUser the current logged-in user (required for permission check)
     * @param id        the unique identifier of the data source to test
     * @throws ServiceException if the resource doesn't exist, permission is denied, or connection fails
     */
    void connectionTest(User loginUser, int id);

    /**
     * delete datasource
     *
     * @param loginUser    login user
     * @param datasourceId data source id
     * @return delete result code
     */
    void delete(User loginUser, int datasourceId);

    /**
     * unauthorized datasource
     *
     * @param loginUser login user
     * @param userId    user id
     * @return unauthed data source result code
     */
    List<DataSource> unAuthDatasource(User loginUser, Integer userId);

    /**
     * authorized datasource
     *
     * @param loginUser login user
     * @param userId    user id
     * @return authorized result code
     */
    List<DataSource> authedDatasource(User loginUser, Integer userId);

    /**
     * Retrieves the list of tables from a specific database within a data source.
     *
     * @param loginUser    the current logged-in user (required for permission check)
     * @param datasourceId the unique identifier of the data source
     * @param database     the specific database/schema name to query (nullable for some DB types like SQLite)
     * @return a list of {@link ParamsOptions} containing table names and optional metadata (e.g., comments)
     * @throws ServiceException if permission denied, resource not found, or connection fails
     */
    List<ParamsOptions> getTables(User loginUser, Integer datasourceId, String database);

    /**
     * Retrieves the list of columns for a specific table in a data source.
     *
     * @param loginUser    current logged-in user
     * @param datasourceId ID of the data source
     * @param database     database/schema name
     * @param tableName    table name to query
     * @return list of {@link ParamsOptions} representing column names and types
     * @throws ServiceException if permission denied, resource not found, or connection fails
     */
    List<ParamsOptions> getTableColumns(User loginUser, Integer datasourceId, String database, String tableName);

    /**
     * Retrieves the list of databases (or schemas) available in a specific data source.
     *
     * @param loginUser    current logged-in user
     * @param datasourceId ID of the data source
     * @return list of {@link ParamsOptions} representing database/schema names
     * @throws ServiceException if permission denied, resource not found, or connection fails
     */
    List<ParamsOptions> getDatabases(User loginUser, Integer datasourceId);

}
