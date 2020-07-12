package org.apache.dolphinscheduler.dao.datasource;

import org.junit.Assert;
import org.junit.Test;

/**
 * test data source of hive
 */
public class HiveDataSourceTest {

  @Test
  public void testfilterOther() {
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