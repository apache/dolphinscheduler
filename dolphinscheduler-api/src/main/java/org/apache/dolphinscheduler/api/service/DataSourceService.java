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
 * datasource service
 */
public interface DataSourceService {

    /**
     * create a new datasource
     *
     * @param loginUser login user
     * @param datasourceParam datasource configuration DTO
     * @return created {@link DataSource} entity (sensitive fields masked)
     * @throws ServiceException if permission denied, security check fails, or connection test fails
     */
    DataSource createDataSource(User loginUser, BaseDataSourceParamDTO datasourceParam);

    /**
     * update datasource
     *
     * @param loginUser login user
     * @param dataSourceParam datasource params
     * @return updated {@link DataSource} entity (sensitive fields masked)
     * @throws ServiceException if permission denied, security check fails, or connection test fails
     */
    DataSource updateDataSource(User loginUser, BaseDataSourceParamDTO dataSourceParam);

    /**
     * query datasource
     *
     * @param loginUser login user
     * @param id datasource id
     * @return a {@link BaseDataSourceParamDTO} entity (sensitive fields masked)
     */
    BaseDataSourceParamDTO queryDataSource(int id, User loginUser);

    /**
     * query datasource list by keyword
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo    page number
     * @param pageSize  page size
     * @return datasource list page
     */
    PageInfo<DataSource> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * query data resource list
     *
     * @param loginUser login user
     * @param type      datasource type
     * @return datasource list
     */
    List<DataSource> queryDataSourceList(User loginUser, Integer type);

    /**
     * verify whether a datasource name already exists
     * <p>
     * If the name already exists, a {@link ServiceException} is thrown.
     * If the name is available (does not exist), the method completes successfully without returning a value.
     *
     * @param name the datasource name to verify
     * @throws ServiceException if the datasource name already exists (Status.DATASOURCE_EXIST)
     */
    void verifyDataSourceName(String name);

    /**
     * check the connectivity of a datasource based on the provided type and parameters
     * <p>
     * This method attempts to establish a connection.
     * - If the connection is successful, the method returns normally (void).
     * - If the connection fails, a {@link ServiceException} is thrown.
     *
     * @param type            the type of the datasource (e.g., MYSQL, POSTGRESQL)
     * @param connectionParam the connection parameters containing host, port, credentials, etc.
     * @throws ServiceException if the connection test fails (Status.CONNECTION_TEST_FAILURE)
     */
    void checkConnection(DbType type, ConnectionParam connectionParam);

    /**
     * test the connectivity of a specific data source
     *
     * @param loginUser the current logged-in user (required for permission check)
     * @param id        the unique identifier of the data source to test
     * @throws ServiceException if the resource doesn't exist, permission is denied, or connection fails
     */
    void connectionTest(User loginUser, int id);

    /**
     * delete a datasource by ID
     *
     * @param loginUser    the current logged-in user
     * @param datasourceId the unique identifier of the datasource to delete
     * @throws ServiceException if checks fail or deletion encounters an error
     */
    void delete(User loginUser, int datasourceId);

    /**
     * query the list of unauthorized data sources for a specific user
     *
     * @param loginUser login user
     * @param userId    user id
     * @return a list of {@link DataSource} objects that are available to be authorized to the target user
     */
    List<DataSource> unAuthDatasource(User loginUser, Integer userId);

    /**
     * query the list of data sources authorized for a specific user
     *
     * @param loginUser login user
     * @param userId    user id
     * @return a list of {@link DataSource} objects that are authorized to the target user
     */
    List<DataSource> authedDatasource(User loginUser, Integer userId);

    /**
     * query the list of tables from a specific database within a data source
     *
     * @param loginUser    the current logged-in user (required for permission check)
     * @param datasourceId the unique identifier of the data source
     * @param database     the specific database/schema name to query (nullable for some DB types like SQLite)
     * @return a list of {@link ParamsOptions} containing table names and optional metadata (e.g., comments)
     * @throws ServiceException if permission denied, resource not found, or connection fails
     */
    List<ParamsOptions> getTables(User loginUser, Integer datasourceId, String database);

    /**
     * query the list of columns for a specific table in a data source
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
     * query the list of databases (or schemas) available in a specific data source
     *
     * @param loginUser    current logged-in user
     * @param datasourceId ID of the data source
     * @return list of {@link ParamsOptions} representing database/schema names
     * @throws ServiceException if permission denied, resource not found, or connection fails
     */
    List<ParamsOptions> getDatabases(User loginUser, Integer datasourceId);

}
