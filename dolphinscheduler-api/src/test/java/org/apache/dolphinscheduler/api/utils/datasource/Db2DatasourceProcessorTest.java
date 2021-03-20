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

import org.apache.dolphinscheduler.api.dto.datasource.Db2DatasourceParamDTO;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Db2DatasourceProcessorTest {

    private Db2DatasourceProcessor db2DatasourceProcessor = new Db2DatasourceProcessor();

    private Logger logger = LoggerFactory.getLogger(Db2DatasourceProcessor.class);

    @Test
    public void checkDatasourceParam() {
        db2DatasourceProcessor.checkDatasourceParam(createParam());
        Assert.assertTrue(true);
    }

    @Test
    public void buildConnectionParams() {
        String connectionParams = db2DatasourceProcessor.buildConnectionParams(createParam());
        logger.info(connectionParams);
        Assert.assertNotNull(connectionParams);
    }

    private Db2DatasourceParamDTO createParam() {
        Db2DatasourceParamDTO db2DatasourceParamDTO = new Db2DatasourceParamDTO();
        db2DatasourceParamDTO.setHost("localhost");
        db2DatasourceParamDTO.setUserName("test");
        db2DatasourceParamDTO.setDatabase("test");
        db2DatasourceParamDTO.setPassword("test");
        db2DatasourceParamDTO.setPort(1234);
        return db2DatasourceParamDTO;
    }
}