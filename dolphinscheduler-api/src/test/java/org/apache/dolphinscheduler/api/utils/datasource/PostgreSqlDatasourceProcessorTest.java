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

import org.apache.dolphinscheduler.api.dto.datasource.PostgreSqlDatasourceParamDTO;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSqlDatasourceProcessorTest {

    private final Logger logger = LoggerFactory.getLogger(PostgreSqlDatasourceProcessorTest.class);
    private PostgreSqlDatasourceProcessor postgreSqlDatasourceProcessor = new PostgreSqlDatasourceProcessor();

    @Test
    public void checkDatasourceParam() {
        postgreSqlDatasourceProcessor.checkDatasourceParam(createParam());
        Assert.assertTrue(true);
    }

    @Test
    public void buildConnectionParams() {
        String connectionParams = postgreSqlDatasourceProcessor.buildConnectionParams(createParam());
        Assert.assertNotNull(connectionParams);
    }

    private PostgreSqlDatasourceParamDTO createParam() {
        PostgreSqlDatasourceParamDTO postgreSqlDatasourceParamDTO = new PostgreSqlDatasourceParamDTO();
        postgreSqlDatasourceParamDTO.setDatabase("test");
        postgreSqlDatasourceParamDTO.setHost("localhost");
        postgreSqlDatasourceParamDTO.setPort(1234);
        postgreSqlDatasourceParamDTO.setUserName("test");
        postgreSqlDatasourceParamDTO.setPassword("1234");
        return postgreSqlDatasourceParamDTO;
    }
}