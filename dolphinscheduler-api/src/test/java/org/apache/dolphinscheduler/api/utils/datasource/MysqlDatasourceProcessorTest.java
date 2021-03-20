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

import org.apache.dolphinscheduler.api.dto.datasource.MysqlDatasourceParamDTO;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlDatasourceProcessorTest {

    private Logger logger = LoggerFactory.getLogger(MysqlDatasourceProcessorTest.class);

    private MysqlDatasourceProcessor mysqlDatasourceProcessor = new MysqlDatasourceProcessor();

    @Test
    public void checkDatasourceParam() {
        mysqlDatasourceProcessor.checkDatasourceParam(createParam());
        Assert.assertTrue(true);
    }

    @Test
    public void buildConnectionParams() {
        String connectionParams = mysqlDatasourceProcessor.buildConnectionParams(createParam());
        Assert.assertNotNull(connectionParams);
    }

    private MysqlDatasourceParamDTO createParam() {
        MysqlDatasourceParamDTO mysqlDatasourceParamDTO = new MysqlDatasourceParamDTO();
        mysqlDatasourceParamDTO.setHost("localhost");
        mysqlDatasourceParamDTO.setPort(3306);
        mysqlDatasourceParamDTO.setUserName("user");
        mysqlDatasourceParamDTO.setPassword("123456");
        mysqlDatasourceParamDTO.setDatabase("test");
        return mysqlDatasourceParamDTO;
    }
}