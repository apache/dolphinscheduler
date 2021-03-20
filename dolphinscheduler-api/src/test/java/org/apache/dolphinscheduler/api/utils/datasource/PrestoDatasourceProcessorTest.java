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

import org.apache.dolphinscheduler.api.dto.datasource.PrestoDatasourceParamDTO;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrestoDatasourceProcessorTest {

    private Logger logger = LoggerFactory.getLogger(PrestoDatasourceProcessorTest.class);
    private PrestoDatasourceProcessor prestoDatasourceProcessor = new PrestoDatasourceProcessor();

    @Test
    public void checkDatasourceParam() {
        prestoDatasourceProcessor.checkDatasourceParam(createParam());
        Assert.assertTrue(true);
    }

    @Test
    public void buildConnectionParams() {
        String connectionParams = prestoDatasourceProcessor.buildConnectionParams(createParam());
        Assert.assertNotNull(connectionParams);
    }

    private PrestoDatasourceParamDTO createParam() {
        PrestoDatasourceParamDTO prestoDatasourceParamDTO = new PrestoDatasourceParamDTO();
        prestoDatasourceParamDTO.setDatabase("test");
        prestoDatasourceParamDTO.setHost("localhost");
        prestoDatasourceParamDTO.setPort(1234);
        prestoDatasourceParamDTO.setUserName("test");
        prestoDatasourceParamDTO.setPassword("1234");
        return prestoDatasourceParamDTO;
    }
}