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

package org.apache.dolphinscheduler.common.datasource.hive;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.sql.DriverManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Class.class, DriverManager.class})
public class HiveDatasourceProcessorTest {

    private HiveDatasourceProcessor hiveDatasourceProcessor = new HiveDatasourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        HiveDataSourceParamDTO hiveDataSourceParamDTO = new HiveDataSourceParamDTO();
        hiveDataSourceParamDTO.setHost("localhost1,localhost2");
        hiveDataSourceParamDTO.setPort(5142);
        hiveDataSourceParamDTO.setUserName("default");
        hiveDataSourceParamDTO.setDatabase("default");
        HiveConnectionParam connectionParams = (HiveConnectionParam) hiveDatasourceProcessor
                .createConnectionParams(hiveDataSourceParamDTO);
        System.out.println(JSONUtils.toJsonString(connectionParams));
        Assert.assertNotNull(connectionParams);
        Assert.assertEquals("jdbc:hive2://localhost1:5142,localhost2:5142", connectionParams.getAddress());
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionParam = "{\"user\":\"default\",\"address\":\"jdbc:hive2://localhost1:5142,localhost2:5142\""
                + ",\"jdbcUrl\":\"jdbc:hive2://localhost1:5142,localhost2:5142/default\"}";
        HiveConnectionParam connectionParams = (HiveConnectionParam) hiveDatasourceProcessor
                .createConnectionParams(connectionParam);
        Assert.assertNotNull(connectionParam);
        Assert.assertEquals("default", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assert.assertEquals(Constants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER, hiveDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        HiveConnectionParam connectionParam = new HiveConnectionParam();
        connectionParam.setJdbcUrl("jdbc:hive2://localhost1:5142,localhost2:5142/default");
        Assert.assertEquals("jdbc:hive2://localhost1:5142,localhost2:5142/default",
                hiveDatasourceProcessor.getJdbcUrl(connectionParam));
    }

    @Test
    public void testGetDbType() {
        Assert.assertEquals(DbType.HIVE, hiveDatasourceProcessor.getDbType());
    }
}