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

import org.apache.dolphinscheduler.data.quality.SparkApplicationTestBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * FlowTestBase
 */
public class FlowTestBase extends SparkApplicationTestBase {

    protected String url = "jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true";

    protected String driver = "org.h2.Driver";

    protected Connection getConnection() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("user", "test");
        properties.setProperty("password", "123456");
        properties.setProperty("rowId", "false");
        DriverManager.registerDriver(new org.h2.Driver());
        Class.forName(driver, false, this.getClass().getClassLoader());
        return DriverManager.getConnection(url, properties);
    }

}
