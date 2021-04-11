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

package org.apache.dolphinscheduler.data.quality.flow;

import static org.apache.dolphinscheduler.data.quality.Constants.DATABASE;
import static org.apache.dolphinscheduler.data.quality.Constants.DEFAULT_DATABASE;
import static org.apache.dolphinscheduler.data.quality.Constants.DEFAULT_DRIVER;
import static org.apache.dolphinscheduler.data.quality.Constants.DRIVER;
import static org.apache.dolphinscheduler.data.quality.Constants.EMPTY;
import static org.apache.dolphinscheduler.data.quality.Constants.PASSWORD;
import static org.apache.dolphinscheduler.data.quality.Constants.TABLE;
import static org.apache.dolphinscheduler.data.quality.Constants.URL;
import static org.apache.dolphinscheduler.data.quality.Constants.USER;

import org.apache.dolphinscheduler.data.quality.Constants;

import java.util.Map;

/**
 * JdbcBaseConfig
 */
public class JdbcBaseConfig {

    private String database;

    private String table;

    private String dbTable;

    private String url;

    private String user;

    private String password;

    private String driver;

    public JdbcBaseConfig(Map<String,Object> config) {
        database = String.valueOf(config.getOrDefault(DATABASE,DEFAULT_DATABASE));
        table = String.valueOf(config.getOrDefault(TABLE,EMPTY));
        dbTable = database + Constants.DOTS + table;
        url = String.valueOf(config.getOrDefault(URL,EMPTY));
        user = String.valueOf(config.getOrDefault(USER,EMPTY));
        password = String.valueOf(config.getOrDefault(PASSWORD,EMPTY));
        driver = String.valueOf(config.getOrDefault(DRIVER,DEFAULT_DRIVER));
    }

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }

    public String getDbTable() {
        return dbTable;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDriver() {
        return driver;
    }
}
