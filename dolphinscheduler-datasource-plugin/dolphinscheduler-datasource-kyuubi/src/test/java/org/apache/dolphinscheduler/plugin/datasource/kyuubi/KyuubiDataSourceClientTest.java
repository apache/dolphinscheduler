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
import org.apache.dolphinscheduler.spi.enums.DbType;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class KyuubiDataSourceClientTest {

    @Mock
    private KyuubiDataSourceClient kyuubiDataSourceClient;

    @Test
    public void testPreInit() {
        kyuubiDataSourceClient.preInit();
        Mockito.verify(kyuubiDataSourceClient).preInit();
    }

    @Test
    public void testCheckEnv() {

        KyuubiConnectionParam kyuubiConnectionParam = new KyuubiConnectionParam();
        kyuubiDataSourceClient.checkEnv(kyuubiConnectionParam);
        Mockito.verify(kyuubiDataSourceClient).checkEnv(Mockito.any(KyuubiConnectionParam.class));
    }

    @Test
    public void testInitClient() {
        KyuubiConnectionParam kyuubiConnectionParam = new KyuubiConnectionParam();
        kyuubiDataSourceClient.initClient(kyuubiConnectionParam, DbType.KYUUBI);
        Mockito.verify(kyuubiDataSourceClient).initClient(Mockito.any(KyuubiConnectionParam.class), Mockito.any());
    }

    @Test
    public void testCheckClient() {
        kyuubiDataSourceClient.checkClient();
        Mockito.verify(kyuubiDataSourceClient).checkClient();
    }

    @Test
    public void testGetConnection() {
        kyuubiDataSourceClient.getConnection();
        Mockito.verify(kyuubiDataSourceClient).getConnection();

    }

}
