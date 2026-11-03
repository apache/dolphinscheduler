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

package org.apache.dolphinscheduler.plugin.datasource.kyuubi;

import org.apache.dolphinscheduler.plugin.datasource.kyuubi.param.KyuubiConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.kyuubi.param.KyuubiDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.kyuubi.param.KyuubiDataSourceProcessor;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class KyuubiPooledDataSourceClientTest {

    @Mock
    private KyuubiPooledDataSourceClient kyuubiPooledDataSourceClient;

    @Test
    public void testGetConnection() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(kyuubiPooledDataSourceClient.getConnection()).thenReturn(connection);
        Assertions.assertNotNull(kyuubiPooledDataSourceClient.getConnection());

    }

    @Test
    public void testRawUrlPoolDoesNotReceiveLegacyProperties() {
        KyuubiDataSourceParamDTO dto = new KyuubiDataSourceParamDTO();
        dto.setRawJdbcUrl("jdbc:kyuubi://localhost:10009/default?kyuubi.engine.share.level=USER");
        dto.setOther(Collections.singletonMap("stale", "must-not-be-injected"));
        KyuubiConnectionParam param =
                (KyuubiConnectionParam) new KyuubiDataSourceProcessor().createConnectionParams(dto);
        try (InspectableClient client = new InspectableClient(param)) {
            Assertions.assertEquals(dto.getRawJdbcUrl(), client.dataSourceUrl());
            Assertions.assertFalse(client.hasProperty("stale"));
        }
    }

    @Test
    public void testLegacyPoolStillReceivesOtherProperties() {
        KyuubiDataSourceParamDTO dto = new KyuubiDataSourceParamDTO();
        dto.setHost("localhost");
        dto.setPort(10009);
        dto.setDatabase("default");
        dto.setOther(Collections.singletonMap("socketTimeout", "600000"));
        KyuubiConnectionParam param =
                (KyuubiConnectionParam) new KyuubiDataSourceProcessor().createConnectionParams(dto);
        try (InspectableClient client = new InspectableClient(param)) {
            Assertions.assertEquals("jdbc:kyuubi://localhost:10009/default;socketTimeout=600000",
                    client.dataSourceUrl());
            Assertions.assertTrue(client.hasProperty("socketTimeout"));
        }
    }

    private static class InspectableClient extends KyuubiPooledDataSourceClient {

        InspectableClient(KyuubiConnectionParam param) {
            super(param, DbType.KYUUBI);
        }

        String dataSourceUrl() {
            return dataSource.getJdbcUrl();
        }

        boolean hasProperty(String key) {
            return dataSource.getDataSourceProperties().containsKey(key);
        }
    }

}
