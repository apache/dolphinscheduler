package org.apache.dolphinscheduler.plugin.doris;

import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannelFactory;


public class DorisDataSourceChannelFactory implements DataSourceChannelFactory {
    @Override
    public DataSourceChannel create() {
        return new DorisDataSourceChannel();
    }

    @Override
    public String getName() {
        return "doris";
    }
}
