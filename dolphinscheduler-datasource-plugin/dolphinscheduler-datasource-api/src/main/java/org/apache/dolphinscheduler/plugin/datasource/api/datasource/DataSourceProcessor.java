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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource;

import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public interface DataSourceProcessor {

    /**
     * cast JSON to relate DTO
     *
     * @param paramJson
     * @return {@link BaseDataSourceParamDTO}
     */
    BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson);

    /**
     * check datasource param is valid
     */
    void checkDatasourceParam(BaseDataSourceParamDTO datasourceParam);

    /**
     * get Datasource Client UniqueId
     *
     * @return UniqueId
     */
    String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType);

    /**
     * create BaseDataSourceParamDTO by connectionJson
     *
     * @param connectionJson see{@link org.apache.dolphinscheduler.dao.entity.Datasource}
     * @return {@link BaseDataSourceParamDTO}
     */
    BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson);

    /**
     * create datasource connection parameter which will be stored at DataSource
     * <p>
     * see {@code org.apache.dolphinscheduler.dao.entity.DataSource.connectionParams}
     */
    ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam);

    /**
     * deserialize json to datasource connection param
     *
     * @param connectionJson {@code org.apache.dolphinscheduler.dao.entity.DataSource.connectionParams}
     * @return {@link BaseConnectionParam}
     */
    ConnectionParam createConnectionParams(String connectionJson);

    /**
     * get datasource Driver
     */
    String getDatasourceDriver();

    /**
     * get validation Query
     */
    String getValidationQuery();

    /**
     * get jdbcUrl by connection param, the jdbcUrl is different with ConnectionParam.jdbcUrl, this method will inject
     * other to jdbcUrl
     *
     * @param connectionParam connection param
     */
    String getJdbcUrl(ConnectionParam connectionParam);

    /**
     * get connection by connectionParam
     *
     * @param connectionParam connectionParam
     * @return {@link Connection}
     */
    Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException, IOException;

    /**
     * test connection, use for not jdbc datasource
     *
     * @param connectionParam connectionParam
     * @return true if connection is valid
     */
    default boolean testConnection(ConnectionParam connectionParam) {
        return false;
    }

    /**
     * @return {@link DbType}
     */
    DbType getDbType();

    /**
     * get datasource processor
     */
    DataSourceProcessor create();
}
