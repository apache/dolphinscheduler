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

package org.apache.dolphinscheduler.server.worker.task;

import static org.junit.Assert.assertNotNull;

import org.apache.dolphinscheduler.common.enums.DataType;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * shell task return test.
 */
@RunWith(PowerMockRunner.class)
public class TaskParamsTest {
    private static final Logger logger = LoggerFactory.getLogger(TaskParamsTest.class);

    @Test
    public void testDealOutParam() {
        List<Property> properties = new ArrayList<>();
        Property property = new Property();
        property.setProp("test1");
        property.setDirect(Direct.OUT);
        property.setType(DataType.VARCHAR);
        property.setValue("test1");
        properties.add(property);

        ShellParameters shellParameters = new ShellParameters();
        String resultShell = "key1=value1$VarPoolkey2=value2";
        shellParameters.varPool = new ArrayList<>();
        shellParameters.setLocalParams(properties);
        shellParameters.dealOutParam(resultShell);
        assertNotNull(shellParameters.getVarPool().get(0));

        String sqlResult = "[{\"id\":6,\"test1\":\"6\"},{\"id\":70002,\"test1\":\"+1\"}]";
        SqlParameters sqlParameters = new SqlParameters();
        String sqlResult1 = "[{\"id\":6,\"test1\":\"6\"}]";
        sqlParameters.setLocalParams(properties);
        sqlParameters.varPool = new ArrayList<>();
        sqlParameters.dealOutParam(sqlResult1);
        assertNotNull(sqlParameters.getVarPool().get(0));

        property.setType(DataType.LIST);
        properties.clear();
        properties.add(property);
        sqlParameters.setLocalParams(properties);
        sqlParameters.dealOutParam(sqlResult);
        assertNotNull(sqlParameters.getVarPool().get(0));
    }

}