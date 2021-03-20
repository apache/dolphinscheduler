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

import org.apache.dolphinscheduler.api.dto.datasource.ClickHouseDatasourceParamDTO;

import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PrepareForTest(ClickHouseDatasourceProcessor.class)
public class ClickHouseDatasourceProcessorTest {

    private final ClickHouseDatasourceProcessor clickHouseDatasourceProcessor = new ClickHouseDatasourceProcessor();

    private Logger logger = LoggerFactory.getLogger(ClickHouseDatasourceProcessorTest.class);

    @Test
    public void checkDatasourceParam() {
        clickHouseDatasourceProcessor.checkDatasourceParam(createDatasourceParam());
        Assert.assertTrue(true);
    }

    @Test
    public void buildConnectionParams() {
        String connectionParams = clickHouseDatasourceProcessor.buildConnectionParams(createDatasourceParam());
        logger.info(connectionParams);
        Assert.assertNotNull(connectionParams);
    }

    private ClickHouseDatasourceParamDTO createDatasourceParam() {
        ClickHouseDatasourceParamDTO datasourceParamDTO = new ClickHouseDatasourceParamDTO();
        datasourceParamDTO.setHost("localhost");
        datasourceParamDTO.setPort(1234);
        datasourceParamDTO.setDatabase("test");
        datasourceParamDTO.setUserName("user");
        datasourceParamDTO.setPassword("user");
        return datasourceParamDTO;
    }
}