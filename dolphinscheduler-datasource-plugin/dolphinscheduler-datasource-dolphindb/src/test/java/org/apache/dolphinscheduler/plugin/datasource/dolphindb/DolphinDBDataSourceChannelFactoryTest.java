package org.apache.dolphinscheduler.plugin.datasource.dolphindb;

import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DolphinDBDataSourceChannelFactoryTest {

    @Test
    public void testCreate() {
        DolphinDBDataSourceChannelFactory dolphinDBDataSourceChannelFactory = new DolphinDBDataSourceChannelFactory();
        DataSourceChannel dataSourceChannel = dolphinDBDataSourceChannelFactory.create();
        Assertions.assertNotNull(dataSourceChannel);
    }
}
