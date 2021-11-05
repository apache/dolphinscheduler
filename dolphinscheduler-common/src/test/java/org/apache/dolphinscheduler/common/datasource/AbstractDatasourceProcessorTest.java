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

package org.apache.dolphinscheduler.common.datasource;

import java.util.HashMap;
import java.util.Map;
import org.apache.dolphinscheduler.common.datasource.mysql.MysqlDatasourceProcessor;
import org.apache.dolphinscheduler.common.enums.DbType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MysqlDatasourceProcessor.class})
public class AbstractDatasourceProcessorTest {

    AbstractDatasourceProcessor abstractDatasourceProcessor;

    @Before
    public void setUp(){
        abstractDatasourceProcessor = PowerMockito.mock(AbstractDatasourceProcessor.class, Answers.CALLS_REAL_METHODS);
    }

    @Test
    public void testParseOther(){
        String other = "serverTimezone=Asia/Shanghai&characterEncoding=utf8";
        Map<String, String> otherMap = abstractDatasourceProcessor.parseOther(DbType.MYSQL, other);
        Assert.assertNotNull(otherMap);
    }

    @Test
    public void testTransferFromOther(){
        Map<String,String > other = new HashMap<>();
        other.put("serverTimezone", "Asia/Shanghai");
        other.put("characterEncoding", "utf8");
        String otherParam = abstractDatasourceProcessor.transformOther(DbType.MYSQL, other);
        Assert.assertEquals(otherParam,"serverTimezone=Asia/Shanghai&characterEncoding=utf8");
    }

}
