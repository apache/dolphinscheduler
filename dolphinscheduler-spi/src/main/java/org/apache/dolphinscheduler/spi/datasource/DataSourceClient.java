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

package org.apache.dolphinscheduler.spi.datasource;

import org.apache.commons.lang3.tuple.MutableTriple;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface DataSourceClient extends AutoCloseable {

    void checkClient();

    @Override
    void close();

    Connection getConnection();

    List<String> getDatabaseList(String databasePattern);

    List<String> getTableList(String dbName, String schemaName, String tablePattern);

    List<Map<String, Object>> getTableStruct(String dbName, String schemaName, String tableName);

    MutableTriple<Map<String, String>, List<Map<String, Object>>, List<Map<String, String>>> executeSql(String dbName, String schemaName, Boolean oneSession, String querySql);

}
