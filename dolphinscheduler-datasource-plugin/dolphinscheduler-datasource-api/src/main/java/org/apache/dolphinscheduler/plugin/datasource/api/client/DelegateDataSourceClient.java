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

package org.apache.dolphinscheduler.plugin.datasource.api.client;

import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientManager;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.List;
import java.util.Map;

public class DelegateDataSourceClient {

    private final DataSourceClientManager dataSourceClientManager;

    public DelegateDataSourceClient(DataSourceClientManager dataSourceClientManager) {
        this.dataSourceClientManager = dataSourceClientManager;
    }

    /**
     * @description: Get a list of databases, optionally filtered by Pattern
     * @param: dbType
     * @param: connectionParam
     * @param: databasePattern
     * @return: java.util.List<java.lang.String>
     **/
    public List<String> getDatabaseList(DbType dbType, ConnectionParam connectionParam, String databasePattern) {
        return dataSourceClientManager.getDataSource(dbType, connectionParam).getDatabaseList(databasePattern);
    }

    /**
     * @description: Get a list of tables, optionally filtered by Pattern
     * @param: dbType
     * @param: connectionParam
     * @param: dbName
     * @param: schemaName
     * @param: tablePattern
     * @return: java.util.List<java.lang.String>
     **/
    public List<String> getTableList(DbType dbType, ConnectionParam connectionParam, String dbName, String schemaName, String tablePattern) {
        return dataSourceClientManager.getDataSource(dbType, connectionParam).getTableList(dbName, schemaName, tablePattern);
    }

    public List<Map<String, Object>> getTableStruct(DbType dbType, ConnectionParam connectionParam, String dbname, String schemaName, String tableName) {
        return dataSourceClientManager.getDataSource(dbType, connectionParam).getTableStruct(dbname, schemaName, tableName);
    }
}
