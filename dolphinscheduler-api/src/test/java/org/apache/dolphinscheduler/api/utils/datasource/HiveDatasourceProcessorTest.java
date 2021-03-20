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

package org.apache.dolphinscheduler.api.utils.datasource;

import org.apache.dolphinscheduler.api.dto.datasource.HiveDataSourceParamDTO;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HiveDatasourceProcessorTest {

    private Logger logger = LoggerFactory.getLogger(HiveDatasourceProcessorTest.class);

    private HiveDatasourceProcessor hiveDatasourceProcessor = new HiveDatasourceProcessor();

    @Test
    public void checkDatasourceParam() {
        hiveDatasourceProcessor.checkDatasourceParam(createParam());
        Assert.assertTrue(true);
    }

    @Test
    public void buildConnectionParams() {
        String connectionParams = hiveDatasourceProcessor.buildConnectionParams(createParam());
        Assert.assertNotNull(connectionParams);
    }

    private HiveDataSourceParamDTO createParam() {
        HiveDataSourceParamDTO hiveDataSourceParamDTO = new HiveDataSourceParamDTO();
        hiveDataSourceParamDTO.setDatabase("test");
        hiveDataSourceParamDTO.setHost("localhost");
        hiveDataSourceParamDTO.setPort(1234);
        hiveDataSourceParamDTO.setLoginUserKeytabPath("/conf");
        hiveDataSourceParamDTO.setJavaSecurityKrb5Conf("xxx.conf");
        hiveDataSourceParamDTO.setLoginUserKeytabUsername("user");
        return hiveDataSourceParamDTO;
    }
}