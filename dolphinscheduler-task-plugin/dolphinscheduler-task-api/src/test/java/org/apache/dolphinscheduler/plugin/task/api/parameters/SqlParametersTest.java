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

package org.apache.dolphinscheduler.plugin.task.api.parameters;

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlParametersTest {

    private final String type = "MYSQL";
    private final String sql = "select * from t_ds_user";
    private final String udfs = "test-udfs-1.0.0-SNAPSHOT.jar";
    private final int datasource = 1;
    private final int sqlType = 0;
    private final Boolean sendEmail = true;
    private final int displayRows = 10;
    private final String showType = "TABLE";
    private final String title = "sql test";
    private final int groupId = 0;

    @Test
    public void testSqlParameters() {
        List<Property> properties = new ArrayList<>();
        Property property = new Property();
        property.setProp("test1");
        property.setDirect(Direct.OUT);
        property.setType(DataType.VARCHAR);
        property.setValue("test1");
        properties.add(property);

        SqlParameters sqlParameters = new SqlParameters();
        Assertions.assertTrue(CollectionUtils.isEmpty(sqlParameters.getResourceFilesList()));

        sqlParameters.setType(type);
        sqlParameters.setSql(sql);
        sqlParameters.setUdfs(udfs);
        sqlParameters.setDatasource(datasource);
        sqlParameters.setSqlType(sqlType);
        sqlParameters.setSendEmail(sendEmail);
        sqlParameters.setDisplayRows(displayRows);
        sqlParameters.setShowType(showType);
        sqlParameters.setTitle(title);
        sqlParameters.setGroupId(groupId);

        Assertions.assertEquals(type, sqlParameters.getType());
        Assertions.assertEquals(sql, sqlParameters.getSql());
        Assertions.assertEquals(udfs, sqlParameters.getUdfs());
        Assertions.assertEquals(datasource, sqlParameters.getDatasource());
        Assertions.assertEquals(sqlType, sqlParameters.getSqlType());
        Assertions.assertEquals(sendEmail, sqlParameters.getSendEmail());
        Assertions.assertEquals(displayRows, sqlParameters.getDisplayRows());
        Assertions.assertEquals(showType, sqlParameters.getShowType());
        Assertions.assertEquals(title, sqlParameters.getTitle());
        Assertions.assertEquals(groupId, sqlParameters.getGroupId());

        String sqlResult = "[{\"id\":6,\"test1\":\"6\"},{\"id\":70002,\"test1\":\"+1\"}]";
        String sqlResult1 = "[{\"id\":6,\"test1\":\"6\"}]";
        sqlParameters.setLocalParams(properties);
        sqlParameters.varPool = new ArrayList<>();
        sqlParameters.dealOutParam(sqlResult1);
        Assertions.assertNotNull(sqlParameters.getVarPool().get(0));

        property.setType(DataType.LIST);
        properties.clear();
        properties.add(property);
        sqlParameters.setLocalParams(properties);
        sqlParameters.dealOutParam(sqlResult);
        Assertions.assertNotNull(sqlParameters.getVarPool().get(0));
    }
}
