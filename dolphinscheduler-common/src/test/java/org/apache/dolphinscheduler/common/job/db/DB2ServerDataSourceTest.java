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
package org.apache.dolphinscheduler.common.job.db;

import org.apache.dolphinscheduler.common.enums.DbType;
import org.junit.Assert;
import org.junit.Test;

public class DB2ServerDataSourceTest {
    @Test
    public void getJdbcUrl(){
        BaseDataSource db2Ds = DataSourceFactory.getDatasource(DbType.DB2,
                "{\"user\":\"user\",\"password\":\"xxx\"," +
                        "\"address\":\"db2host\",\"database\":\"db\",\"other\":\"otherPara\"}");
        Assert.assertTrue(db2Ds instanceof DB2ServerDataSource);
        DB2ServerDataSource db2ServerDataSource = (DB2ServerDataSource) db2Ds;
        Assert.assertEquals("db2host/db:otherPara",db2ServerDataSource.getJdbcUrl());
        try {
            db2ServerDataSource.isConnectable();
            Assert.fail("db2 data source can not be connected");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

    }
}
