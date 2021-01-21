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

package org.apache.dolphinscheduler.dao.datasource;

import org.junit.Assert;
import org.junit.Test;

/**
 * test data source of hive
 */
public class HiveDataSourceTest {

    @Test
    public void testFilterOther() {
        BaseDataSource hiveDataSource = new HiveDataSource();

        // not contain hive_site_conf
        String other = hiveDataSource.filterOther("charset=UTF-8");
        Assert.assertEquals("charset=UTF-8", other);

        // not contain
        other = hiveDataSource.filterOther("");
        Assert.assertEquals("", other);

        // only contain hive_site_conf
        other = hiveDataSource.filterOther("hive.mapred.mode=strict");
        Assert.assertEquals("?hive.mapred.mode=strict", other);

        // contain hive_site_conf at the first
        other = hiveDataSource.filterOther("hive.mapred.mode=strict;charset=UTF-8");
        Assert.assertEquals("charset=UTF-8?hive.mapred.mode=strict", other);

        // contain hive_site_conf in the middle
        other = hiveDataSource.filterOther("charset=UTF-8;hive.mapred.mode=strict;foo=bar");
        Assert.assertEquals("charset=UTF-8;foo=bar?hive.mapred.mode=strict", other);

        // contain hive_site_conf at the end
        other = hiveDataSource.filterOther("charset=UTF-8;foo=bar;hive.mapred.mode=strict");
        Assert.assertEquals("charset=UTF-8;foo=bar?hive.mapred.mode=strict", other);

        // contain multi hive_site_conf
        other = hiveDataSource.filterOther("charset=UTF-8;foo=bar;hive.mapred.mode=strict;hive.exec.parallel=true");
        Assert.assertEquals("charset=UTF-8;foo=bar?hive.mapred.mode=strict;hive.exec.parallel=true", other);

        // the security authorization hive conf var
        other = hiveDataSource.filterOther("tez.queue.name=tezTest");
        Assert.assertEquals("?tez.queue.name=tezTest", other);

    }

    @Test
    public void testGetHiveJdbcUrlOther() {

        BaseDataSource hiveDataSource = new HiveDataSource();
        hiveDataSource.setAddress("jdbc:hive2://127.0.0.1:10000");
        hiveDataSource.setDatabase("test");
        hiveDataSource.setPassword("123456");
        hiveDataSource.setUser("test");
        Assert.assertEquals("jdbc:hive2://127.0.0.1:10000/test", hiveDataSource.getJdbcUrl());

        hiveDataSource.setOther("charset=UTF-8;hive.mapred.mode=strict;hive.server2.thrift.http.path=hs2");

        Assert.assertEquals(
                "jdbc:hive2://127.0.0.1:10000/test;charset=UTF-8?hive.mapred.mode=strict;hive.server2.thrift.http.path=hs2",
                hiveDataSource.getJdbcUrl());

        hiveDataSource.setOther("hive.mapred.mode=strict;hive.server2.thrift.http.path=hs2");
        Assert.assertEquals(
                "jdbc:hive2://127.0.0.1:10000/test;?hive.mapred.mode=strict;hive.server2.thrift.http.path=hs2",
                hiveDataSource.getJdbcUrl());

    }

}
