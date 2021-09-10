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

package org.apache.dolphinscheduler.common.task;

import static org.junit.Assert.assertNotNull;

import org.apache.dolphinscheduler.common.enums.DataType;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue(CollectionUtils.isEmpty(sqlParameters.getResourceFilesList()));

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

        Assert.assertEquals(type, sqlParameters.getType());
        Assert.assertEquals(sql, sqlParameters.getSql());
        Assert.assertEquals(udfs, sqlParameters.getUdfs());
        Assert.assertEquals(datasource, sqlParameters.getDatasource());
        Assert.assertEquals(sqlType, sqlParameters.getSqlType());
        Assert.assertEquals(sendEmail, sqlParameters.getSendEmail());
        Assert.assertEquals(displayRows, sqlParameters.getDisplayRows());
        Assert.assertEquals(showType, sqlParameters.getShowType());
        Assert.assertEquals(title, sqlParameters.getTitle());
        Assert.assertEquals(groupId, sqlParameters.getGroupId());

        String sqlResult = "[{\"id\":6,\"test1\":\"6\"},{\"id\":70002,\"test1\":\"+1\"}]";
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
