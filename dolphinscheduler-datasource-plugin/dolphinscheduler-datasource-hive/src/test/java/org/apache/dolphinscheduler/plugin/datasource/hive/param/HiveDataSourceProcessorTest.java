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

package org.apache.dolphinscheduler.plugin.datasource.hive.param;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HiveDataSourceProcessorTest {

    private HiveDataSourceProcessor hiveDatasourceProcessor = new HiveDataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
        Map<String, String> props = new HashMap<>();
        props.put("serverTimezone", "utc");
        HiveDataSourceParamDTO hiveDataSourceParamDTO = new HiveDataSourceParamDTO();
        hiveDataSourceParamDTO.setHost("localhost1,localhost2");
        hiveDataSourceParamDTO.setPort(5142);
        hiveDataSourceParamDTO.setUserName("default");
        hiveDataSourceParamDTO.setDatabase("default");
        hiveDataSourceParamDTO.setOther(props);

        try (
                MockedStatic<PasswordUtils> mockedStaticPasswordUtils = Mockito.mockStatic(PasswordUtils.class);
                MockedStatic<CommonUtils> mockedStaticCommonUtils = Mockito.mockStatic(CommonUtils.class)) {
            mockedStaticPasswordUtils.when(() -> PasswordUtils.encodePassword(Mockito.anyString())).thenReturn("test");
            mockedStaticCommonUtils.when(CommonUtils::getKerberosStartupState).thenReturn(false);
            HiveConnectionParam connectionParams = (HiveConnectionParam) hiveDatasourceProcessor
                    .createConnectionParams(hiveDataSourceParamDTO);
            Assertions.assertNotNull(connectionParams);
            Assertions.assertEquals("jdbc:hive2://localhost1:5142,localhost2:5142", connectionParams.getAddress());
        }
    }

    @Test
    public void testCreateConnectionParams2() {
        String connectionParam = "{\"user\":\"default\",\"address\":\"jdbc:hive2://localhost1:5142,localhost2:5142\""
                + ",\"jdbcUrl\":\"jdbc:hive2://localhost1:5142,localhost2:5142/default\"}";
        HiveConnectionParam connectionParams = (HiveConnectionParam) hiveDatasourceProcessor
                .createConnectionParams(connectionParam);
        Assertions.assertNotNull(connectionParam);
        Assertions.assertEquals("default", connectionParams.getUser());
    }

    @Test
    public void testGetDatasourceDriver() {
        Assertions.assertEquals(DataSourceConstants.ORG_APACHE_HIVE_JDBC_HIVE_DRIVER,
                hiveDatasourceProcessor.getDatasourceDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        HiveConnectionParam connectionParam = new HiveConnectionParam();
        connectionParam.setJdbcUrl("jdbc:hive2://localhost1:5142,localhost2:5142/default");
        Assertions.assertEquals("jdbc:hive2://localhost1:5142,localhost2:5142/default",
                hiveDatasourceProcessor.getJdbcUrl(connectionParam));
    }

    @Test
    public void testGetDbType() {
        Assertions.assertEquals(DbType.HIVE, hiveDatasourceProcessor.getDbType());
    }

    @Test
    public void testGetValidationQuery() {
        Assertions.assertEquals(DataSourceConstants.HIVE_VALIDATION_QUERY,
                hiveDatasourceProcessor.getValidationQuery());
    }

    @Test
    void splitAndRemoveComment() {
        String sql = "create table if not exists test_ods.tb_test(\n" +
                "  `id` bigint COMMENT 'id',   -- auto increment\n" +
                "  `user_name` string COMMENT 'username',\n" +
                "  `birthday` string COMMENT 'birthday',\n" +
                "  `gender` int COMMENT '1 male 2 female'\n" +
                ") COMMENT 'user information table' PARTITIONED BY (`date_id` string);\n" +
                "\n" +
                "-- insert\n" +
                "insert\n" +
                "  overwrite table test_ods.tb_test partition(date_id = '2024-03-28') -- partition\n" +
                "values\n" +
                "  (1, 'Magic', '1990-10-01', '1');";
        List<String> list = hiveDatasourceProcessor.splitAndRemoveComment(sql);
        Assertions.assertEquals(list.size(), 2);

    }
}
